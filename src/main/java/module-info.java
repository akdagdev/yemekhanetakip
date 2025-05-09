module com.example.yemekhanetakip {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.jsoup;
    requires java.sql;
    requires jbcrypt;


    opens yemekhanetakip to javafx.fxml;
    exports yemekhanetakip;
    exports yemekhanetakip.db;
    opens yemekhanetakip.db to javafx.fxml;
    exports yemekhanetakip.controllers;
    opens yemekhanetakip.controllers to javafx.fxml;
    exports yemekhanetakip.scraper;
    opens yemekhanetakip.scraper to javafx.fxml;
}