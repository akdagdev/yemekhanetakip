package yemekhanetakip;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/yemekhane";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "rv9yl2qc";

    // Constructor: assume schema already exists
    public DatabaseManager()
    {
        // No DDL execution; tables must be created externally

    }

    // Connect to database
    private Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    /*================== USER API ==================*/

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
        Optional<User> opt = authenticateInternal(username, plainPassword);
        return opt.orElse(null);
    }

    private int createUserInternal(User user, String plainPassword) {
        final String SQL = "INSERT INTO users (user_fullname,user_name,user_password,user_phone,user_mail) VALUES (?,?,?,?,?)";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)
            )

        {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, PasswordUtil.hashPassword(plainPassword));
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

    private Optional<User> authenticateInternal(String username, String plainPassword)
    {
        final String SQL = "SELECT * FROM users WHERE user_name=?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)
            )

        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next() && PasswordUtil.checkPassword(plainPassword, rs.getString("user_password")))
                {
                    return Optional.of(toUser(rs));
                }
            }
        }
        catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "authenticateInternal error", e);
        }

        return Optional.empty();
    }

    private User toUser(ResultSet rs) throws SQLException
    {
        return new User(
                rs.getInt("user_id"),
                rs.getString("user_fullname"),
                rs.getString("user_mail"),
                rs.getString("user_name"),
                null
        );
    }

    /*================== MEAL API ==================*/

    public void insertMealIfNotExists(String mealName)
    {
        getOrCreateMeal(mealName);
    }

    public int getOrCreateMeal(String mealName)
    {
        final String FIND = "SELECT meal_id FROM meals WHERE meal_name=?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(FIND)
            )

        {
            ps.setString(1, mealName);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return rs.getInt("meal_id");
                }
            }
        }
        catch (SQLException e)
        {
            LOG.log(Level.WARNING, "getOrCreateMeal error", e);
        }

        return addMealInternal(mealName);
    }

    private int addMealInternal(String mealName)
    {
        final String SQL = "INSERT INTO meals (meal_name,meal_av_rate) VALUES (?,0)";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)
            )

        {
            ps.setString(1, mealName);
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
            LOG.log(Level.WARNING, "addMealInternal error", e);
        }

        return -1;
    }

    /*================== VOTE API ==================*/

    public boolean addOrUpdateVote(int userId, int mealId, int rating, String comment)
    {
        return upsertVoteInternal(userId, mealId, rating, comment);
    }

    private boolean upsertVoteInternal(int userId, int mealId, int rating, String comment)
    {
        final String CHECK = "SELECT 1 FROM meal_votes WHERE user_id=? AND meal_id=?";

        try (Connection c = getConnection();
             PreparedStatement psCheck = c.prepareStatement(CHECK)
            )

        {

            psCheck.setInt(1, userId);
            psCheck.setInt(2, mealId);
            boolean exists;

            try (ResultSet rs = psCheck.executeQuery())
            {
                exists = rs.next();
            }
            String sql;

            if (exists)
            {
                sql = "UPDATE meal_votes SET user_rate=?, user_comment=?, comment_date=CURDATE() WHERE user_id=? AND meal_id=?";
            }
            else
            {
                sql = "INSERT INTO meal_votes (user_id,meal_id,user_rate,user_comment,comment_date) VALUES (?,?,?,?,CURDATE())";
            }

            try (PreparedStatement ps = c.prepareStatement(sql))
            {
                int idx = 1;
                if (exists)
                {
                    ps.setInt(idx++, rating);
                    ps.setString(idx++, comment);
                    ps.setInt(idx++, userId);
                    ps.setInt(idx++, mealId);
                }
                else
                {
                    ps.setInt(idx++, userId);
                    ps.setInt(idx++, mealId);
                    ps.setInt(idx++, rating);
                    ps.setString(idx, comment);
                }

                return ps.executeUpdate() > 0;
            }
        }
        catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "upsertVoteInternal error", e);
            return false;
        }
    }

    /*================== FAVORITE API ==================*/

    public boolean addToFavorites(int userId, int mealId)
    {
        final String SQL = "INSERT IGNORE INTO favorites (user_id,meal_id) VALUES (?,?)";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)
            )

        {
            ps.setInt(1, userId);
            ps.setInt(2, mealId);
            return ps.executeUpdate() > 0;

        }
        catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "addToFavorites error", e);
            return false;
        }
    }

    public boolean removeFavorite(int userId, int mealId)
    {
        final String SQL = "DELETE FROM favorites WHERE user_id=? AND meal_id=?";

        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(SQL))
        {
            ps.setInt(1, userId);
            ps.setInt(2, mealId);
            return ps.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "removeFavorite error", e);
            return false;
        }
    }

    public List<FavoriteMeal> getFavoritesByUser(int userId, String orderBy)
    {
        List<FavoriteMeal> result = new ArrayList<>();

        final String SQL = "SELECT f.meal_id,m.meal_name,f.date_added FROM favorites f JOIN meals m ON f.meal_id=m.meal_id WHERE f.user_id=? ORDER BY " + orderBy;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)
            )

        {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    result.add(new FavoriteMeal(
                            rs.getInt("meal_id"),
                            rs.getString("meal_name"),
                            rs.getDate("date_added").toString()
                    ));
                }
            }
        }
        catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "getFavoritesByUser error", e);
        }
        return result;
    }

    /*================== HELPERS ==================*/

    private boolean simpleExists(String sql, Object... args)
    {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)
            )

        {
            for (int i = 0; i < args.length; i++)
            {
                ps.setObject(i+1, args[i]);
            }

            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next();
            }
        }
        catch (SQLException e)
        {
            LOG.log(Level.SEVERE, "simpleExists error", e);
            return false;
        }
    }

    // POJO for favorite meals
    public static class FavoriteMeal
    {
        private final int mealId;
        private final String mealName;
        private final String dateAdded;

        public FavoriteMeal(int mealId, String mealName, String dateAdded)
        {
            this.mealId = mealId;
            this.mealName = mealName;
            this.dateAdded = dateAdded;
        }

        public int getMealId() { return mealId; }
        public String getMealName() { return mealName; }
        public String getDateAdded() { return dateAdded; }

    }
}
