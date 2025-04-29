package yemekhanetakip;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;

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
    private ToggleButton themeToggle;
    
    @FXML
    private AnchorPane rootPane;
    
    private boolean isDarkMode = false;
    
    private Scraper scraper;
    
    @FXML
    public void initialize() {
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
        
        // Initialize theme toggle button with modern styling
        themeToggle.getStyleClass().add("theme-toggle");
        themeToggle.setSelected(false);
        updateThemeToggleIcon();
    }
    
    @FXML
    public void toggleTheme() {
        isDarkMode = themeToggle.isSelected();
        
        if (isDarkMode) {
            // Switch to dark mode
            rootPane.getStyleClass().add("dark-mode");
        } else {
            // Switch to light mode
            rootPane.getStyleClass().remove("dark-mode");
        }
        
        updateThemeToggleIcon();
    }
    
    private void updateThemeToggleIcon() {
        if (isDarkMode) {
            // Sun icon for dark mode
            themeToggle.setText("\u2600"); // Sun emoji
        } else {
            // Moon icon for light mode
            themeToggle.setText("\uD83C\uDF19"); // Moon emoji
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
} 