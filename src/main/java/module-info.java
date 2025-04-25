module com.example.yemekhanetakip {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.yemekhanetakip to javafx.fxml;
    exports com.example.yemekhanetakip;
}