package ingram.andrew.newbpdmonitor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

public class OpenCallsDownloadRunnable implements Runnable {

    private final BPDMonitorController PROGRAM_CONTROLLER;

    public OpenCallsDownloadRunnable(BPDMonitorController programController) {
        this.PROGRAM_CONTROLLER = programController;
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

        if (connection == null) {
            System.exit( 1 );
        }



        // fetch data
        int dataAmount = 30; // how many open calls to fetch
        long currentTime = System.currentTimeMillis(); // used to verify request on server end

        String postData = "t=ccc&_search=false&nd=" + currentTime + "&rows=" + dataAmount + "&page=1&sidx=starttime&sord=desc";
        connection.setRequestProperty("Content-Length", Integer.toString(postData.length()));

        DataOutputStream outputStream;
        try {
            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(postData);
        } catch (UnknownHostException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
            return;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        // read data into string with json formatting
        BufferedReader buffReader = null;
        String line;

        try {
            buffReader = new BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
        } catch (IOException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        }

        StringBuilder jsonString = new StringBuilder();
        while (true) {
            try {
                if (buffReader == null) {
                    System.out.println("ERROR > buffReader is null when reading open-call data");
                    System.exit(1);
                }
                if ((line = buffReader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            jsonString.append(line);
        }


        boolean shouldPlayNotificationSound = false;
        boolean shouldShowNotification = false;

        OpenCallData[] previousOpenCallData = PROGRAM_CONTROLLER.getOpenCalls();

        // list to keep track of what hidden calls are no longer required to be tracked (list of hidden open calls that are now closed)
        ArrayList<Long> hiddenCallsToFreeFromMemory = PROGRAM_CONTROLLER.getHiddenOpenCalls();

        ArrayList<OpenCallData> newOpenCallData = OpenCallData.parseDataMap(PROGRAM_CONTROLLER.getGson().fromJson(jsonString.toString(), Map.class));
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
