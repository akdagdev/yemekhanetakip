module com.example.yemekhanetakip {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires java.sql;

    opens yemekhanetakip to javafx.fxml;
    exports yemekhanetakip;
}