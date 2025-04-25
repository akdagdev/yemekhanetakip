package yemekhanetakip;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
        Scraper scraper = new Scraper("https://mediko.gazi.edu.tr/view/page/20412");
    }
}