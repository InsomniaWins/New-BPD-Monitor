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
    private Button testButton;

    @FXML
    protected void onTestButtonPressed() {
        testButton.setDisable(true);
        getOpenCalls();
    }

    private void getOpenCalls() {
        Thread downloadThread = new Thread(new OpenCallsDownloadRunnable(this));
        downloadThread.start();
    }

    public void gotOpenCallsJsonString(String jsonString) {
        ArrayList<?> data = parseOpenCallMap(GSON.fromJson(jsonString.toString(), Map.class));
        System.out.println(data);
        testButton.setDisable(false);
    }

    private ArrayList<OpenCallData> parseOpenCallMap(Map<?,?> dataMap) {

        ArrayList<OpenCallData> returnArray = new ArrayList<>();
        ArrayList<Map<Object, Object>> rows = (ArrayList<Map<Object, Object>>) dataMap.get("rows");

        for (Map<Object, Object> callDetailsMap : rows) {

            String agency = (String) callDetailsMap.get("agency");
            String service = (String) callDetailsMap.get("service");
            String startTime = (String) callDetailsMap.get("starttime");
            long id = Long.parseLong((String) callDetailsMap.get("id"));
            String nature = (String) callDetailsMap.get("nature");
            String address = (String) callDetailsMap.get("address");

            returnArray.add(new OpenCallData(agency, service, startTime, id, nature, address));
        }

        return returnArray;
    }
}