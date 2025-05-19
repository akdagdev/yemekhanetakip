package yemekhanetakip;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import yemekhanetakip.controllers.MainController;
import yemekhanetakip.controllers.SceneFactory;
import yemekhanetakip.db.DBMealListUpdater;
import yemekhanetakip.scraper.Scraper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load user settings to apply theme from the beginning
        Properties settings = loadSettings();

        // Load Meals
        Scraper scraper = Scraper.getInstance();
        DBMealListUpdater.getInstance().updateList(scraper);

        FXMLLoader fxmlLoader = SceneFactory.getScene("MAIN");
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        
        // First add the base stylesheet
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        // Apply theme based on settings
        String theme = settings.getProperty("theme", "Açık");
        boolean isDarkMode = "Koyu".equals(theme);
        
        // If dark mode is enabled, set the dark-mode class on the root
        if (isDarkMode) {
            scene.getRoot().getStyleClass().add("dark-mode");
        }
        
        // Apply any custom colors if defined in settings
        String primaryColor = settings.getProperty("primaryColor", "#4dabf7");
        String secondaryColor = settings.getProperty("secondaryColor", "#228be6");
        if (primaryColor != null && secondaryColor != null) {
            applyInitialColors(scene, primaryColor, secondaryColor);
        }
        
        stage.setTitle("Gazi Yemekhane Takip");
        stage.setScene(scene);
        stage.show();
        
        // Get the controller and set the dark mode
        MainController controller = fxmlLoader.getController();
        controller.setDarkMode(isDarkMode);
    }
    
    private Properties loadSettings() {
        Properties settings = new Properties();
        try {
            File settingsFile = new File("yemekhanetakip_settings.properties");
            if (settingsFile.exists()) {
                FileInputStream fis = new FileInputStream(settingsFile);
                settings.load(fis);
                fis.close();
            }
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
        }
        return settings;
    }
    
    private void applyInitialColors(Scene scene, String primaryColor, String secondaryColor) {
        // Create a basic style with just the essential color definitions
        String basicStyles = String.format(
            "*.button, *.primary-button { -fx-background-color: %s !important; } " +
            "*.button:hover, *.primary-button:hover { -fx-background-color: %s !important; } " +
            ".chart-bar, .nutrition-chart .chart-bar { -fx-bar-fill: %s !important; }",
            primaryColor, secondaryColor, primaryColor
        );
        
        // Apply as a dynamic stylesheet
        String encodedCSS = basicStyles.replaceAll("#", "%23");
        scene.getStylesheets().add("data:text/css," + encodedCSS);
        
        // Also set JavaFX core styling properties
        scene.getRoot().setStyle(String.format(
            "-fx-base: %s; -fx-accent: %s; -fx-default-button: %s;",
            primaryColor, primaryColor, primaryColor
        ));
    }

    public static void main(String[] args) {
        launch();
    }
}