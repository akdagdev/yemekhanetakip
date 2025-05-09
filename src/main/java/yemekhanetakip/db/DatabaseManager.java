package yemekhanetakip.db;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DatabaseManager  {

    public static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/yemekhane";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "rv9yl2qc";

    // Connect to database
    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public boolean simpleExists(String sql, Object... args)
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
}
