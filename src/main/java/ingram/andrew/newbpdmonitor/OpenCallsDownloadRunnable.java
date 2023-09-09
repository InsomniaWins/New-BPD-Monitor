package ingram.andrew.newbpdmonitor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

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
            e.printStackTrace();
        }

        if (connection == null) {
            System.exit( 1 );
        }



        // fetch data
        int dataAmount = 30; // how many open calls to fetch
        long currentTime = System.currentTimeMillis(); // used to verify request on server end

        String postData = "t=ccc&_search=false&nd=" + Long.toString(currentTime) + "&rows=" + Integer.toString(dataAmount) + "&page=1&sidx=starttime&sord=desc";
        connection.setRequestProperty("Content-Length", Integer.toString(postData.length()));

        DataOutputStream outputStream;
        try {
            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(postData);
        } catch (UnknownHostException e) {
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

        PROGRAM_CONTROLLER.gotOpenCallsJsonString(jsonString.toString());
    }
}
