package yemekhanetakip;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProTestApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Scraper scraper = new Scraper("https://mediko.gazi.edu.tr/view/page/20412");
        FXMLLoader fxmlLoader = new FXMLLoader(ProTestApp.class.getResource("ProTest.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Gazi yemekhane Takip");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}