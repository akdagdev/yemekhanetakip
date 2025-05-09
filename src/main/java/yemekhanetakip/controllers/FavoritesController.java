package yemekhanetakip.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import yemekhanetakip.User;
import yemekhanetakip.db.FavoritesDBManager;
import java.util.List;

public class FavoritesController {
    
    @FXML
    private VBox favoritesContainer;
    
    @FXML
    private Label noFavoritesLabel;
    
    @FXML
    private ComboBox<String> sortComboBox;
    
    private FavoritesDBManager dbManager;
    
    public void initialize() {
        dbManager = FavoritesDBManager.getInstance();
        
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
        loadFavorites();
    }

    
    private void loadFavorites() {
        if (User.current == null) {
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
        
        List<FavoritesDBManager.FavoriteMeal> favorites = dbManager.getFavoritesByUserId(User.current.getId());
        
        if (favorites.isEmpty()) {
            noFavoritesLabel.setVisible(true);
        } else {
            noFavoritesLabel.setVisible(false);
            displayFavorites(favorites);
        }
    }
    
    private void displayFavorites(List<FavoritesDBManager.FavoriteMeal> favorites) {
        for (FavoritesDBManager.FavoriteMeal meal : favorites) {
            HBox mealItem = createMealItemUI(meal);
            favoritesContainer.getChildren().add(mealItem);
        }
    }
    
    private HBox createMealItemUI(FavoritesDBManager.FavoriteMeal meal) {
        HBox mealItem = new HBox();
        mealItem.getStyleClass().add("favorite-meal-item");
        mealItem.setSpacing(10);
        mealItem.setPadding(new Insets(10));
        
        // Meal name label
        Label nameLabel = new Label(meal.getName());
        nameLabel.getStyleClass().add("meal-name");
        nameLabel.setWrapText(true);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        // Remove button
        Button removeButton = new Button("Kaldır");
        removeButton.getStyleClass().add("remove-button");
        removeButton.setOnAction(e -> {
            dbManager.removeFavorite(User.current.getId(), meal.getId());
            loadFavorites();
        });
        
        mealItem.getChildren().addAll(nameLabel, removeButton);
        return mealItem;
    }
}