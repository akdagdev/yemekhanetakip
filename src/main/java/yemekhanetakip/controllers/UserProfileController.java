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

    @FXML private Label fullNameValue;
    @FXML private Label usernameValue;
    @FXML private Label emailValue;

    // Notifications Panel
    @FXML private VBox notificationsPanel;

    @FXML
    public void initialize() {

        // Populate labels with the current user's data
        if (User.current != null) {
            userFullNameLabel.setText(User.current.getFullName());
            usernameLabel.setText("@" + User.current.getUsername());

            fullNameValue.setText(User.current.getFullName());
            usernameValue.setText(User.current.getUsername());
            emailValue.setText(User.current.getEmail());
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

}