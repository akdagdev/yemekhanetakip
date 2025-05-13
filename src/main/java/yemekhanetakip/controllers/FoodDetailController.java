package yemekhanetakip.controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import yemekhanetakip.User;
import yemekhanetakip.db.MealDBManager;
import yemekhanetakip.db.VoteDBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the food detail view
 */
public class FoodDetailController {
    private static final Logger LOG = Logger.getLogger(FoodDetailController.class.getName());
    
    @FXML
    private AnchorPane rootPane;
    
    @FXML
    private Label foodNameLabel;
    
    @FXML
    private Label avgRatingLabel;
    
    @FXML
    private Label totalRatingsLabel;
    
    @FXML
    private HBox avgRatingStars;
    
    @FXML
    private HBox userRatingContainer;
    
    @FXML
    private ToggleButton star1;
    
    @FXML
    private ToggleButton star2;
    
    @FXML
    private ToggleButton star3;
    
    @FXML
    private ToggleButton star4;
    
    @FXML
    private ToggleButton star5;
    
    @FXML
    private TextArea commentTextArea;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private VBox commentsContainer;

    @FXML
    private ScrollPane scrollPane;
    
    private String foodName;
    private LocalDate foodDate;
    private int currentRating = 0;
    private MealDBManager mealDBManager;
    private VoteDBManager voteDBManager;
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        mealDBManager = MealDBManager.getInstance();
        voteDBManager = VoteDBManager.getInstance();

        setupStarButtons();

