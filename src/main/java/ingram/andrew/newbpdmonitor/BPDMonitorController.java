package ingram.andrew.newbpdmonitor;

import com.google.gson.Gson;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BPDMonitorController {

    private final Gson GSON = new Gson();
    private final ArrayList<ClosedCallData> CLOSED_CALLS = new ArrayList<>();
    private final ArrayList<OpenCallData> OPEN_CALLS = new ArrayList<>();
    private final ArrayList<Long> callsDisplayedList = new ArrayList<>();

    @FXML
    private Button saveClosedCallsButton;
    @FXML
    private Button getOpenCallsButton;
    @FXML
    private Button getClosedCallsButton;
    @FXML
    private TableView openCallsTable;
    @FXML
    private VBox searchTermsVBox;
    @FXML
    private TableView closedCallsTable;

    @FXML
    protected void onOpenCallsButtonPressed() {
        getOpenCallsButton.setDisable(true);
        downloadOpenCalls();
    }

    @FXML
    protected void onClosedCallsButtonPressed() {
        getClosedCallsButton.setDisable(true);
        downloadClosedCalls();
    }

    @FXML
    protected void onSaveClosedCallsButtonPressed() {
        saveClosedCallsButton.setDisable(true);
        Thread saveThread = new Thread(new SaveClosedCallsRunnable(this));
        saveThread.start();
    }

    private void addSearchTermNode(String text) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(BPDMonitor.class.getResource("search-term.fxml"));
            BorderPane searchTermNode = (BorderPane) fxmlLoader.load();
            Label searchTermLabel = (Label) ((HBox) searchTermNode.getChildren().get(0)).getChildren().get(0);
            searchTermLabel.setText(text);
            searchTermsVBox.getChildren().add(searchTermNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savedClosedCalls() {
        saveClosedCallsButton.setDisable(false);
    }

    private void downloadClosedCalls() {
        Thread downloadThread = new Thread(new ClosedCallsDownloadRunnable(this));
        downloadThread.start();
    }

    public void gotClosedCalls(String jsonString) {
        CLOSED_CALLS.clear();
        CLOSED_CALLS.addAll(ClosedCallData.parseDataMap(GSON.fromJson(jsonString.toString(), Map.class)));

        ObservableList<TableColumn> columns = closedCallsTable.getColumns();
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

        getClosedCallsButton.setDisable(false);
    }

    private void downloadOpenCalls() {
        Thread downloadThread = new Thread(new OpenCallsDownloadRunnable(this));
        downloadThread.start();
    }

    public void gotOpenCalls(String jsonString) {
        OPEN_CALLS.clear();
        OPEN_CALLS.addAll(OpenCallData.parseDataMap(GSON.fromJson(jsonString.toString(), Map.class)));

        ObservableList<TableColumn> columns = openCallsTable.getColumns();
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

        getOpenCallsButton.setDisable(false);
    }

    public ClosedCallData[] getClosedCalls() {
        return CLOSED_CALLS.toArray(new ClosedCallData[]{});
    }
}