package ingram.andrew.newbpdmonitor;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Notifications;
import java.io.IOException;
import java.util.*;

public class BPDMonitorController {

    private final Gson GSON = new Gson();
    private final ArrayList<ClosedCallData> CLOSED_CALLS = new ArrayList<>();
    private final ArrayList<OpenCallData> OPEN_CALLS = new ArrayList<>();
    private final ArrayList<Long> HIDDEN_OPEN_CALLS = new ArrayList<>(); // list of open-calls that have been hidden from search results
    private Timer downloadOpenCallsTimer;
    private Timer downloadClosedCallsTimer;

    @FXML
    private Button getOpenCallsButton;
    @FXML
    private TableView<OpenCallData> openCallsTable;
    @FXML
    private VBox searchTermsVBox;
    @FXML
    private TableView<ClosedCallData> closedCallsTable;
    @FXML
    private TextField addSearchTermTextField;
    @FXML
    private Button hideSelectedCallButton;
    @FXML
    private Label nextOpenCallCheckLabel;
    @FXML
    private Label nextClosedCallCheckLabel;

    @FXML
    protected void onAddSearchTermTextFieldEntered() {
        tryToAddSearchTerm(addSearchTermTextField.getText());
    }
    @FXML
    protected void onAddSearchTermButtonPressed() {
        tryToAddSearchTerm(addSearchTermTextField.getText());
    }


