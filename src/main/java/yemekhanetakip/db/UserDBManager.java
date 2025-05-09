package yemekhanetakip.db;

import yemekhanetakip.User;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;

public class UserDBManager extends DatabaseManager {

    // Singleton design pattern
    private static UserDBManager instance;
    public static UserDBManager getInstance()
    {
        if (instance == null)
        {
            instance = new UserDBManager();
        }
        return instance;
    }

    // We make constructor private to be sure that no one creates an instance directly
    private UserDBManager() { }

    public boolean usernameExists(String username) {
        return simpleExists("SELECT 1 FROM users WHERE user_name=?", username);
    }

    public boolean emailExists(String email) {
        return simpleExists("SELECT 1 FROM users WHERE user_mail=?", email);
    }

    public boolean registerUser(User user, String plainPassword) {
        int id = createUserInternal(user, plainPassword);
        if (id > 0) {
            user.setId(id);
            return true;
        }
        return false;
    }

    public User authenticateUser(String username, String plainPassword) {
        return authenticateInternal(username, plainPassword);
    }

    private int createUserInternal(User user, String plainPassword) {
        final String SQL = "INSERT INTO users (user_fullname,user_name,user_password,user_phone,user_mail) VALUES (?,?,?,?,?)";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)
        )

        {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, PasswordHasher.hashPassword(plainPassword));
            ps.setString(4, null); // phone optional, not used currently
            ps.setString(5, user.getEmail());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys())
            {
                if (keys.next())
                {
                    return keys.getInt(1);
                }
            }

        }
        catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "createUserInternal error", e);
        }

        return -1;
    }

    private User authenticateInternal(String username, String plainPassword)
    {
        final String SQL = "SELECT * FROM users WHERE user_name=?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)
        )

        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next() && PasswordHasher.checkPassword(plainPassword, rs.getString("user_password")))
                {
                    return toUser(rs);
                }
            }
        }
        catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "authenticateInternal error", e);
        }

        return null;
    }

    private User toUser(ResultSet rs) throws SQLException
    {
        return new User(
                rs.getInt("user_id"),
                rs.getString("user_fullname"),
                rs.getString("user_mail"),
                rs.getString("user_name")
        );
    }

}
