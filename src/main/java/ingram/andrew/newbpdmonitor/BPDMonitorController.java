package ingram.andrew.newbpdmonitor;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.util.ArrayList;
import java.util.Map;

public class BPDMonitorController {

    private final Gson GSON = new Gson();
    private final ArrayList<Long> callsDisplayedList = new ArrayList<>();

    @FXML
    private Button getOpenCallsButton;
    @FXML
    private Button getClosedCallsButton;

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


    private void getClosedCalls() {
        Thread downloadThread = new Thread(new ClosedCallsDownloadRunnable(this));
        downloadThread.start();
    }

    public void gotClosedCalls(String jsonString) {
        ArrayList<ClosedCallData> data = ClosedCallData.parseDataMap(GSON.fromJson(jsonString.toString(), Map.class));

        for (ClosedCallData closedCallData : data) {
            System.out.println(closedCallData);
        }

        getClosedCallsButton.setDisable(false);
    }

    private void getOpenCalls() {
        Thread downloadThread = new Thread(new OpenCallsDownloadRunnable(this));
        downloadThread.start();
    }

    public void gotOpenCalls(String jsonString) {
        ArrayList<OpenCallData> data = OpenCallData.parseDataMap(GSON.fromJson(jsonString.toString(), Map.class));

        for (OpenCallData openCallData : data) {
            System.out.println(openCallData);
        }

        getOpenCallsButton.setDisable(false);
    }
}