package yemekhanetakip;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import yemekhanetakip.db.DatabaseManager;
import yemekhanetakip.db.UserDBManager;

public class ProfileController {
    
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
    
    @FXML
    public void initialize() {
        // Initialize the database manager
        dbManager = new UserDBManager();
        
        // Clear any previous message
        loginMessageLabel.setText("");
        registerMessageLabel.setText("");
    }
    
    @FXML
    public void handleLogin() {
        String username = loginUsername.getText();
        String password = loginPassword.getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            loginMessageLabel.setTextFill(Color.RED);
            loginMessageLabel.setText("Kullanıcı adı ve şifre alanları boş bırakılamaz!");
            return;
        }
        
        // Attempt to authenticate the user
        User user = dbManager.authenticateUser(username, password);

        if (user != null)
        {
            loginMessageLabel.setTextFill(Color.GREEN);
            loginMessageLabel.setText("Giriş başarılı! Hoş geldiniz, " + user.getFullName());
            
            // TODO: Update UI to show user is logged in
            // TODO: Store user information in session
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
        User newUser = new User(0, fullName, email, username, password);
        
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