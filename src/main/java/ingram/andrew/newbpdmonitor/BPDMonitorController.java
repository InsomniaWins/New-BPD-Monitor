package ingram.andrew.newbpdmonitor;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.*;
import java.util.*;

public class BPDMonitorController {
    private final Gson GSON = new Gson();
    private final ArrayList<ClosedCallData> CLOSED_CALLS = new ArrayList<>();
    private final ArrayList<OpenCallData> OPEN_CALLS = new ArrayList<>();
    private final ArrayList<Long> HIDDEN_OPEN_CALLS = new ArrayList<>(); // list of open-calls that have been hidden from search results
    private final MediaPlayer NOTIFICATION_SOUND_MEDIA_PLAYER = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("audio/new_open_call.wav")).toString()));
    private final MediaPlayer NOTIFICATION_REMINDER_SOUND_MEDIA_PLAYER = new MediaPlayer(new Media(Objects.requireNonNull(getClass().getResource("audio/notification_reminder.wav")).toString()));
    private Timer downloadOpenCallsTimer;
    private Timer downloadClosedCallsTimer;

    @FXML
    private CheckBox muteAlertAudioCheckbox;
    @FXML
    private CheckBox muteReminderAudioCheckbox;
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
                Platform.runLater(() -> {
                    nextOpenCallCheckLabel.setText("Next Check at " + new Date(System.currentTimeMillis() + 120000));
                    downloadOpenCalls();
                });
            }
        };
        downloadOpenCallsTimer.scheduleAtFixedRate(openCallsTimerTask, 1000, 120000); // every two minutes

        // make timer that auto-downloads closed calls every 1 hour
        downloadClosedCallsTimer = new Timer();
        TimerTask closedCallsTimerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    nextClosedCallCheckLabel.setText("Next Check at " + new Date(System.currentTimeMillis() + 3600000));
                    downloadClosedCalls();
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
                    System.out.println("ERROR > failed to update display of search terms!");
                }
            }
        });

        // load search terms
        SearchTerms.load();

        // load settings
        loadSettings();
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
            BorderPane searchTermNode = fxmlLoader.load();

            Label searchTermLabel = (Label) searchTermNode.getCenter();
            searchTermLabel.setText(searchTerm);

            Button removeSearchTermButton = (Button) searchTermNode.getRight();
            removeSearchTermButton.addEventHandler(ActionEvent.ACTION, (ActionEvent actionEvent) -> SearchTerms.removeSearchTerm(searchTerm));

            searchTermsVBox.getChildren().add(searchTermNode);
        }
    }

    @FXML
    protected void onHideSelectedCallButtonPressed() {
        hideSelectedCallButton.setDisable(true);

        OpenCallData selectedOpenCallData = openCallsTable.getSelectionModel().getSelectedItem();
        if (selectedOpenCallData == null) return;

        HIDDEN_OPEN_CALLS.add(selectedOpenCallData.getID());

        OPEN_CALLS.remove(selectedOpenCallData);

        openCallsTable.getSelectionModel().select(null);

        updateOpenCallsTable();

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

    @FXML
    protected void onMuteAlertAudioCheckboxPressed() {
        saveSettings();
    }

    @FXML
    protected void onMuteReminderAudioCheckboxPressed() {
        saveSettings();
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

        @SuppressWarnings("unchecked") Map<String, ArrayList<Map<Object, Object>>> dataMap = GSON.fromJson(jsonString, Map.class);

        CLOSED_CALLS.addAll(ClosedCallData.parseDataMap(dataMap));

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

    public void gotOpenCalls(ArrayList<OpenCallData> newOpenCallData, boolean shouldShowNotification, boolean shouldPlayNotificationSound, ArrayList<Long> hiddenCallsToFreeFromMemory) {

        // remove call-id's of calls in the HIDDEN_OPEN_CALLS list that are no longer open
        for (long callId : hiddenCallsToFreeFromMemory) {
            HIDDEN_OPEN_CALLS.remove(callId);
        }

        // disable hide call button to prevent user from pressing it while no call is selected
        hideSelectedCallButton.setDisable(true);


        // clear and update open calls list
        OPEN_CALLS.clear();
        OPEN_CALLS.addAll(newOpenCallData);

        // clear and update open calls table
        updateOpenCallsTable();

        // enable "Get Open Calls" button
        getOpenCallsButton.setDisable(false);


        // display notification
        if (shouldShowNotification) {
            showNotification(Notifications.create().title("BPD Monitor").text("There are open-calls pending action!"));
        }

        // play notification sound
        if (shouldPlayNotificationSound) {
            playNotificationSound();
        } else if (shouldShowNotification) {
            playNotificationReminderSound();
        }
    }

    public void updateOpenCallsTable() {
        ObservableList<TableColumn<OpenCallData, ?>> columns = openCallsTable.getColumns();
        if (columns.get(0).getCellValueFactory() == null) {
            columns.get(0).setCellValueFactory(new PropertyValueFactory<>("agency"));
            columns.get(1).setCellValueFactory(new PropertyValueFactory<>("service"));
            columns.get(2).setCellValueFactory(new PropertyValueFactory<>("startTime"));
            columns.get(3).setCellValueFactory(new PropertyValueFactory<>("ID"));
            columns.get(4).setCellValueFactory(new PropertyValueFactory<>("nature"));
            columns.get(5).setCellValueFactory(new PropertyValueFactory<>("address"));
        }
        openCallsTable.getItems().clear();
        for (OpenCallData openCallData : OPEN_CALLS) {
            openCallsTable.getItems().add(openCallData);
        }
    }

    // plays loud obvious alert sound
    private void playNotificationSound() {

        if (muteAlertAudioCheckbox.isSelected()) return;

        NOTIFICATION_SOUND_MEDIA_PLAYER.seek(Duration.ZERO);
        NOTIFICATION_SOUND_MEDIA_PLAYER.play();
    }

    // plays quiet casual reminder alert sound
    private void playNotificationReminderSound() {

        if (muteReminderAudioCheckbox.isSelected()) return;

        NOTIFICATION_REMINDER_SOUND_MEDIA_PLAYER.seek(Duration.ZERO);
        NOTIFICATION_REMINDER_SOUND_MEDIA_PLAYER.play();
    }

    // makes sure a notification is only shown using the JavaFX thread
    public static void showNotification(Notifications notification) {
        Platform.runLater(notification::showWarning);
    }

    private void loadSettings() {

        File settingsFile = new File("settings.txt");
        if (!settingsFile.exists()) {
            System.out.println("Tried loading settings file while settings file does not exist.");
            saveSettings();
            return;
        }


        try {
            FileInputStream inputStream = new FileInputStream(settingsFile);

            int muteAlertSound = inputStream.read();
            int muteReminderSound = inputStream.read();

            if (muteAlertSound == 0) {
                muteAlertAudioCheckbox.setSelected(false);
            } else if (muteReminderSound != -1) {
                muteAlertAudioCheckbox.setSelected(true);
            }

            if (muteReminderSound == 0) {
                muteReminderAudioCheckbox.setSelected(false);
            } else if (muteReminderSound != -1) {
                muteReminderAudioCheckbox.setSelected(true);
            }

            inputStream.close();
        } catch (FileNotFoundException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        }

    }

    private void saveSettings() {

        File outputFile = new File("settings.txt");

        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            if (muteAlertAudioCheckbox.isSelected()) outputStream.write(1); else outputStream.write(0);
            if (muteReminderAudioCheckbox.isSelected()) outputStream.write(1); else outputStream.write(0);

            outputStream.close();
        } catch (FileNotFoundException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        }

    }

    public ClosedCallData[] getClosedCalls() {
        return CLOSED_CALLS.toArray(new ClosedCallData[]{});
    }

    public OpenCallData[] getOpenCalls() {
        return OPEN_CALLS.toArray(new OpenCallData[]{});
    }

    public ArrayList<Long> getHiddenOpenCalls() {
        return new ArrayList<>(HIDDEN_OPEN_CALLS);
    }

    public Gson getGson() {
        return GSON;
    }
}