    // called when window/program is opened
    public void onProgramInitialize() {
        // make timer that auto-downloads open calls every 2 minutes
        downloadOpenCallsTimer = new Timer();
        TimerTask openCallsTimerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        nextOpenCallCheckLabel.setText("Next Check at " + new Date(System.currentTimeMillis() + 120000));
                        downloadOpenCalls();
                    }
                });
            }
        };
        downloadOpenCallsTimer.scheduleAtFixedRate(openCallsTimerTask, 1000, 120000); // every two minutes

        // make timer that auto-downloads closed calls every 1 hour
        downloadClosedCallsTimer = new Timer();
        TimerTask closedCallsTimerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        nextClosedCallCheckLabel.setText("Next Check at " + new Date(System.currentTimeMillis() + 3600000));
                        downloadClosedCalls();
                    }
                });
            }
        };
        downloadClosedCallsTimer.scheduleAtFixedRate(closedCallsTimerTask, 1000, 120000); // every two minutes

        // create event listeners
        SearchTerms.addListener(new SearchTermsEventListener() {
            @Override
            public void onSearchTermsUpdated() {
                try {
                    updateSearchTermsDisplay();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // load search terms
        SearchTerms.load();
    }

    // called when program is closed
    public void onProgramClose() {
        // cancel download timers
        downloadOpenCallsTimer.cancel();
        downloadClosedCallsTimer.cancel();
    }

    private void tryToAddSearchTerm(String searchTerm) {
        if (!SearchTerms.addSearchTerm(searchTerm)) return;
        addSearchTermTextField.setText("");
    }

    private void updateSearchTermsDisplay() throws IOException {

        searchTermsVBox.getChildren().clear();

        String[] searchTerms = SearchTerms.getSearchTerms();

        for (String searchTerm : searchTerms) {
            FXMLLoader fxmlLoader = new FXMLLoader(BPDMonitor.class.getResource("search-term.fxml"));
            BorderPane searchTermNode = (BorderPane) fxmlLoader.load();

            Label searchTermLabel = (Label) searchTermNode.getCenter();
            searchTermLabel.setText(searchTerm);

            Button removeSearchTermButton = (Button) searchTermNode.getRight();
            removeSearchTermButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Button removeSearchTermButton = (Button) actionEvent.getSource();
                    Node parentNode = removeSearchTermButton.getParent();
                    BorderPane searchTermNode = (BorderPane) parentNode;
                    Label searchTermLabel = (Label) searchTermNode.getCenter();
                    String searchTerm = searchTermLabel.getText();

                    SearchTerms.removeSearchTerm(searchTerm);
                }
            });

            searchTermsVBox.getChildren().add(searchTermNode);
        }
    }

    @FXML
    protected void onHideSelectedCallButtonPressed() {
        hideSelectedCallButton.setDisable(true);

        OpenCallData selectedOpenCallData = openCallsTable.getSelectionModel().getSelectedItem();
        if (selectedOpenCallData == null) return;

        HIDDEN_OPEN_CALLS.add(selectedOpenCallData.getID());

        openCallsTable.getSelectionModel().select(null);
        downloadOpenCalls();
    }

    @FXML
    protected  void onOpenCallsTableClicked() {
        OpenCallData selectedOpenCallData = openCallsTable.getSelectionModel().getSelectedItem();

        if (selectedOpenCallData == null) {
            hideSelectedCallButton.setDisable(true);
            return;
        }

        hideSelectedCallButton.setDisable(false);
    }

    @FXML
    protected void onOpenCallsButtonPressed() {
        downloadOpenCalls();
    }

    private void saveClosedCalls() {
        Thread saveThread = new Thread(new SaveClosedCallsRunnable(this));
        saveThread.start();
    }

    public void savedClosedCalls() {

    }

    private void downloadClosedCalls() {
        Thread downloadThread = new Thread(new ClosedCallsDownloadRunnable(this));
        downloadThread.start();
    }

    public void gotClosedCalls(String jsonString) {
        CLOSED_CALLS.clear();
        CLOSED_CALLS.addAll(ClosedCallData.parseDataMap(GSON.fromJson(jsonString, Map.class)));

        ObservableList<TableColumn<ClosedCallData, ?>> columns = closedCallsTable.getColumns();
        if (columns.get(0).getCellValueFactory() == null) {
            columns.get(0).setCellValueFactory(new PropertyValueFactory<>("agency"));
            columns.get(1).setCellValueFactory(new PropertyValueFactory<>("service"));
            columns.get(2).setCellValueFactory(new PropertyValueFactory<>("startTime"));
            columns.get(3).setCellValueFactory(new PropertyValueFactory<>("endTime"));
            columns.get(4).setCellValueFactory(new PropertyValueFactory<>("ID"));
            columns.get(5).setCellValueFactory(new PropertyValueFactory<>("nature"));
            columns.get(6).setCellValueFactory(new PropertyValueFactory<>("address"));
        }

        closedCallsTable.getItems().clear();
        for (ClosedCallData closedCallData : CLOSED_CALLS) {
            closedCallsTable.getItems().add(closedCallData);
        }

        saveClosedCalls();
    }

    private void downloadOpenCalls() {
        getOpenCallsButton.setDisable(true);
        Thread downloadThread = new Thread(new OpenCallsDownloadRunnable(this));
        downloadThread.start();
    }

    public void gotOpenCalls(String jsonString) {

        hideSelectedCallButton.setDisable(true);

        // make sure table has the needed property value factories
        ObservableList<TableColumn<OpenCallData, ?>> columns = openCallsTable.getColumns();
        if (columns.get(0).getCellValueFactory() == null) {
            columns.get(0).setCellValueFactory(new PropertyValueFactory<>("agency"));
            columns.get(1).setCellValueFactory(new PropertyValueFactory<>("service"));
            columns.get(2).setCellValueFactory(new PropertyValueFactory<>("startTime"));
            columns.get(3).setCellValueFactory(new PropertyValueFactory<>("ID"));
            columns.get(4).setCellValueFactory(new PropertyValueFactory<>("nature"));
            columns.get(5).setCellValueFactory(new PropertyValueFactory<>("address"));
        }

        // list to keep track of what hidden calls are no longer required to be tracked (list of hidden open calls that are now closed)
        ArrayList<Long> hiddenCallsToFreeFromMemory = (ArrayList<Long>) HIDDEN_OPEN_CALLS.clone();

        // clear open calls list and the table-view
        OPEN_CALLS.clear();
        openCallsTable.getItems().clear();

        // get open calls
        boolean showNotification = false;
        ArrayList<OpenCallData> openCalls = OpenCallData.parseDataMap(GSON.fromJson(jsonString, Map.class));
        for (OpenCallData openCallData : openCalls) {

            // if the current open call fits a search term
            if (SearchTerms.containsSearchTerm(openCallData)) {

                // if current open call is within HIDDEN_OPEN_CALLS list
                if (HIDDEN_OPEN_CALLS.contains(openCallData.getID())) {

                    // make sure the program remembers this case is hidden and to not free the case-id from the hidden list
                    hiddenCallsToFreeFromMemory.remove(openCallData.getID());

                    // don't add open call to table-view or OPEN_CALLS list
                    continue;
                }

                // add open call to OPEN_CALLS list and table-view
                OPEN_CALLS.add(openCallData);
                openCallsTable.getItems().add(openCallData);
                showNotification = true;
            }
        }

        if (showNotification) {
            showNotification(Notifications.create().title("BPD Monitor").text("There are open-calls pending action!"));
        }

        // enable "Get Open Calls" button
        getOpenCallsButton.setDisable(false);

        // remove call-id's of calls in the HIDDEN_OPEN_CALLS list that are no longer open
        for (long callId : hiddenCallsToFreeFromMemory) {
            HIDDEN_OPEN_CALLS.remove(callId);
        }
    }


    // makes sure a notification is only shown using the JavaFX thread
    public static void showNotification(Notifications notification) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                notification.showWarning();
            }
        });
    }

    public ClosedCallData[] getClosedCalls() {
        return CLOSED_CALLS.toArray(new ClosedCallData[]{});
    }
}