module com.desktop.scrapper {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires org.slf4j;
    requires java.desktop;
    requires static lombok;
    requires java.net.http;
    requires org.apache.commons.io;

    opens com.desktop.scrapper to javafx.fxml;
    opens com.desktop.scrapper.controller to javafx.fxml;
    exports com.desktop.scrapper;
    exports com.desktop.scrapper.controller;
}