package yemekhanetakip.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class FavoritesDBManager extends DatabaseManager {

    // Singleton design pattern
    private static FavoritesDBManager instance;
    public static FavoritesDBManager getInstance()
    {
        if (instance == null)
        {
            instance = new FavoritesDBManager();
        }
        return instance;
    }

    // We make constructor private to be sure that no one creates an instance directly
    private FavoritesDBManager() { }

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

    public List<FavoriteMeal> getFavoritesByUserId(int userId) {
        return getFavoritesByUserId(userId, "meal_name ASC");
    }

    public List<FavoriteMeal> getFavoritesByUserId(int userId, String orderBy) {
        List<FavoriteMeal> favorites = new ArrayList<>();

        try {
            Connection conn = getConnection();
            String query = "SELECT f.meal_id, m.meal_name " +
                    "FROM favorites f " +
                    "JOIN meals m ON f.meal_id = m.meal_id " +
                    "WHERE f.user_id = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                FavoriteMeal meal = new FavoriteMeal(
                        rs.getInt("meal_id"),
                        rs.getString("meal_name")
                );
                favorites.add(meal);
            }

            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return favorites;
    }

    // Helper class to represent a favorite meal
    public class FavoriteMeal {
        private int id;
        private String name;

        public FavoriteMeal(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
