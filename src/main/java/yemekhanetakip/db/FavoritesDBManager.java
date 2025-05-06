package yemekhanetakip.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class FavoritesDBManager extends DatabaseManager {

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
