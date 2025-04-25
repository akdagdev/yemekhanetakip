module com.example.yemekhanetakip {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;


    opens com.example.yemekhanetakip to javafx.fxml;
    exports com.example.yemekhanetakip;
}