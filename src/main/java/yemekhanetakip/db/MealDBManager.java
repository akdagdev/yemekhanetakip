package yemekhanetakip.db;

import java.sql.*;
import java.util.logging.Level;

public class MealDBManager extends DatabaseManager {

    // Singleton design pattern
    private static MealDBManager instance;
    public static MealDBManager getInstance()
    {
        if (instance == null)
        {
            instance = new MealDBManager();
        }
        return instance;
    }

    // We make constructor private to be sure that no one creates an instance directly
    private MealDBManager() { }

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
        final String SQL = "INSERT INTO meals (meal_name) VALUES (?)";

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

}
