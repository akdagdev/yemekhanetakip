package yemekhanetakip.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import yemekhanetakip.User;

public class UserProfileController {

    // Top section - User info
    @FXML private Label userFullNameLabel;
    @FXML private Label usernameLabel;

    // Profile Info Panel
    @FXML private VBox profileInfoPanel;
    @FXML private Label fullNameValue;
    @FXML private Label usernameValue;
    @FXML private Label emailValue;
    @FXML private Label registrationDateValue;

    // Account Settings Panel
    @FXML private VBox accountSettingsPanel;
    @FXML private TextField newEmailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label uploadImageStatus;

    // Notifications Panel
    @FXML private VBox notificationsPanel;
    @FXML private VBox notificationsContainer;
    @FXML private Label noNotificationsLabel;

    // General components
    @FXML private ImageView profileImage;

    // Sidebar menu buttons
    @FXML private Button profileInfoButton;
    @FXML private Button mealHistoryButton;
    @FXML private Button accountSettingsButton;
    @FXML private Button notificationsButton;// Make sure this matches your actual package

    @FXML
    private void initialize() {
        // Only show profile info panel on load
        showPanel(profileInfoPanel);

        // Populate labels with the current user's data
        if (User.current != null) {
            userFullNameLabel.setText(User.current.getFullName());
            usernameLabel.setText("@" + User.current.getUsername());

            fullNameValue.setText(User.current.getFullName());
            usernameValue.setText(User.current.getUsername());
            emailValue.setText(User.current.getEmail());
            registrationDateValue.setText("N/A"); // Replace with actual value if available
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

    @FXML
    private void handleLogout() {
        // Logout logic
        System.out.println("Logging out...");
        // Example: redirect to login screen
    }

    @FXML
    private void showProfileInfo() {
        showPanel(profileInfoPanel);
    }

    @FXML
    private void showAccountSettings() {
        showPanel(accountSettingsPanel);
    }

    @FXML
    private void showNotifications() {
        showPanel(notificationsPanel);
        // You can fetch and display notifications here
    }

    @FXML
    private void handleEditProfile() {
        System.out.println("Opening profile editing screen...");
        // Optional: open a new window or show edit UI
    }

    @FXML
    private void handleUpdateEmail() {
        String newEmail = newEmailField.getText().trim();
        if (!newEmail.isEmpty()) {
            System.out.println("New email: " + newEmail);
            // Perform email update logic
        } else {
            System.out.println("Please enter a valid email.");
        }
    }

    @FXML
    private void handleUpdatePassword() {
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (newPass.equals(confirm)) {
            System.out.println("Updating password...");
            // Perform password update logic
        } else {
            System.out.println("New passwords do not match.");
        }
    }

    @FXML
    private void handleUploadImage() {
        System.out.println("Starting image upload...");
        // Use FileChooser to select image and update profileImage
    }

    @FXML
    private void handleDeleteProfile() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("You are about to delete your account.");
        alert.setContentText("This action is irreversible. Do you want to continue?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Deleting account...");
                // Perform account deletion logic
            }
        });
    }

    private void showPanel(VBox panelToShow) {
        // Hide all panels
        profileInfoPanel.setVisible(false);
        accountSettingsPanel.setVisible(false);
        notificationsPanel.setVisible(false);

        // Show the selected panel
        panelToShow.setVisible(true);
    }
}