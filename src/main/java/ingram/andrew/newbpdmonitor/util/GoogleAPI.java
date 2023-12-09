package ingram.andrew.newbpdmonitor.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import ingram.andrew.newbpdmonitor.BPDMonitor;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleAPI {

    private static final String APPLICATION_NAME = "BPD Monitor";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SHEETS_SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final List<String> DRIVE_SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "google_api_credentials_desktop.json";

    public static String createSpreadsheetInFolder(String sheetName, String folderId) throws Exception {
        String spreadsheetId = createSpreadsheet(sheetName);
        moveFileToFolder(spreadsheetId, folderId);

        return spreadsheetId;
    }

    public static String createSpreadsheet(String sheetName) throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, DRIVE_SCOPES))
                .setApplicationName(APPLICATION_NAME)
                .build();


        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties().setTitle(sheetName));

        spreadsheet = service.spreadsheets().create(spreadsheet).setFields("spreadsheetId").execute();

        return spreadsheet.getSpreadsheetId();
    }

    /**
     *
     * Creates a folder in Google Drive.
     * If folder already exists, returns null
     *
     * @param folderName
     * @return
     * @throws Exception
     */
    public static String createFolder(String folderName) throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, DRIVE_SCOPES))
                .setApplicationName(APPLICATION_NAME)
                .build();

        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        try {
            com.google.api.services.drive.model.File file = service.files().create(fileMetadata).setFields("id").execute();
            return file.getId();
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     *
     * Moves file with file-id of fileId to folder with folder-id of folderId.
     * If either do not exist, an exception is thrown.
     * Returns String List of parent id's for the file with file-id of fileId.
     *
     * @param fileId
     * @param folderId
     * @return String List of parent id's for the file with file-id of fileId.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static List<String> moveFileToFolder(String fileId, String folderId) throws GeneralSecurityException, IOException {

        System.out.println("moving file " + fileId + " to folder " + folderId + " . . . ");

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, DRIVE_SCOPES))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // get parent(s) of file to remove from
        com.google.api.services.drive.model.File file = service.files().get(fileId).setFields("parents").execute();
        StringBuilder previousParents = new StringBuilder();
        for (String parent : file.getParents()) {

            previousParents.append(parent);
            previousParents.append(",");

        }

        // remove parent(s) and set new one
        file = service.files().update(fileId, null)
                .setAddParents(folderId)
                .setRemoveParents(previousParents.toString())
                .setFields("id, parents")
                .execute();

        System.out.println("Finished!");

        return file.getParents();
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, final List<String> SCOPES)
            throws IOException {
        // Load client secrets.
        InputStream in = BPDMonitor.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    /**
     *
     * Returns true if the folder exists in Google Drive.
     * Folder MUST NOT be trashed to return true.
     *
     * Returns false if the folder either does not exist, or
     * if the folder is in the trash (is trashed).
     *
     * @param folderId
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static boolean folderExists(String folderId) throws GeneralSecurityException, IOException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, DRIVE_SCOPES))
                .setApplicationName(APPLICATION_NAME)
                .build();

        String pageToken = null;
        do {
            FileList result = service.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, trashed)")
                    .setPageToken(pageToken)
                    .execute();


            for (com.google.api.services.drive.model.File file : result.getFiles()) {
                boolean fileIdMatches = file.getId().equals(folderId);
                boolean isTrashed = file.getTrashed();

                if (fileIdMatches && !isTrashed) {
                    return true;
                }
            }

            pageToken = result.getNextPageToken(); // TODO: might cause memory leak if while loop is not finished ????? (maybe ???? )
        } while (pageToken != null);

        return false;
    }

    /**
     *
     * Returns file id of the file with file-name of fileName parameter if it exists and the file is not trashed.
     * Returns null if file does not exist or file is trashed.
     *
     * @param fileName
     * @return
     */
    public static String fileExistsByName(String fileName) throws IOException, GeneralSecurityException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, DRIVE_SCOPES))
                .setApplicationName(APPLICATION_NAME)
                .build();

        String pageToken = null;
        do {
            FileList result = service.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, trashed)")
                    .setPageToken(pageToken)
                    .execute();


            for (com.google.api.services.drive.model.File file : result.getFiles()) {
                boolean fileIdMatches = file.getName().equals(fileName);
                boolean isTrashed = file.getTrashed();

                if (fileIdMatches && !isTrashed) {
                    return file.getId();
                }
            }

            pageToken = result.getNextPageToken(); // TODO: might cause memory leak if while loop is not finished ????? (maybe ???? )
        } while (pageToken != null);

        return null;

    }

    /**
     *
     * Returns fileId if file exists within folder.
     * Returns null otherwise.
     * Pass null into 'folderId' argument to look in the root folder of Google Drive.
     *
     * @param fileName
     * @param folderId
     * @return
     */
    public static String fileExistsByNameInFolder(String fileName, String folderId) throws Exception {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, DRIVE_SCOPES))
                .setApplicationName(APPLICATION_NAME)
                .build();

        String pageToken = null;
        do {
            FileList result = service.files().list()
                    .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, parents, trashed)")
                    .setPageToken(pageToken)
                    .execute();


            for (com.google.api.services.drive.model.File file : result.getFiles()) {
                boolean fileIdMatches = file.getName().equals(fileName);
                boolean isTrashed = file.getTrashed();
                List<String> parents = file.getParents();
                boolean isInFolder = (parents != null && parents.contains(folderId)) || (parents == null && folderId == null);

                if (fileIdMatches && !isTrashed && isInFolder) {
                    return file.getId();
                }
            }

            pageToken = result.getNextPageToken(); // TODO: might cause memory leak if while loop is not finished ????? (maybe ???? )
        } while (pageToken != null);

        return null;

    }

    /**
     *
     *
     * Old method to test Google's API. Might be useful later.
     * Depricated
     *
     * @throws Exception
     */
    @Deprecated
    public static void oldTestGoogleApi() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
        final String range = "Class Data!A2:E";

        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, SHEETS_SCOPES))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();



        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            System.out.println("Name, Major");
            for (List row : values) {
                System.out.printf("%s, %s\n", row.get(0), row.get(4));
            }
        }
    }
}
