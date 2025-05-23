package yemekhanetakip.controllers;

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
import yemekhanetakip.scraper.Scraper;
import yemekhanetakip.db.FavoritesDBManager;
import yemekhanetakip.db.MealDBManager;
import yemekhanetakip.user.User;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainController {
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

    public boolean getIsDarkMode() {
        return isDarkMode;
    }

    public void setIsDarkMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
    }

    public Scraper getScraper() {
        return scraper;
    }

    // Original menu and nutrition chart containers
    private VBox menuPanel;
    private VBox nutritionChart;

    private MealDBManager mealDBManager;
    private FavoritesDBManager favoritesDBManager;

    @FXML
    public void initialize() {
        // Initialize the database manager using the singleton pattern design
        mealDBManager = MealDBManager.getInstance();
        favoritesDBManager = FavoritesDBManager.getInstance();

        // Initialize the scraper
        scraper = Scraper.getInstance();

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
        FXMLLoader loader = SceneFactory.getScene("MAIN");
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
        FXMLLoader loader = SceneFactory.getScene("MAIN");
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

    private void updateMenuForDate(LocalDate date) {
        resetMealLabels();
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
            meal1Label.setText("Hafta Sonu Kapalı");
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

        if (foods[0].equals("Resmi Tatil")) {
            meal1Label.setText("Resmi Tatil");
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

        // Update the menu labels
        updateMealLabels(foods);


        // Update the menu date label
        menuDateLabel.setText(menuDate + "\nGünün Menüsü");
        menuDateLabel.setStyle("-fx-text-alignment: center; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px 10px 10px; -fx-min-width: 250px; -fx-min-height: 60px; -fx-alignment: center;");

        // Update the chart with total calorie data
        updateChartWithCalories(calorieData);
    }

    private void resetMealLabels() {
        Label[] mealLabels = {meal1Label, meal2Label, meal3Label, meal4Label, meal5Label};
        CheckBox[] mealCheckBoxes = {meal1CheckBox, meal2CheckBox, meal3CheckBox, meal4CheckBox, meal5CheckBox};

        // Clear all meal labels first
        for (int i = 0; i < mealLabels.length; i++) {
            mealLabels[i].setText("");
            mealCheckBoxes[i].setVisible(false);
            mealCheckBoxes[i].setSelected(false);

            // Remove existing event handlers
            mealLabels[i].setOnMouseClicked(null);
        }
    }

    private void updateMealLabels(String[] foods) {
        Label[] mealLabels = {meal1Label, meal2Label, meal3Label, meal4Label, meal5Label};
        CheckBox[] mealCheckBoxes = {meal1CheckBox, meal2CheckBox, meal3CheckBox, meal4CheckBox, meal5CheckBox};

        // Get list of favorite meal names
        List<String> FavMealNames = new ArrayList<>();
        try {
            List<FavoritesDBManager.FavoriteMeal> favorites = new ArrayList<>();
            if (User.current != null) {
                favorites = favoritesDBManager.getFavoritesByUserId(User.current.getId());
            }

            // Extract the names from favorite meals
            for (FavoritesDBManager.FavoriteMeal meal : favorites) {
                FavMealNames.add(meal.getName());
            }
        } catch (Exception e) {
            System.err.println("Error getting favorite meals: " + e.getMessage());
        }

        // Clear all meal labels first
        for (int i = 0; i < mealLabels.length; i++) {
            mealLabels[i].setText("");
            mealCheckBoxes[i].setVisible(false);
            mealCheckBoxes[i].setSelected(false);

            // Remove existing event handlers
            mealLabels[i].setOnMouseClicked(null);
        }

        // Update checkbox selection based on favorites
        for (int i = 0; i < Math.min(foods.length, 5); i++) {
            mealCheckBoxes[i].setVisible(true);
            mealCheckBoxes[i].setSelected(FavMealNames.contains(foods[i]));
        }

        // Update with actual food itemsx
        for (int i = 0; i < Math.min(foods.length, 5); i++) {
            mealLabels[i].setText(foods[i]);

            // Add click event to open food detail
            final int index = i;
            mealLabels[i].setOnMouseClicked(event -> openFoodDetail(foods[index], datePicker.getValue()));

            // Add hover effect to indicate clickable
            mealLabels[i].setStyle("-fx-cursor: hand;");
        }
    }

    private void openFoodDetail(String foodName, LocalDate date) {
        try {
            FXMLLoader loader = SceneFactory.getScene("FOODS");
            Parent foodDetailView = loader.load();

            // Get the controller
            Object controller = loader.getController();

            // Try to set food details if the controller supports it
            try {
                // Use reflection to find and call the setFoodDetails method if it exists
                java.lang.reflect.Method setFoodDetails = controller.getClass().getMethod("setFoodDetails", String.class, LocalDate.class);
                setFoodDetails.invoke(controller, foodName, date);
            } catch (Exception e) {
                System.err.println("Warning: Could not set food details on controller: " + e.getMessage());
                // Continue anyway - the controller might initialize with default values
            }

            // Clear content pane and add food detail view
            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(foodDetailView);

                // Apply current theme
                applyTheme();
            }
        } catch (IOException e) {
            System.err.println("Error loading food detail view: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Hata", "Yemek detayları yüklenirken bir hata oluştu.");
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
        if (User.current == null) {
            // User is not logged in, show alert
            showLoginRequiredAlert();
            checkBox.setSelected(false);
            return;
        }

        int mealId = mealDBManager.getOrCreateMeal(mealName);
        if (checkBox.isSelected()) {
            // Add to favorites
            boolean success = favoritesDBManager.addToFavorites(User.current.getId(), mealId);
            if (!success) {
                // Show error and revert checkbox state
                showErrorAlert("Favorilere eklenemedi", "Yemek favorilere eklenirken bir hata oluştu.");
                checkBox.setSelected(false);
            }
        } else {
            favoritesDBManager.removeFavorite(User.current.getId(), mealId);
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
            FXMLLoader loader = SceneFactory.getScene("SETTINGS");
            Parent settingsView = loader.load();

            // Get the controller
            SettingsController settingsController = loader.getController();

            // Set this controller as the main controller
            settingsController.setMainController(this);

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
            FXMLLoader loader;
            if (User.current == null) {
                // If the user is not logged in
                // Load the Login.fxml
                loader = SceneFactory.getScene("LOGIN");

                // Get the controller
                LoginContoller loginContoller = loader.getController();
                // TODO: Handle logged in state in profile view
            } else {
                // If the user is logged in
                loader = SceneFactory.getScene("USER_PROFILE");

                // Get the controller
                LoginContoller loginContoller = loader.getController();
            }

            Parent profileView = loader.load();


            // Set the currentUser if already logged in


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
            if (User.current == null) {
                showLoginRequiredAlert();
                return;
            }

            // Load the Favorites.fxml
            FXMLLoader loader = SceneFactory.getScene("FAVORITES");
            Parent favoritesView = loader.load();

            // Get the controller
            FavoritesController favoritesController = loader.getController();
            favoritesController.initialize();

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