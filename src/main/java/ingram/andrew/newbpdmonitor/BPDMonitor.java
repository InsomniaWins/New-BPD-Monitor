package ingram.andrew.newbpdmonitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class BPDMonitor extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BPDMonitor.class.getResource("bpdmonitor-main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1360, 768);
        stage.setMinWidth(650);
        stage.setMinHeight(550);
        stage.setTitle("BPD Monitor");
        stage.setScene(scene);
        stage.show();
        BPDMonitorController controller = fxmlLoader.getController();
        controller.initialize();
    }

    public static void main(String[] args) {
        launch();
    }
}