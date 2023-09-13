module ingram.andrew.newbpdmonitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.google.gson;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.logging.log4j;

    opens ingram.andrew.newbpdmonitor to javafx.fxml;
    exports ingram.andrew.newbpdmonitor;
}