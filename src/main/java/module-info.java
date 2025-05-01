module com.example.yemekhanetakip {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires java.sql;
    requires jbcrypt;


    opens yemekhanetakip to javafx.fxml;
    exports yemekhanetakip;
}