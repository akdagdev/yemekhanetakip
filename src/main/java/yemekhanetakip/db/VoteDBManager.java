package yemekhanetakip.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class VoteDBManager extends DatabaseManager {
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

}
