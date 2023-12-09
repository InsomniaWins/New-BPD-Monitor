package ingram.andrew.newbpdmonitor;

import ingram.andrew.newbpdmonitor.util.GoogleAPI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BPDMonitor extends Application {

    private static BPDMonitorController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BPDMonitor.class.getResource("scenes/bpdmonitor-main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 650, 550);
        stage.setTitle("BPD Monitor");
        stage.setScene(scene);
        stage.show();
        controller = fxmlLoader.getController();
        controller.onProgramInitialize();
    }

    @Override
    public void stop() {
        controller.onProgramClose();
    }

    public static void main(String[] args) {
        /*
        try {
            GoogleAPI.createFolder("Test Folder");
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        launch();
    }

}