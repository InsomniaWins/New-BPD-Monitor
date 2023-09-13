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

    @Override
    public void run() {
        try {

            HashMap<String, Workbook> workbookMap = new HashMap<>();

            for (ClosedCallData closedCallData : PROGRAM_CONTROLLER.getClosedCalls()) {

                String[] closeDateTime = closedCallData.getEndTime().split(" ");
                String[] closeDate = closeDateTime[0].split("/");

                int closeMonth = Integer.parseInt(closeDate[0]);
                int closeDayOfMonth = Integer.parseInt(closeDate[1]);
                int closeYear = Integer.parseInt(closeDate[2]);

                String fileName = new StringBuilder().append("Output_")
                        .append(closeMonth)
                        .append("-")
                        .append(closeDayOfMonth)
                        .append("-")
                        .append(closeYear)
                        .append(".xlsx")
                        .toString();

                String sheetName = "Closed Calls";

                // make sure workbook exists
                // if it doesn't, create and open it
                if (workbookMap.get(fileName) == null) {
                    File tempFile = new File(fileName);
                    Workbook workbook;
                    if (tempFile.exists()) {
                        FileInputStream inputStream = new FileInputStream(fileName);
                        workbook = WorkbookFactory.create(inputStream);
                        inputStream.close();
                        tempFile.delete();

                        if (workbook.getSheet(sheetName) == null) {
                            workbook.createSheet();
                            workbook.setSheetName(workbook.getNumberOfSheets()-1, sheetName);
                        }
                    } else {
                        workbook = WorkbookFactory.create(true);
                        workbook.createSheet();
                        workbook.setSheetName(0, sheetName);
                    }

                    workbookMap.put(fileName, workbook);
                }

                Workbook workbook = workbookMap.get(fileName);
                Sheet sheet = workbook.getSheet(sheetName);

                int lastRow = sheet.getLastRowNum();

                Row row = lastRow == -1 ? sheet.createRow(0) : sheet.createRow(lastRow + 1);
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

            /*
            // old method
            File tempFile = new File("Output.xlsx");
            Workbook workbook;
            if (tempFile.exists()) {
                // append Output.xlsx
                try {
                    workbook = WorkbookFactory.create(new FileInputStream("Output.xlsx"));
                } catch (EmptyFileException e) {
                    workbook = WorkbookFactory.create(true);
                }
            } else {
                // make Output.xlsx
                 workbook = WorkbookFactory.create(true);
            }

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) sheet = workbook.createSheet("Closed Calls");

            long latestCaseNumber = Long.parseLong(sheet.getRow(0).getCell(4).getStringCellValue());

            ClosedCallData[] closedCalls = PROGRAM_CONTROLLER.getClosedCalls();

            int appendIndex = 0;

            for (int i = 0; i < closedCalls.length; i++) {
                ClosedCallData closedCallData = closedCalls[i];
                if (closedCallData.getID() == latestCaseNumber) {
                    appendIndex = i;
                    break;
                }
            }

            sheet.shiftRows(0, sheet.getLastRowNum(), closedCalls.length - appendIndex);

            for (int i = appendIndex; i < closedCalls.length; i++) {
                ClosedCallData closedCallData = closedCalls[i];

                Row row = sheet.createRow(i - appendIndex);

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

            FileOutputStream outputStream = new FileOutputStream("Output.xlsx");
            workbook.write(outputStream);

            */
        } catch (IOException e) {
            e.printStackTrace();
        }

        PROGRAM_CONTROLLER.savedClosedCalls();
    }
}
