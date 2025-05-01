package yemekhanetakip;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javafx.scene.layout.AnchorPane;

public class SettingsController {
    @FXML
    private ComboBox<String> themeSelector;
    
    @FXML
    private ColorPicker primaryColorPicker;
    
    @FXML
    private ColorPicker secondaryColorPicker;
    
    @FXML
    private Slider fontSizeSlider;
    
    @FXML
    private Label fontSizeLabel;
    
    @FXML
    private ComboBox<String> languageSelector;
    
    @FXML
    private CheckBox enableNotificationsCheckbox;
    
    @FXML
    private CheckBox favoriteNotificationsCheckbox;
    
    @FXML
    private Button restoreDefaultsButton;
    
    @FXML
    private Button saveSettingsButton;
    
    // Default values
    private final String DEFAULT_THEME = "Açık";
    private final Color DEFAULT_PRIMARY_COLOR = Color.web("#4dabf7");
    private final Color DEFAULT_SECONDARY_COLOR = Color.web("#228be6");
    private final double DEFAULT_FONT_SIZE = 14.0;
    private final String DEFAULT_LANGUAGE = "Türkçe";
    private final boolean DEFAULT_NOTIFICATIONS_ENABLED = true;
    private final boolean DEFAULT_FAVORITE_NOTIFICATIONS = true;
    
    // Properties for settings storage
    private Properties settings;
    private final String SETTINGS_FILE = "yemekhanetakip_settings.properties";
    
    // Main controller reference
    private ProTestController mainController;
    
