package yemekhanetakip;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import yemekhanetakip.db.DatabaseManager;
import yemekhanetakip.db.FavoritesDBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

public class FavoritesController {
    
    @FXML
    private VBox favoritesContainer;
    
    @FXML
    private Label noFavoritesLabel;
    
    @FXML
    private ComboBox<String> sortComboBox;
    
    private FavoritesDBManager dbManager;
    private User currentUser;
    
    // Database connection properties (same as in DatabaseManager)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/yemekhanetakip";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "rv9yl2qc";
    
    public void initialize() {
        dbManager = new FavoritesDBManager();
        
        // Setup sort options
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
            "Alfabetik (A-Z)", 
            "Alfabetik (Z-A)",
            "En son eklenen",
            "En eski eklenen"
        );
        sortComboBox.setItems(sortOptions);
        sortComboBox.getSelectionModel().selectFirst();
        
        // Add listener for sort option changes
        sortComboBox.setOnAction(event -> loadFavorites());
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        loadFavorites();
    }
    
    private void loadFavorites() {
        if (currentUser == null) {
            noFavoritesLabel.setVisible(true);
            return;
        }
        
        // Clear previous items
        favoritesContainer.getChildren().clear();
        favoritesContainer.getChildren().add(noFavoritesLabel);
        
        // Get sort option
        String sortOption = sortComboBox.getValue();
        String orderBy = "meal_name ASC"; // Default sorting
        
        if (sortOption != null) {
            switch (sortOption) {
                case "Alfabetik (A-Z)":
                    orderBy = "meal_name ASC";
                    break;
                case "Alfabetik (Z-A)":
                    orderBy = "meal_name DESC";
                    break;
                case "En son eklenen":
                    orderBy = "date_added DESC";
                    break;
                case "En eski eklenen":
                    orderBy = "date_added ASC";
                    break;
            }
        }
        
        List<FavoriteMeal> favorites = getFavoritesByUserId(currentUser.getId(), orderBy);
        
        if (favorites.isEmpty()) {
            noFavoritesLabel.setVisible(true);
        } else {
            noFavoritesLabel.setVisible(false);
            displayFavorites(favorites);
        }
    }
    
    private void displayFavorites(List<FavoriteMeal> favorites) {
        for (FavoriteMeal meal : favorites) {
            HBox mealItem = createMealItemUI(meal);
            favoritesContainer.getChildren().add(mealItem);
        }
    }
    
    private HBox createMealItemUI(FavoriteMeal meal) {
        HBox mealItem = new HBox();
        mealItem.getStyleClass().add("favorite-meal-item");
        mealItem.setSpacing(10);
        mealItem.setPadding(new Insets(10));
        
        // Meal name label
        Label nameLabel = new Label(meal.getName());
        nameLabel.getStyleClass().add("meal-name");
        nameLabel.setWrapText(true);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        // Add Date label
        Label dateLabel = new Label(meal.getDateAdded());
        dateLabel.getStyleClass().add("date-label");
        
        // Remove button
        Button removeButton = new Button("Kaldır");
        removeButton.getStyleClass().add("remove-button");
        removeButton.setOnAction(e -> removeFavorite(meal.getId()));
        
        mealItem.getChildren().addAll(nameLabel, dateLabel, removeButton);
        return mealItem;
    }
    
    private void removeFavorite(int favoriteId) {
        try {
            Connection conn = getConnection();
            String query = "DELETE FROM favorites WHERE meal_id = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, favoriteId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                loadFavorites(); // Refresh the list
            }
            
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText("Favori silme hatası");
            alert.setContentText("Favori yemek silinemedi: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private List<FavoriteMeal> getFavoritesByUserId(int userId, String orderBy) {
        List<FavoriteMeal> favorites = new ArrayList<>();
        
        try {
            Connection conn = getConnection();
            String query = "SELECT id, meal_name, date_added FROM favorites WHERE user_id = ? ORDER BY " + orderBy;
            
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                FavoriteMeal meal = new FavoriteMeal(
                    rs.getInt("id"),
                    rs.getString("meal_name"),
                    rs.getString("date_added")
                );
                favorites.add(meal);
            }
            
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return favorites;
    }
    
    // Get database connection (since we can't access the private method in DatabaseManager)
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    // Helper class to represent a favorite meal
    private class FavoriteMeal {
        private int id;
        private String name;
        private String dateAdded;
        
        public FavoriteMeal(int id, String name, String dateAdded) {
            this.id = id;
            this.name = name;
            this.dateAdded = dateAdded;
        }
        
        public int getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDateAdded() {
            return dateAdded;
        }
    }
} 