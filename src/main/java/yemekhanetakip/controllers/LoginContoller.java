package yemekhanetakip.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import yemekhanetakip.user.User;
import yemekhanetakip.db.UserDBManager;
import javafx.scene.media.MediaPlayer;

public class LoginContoller {
    
    @FXML
    private TextField loginUsername;
    
    @FXML
    private PasswordField loginPassword;
    
    @FXML
    private Label loginMessageLabel;
    
    @FXML
    private TextField registerFullName;
    
    @FXML
    private TextField registerEmail;
    
    @FXML
    private TextField registerUsername;
    
    @FXML
    private PasswordField registerPassword;
    
    @FXML
    private Label registerMessageLabel;
    
    @FXML
    private TabPane authTabPane;
    
    private UserDBManager dbManager;
    
    private int loginClickCount = 0;
    private MediaPlayer mysterySoundPlayer;
    
    @FXML
    public void initialize() {
        // Initialize the database manager
        dbManager = UserDBManager.getInstance();
        
        // Clear any previous message
        loginMessageLabel.setText("");
        registerMessageLabel.setText("");
    }
    
    @FXML
    public void handleLogin() {
        loginClickCount++;
        
        if (loginClickCount >= 20) {
            
            loginClickCount = 0;
            return;
        }
        
        String username = loginUsername.getText();
        String password = loginPassword.getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            loginMessageLabel.setTextFill(Color.RED);
            loginMessageLabel.setText("Kullanıcı adı ve şifre alanları boş bırakılamaz!");
            return;
        }
        
        // Attempt to authenticate the user
        User attempt_user = dbManager.authenticateUser(username, password);

        if (attempt_user != null)
        {
            loginMessageLabel.setTextFill(Color.GREEN);
            loginMessageLabel.setText("Giriş başarılı! Hoş geldiniz, " + attempt_user.getFullName());

            User user = new User();
            user.login(attempt_user.getId(), attempt_user.getEmail(), attempt_user.getEmail(), attempt_user.getUsername());
        }
        else
        {
            loginMessageLabel.setTextFill(Color.RED);
            loginMessageLabel.setText("Geçersiz kullanıcı adı veya şifre!");
        }
    }
    
    @FXML
    public void handleRegistration()
    {
        String fullName = registerFullName.getText();
        String email = registerEmail.getText();
        String username = registerUsername.getText();
        String password = registerPassword.getText();
        
        // Validate input
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty())
        {
            registerMessageLabel.setTextFill(Color.RED);
            registerMessageLabel.setText("Lütfen tüm alanları doldurun!");
            return;
        }
        
        // Basic email validation
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))
        {
            registerMessageLabel.setTextFill(Color.RED);
            registerMessageLabel.setText("Geçerli bir e-posta adresi girin!");
            return;
        }
        
        // Password length validation
        if (password.length() < 6)
        {
            registerMessageLabel.setTextFill(Color.RED);
            registerMessageLabel.setText("Şifreniz en az 6 karakter uzunluğunda olmalıdır!");
            return;
        }
        
        // Check if user name already exists
        if (dbManager.usernameExists(username)) {
            registerMessageLabel.setTextFill(Color.RED);
            registerMessageLabel.setText("Bu kullanıcı adı zaten kullanılıyor!");
            return;
        }
        
        // Create a new user
        User newUser = new User(0, fullName, email, username);
        
        // Attempt to register the user
        boolean registrationSuccess = dbManager.registerUser(newUser,  password);
        if (registrationSuccess) {
            registerMessageLabel.setTextFill(Color.GREEN);
            registerMessageLabel.setText("Kayıt başarılı! Şimdi giriş yapabilirsiniz.");
            
            // Clear the registration form
            registerFullName.clear();
            registerEmail.clear();
            registerUsername.clear();
            registerPassword.clear();
            
            // Switch to log in tab
            authTabPane.getSelectionModel().select(0);
        } else {
            registerMessageLabel.setTextFill(Color.RED);
            registerMessageLabel.setText("Kayıt işlemi başarısız oldu! Lütfen tekrar deneyin.");
        }
    }
} 