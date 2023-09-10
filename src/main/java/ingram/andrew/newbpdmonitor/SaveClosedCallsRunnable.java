package ingram.andrew.newbpdmonitor;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveClosedCallsRunnable implements Runnable{

    private final BPDMonitorController PROGRAM_CONTROLLER;

    public SaveClosedCallsRunnable(BPDMonitorController controller) {
        this.PROGRAM_CONTROLLER = controller;
    }
    @Override
    public void run() {
        try {
            File tempFile = new File("Output.xlsx");
            if (tempFile.exists()) tempFile.delete();

            Workbook workbook = WorkbookFactory.create(true);
            Sheet sheet = workbook.createSheet("Closed Calls");

            ClosedCallData[] closedCalls = PROGRAM_CONTROLLER.getClosedCalls();
            for (int i = 0; i < closedCalls.length; i++) {
                ClosedCallData closedCallData = closedCalls[i];
                Row row = sheet.createRow(i);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PROGRAM_CONTROLLER.savedClosedCalls();
    }
}
