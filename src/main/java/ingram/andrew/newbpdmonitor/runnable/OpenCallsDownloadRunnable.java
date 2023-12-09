package ingram.andrew.newbpdmonitor.runnable;

import ingram.andrew.newbpdmonitor.BPDMonitorController;
import ingram.andrew.newbpdmonitor.searchterms.SearchTerms;
import ingram.andrew.newbpdmonitor.data.OpenCallData;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

public class OpenCallsDownloadRunnable implements Runnable {

    private final BPDMonitorController PROGRAM_CONTROLLER;

    public OpenCallsDownloadRunnable(BPDMonitorController programController) {
        this.PROGRAM_CONTROLLER = programController;
    }

    private DataOutputStream fetchData(URLConnection connection) {
        int dataAmount = 30; // how many open calls to fetch
        long currentTime = System.currentTimeMillis(); // used to verify request on server end

        String postData = "t=ccc&_search=false&nd=" + currentTime + "&rows=" + dataAmount + "&page=1&sidx=starttime&sord=desc";
        connection.setRequestProperty("Content-Length", Integer.toString(postData.length()));

        try {
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(postData);
            outputStream.close();
            return outputStream;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }


    private String readData(URLConnection connection) {

        try {
            BufferedReader bufferedReader = new BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));

            StringBuilder jsonString = new StringBuilder();
            String currentLine;
            while (true) {
                if ((currentLine = bufferedReader.readLine()) == null) {
                    break;
                }
                jsonString.append(currentLine);
            }

            bufferedReader.close();

            return jsonString.toString();

        } catch (IOException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public void run() {

        // establish connection
        URL url;
        URLConnection connection = null;
        try {
            url = new URL("https://p2c.beaumonttexas.gov/p2c/cad/cadHandler.ashx?op=s");
            connection = url.openConnection();
            connection.setDoOutput(true);
        } catch (IOException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        }

        // fetch data
        fetchData(connection);

        // read data into string with json formatting
        String jsonString = readData(connection);
        ArrayList<OpenCallData> newOpenCallData = OpenCallData.parseDataMap(PROGRAM_CONTROLLER.getGson().fromJson(jsonString.toString(), Map.class));


        // list to keep track of what hidden calls are no longer required to be tracked (list of hidden open calls that are now closed)
        ArrayList<Long> hiddenCallsToFreeFromMemory = PROGRAM_CONTROLLER.getHiddenOpenCalls();

        // these three variables are used to check if program should play sound and/or show notification
        boolean shouldPlayNotificationSound = false;
        boolean shouldShowNotification = false;
        OpenCallData[] previousOpenCallData = PROGRAM_CONTROLLER.getOpenCalls();

        for (int i = 0; i < newOpenCallData.size(); i++) {

            OpenCallData openCallData = newOpenCallData.get(i);

            // if the current open call does not fit a search term, remove it from return list
            if (!SearchTerms.containsSearchTerm(openCallData)) {
                newOpenCallData.remove(openCallData);
                i--;
                continue;
            }

            // if current open call is within HIDDEN_OPEN_CALLS list
            if (hiddenCallsToFreeFromMemory.contains(openCallData.getID())) {
                // make sure the program remembers this case is hidden and to not free the case-id from the hidden list
                hiddenCallsToFreeFromMemory.remove(openCallData.getID());
                // don't add open call to table-view or OPEN_CALLS list
                newOpenCallData.remove(openCallData);
                i--;
                continue;
            }

            // check if notification sound should be played (only when a new open-call is caught)
            if (!shouldPlayNotificationSound) {

                boolean isOld = false;

                for (OpenCallData tempOpenCallData : previousOpenCallData) {
                    if (openCallData.getID() == tempOpenCallData.getID()) {
                        isOld = true;
                        break;
                    }
                }

                if (!isOld) {
                    shouldPlayNotificationSound = true;
                }
            }

            // if open call is not hidden, show notification
            shouldShowNotification = true;
        }

        PROGRAM_CONTROLLER.gotOpenCalls(newOpenCallData, shouldShowNotification, shouldPlayNotificationSound, hiddenCallsToFreeFromMemory);

    }

}
