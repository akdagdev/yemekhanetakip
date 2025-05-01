package yemekhanetakip;

import java.sql.*;

public class DatabaseManager {
    // Database connection properties
    private static final String DB_URL = "jdbc:mysql://localhost:3306/yemekhanetakip";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // SQL queries
    private static final String CREATE_USERS_TABLE = 
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "full_name VARCHAR(100) NOT NULL," +
            "email VARCHAR(100) NOT NULL UNIQUE," +
            "username VARCHAR(50) NOT NULL UNIQUE," +
            "password VARCHAR(100) NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    
    private static final String CREATE_FAVORITES_TABLE = 
            "CREATE TABLE IF NOT EXISTS favorites (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT NOT NULL," +
            "meal_name VARCHAR(200) NOT NULL," +
            "date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";
    
    private static final String INSERT_USER = 
            "INSERT INTO users (full_name, email, username, password) VALUES (?, ?, ?, ?)";
    
    private static final String CHECK_USERNAME = 
            "SELECT COUNT(*) FROM users WHERE username = ?";
    
    private static final String AUTHENTICATE_USER = 
            "SELECT * FROM users WHERE username = ? AND password = ?";
    
    // Constructor
    public DatabaseManager() {
        initializeDatabase();
    }
    
    // Initialize database and create tables if needed
    private void initializeDatabase() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create database if it doesn't exist
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/", DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement()) {
                
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS yemekhanetakip");
                System.out.println("Database created or already exists.");
                
                // Create users table
                try (Connection dbConn = getConnection();
                     Statement dbStmt = dbConn.createStatement()) {
                    dbStmt.executeUpdate(CREATE_USERS_TABLE);
                    System.out.println("Users table created or already exists.");
                    
                    // Create favorites table
                    dbStmt.executeUpdate(CREATE_FAVORITES_TABLE);
                    System.out.println("Favorites table created or already exists.");
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Get database connection
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    // Register a new user
    public boolean registerUser(User user) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getUsername());
            pstmt.setString(4, user.getPassword()); // In a real app, hash the password
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Add a meal to favorites
    public boolean addToFavorites(int userId, String mealName) {
        String query = "INSERT INTO favorites (user_id, meal_name) VALUES (?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, mealName);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding to favorites: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Remove a meal from favorites
    public boolean removeFromFavorites(int favoriteId) {
        String query = "DELETE FROM favorites WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, favoriteId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error removing from favorites: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Check if username already exists
    public boolean usernameExists(String username) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(CHECK_USERNAME)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Authenticate user
    public User authenticateUser(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(AUTHENTICATE_USER)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String fullName = rs.getString("full_name");
                    String email = rs.getString("email");
                    
                    return new User(id, fullName, email, username, "");
                }
            }
            
            return null;
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
} 