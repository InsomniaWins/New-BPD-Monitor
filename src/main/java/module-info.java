module ingram.andrew.newbpdmonitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.controlsfx.controls;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.logging.log4j;
    requires com.google.gson;
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.common;
    requires com.google.api.client.http.apache.v2;
    requires com.google.api.client.json.gson;
    requires com.google.errorprone.annotations;
    requires google.api.client;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.services.sheets;
    requires com.google.api.services.drive;
    requires jdk.httpserver;


    opens ingram.andrew.newbpdmonitor to javafx.fxml;
    exports ingram.andrew.newbpdmonitor;
    exports ingram.andrew.newbpdmonitor.data;
    opens ingram.andrew.newbpdmonitor.data to javafx.fxml;
    exports ingram.andrew.newbpdmonitor.runnable;
    opens ingram.andrew.newbpdmonitor.runnable to javafx.fxml;
    exports ingram.andrew.newbpdmonitor.searchterms;
    opens ingram.andrew.newbpdmonitor.searchterms to javafx.fxml;
}