    @FXML
    public void initialize() {
        // Initialize settings
        settings = new Properties();
        loadSettings();
        
        // Set up the theme selector
        themeSelector.getSelectionModel().select(
            settings.getProperty("theme", DEFAULT_THEME)
        );
        
        // Add listener to theme selector to apply theme immediately when changed
        themeSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (mainController != null && newVal != null) {
                boolean isDarkMode = newVal.equals("Koyu");
                mainController.setDarkMode(isDarkMode);
            }
        });
        
        // Add style class to font size label for better visibility
        fontSizeLabel.getStyleClass().add("settings-label");
        
        // Set up color pickers
        primaryColorPicker.setValue(Color.web(
            settings.getProperty("primaryColor", toHexString(DEFAULT_PRIMARY_COLOR))
        ));
        
        secondaryColorPicker.setValue(Color.web(
            settings.getProperty("secondaryColor", toHexString(DEFAULT_SECONDARY_COLOR))
        ));
        
        // Set up font size slider
        try {
            double fontSize = Double.parseDouble(settings.getProperty("fontSize", String.valueOf(DEFAULT_FONT_SIZE)));
            fontSizeSlider.setValue(fontSize);
        } catch (NumberFormatException e) {
            fontSizeSlider.setValue(DEFAULT_FONT_SIZE);
        }
        updateFontSizeLabel(fontSizeSlider.getValue());
        
        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateFontSizeLabel(newVal.doubleValue());
        });
        
        // Set up language selector
        languageSelector.getSelectionModel().select(
            settings.getProperty("language", DEFAULT_LANGUAGE)
        );
        
        // Set up notification checkboxes
        enableNotificationsCheckbox.setSelected(
            Boolean.parseBoolean(settings.getProperty("notificationsEnabled", String.valueOf(DEFAULT_NOTIFICATIONS_ENABLED)))
        );
        
        favoriteNotificationsCheckbox.setSelected(
            Boolean.parseBoolean(settings.getProperty("favoriteNotifications", String.valueOf(DEFAULT_FAVORITE_NOTIFICATIONS)))
        );
        
        // Enable/disable favorite notifications based on main notifications setting
        favoriteNotificationsCheckbox.disableProperty().bind(
            enableNotificationsCheckbox.selectedProperty().not()
        );
    }
    
    public void setMainController(ProTestController controller) {
        this.mainController = controller;
        System.out.println("Main controller set in settings controller");
        
        // Apply current theme setting immediately
        if (themeSelector.getValue() != null) {
            String theme = themeSelector.getValue();
            boolean isDarkMode = theme.equals("Koyu");
            controller.setDarkMode(isDarkMode);
        }
    }
    
    @FXML
    public void saveSettings() {
        // Save all settings to properties
        settings.setProperty("theme", themeSelector.getValue());
        settings.setProperty("primaryColor", toHexString(primaryColorPicker.getValue()));
        settings.setProperty("secondaryColor", toHexString(secondaryColorPicker.getValue()));
        settings.setProperty("fontSize", String.valueOf(fontSizeSlider.getValue()));
        settings.setProperty("language", languageSelector.getValue());
        settings.setProperty("notificationsEnabled", String.valueOf(enableNotificationsCheckbox.isSelected()));
        settings.setProperty("favoriteNotifications", String.valueOf(favoriteNotificationsCheckbox.isSelected()));
        
        // Save properties to file
        saveSettingsToFile();
        
        // Apply the theme immediately
        applySettings();
        
        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ayarlar Kaydedildi");
        alert.setHeaderText(null);
        alert.setContentText("Ayarlarınız başarıyla kaydedildi.");
        alert.showAndWait();
    }
    
    @FXML
    public void restoreDefaults() {
        // Reset all controls to default values
        themeSelector.getSelectionModel().select(DEFAULT_THEME);
        primaryColorPicker.setValue(DEFAULT_PRIMARY_COLOR);
        secondaryColorPicker.setValue(DEFAULT_SECONDARY_COLOR);
        fontSizeSlider.setValue(DEFAULT_FONT_SIZE);
        languageSelector.getSelectionModel().select(DEFAULT_LANGUAGE);
        enableNotificationsCheckbox.setSelected(DEFAULT_NOTIFICATIONS_ENABLED);
        favoriteNotificationsCheckbox.setSelected(DEFAULT_FAVORITE_NOTIFICATIONS);
    }
    
    private void loadSettings() {
        try {
            File settingsFile = new File(SETTINGS_FILE);
            if (settingsFile.exists()) {
                FileInputStream fis = new FileInputStream(settingsFile);
                settings.load(fis);
                fis.close();
            }
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void saveSettingsToFile() {
        try {
            FileOutputStream fos = new FileOutputStream(SETTINGS_FILE);
            settings.store(fos, "Yemekhane Takip Application Settings");
            fos.close();
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void applySettings() {
        if (mainController != null) {
            // Apply theme based on selection
            String theme = themeSelector.getValue();
            boolean isDarkMode = theme.equals("Koyu");
            mainController.setDarkMode(isDarkMode);
            
            // Apply primary and secondary colors
            applyColorScheme(primaryColorPicker.getValue(), secondaryColorPicker.getValue());
            
            // Apply font size
            applyFontSize(fontSizeSlider.getValue());
        }
    }
    
    private void applyColorScheme(Color primaryColor, Color secondaryColor) {
        if (mainController == null) return;
        
        // Get the main AnchorPane (rootPane) from the ProTestController
        AnchorPane rootPane = mainController.getRootPane();
        
        if (rootPane == null) {
            System.out.println("Cannot apply color scheme - rootPane is null");
            return;
        }
        
        // Convert the colors to hex strings for CSS
        String primaryColorHex = toHexString(primaryColor);
        String secondaryColorHex = toHexString(secondaryColor);
        
        // Create a style string for the inline CSS
        String style = String.format(
            ".button, .primary-button { -fx-background-color: %s; } " +
            ".button:hover, .primary-button:hover { -fx-background-color: %s; } " +
            ".dark-mode .primary-button { -fx-background-color: %s; } " +
            ".dark-mode .primary-button:hover { -fx-background-color: %s; } " +
            ".nutrition-chart .chart-bar { -fx-bar-fill: %s; } " +
            ".nutrition-chart .chart-bar:hover { -fx-bar-fill: %s; } " +
            ".dark-mode .combo-box .combo-box-popup .list-view .list-cell:hover, " +
            ".dark-mode .combo-box .combo-box-popup .list-view .list-cell:selected { -fx-background-color: %s; }",
            primaryColorHex, secondaryColorHex, 
            primaryColorHex, secondaryColorHex,
            primaryColorHex, secondaryColorHex,
            primaryColorHex
        );
        
        // Apply the inline style to the root pane
        rootPane.setStyle(style);
        
        System.out.println("Applied color scheme: primary=" + primaryColorHex + 
                           ", secondary=" + secondaryColorHex);
    }
    
    private void applyFontSize(double fontSize) {
        if (mainController == null) return;
        
        // Get the root pane from main controller
        AnchorPane rootPane = mainController.getRootPane();
        
        if (rootPane == null) {
            System.out.println("Cannot apply font size - rootPane is null");
            return;
        }
        
        // Create CSS rules for different text elements
        String fontSizeCSS = String.format(
            ".nav-label { -fx-font-size: %.1fpx; } " +
            ".meal-label { -fx-font-size: %.1fpx; } " +
            ".settings-label { -fx-font-size: %.1fpx; } " +
            ".check-box { -fx-font-size: %.1fpx; }",
            fontSize, fontSize, fontSize, fontSize
        );
        
        // Get the current style and append font size CSS
        String currentStyle = rootPane.getStyle();
        
        // If there's already a style, append to it
        if (currentStyle != null && !currentStyle.isEmpty()) {
            rootPane.setStyle(currentStyle + " " + fontSizeCSS);
        } else {
            rootPane.setStyle(fontSizeCSS);
        }
        
        System.out.println("Applied font size: " + fontSize);
    }
    
    private void updateFontSizeLabel(double fontSize) {
        // Round to nearest 0.5
        double roundedSize = Math.round(fontSize * 2) / 2.0;
        fontSizeLabel.setText(String.format("%.1f px", roundedSize));
    }
    
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X", 
            (int)(color.getRed() * 255), 
            (int)(color.getGreen() * 255), 
            (int)(color.getBlue() * 255));
    }
} 