        Platform.runLater(() -> {
            PauseTransition delay = new PauseTransition(Duration.millis(25));
            delay.setOnFinished(e -> scrollPane.setVvalue(0));
            delay.play();
        });
    }
    
    /**
     * Set food details to display
     * 
     * @param foodName The name of the food
     * @param date The date the food was/will be served
     */
    public void setFoodDetails(String foodName, LocalDate date) {
        this.foodName = foodName;
        this.foodDate = date;
        
        foodNameLabel.setText(foodName);
        Platform.runLater(() -> scrollPane.setVvalue(0));
        
        loadFoodRatings();
    }
    
    /**
     * Set up the star buttons for user rating
     */
    private void setupStarButtons() {
        ToggleButton[] stars = {star1, star2, star3, star4, star5};
        
        // Clear any existing handlers
        for (ToggleButton star : stars) {
            star.setSelected(false);
        }
    }
    
    /**
     * Load and display food ratings and comments
     */
    private void loadFoodRatings() {
        int mealId = mealDBManager.getOrCreateMeal(foodName);
        if (mealId == -1) {
            avgRatingLabel.setText("0/5");
            totalRatingsLabel.setText("(0 değerlendirme)");
            commentsContainer.getChildren().clear();
            updateAverageRatingStars(0);
            return;
        }
        
        // Get all votes for this meal
        List<Map<String, Object>> votes = getVotesForMeal(mealId);
        
        // Calculate average rating
        if (votes.isEmpty()) {
            avgRatingLabel.setText("0/5");
            totalRatingsLabel.setText("(0 değerlendirme)");
            updateAverageRatingStars(0);
        } else {
            double totalRating = 0;
            for (Map<String, Object> vote : votes) {
                totalRating += (Integer) vote.get("rating");
            }
            float avgRating = (float) (totalRating / votes.size());
            
            avgRatingLabel.setText(String.format("%.1f/5", avgRating));
            totalRatingsLabel.setText("(" + votes.size() + " değerlendirme)");
            updateAverageRatingStars(avgRating);
        }
        
        commentsContainer.getChildren().clear();
        
        loadCommentsFromDatabase(votes);
    }
    
    /**
     * Get votes for a specific meal
     * 
     * @param mealId The meal ID
     * @return List of votes
     */
    private List<Map<String, Object>> getVotesForMeal(int mealId) {
        List<Map<String, Object>> votes = new ArrayList<>();
        final String SQL = "SELECT mv.user_rate, mv.user_comment, mv.comment_date, u.user_name " +
                           "FROM meal_votes mv " +
                           "JOIN users u ON mv.user_id = u.user_id " +
                           "WHERE mv.meal_id = ?";
        
        try (Connection conn = voteDBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            
            ps.setInt(1, mealId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> vote = new HashMap<>();
                    vote.put("rating", rs.getInt("user_rate"));
                    vote.put("comment", rs.getString("user_comment"));
                    vote.put("date", rs.getDate("comment_date").toLocalDate());
                    vote.put("username", rs.getString("user_name"));
                    votes.add(vote);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error getting votes for meal", e);
        }
        
        return votes;
    }
    
    /**
     * Load comments from database
     * 
     * @param votes List of votes with comments
     */
    private void loadCommentsFromDatabase(List<Map<String, Object>> votes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        
        for (Map<String, Object> vote : votes) {
            String username = (String) vote.get("username");
            String comment = (String) vote.get("comment");
            int rating = (Integer) vote.get("rating");
            LocalDate voteDate = (LocalDate) vote.get("date");
            
            // Skip if comment is empty
            if (comment == null || comment.trim().isEmpty()) {
                continue;
            }
            
            VBox commentBox = new VBox(8);
            commentBox.getStyleClass().add("comment-box");
            commentBox.setPadding(new javafx.geometry.Insets(15, 15, 15, 15));
            
            HBox commentHeader = new HBox(10);
            commentHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label usernameLabel = new Label(username);
            usernameLabel.getStyleClass().add("comment-user");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            Label dateLabel = new Label(voteDate.format(formatter));
            dateLabel.getStyleClass().add("comment-date");
            
            commentHeader.getChildren().addAll(usernameLabel, spacer, dateLabel);
            
            HBox ratingBox = new HBox(5);
            ratingBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label ratingLabel = new Label("★".repeat(rating));
            ratingLabel.getStyleClass().add("comment-rating");
            ratingBox.getChildren().add(ratingLabel);
            
            Label commentText = new Label(comment);
            commentText.getStyleClass().add("comment-text");
            commentText.setWrapText(true);
            
            commentBox.getChildren().addAll(commentHeader, ratingBox, commentText);
            commentsContainer.getChildren().add(commentBox);
        }
        
        commentsContainer.setSpacing(20);
    }
    
    /**
     * Update the average rating stars display
     * 
     * @param rating The average rating (0-5)
     */
    private void updateAverageRatingStars(float rating) {
        avgRatingStars.getChildren().clear();
        
        int fullStars = (int) rating;
        boolean halfStar = rating - fullStars >= 0.5f;
        
        for (int i = 0; i < 5; i++) {
            Label star = new Label();
            star.getStyleClass().add("comment-rating");
            
            if (i < fullStars) {
                star.setText("★");
            } else if (i == fullStars && halfStar) {
                star.setText("★");
                star.setOpacity(0.5);
            } else {
                star.setText("★");
                star.setOpacity(0.2);
            }
            
            avgRatingStars.getChildren().add(star);
        }
    }
    
    /**
     * Handle user rating selection
     */
    @FXML
    public void handleRating() {
        // Get the selected rating from the clicked star button
        Object source = userRatingContainer.getScene().getFocusOwner();
        if (source instanceof ToggleButton) {
            ToggleButton clickedStar = (ToggleButton) source;
            currentRating = Integer.parseInt(clickedStar.getUserData().toString());
            
            // Update the UI to show the selected stars
            updateStarsUI(currentRating);
        }
    }
    
    /**
     * Update the stars UI based on the selected rating
     * 
     * @param rating The selected rating (1-5)
     */
    private void updateStarsUI(int rating) {
        ToggleButton[] stars = {star1, star2, star3, star4, star5};
        
        for (int i = 0; i < stars.length; i++) {
            stars[i].setSelected(i < rating);
        }
    }
    
    /**
     * Check if a user has already voted for a meal
     * 
     * @param userId The user ID
     * @param mealId The meal ID
     * @return True if the user has already voted
     */
    private boolean hasUserVotedForMeal(int userId, int mealId) {
        final String SQL = "SELECT 1 FROM meal_votes WHERE user_id = ? AND meal_id = ?";
        
        try (Connection conn = voteDBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, mealId);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error checking if user voted for meal", e);
            return false;
        }
    }
    
    /**
     * Handle submission of user rating and comment
     */
    @FXML
    public void handleSubmitRating() {
        if (User.current == null) {
            showLoginRequiredAlert();
            return;
        }
        
        // Check if a rating was selected
        if (currentRating == 0) {
            showErrorAlert("Değerlendirme Gerekli", "Lütfen bir yıldız derecesi seçin.");
            return;
        }
        
        // Get the comment
        String comment = commentTextArea.getText().trim();
        
        // Get meal ID for this food
        int mealId = mealDBManager.getOrCreateMeal(foodName);
        if (mealId == -1) {
            showErrorAlert("Hata", "Bu yemek bulunamadı.");
            return;
        }
        
        // Add or update the vote using the VoteDBManager
        boolean success = voteDBManager.addOrUpdateVote(User.current.getId(), mealId, currentRating, comment);
        
        if (success) {
            currentRating = 0;
            updateStarsUI(0);
            commentTextArea.clear();
            
            showInfoAlert("Başarılı", "Değerlendirmeniz kaydedildi. Teşekkürler!");
            
            loadFoodRatings();
        } else {
            showErrorAlert("Hata", "Değerlendirme kaydedilirken bir hata oluştu. Lütfen tekrar deneyin.");
        }
    }
    
    /**
     * Show an alert that login is required
     */
    private void showLoginRequiredAlert() {
        showErrorAlert("Giriş Gerekli", "Bu işlemi gerçekleştirmek için giriş yapmanız gerekmektedir.");
    }
    
    /**
     * Show an error alert
     */
    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show an information alert
     */
    private void showInfoAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 