package yemekhanetakip;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import yemekhanetakip.db.DatabaseManager;
import yemekhanetakip.db.FavoritesDBManager;
import yemekhanetakip.db.MealDBManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProTestController {
    @FXML
    private BarChart<String, Number> calorieChart;
    
    @FXML
    private CategoryAxis mealAxis;
    
    @FXML
    private NumberAxis calorieAxis;
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private Label menuDateLabel;
    
    @FXML
    private Label meal1Label;
    @FXML
    private Label meal2Label;
    @FXML
    private Label meal3Label;
    @FXML
    private Label meal4Label;
    @FXML
    private Label meal5Label;
    
    @FXML
    private CheckBox meal1CheckBox;
    @FXML
    private CheckBox meal2CheckBox;
    @FXML
    private CheckBox meal3CheckBox;
    @FXML
    private CheckBox meal4CheckBox;
    @FXML
    private CheckBox meal5CheckBox;
    
    @FXML
    private AnchorPane rootPane;
    
    @FXML
    private AnchorPane contentPane;
    
    private boolean isDarkMode = false;
    
    private Scraper scraper;
    
    // Current logged in user
    private User currentUser = null;
    
    // Original menu and nutrition chart containers
    private VBox menuPanel;
    private VBox nutritionChart;
    
    private MealDBManager mealDBManager;
    private FavoritesDBManager favoritesDBManager;
    
    @FXML
    public void initialize() {
        // Initialize the database manager
        mealDBManager = new MealDBManager();
        favoritesDBManager = new FavoritesDBManager();
        
        // Initialize the scraper
        scraper = new Scraper("https://mediko.gazi.edu.tr/view/page/20412");
        
        // Initialize the chart
        setupChart();
        
        // Set up date picker listener
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateMenuForDate(newValue);
            }
        });
        
        // Set initial date to today
        datePicker.setValue(LocalDate.now());
        
        // Store references to original content
        storeOriginalContent();
        
        // Set up checkbox listeners for favorites
        setupFavoriteCheckboxes();
    }
    
    /**
     * Stores references to the original content components for later restoration
     */
    private void storeOriginalContent() {
        try {
            if (contentPane != null && contentPane.getChildren().size() >= 2) {
                for (int i = 0; i < contentPane.getChildren().size(); i++) {
                    if (contentPane.getChildren().get(i) instanceof VBox) {
                        if (menuPanel == null) {
                            menuPanel = (VBox) contentPane.getChildren().get(i);
                        } else if (nutritionChart == null) {
                            nutritionChart = (VBox) contentPane.getChildren().get(i);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error storing original content: " + e.getMessage());
        }
    }
    
    /**
     * Safely finds the content container regardless of FXML structure changes
     */
    private HBox findContentContainer() {
        // Look for HBox in rootPane children that is likely the content container
        for (int i = 0; i < rootPane.getChildren().size(); i++) {
            if (rootPane.getChildren().get(i) instanceof HBox) {
                return (HBox) rootPane.getChildren().get(i);
            }
        }
        
        // Fallback to the known index if available
        if (rootPane.getChildren().size() > 2 && 
            rootPane.getChildren().get(2) instanceof HBox) {
            return (HBox) rootPane.getChildren().get(2);
        }
        
        return null;
    }
    
    @FXML
    public void goToHome() {
        try {
            // If we have the contentPane defined in FXML
            if (contentPane != null) {
                // First clear the content
                contentPane.getChildren().clear();
                
                // If we have stored the original panels
                if (menuPanel != null && nutritionChart != null) {
                    // Add them back
                    contentPane.getChildren().addAll(menuPanel, nutritionChart);
                } else {
                    // Otherwise reload the default content
                    loadDefaultContent(contentPane);
                }
            } else {
                // Find the content container safely
                HBox contentContainer = findContentContainer();
                if (contentContainer == null) {
                    System.err.println("Could not find content container");
                    return;
                }
                
                // Clear except for navigation (assuming first child is navigation)
                if (contentContainer.getChildren().size() > 1) {
                    contentContainer.getChildren().remove(1, contentContainer.getChildren().size());
                }
                
                // Load default content into the container
                loadDefaultContentIntoContainer(contentContainer);
            }
            
            // Update the chart and menu date after restoring
            if (datePicker != null && datePicker.getValue() != null) {
                updateMenuForDate(datePicker.getValue());
            }
            
            // Apply current theme
            applyTheme();
            
        } catch (IOException e) {
            System.err.println("Error loading home view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads default content into the provided content pane
     */
    private void loadDefaultContent(AnchorPane targetPane) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProTest.fxml"));
        Parent mainView = loader.load();
        
        // Try to find content container
        for (int i = 0; i < ((AnchorPane) mainView).getChildren().size(); i++) {
            if (((AnchorPane) mainView).getChildren().get(i) instanceof HBox) {
                HBox contentContainer = (HBox) ((AnchorPane) mainView).getChildren().get(i);
                
                // Look for the content pane within the HBox
                for (int j = 0; j < contentContainer.getChildren().size(); j++) {
                    if (contentContainer.getChildren().get(j) instanceof AnchorPane) {
                        AnchorPane newContentPane = (AnchorPane) contentContainer.getChildren().get(j);
                        
                        // Store original content for future reference
                        for (int k = 0; k < newContentPane.getChildren().size(); k++) {
                            if (newContentPane.getChildren().get(k) instanceof VBox) {
                                if (menuPanel == null) {
                                    menuPanel = (VBox) newContentPane.getChildren().get(k);
                                } else if (nutritionChart == null) {
                                    nutritionChart = (VBox) newContentPane.getChildren().get(k);
                                }
                            }
                        }
                        
                        // Add all content
                        targetPane.getChildren().addAll(newContentPane.getChildren());
                        return;
                    }
                }
            }
        }
        
        // Fallback to old method if structure is as expected
        HBox contentContainer = (HBox) ((AnchorPane) mainView).getChildren().get(2);
        AnchorPane newContentPane = (AnchorPane) contentContainer.getChildren().get(1);
        targetPane.getChildren().addAll(newContentPane.getChildren());
    }
    
    /**
     * Loads default content into the provided container
     */
    private void loadDefaultContentIntoContainer(HBox contentContainer) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProTest.fxml"));
        Parent mainView = loader.load();
        
        // Try to find the content pane
        AnchorPane originalContentPane = null;
        
        // Look for the HBox content container in the loaded view
        for (int i = 0; i < ((AnchorPane) mainView).getChildren().size(); i++) {
            if (((AnchorPane) mainView).getChildren().get(i) instanceof HBox) {
                HBox originalContentContainer = (HBox) ((AnchorPane) mainView).getChildren().get(i);
                
                // Find the content pane within the HBox
                for (int j = 0; j < originalContentContainer.getChildren().size(); j++) {
                    if (originalContentContainer.getChildren().get(j) instanceof AnchorPane) {
                        originalContentPane = (AnchorPane) originalContentContainer.getChildren().get(j);
                        break;
                    }
                }
                
                if (originalContentPane != null) break;
            }
        }
        
        // Fallback to old method if structure is as expected
        if (originalContentPane == null) {
            HBox originalContentContainer = (HBox) ((AnchorPane) mainView).getChildren().get(2);
            originalContentPane = (AnchorPane) originalContentContainer.getChildren().get(1);
        }
        
        // Add the original content
        contentContainer.getChildren().add(originalContentPane);
    }
    
    // Add a setter for dark mode that can be called from the SettingsController
    public void setDarkMode(boolean darkMode) {
        if (isDarkMode != darkMode) {
            isDarkMode = darkMode;
            applyTheme();
            System.out.println("Theme changed to: " + (isDarkMode ? "Dark" : "Light"));
        }
    }
    
    // Get current theme state
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    private void applyTheme() {
        if (isDarkMode) {
            if (!rootPane.getStyleClass().contains("dark-mode")) {
                rootPane.getStyleClass().add("dark-mode");
            }
        } else {
            rootPane.getStyleClass().remove("dark-mode");
        }
    }
    
    private void setupChart() {
        // Configure chart appearance
        calorieChart.setTitle("Kalori Değerleri");
        mealAxis.setLabel("Menü");
        calorieAxis.setLabel("Kalori (kcal)");
        
        // Set up axis ranges for total calories (typically higher than individual meals)
        calorieAxis.setAutoRanging(true);
        calorieAxis.setLowerBound(0);
        calorieAxis.setUpperBound(1500); // Adjusted upper bound for total daily calories
        calorieAxis.setTickUnit(250);    // Larger tick unit for better readability
    }
    
    // Method to set the current user after login
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    private void updateMenuForDate(LocalDate date) {
        // Check if the date is a weekend
        if (date.getDayOfWeek().getValue() >= 6) { // 6 is Saturday, 7 is Sunday
            // Clear all meal labels and checkboxes
            Label[] mealLabels = {meal1Label, meal2Label, meal3Label, meal4Label, meal5Label};
            CheckBox[] mealCheckBoxes = {meal1CheckBox, meal2CheckBox, meal3CheckBox, meal4CheckBox, meal5CheckBox};
            
            for (int i = 0; i < 5; i++) {
                mealLabels[i].setText("");
                mealCheckBoxes[i].setVisible(false);
            }
            
            // Set the first label to show weekend message
            meal1Label.setText("Hafta sonu yemekhane kapalıdır.");
            meal1Label.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
            meal1Label.setWrapText(true);
            meal1Label.setMaxWidth(200);
            
            // Update the menu date label
            menuDateLabel.setText(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "\nGünün Menüsü");
            menuDateLabel.setStyle("-fx-text-alignment: center; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px 10px 10px; -fx-min-width: 250px; -fx-min-height: 60px; -fx-alignment: center;");
            
            // Clear the chart
            calorieChart.getData().clear();
            return;
        }
        
        // Format the date to match the scraper's output format
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Get the menu for the selected date
        String menuData = scraper.getMenuForDate(formattedDate);
        
        // Get the calorie information for the selected date
        String calorieData = scraper.getCaloriesForDate(formattedDate);
        
        // Parse the menu data
        String[] parts = menuData.split("\n");
        String[] foods = parts[0].replace("[", "").replace("]", "").split(", ");
        String menuDate = parts[1];
        
        // Update the menu labels
        updateMealLabels(foods);
        
        // Update the menu date label
        menuDateLabel.setText(menuDate + "\nGünün Menüsü");
        menuDateLabel.setStyle("-fx-text-alignment: center; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px 10px 10px; -fx-min-width: 250px; -fx-min-height: 60px; -fx-alignment: center;");
        
        // Update the chart with total calorie data
        updateChartWithCalories(calorieData);
    }
    
    private void updateMealLabels(String[] foods) {
        Label[] mealLabels = {meal1Label, meal2Label, meal3Label, meal4Label, meal5Label};
        CheckBox[] mealCheckBoxes = {meal1CheckBox, meal2CheckBox, meal3CheckBox, meal4CheckBox, meal5CheckBox};
        
        // Clear all labels and checkboxes first
        for (int i = 0; i < 5; i++) {
            mealLabels[i].setText("");
            mealCheckBoxes[i].setVisible(false);
        }
        
        // Update with actual food items
        for (int i = 0; i < Math.min(foods.length, 5); i++) {
            mealLabels[i].setText(foods[i]);
            mealCheckBoxes[i].setVisible(true);
        }
    }
    
    private void updateChartWithCalories(String calorieInfo) {
        // Clear existing data
        calorieChart.getData().clear();
        
        // Create a new series for the chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Toplam Kalori Değeri");
        
        // Extract the calorie value from the string (e.g., "Kalori:1100")
        int calories = 0;
        if (calorieInfo != null && !calorieInfo.equals("Bilgi yok")) {
            try {
                // Extract the numeric part from the string
                String calorieValueStr = calorieInfo.replaceAll("[^0-9]", "");
                calories = Integer.parseInt(calorieValueStr);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing calorie value: " + e.getMessage());
                calories = 0;
            }
        }
        
        // Add a single data point for the total calories
        series.getData().add(new XYChart.Data<>("Toplam", calories));
        
        // Add the series to the chart
        calorieChart.getData().add(series);
        
        // Update chart title to reflect total calories
        calorieChart.setTitle("Toplam Kalori: " + calories + " kcal");
        
        // Force chart update
        calorieChart.layout();
    }
    
    private void setupFavoriteCheckboxes() {
        meal1CheckBox.setOnAction(e -> handleFavoriteToggle(meal1CheckBox, meal1Label.getText()));
        meal2CheckBox.setOnAction(e -> handleFavoriteToggle(meal2CheckBox, meal2Label.getText()));
        meal3CheckBox.setOnAction(e -> handleFavoriteToggle(meal3CheckBox, meal3Label.getText()));
        meal4CheckBox.setOnAction(e -> handleFavoriteToggle(meal4CheckBox, meal4Label.getText()));
        meal5CheckBox.setOnAction(e -> handleFavoriteToggle(meal5CheckBox, meal5Label.getText()));
    }
    
    private void handleFavoriteToggle(CheckBox checkBox, String mealName) {
        if (currentUser == null) {
            // User not logged in, show alert
            showLoginRequiredAlert();
            checkBox.setSelected(false);
            return;
        }
        
        if (checkBox.isSelected()) {
            // Add to favorites
            int mealId = mealDBManager.getOrCreateMeal(mealName);
            boolean success = favoritesDBManager.addToFavorites(currentUser.getId(), mealId);
            if (!success) {
                // Show error and revert checkbox state
                showErrorAlert("Favorilere eklenemedi", "Yemek favorilere eklenirken bir hata oluştu.");
                checkBox.setSelected(false);
            }
        } else {
            // TODO: Remove from favorites - would need to get the favorite ID
            // For now, just show a message
            showInfoAlert("Favorilerden kaldırıldı", "Yemek favorilerinizden kaldırıldı.");
        }
    }
    
    private void showLoginRequiredAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Giriş Gerekli");
        alert.setHeaderText("Lütfen giriş yapın");
        alert.setContentText("Favori işlemleri için giriş yapmanız gerekmektedir.");
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bilgi");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Add a method to navigate to settings page
    @FXML
    public void openSettings() {
        try {
            // Load the Settings.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Settings.fxml"));
            Parent settingsView = loader.load();
            
            // Get the controller
            SettingsController settingsController = loader.getController();
            
            // Set this controller as the main controller
            settingsController.setMainController(this);
            System.out.println("Main controller passed to settings controller");
            
            // Replace content in the main content area
            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(settingsView);
            } else {
                // Find the content container safely
                HBox contentContainer = findContentContainer();
                if (contentContainer == null) {
                    System.err.println("Could not find content container for settings view");
                    return;
                }
                
                // Clear the container except for the navigation
                if (contentContainer.getChildren().size() > 1) {
                    contentContainer.getChildren().remove(1, contentContainer.getChildren().size());
                }
                
                // Add the settings view
                contentContainer.getChildren().add(settingsView);
            }
            
            // Apply current theme
            applyTheme();
        } catch (IOException e) {
            System.err.println("Error loading settings view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getter for rootPane to allow other controllers to access it
    public AnchorPane getRootPane() {
        return rootPane;
    }
    
    @FXML
    public void openProfile() {
        try {
            // Load the Profile.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Profile.fxml"));
            Parent profileView = loader.load();
            
            // Get the controller
            ProfileController profileController = loader.getController();
            
            // Set the currentUser if already logged in
            if (currentUser != null) {
                // TODO: Handle logged in state in profile view
            }
            
            // Replace content in the main content area
            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(profileView);
            } else {
                // Find the content container safely
                HBox contentContainer = findContentContainer();
                if (contentContainer == null) {
                    System.err.println("Could not find content container for profile view");
                    return;
                }
                
                // Clear the container except for the navigation
                if (contentContainer.getChildren().size() > 1) {
                    contentContainer.getChildren().remove(1, contentContainer.getChildren().size());
                }
                
                // Add the profile view
                contentContainer.getChildren().add(profileView);
            }
            
            // Apply current theme
            applyTheme();
        } catch (IOException e) {
            System.err.println("Error loading profile view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void openFavorites() {
        try {
            // Load the Favorites.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Favorites.fxml"));
            Parent favoritesView = loader.load();
            
            // Get the controller
            FavoritesController favoritesController = loader.getController();
            
            // Set the currentUser if already logged in
            if (currentUser != null) {
                favoritesController.setUser(currentUser);
            }
            
            // Replace content in the main content area
            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(favoritesView);
            } else {
                // Find the content container safely
                HBox contentContainer = findContentContainer();
                if (contentContainer == null) {
                    System.err.println("Could not find content container for favorites view");
                    return;
                }
                
                // Clear the container except for the navigation
                if (contentContainer.getChildren().size() > 1) {
                    contentContainer.getChildren().remove(1, contentContainer.getChildren().size());
                }
                
                // Add the favorites view
                contentContainer.getChildren().add(favoritesView);
            }
            
            // Apply current theme
            applyTheme();
        } catch (IOException e) {
            System.err.println("Error loading favorites view: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 