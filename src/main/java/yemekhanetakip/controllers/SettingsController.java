package yemekhanetakip.controllers;

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
    private MainController mainController;

    public MainController getMainController() {
        return mainController;
    }
    
    // Track if the user has previously selected English
    private boolean wasEnglishSelected = false;
    
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
        
        // Initialize wasEnglishSelected flag based on current setting
        wasEnglishSelected = "English".equals(languageSelector.getValue());
        
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
    
    public void setMainController(MainController controller) {
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
        
        if ("English".equals(languageSelector.getValue()) && !wasEnglishSelected) {
            wasEnglishSelected = true;
        } else if ("Türkçe".equals(languageSelector.getValue())) {
            wasEnglishSelected = false;
        }

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
        
        // Get the main AnchorPane (rootPane) from the MainController
        AnchorPane rootPane = mainController.getRootPane();
        
        if (rootPane == null) {
            System.out.println("Cannot apply color scheme - rootPane is null");
            return;
        }
        
        // Convert the colors to hex strings for CSS
        String primaryColorHex = toHexString(primaryColor);
        String secondaryColorHex = toHexString(secondaryColor);
        
        // Create a more comprehensive dynamic stylesheet with higher specificity
        String dynamicStyles = String.format(
            /* Buttons */
            "*.button, *.primary-button { -fx-background-color: %s !important; } " +
            "*.button:hover, *.primary-button:hover { -fx-background-color: %s !important; } " +
            
            /* Dark mode buttons */
            "*.dark-mode *.button, *.dark-mode *.primary-button { -fx-background-color: %s !important; } " +
            "*.dark-mode *.button:hover, *.dark-mode *.primary-button:hover { -fx-background-color: %s !important; } " +
            
            /* Charts */
            ".chart-bar, .nutrition-chart .chart-bar { -fx-bar-fill: %s !important; } " +
            ".chart-bar:hover, .nutrition-chart .chart-bar:hover { -fx-bar-fill: %s !important; } " +
            
            /* Selection elements */
            ".check-box:selected .box { -fx-background-color: %s !important; } " +
            ".dark-mode .check-box:selected .box { -fx-background-color: %s !important; } " +
            
            /* ComboBox elements */
            ".combo-box .list-cell:selected, .combo-box .list-view .list-cell:selected { -fx-background-color: %s !important; } " +
            ".combo-box:showing, .combo-box:focused { -fx-border-color: %s !important; } " +
            ".combo-box .list-view .list-cell:hover { -fx-background-color: %s !important; } " +
            
            /* DatePicker elements */
            ".date-picker .selected { -fx-background-color: %s !important; } " +
            ".date-picker .day-cell:hover { -fx-background-color: %s !important; } " +
            
            /* Dark mode selection elements */
            ".dark-mode .combo-box .list-cell:selected, .dark-mode .combo-box .list-view .list-cell:selected { -fx-background-color: %s !important; } " +
            ".dark-mode .combo-box .list-view .list-cell:hover { -fx-background-color: %s !important; } " +
            
            /* Nav items */
            ".nav-item:selected, .nav-item.selected { -fx-background-color: %s !important; } " +
            ".dark-mode .nav-item:selected, .dark-mode .nav-item.selected { -fx-background-color: %s !important; }",
            
            primaryColorHex, secondaryColorHex,  // Buttons
            primaryColorHex, secondaryColorHex,  // Dark mode buttons
            primaryColorHex, secondaryColorHex,  // Charts
            primaryColorHex, primaryColorHex,    // Checkboxes
            primaryColorHex, primaryColorHex, secondaryColorHex,  // ComboBox
            primaryColorHex, secondaryColorHex,  // DatePicker
            primaryColorHex, secondaryColorHex,  // Dark mode ComboBox
            primaryColorHex, primaryColorHex     // Nav items
        );
        
        // Get the current scene
        if (rootPane.getScene() != null) {
            // Apply to Scene and Stage
            rootPane.getScene().getStylesheets().removeIf(stylesheet -> 
                stylesheet.startsWith("data:text/css"));
                
            // Add the dynamic stylesheet with encoded characters
            String encodedCSS = dynamicStyles.replaceAll("#", "%23");
            rootPane.getScene().getStylesheets().add("data:text/css," + encodedCSS);
            
            // Also apply a limited set of styles directly to root for immediate effect
            String limitedStyles = String.format(
                "-fx-base: %s; -fx-accent: %s; -fx-default-button: %s;",
                primaryColorHex, primaryColorHex, primaryColorHex
            );
            rootPane.setStyle(limitedStyles);
            
            System.out.println("Applied comprehensive color scheme: primary=" + primaryColorHex + 
                           ", secondary=" + secondaryColorHex);
                           
            // Request a layout pass to ensure changes are visible
            rootPane.requestLayout();
        } else {
            // Fallback to inline style if scene isn't available
            rootPane.setStyle(dynamicStyles);
            System.out.println("Applied inline color scheme (fallback): primary=" + primaryColorHex + 
                           ", secondary=" + secondaryColorHex);
        }
        
        // Store colors in settings
        settings.setProperty("primaryColor", primaryColorHex);
        settings.setProperty("secondaryColor", secondaryColorHex);
    }
    
    private void applyFontSize(double fontSize) {
        if (mainController == null) return;
        
        // Get the root pane from main controller
        AnchorPane rootPane = mainController.getRootPane();
        
        if (rootPane == null) {
            System.out.println("Cannot apply font size - rootPane is null");
            return;
        }
        
        // Create CSS rules for different text elements with specific selectors and !important
        String fontSizeCSS = String.format(
            ".nav-label { -fx-font-size: %.1fpx !important; } " +
            ".meal-label { -fx-font-size: %.1fpx !important; } " +
            ".settings-label { -fx-font-size: %.1fpx !important; } " +
            ".check-box { -fx-font-size: %.1fpx !important; } " +
            ".button, .primary-button { -fx-font-size: %.1fpx !important; } " +
            ".combo-box, .date-picker { -fx-font-size: %.1fpx !important; }",
            fontSize, fontSize, fontSize, fontSize, fontSize, fontSize
        );
        
        // Apply using the stylesheet mechanism
        if (rootPane.getScene() != null) {
            // Remove any previous font size styles
            rootPane.getScene().getStylesheets().removeIf(stylesheet -> 
                stylesheet.startsWith("data:text/css,fontsize"));
                
            // Add as a dynamic stylesheet with a prefix to identify it
            rootPane.getScene().getStylesheets().add("data:text/css,fontsize=" + fontSizeCSS);
            
            System.out.println("Applied font size stylesheet: " + fontSize);
        } else {
            // Fallback to inline style
            String currentStyle = rootPane.getStyle();
            
            // Extract any existing font styles to avoid conflicts
            if (currentStyle != null && !currentStyle.isEmpty()) {
                // Remove existing font-size properties if any
                currentStyle = currentStyle.replaceAll("-fx-font-size:\\s*[\\d.]+px\\s*;", "");
                rootPane.setStyle(currentStyle + " " + fontSizeCSS);
            } else {
                rootPane.setStyle(fontSizeCSS);
            }
            
            System.out.println("Applied font size inline style: " + fontSize);
        }
        
        // Store the font size in settings
        settings.setProperty("fontSize", String.valueOf(fontSize));
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