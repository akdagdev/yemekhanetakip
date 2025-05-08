package yemekhanetakip;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import yemekhanetakip.db.DatabaseManager;
import yemekhanetakip.db.UserDBManager;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Scene;

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
    
    private int loginClickCount = 0;
    private MediaPlayer mysterySoundPlayer;
    
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
        loginClickCount++;
        
        if (loginClickCount >= 20) {
            showMysteryContent();
            
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
    
    private void showMysteryContent() {
        try {
            String soundPath = getClass().getResource("/sounds/mystery2.wav").toExternalForm();
            Media sound = new Media(soundPath);
            mysterySoundPlayer = new MediaPlayer(sound);
        
            javafx.util.Duration stopTime = javafx.util.Duration.seconds(30);
            mysterySoundPlayer.setStopTime(stopTime);
            
            mysterySoundPlayer.setOnEndOfMedia(() -> mysterySoundPlayer.dispose());
            mysterySoundPlayer.play();
            
            ImageView mysteryImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/Mystery2.jpeg")));
            mysteryImageView.setFitWidth(996);
            mysteryImageView.setFitHeight(816);
            mysteryImageView.setPreserveRatio(true);
            
            Stage mysteryStage = new Stage();
            StackPane root = new StackPane(mysteryImageView);
            Scene scene = new Scene(root, 996, 816);
            mysteryStage.setTitle("Mystery Image 2");
            mysteryStage.setScene(scene);
            mysteryStage.show();
        } catch (Exception e) {
            System.err.println("Error showing mystery image or playing sound: " + e.getMessage());
            e.printStackTrace();
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