package ingram.andrew.newbpdmonitor;

import com.google.gson.Gson;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
    private TableView closedCallsTable;

    @FXML
    protected void onOpenCallsButtonPressed() {
        getOpenCallsButton.setDisable(true);
        getOpenCalls();
    }

    @FXML
    protected void onClosedCallsButtonPressed() {
        getClosedCallsButton.setDisable(true);
        getClosedCalls();
    }

    @FXML
    protected void onSaveClosedCallsButtonPressed() {
    }


    private void getClosedCalls() {
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

    private void getOpenCalls() {
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
}