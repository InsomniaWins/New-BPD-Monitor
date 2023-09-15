module ingram.andrew.newbpdmonitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.controlsfx.controls;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.logging.log4j;
    requires com.google.gson;

    opens ingram.andrew.newbpdmonitor to javafx.fxml;
    exports ingram.andrew.newbpdmonitor;
}