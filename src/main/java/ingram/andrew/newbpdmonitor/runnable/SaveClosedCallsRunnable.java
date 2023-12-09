package ingram.andrew.newbpdmonitor.runnable;

import com.google.api.client.auth.oauth2.Credential;
import ingram.andrew.newbpdmonitor.BPDMonitorController;
import ingram.andrew.newbpdmonitor.data.ClosedCallData;
import ingram.andrew.newbpdmonitor.util.GoogleAPI;
import ingram.andrew.newbpdmonitor.util.Settings;
import javafx.application.Platform;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;

public class SaveClosedCallsRunnable implements Runnable{

    private static volatile boolean busy = false;

    private final BPDMonitorController PROGRAM_CONTROLLER;

    public SaveClosedCallsRunnable(BPDMonitorController controller) {
        this.PROGRAM_CONTROLLER = controller;
    }

    @Deprecated
    private void addClosedCallDataToRow(ClosedCallData closedCallData, Row row) {
        Cell agencyCell = row.createCell(0);
        Cell serviceCell = row.createCell(1);
        Cell startTimeCell = row.createCell(2);
        Cell closeTimeCell = row.createCell(3);
        Cell idCell = row.createCell(4);
        Cell natureCell = row.createCell(5);
        Cell addressCell = row.createCell(6);

        agencyCell.setCellValue(closedCallData.getAgency());
        serviceCell.setCellValue(closedCallData.getService());
        startTimeCell.setCellValue(closedCallData.getStartTime());
        closeTimeCell.setCellValue(closedCallData.getEndTime());
        idCell.setCellValue(closedCallData.getID());
        natureCell.setCellValue(closedCallData.getNature());
        addressCell.setCellValue(closedCallData.getAddress());
    }

    private String getFileNameFromClosedCall(ClosedCallData closedCallData) {

        String[] closeDateTime = closedCallData.getEndTime().split(" ");
        String[] closeDate = closeDateTime[0].split("/");

        int closeMonth = Integer.parseInt(closeDate[0]);
        int closeDayOfMonth = Integer.parseInt(closeDate[1]);
        int closeYear = Integer.parseInt(closeDate[2]);

        return "output/Output_" + closeMonth + "-" + closeDayOfMonth + "-" + closeYear;// + ".xlsx";
    }

    @Deprecated
    private Workbook createAndOpenWorkbook(String fileName, String sheetName) throws IOException {

        File fileDirectory = new File("output");
        boolean makeDirectoryResult = fileDirectory.mkdirs();

        File tempFile = new File(fileName);
        if (tempFile.exists()) {

            FileInputStream inputStream = new FileInputStream(fileName);
            Workbook workbook = WorkbookFactory.create(inputStream);
            inputStream.close();
            boolean makeFileResult = tempFile.delete();

            if (!makeFileResult) {
                System.out.println("ERROR > failed to delete old closed-calls spreadsheet file!");
            }

            if (workbook.getSheet(sheetName) == null) {
                workbook.createSheet();
                workbook.setSheetName(workbook.getNumberOfSheets() - 1, sheetName);
            }

            return workbook;
        }


        Workbook workbook = WorkbookFactory.create(true);
        workbook.createSheet();
        workbook.setSheetName(0, sheetName);
        return workbook;
    }


    /**
     * Creates Excel file filled with closed calls data.
     * Deprecated firstly because it created a memory leak, and secondly because it will be replaced with Google Sheets API.
     */
    @Deprecated
    private void oldSaveMethod() throws IOException {
        HashMap<String, Workbook> workbookMap = new HashMap<>();

        String sheetName = "Closed Calls";
        for (ClosedCallData closedCallData : PROGRAM_CONTROLLER.getClosedCalls()) {

            String fileName = getFileNameFromClosedCall(closedCallData);

            // make sure workbook exists
            // if it doesn't, create and open it
            if (workbookMap.get(fileName) == null) {
                Workbook workbook = createAndOpenWorkbook(fileName, sheetName);
                workbookMap.put(fileName, workbook);
            }

            // get the workbook's 'closed calls' sheet for editing
            Workbook workbook = workbookMap.get(fileName);
            Sheet sheet = workbook.getSheet(sheetName);

            // make new row for data
            int lastRow = sheet.getLastRowNum();
            Row row = lastRow == -1 ? sheet.createRow(0) : sheet.createRow(lastRow + 1);

            // add data to row
            addClosedCallDataToRow(closedCallData, row);
        }

        // save workbooks
        for (String fileName : workbookMap.keySet()) {
            Workbook workbook = workbookMap.get(fileName);
            FileOutputStream outputStream = new FileOutputStream(fileName);
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();
        }

        workbookMap.clear();
    }

    /**\
     *
     * Makes sure the output folder exists on Google Drive.
     * If it does not exist (or if it is trashed), it will create it.
     *
     * @throws Exception
     */
    private void makeSureOutputFolderExists() throws Exception {

        if (Settings.getClosedCallsFolderId() == null || !GoogleAPI.folderExists(Settings.getClosedCallsFolderId())) {
            String folderId = Settings.getClosedCallsFolderId();
            System.out.println("folder: " + Settings.getClosedCallsFolderId() + " does NOT exist!");

            folderId = GoogleAPI.createFolder("BPD Monitor Output");
            Settings.setClosedCallsFolderId(folderId);
            System.out.println("created: " + folderId);

            Platform.runLater(() -> Settings.save());
        } else {
            System.out.println("folder: " + Settings.getClosedCallsFolderId() + " exists!");
        }

    }

    private void saveToGoogleSheets() throws Exception {

        for (ClosedCallData closedCallData : BPDMonitorController.getInstance().getClosedCalls()) {

            String fileName = getFileNameFromClosedCall(closedCallData);
            String fileId = GoogleAPI.fileExistsByNameInFolder(fileName, Settings.getClosedCallsFolderId());
            if (fileId != null) {
                // modify file
                System.out.println("Spreadsheet " + fileName + " exists, modifying it!");
            } else {
                fileId = GoogleAPI.createSpreadsheetInFolder(fileName, Settings.getClosedCallsFolderId());
                System.out.println("Spreadsheet " + fileName + " didn't exist, creating it!");
                //GoogleAPI.moveFileToFolder(fileId, Settings.getClosedCallsFolderId());
                // create and modify file
            }


        }

    }

    private void saveClosedCalls() throws Exception {
        makeSureOutputFolderExists();
        saveToGoogleSheets();
        //oldSaveMethod(); // <- causes memory leak for some reason
    }

    @Override
    public void run() {
        SaveClosedCallsRunnable.busy = true;

        try {
            saveClosedCalls();
        } catch (Exception e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        }

        PROGRAM_CONTROLLER.savedClosedCalls();

        SaveClosedCallsRunnable.busy = false;
    }

    public static boolean isBusy() {
        return busy;
    }
}
