package ingram.andrew.newbpdmonitor;

import org.apache.poi.EmptyFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SaveClosedCallsRunnable implements Runnable{

    private final BPDMonitorController PROGRAM_CONTROLLER;

    public SaveClosedCallsRunnable(BPDMonitorController controller) {
        this.PROGRAM_CONTROLLER = controller;
    }

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

        return new StringBuilder().append("Output_")
                .append(closeMonth)
                .append("-")
                .append(closeDayOfMonth)
                .append("-")
                .append(closeYear)
                .append(".xlsx")
                .toString();
    }

    private Workbook createAndOpenWorkbook(String fileName, String sheetName) throws IOException {
        File tempFile = new File(fileName);
        if (tempFile.exists()) {
            FileInputStream inputStream = new FileInputStream(fileName);
            Workbook workbook = WorkbookFactory.create(inputStream);
            inputStream.close();
            tempFile.delete();

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

    @Override
    public void run() {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        PROGRAM_CONTROLLER.savedClosedCalls();
    }
}
