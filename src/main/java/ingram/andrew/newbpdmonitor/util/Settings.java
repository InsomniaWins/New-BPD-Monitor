package ingram.andrew.newbpdmonitor.util;

import ingram.andrew.newbpdmonitor.BPDMonitorController;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Settings {
    private static String closedCallsFolderId = null;

    public static void load() {

        System.out.println("Loading . . . ");

        boolean shouldSaveAfterLoad = false;

        File settingsFile = new File("settings.txt");
        if (!settingsFile.exists()) {
            System.out.println("Tried loading settings file while settings file does not exist.");
            save();
            return;
        }


        try {
            FileInputStream inputStream = new FileInputStream(settingsFile);

            int muteAlertSound = inputStream.read();
            int muteReminderSound = inputStream.read();
            int closedCallsFolderIdByteAmount = inputStream.read();

            if (closedCallsFolderIdByteAmount > 0) {
                byte[] bytes = inputStream.readNBytes(closedCallsFolderIdByteAmount);
                closedCallsFolderId = new String(bytes, 0, closedCallsFolderIdByteAmount);
            }



            if (muteAlertSound == 0) {
                BPDMonitorController.getInstance().getMuteAlertAudioCheckbox().setSelected(false);
            } else if (muteReminderSound != -1) {
                BPDMonitorController.getInstance().getMuteAlertAudioCheckbox().setSelected(true);
            }

            if (muteReminderSound == 0) {
                BPDMonitorController.getInstance().getMuteReminderAudioCheckBox().setSelected(false);
            } else if (muteReminderSound != -1) {
                BPDMonitorController.getInstance().getMuteReminderAudioCheckBox().setSelected(true);
            }

            inputStream.close();
        } catch (FileNotFoundException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        }

        if (shouldSaveAfterLoad) {
            save();
        }

        System.out.println("Loaded!");
    }

    public static void save() {

        System.out.println("saving . . .");

        File outputFile = new File("settings.txt");

        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            if (BPDMonitorController.getInstance().getMuteAlertAudioCheckbox().isSelected()) outputStream.write(1); else outputStream.write(0);
            if (BPDMonitorController.getInstance().getMuteReminderAudioCheckBox().isSelected()) outputStream.write(1); else outputStream.write(0);


            if (closedCallsFolderId != null && closedCallsFolderId.length() != 0) {

                byte[] stringData = closedCallsFolderId.getBytes();
                outputStream.write(stringData.length);
                for (int i = 0; i < closedCallsFolderId.length(); i++) {
                    outputStream.write(stringData[i]);
                }

            } else {
                outputStream.write(0);
            }

            outputStream.close();
        } catch (FileNotFoundException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: replace with better logging system
            e.printStackTrace();
        }


        System.out.println("saved!");
    }

    public static String getClosedCallsFolderId() {
        return closedCallsFolderId;
    }

    public static void setClosedCallsFolderId(String newFolderId) {
        closedCallsFolderId = newFolderId;
    }
}
