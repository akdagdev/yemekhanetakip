package yemekhanetakip;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager
{
    private static final String URL = "jdbc:mysql://localhost:3306/yemekhane";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "rv9yl2qc";


    public static Connection connect() throws SQLException
    {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void insertMealIfNotExists(String mealName)
    {
        String checkSQL = "SELECT meal_id FROM meals WHERE meal_name = ?";
        String insertSQL = "INSERT INTO meals (meal_name) VALUES (?)";

        try (
                Connection connect = connect();

                PreparedStatement checkStatement = connect.prepareStatement(checkSQL);
                PreparedStatement insertStatement = connect.prepareStatement(insertSQL)
            )

        {
                checkStatement.setString(1, mealName);
                ResultSet Results = checkStatement.executeQuery();

                if(!Results.next())
                {
                    insertStatement.setString(1, mealName);
                    insertStatement.executeUpdate();
                    System.out.println("Yeni yemek eklendi: " + mealName);
                }

        }
        catch (SQLException e)
        {
            //Exception handling and outputting exception code
            System.out.println("Yeni yemek dataya eklenemdi\nHata: " + e.getMessage());
        }

    }


    public static String insertUserIfNotExists(String username, String password,String phone, String mail)
    {
        String checkSQL = "SELECT user_id FROM users WHERE user_name = ?";
        String insertSQL = "INSERT INTO users (user_name, user_password, user_phone, user_mail) VALUES (?, ?, ?, ?)";

        try (
                Connection connnect = connect();

                PreparedStatement checkStatement = connnect.prepareStatement(checkSQL);
                PreparedStatement insertStatement = connnect.prepareStatement(insertSQL)
            )

        {
            checkStatement.setString(1, username);
            ResultSet result = checkStatement.executeQuery();

            if (!result.next())
            {
                //hash the password for security
                String hashedPassword = PasswordUtil.hashPassword(password);

                insertStatement.setString(1, username);
                insertStatement.setString(2, hashedPassword);
                insertStatement.setString(3, mail);
                insertStatement.setString(4, phone);

                insertStatement.executeUpdate();
                return "Yeni kullanıcı eklendi";
            }
            else
            {
                return "Kullanıcı zaten mevcut: " + username;
            }


        }
        catch (SQLException e)
        {
            return "Kullanıcı eklenemedi\nHata: " + e.getMessage();
        }
    }


    public static boolean verifyLogin(String username, String plainPassword)
    {
        //getting hashed password from database
        String checkSQL = "SELECT user_password FROM users WHERE user_name = ?";

        try (
                Connection connect = connect();

                PreparedStatement statement = connect.prepareStatement(checkSQL)
             )

        {
            statement.setString(1, username);
            ResultSet results =  statement.executeQuery();

            if(results.next())
            {
                String hashedPassword = results.getString("user_password");
                //bcrypt check
                return PasswordUtil.checkPassword(plainPassword, hashedPassword);
            }
            else
            {
                //security for time attacks with fake hash comparison
                String fakeHash = "$2a$10$KbQiBLUYOEM3TYxL2rLrTeUq1uDezqZrxzLKRi1O70UPIN8UL3ROa";
                PasswordUtil.checkPassword(plainPassword, fakeHash);
                return false;
            }

        }
        catch (SQLException e)
        {
            return false;
        }

    }


    public static boolean addOrUpdateVote(int userId, int mealId, int rate, String comment)
    {
        if (rate < 1 || rate > 5)
        {
            return false;
        }

        String checkSQL = "SELECT * FROM meal_votes WHERE user_id = ? AND meal_id = ?";
        String insertSQL = "INSERT INTO meal_votes (user_id, meal_id, user_rate, user_comment) VALUES (?, ?, ?, ?)";
        String updateSQL = "UPDATE meal_votes SET user_rate = ?, user_comment = ? WHERE user_id = ? AND meal_id = ?";

        try (
                Connection conn = connect();
                PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                PreparedStatement updateStmt = conn.prepareStatement(updateSQL)
            )

        {
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, mealId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next())
            {
                updateStmt.setInt(1, rate);
                updateStmt.setString(2, comment);
                updateStmt.setInt(3, userId);
                updateStmt.setInt(4, mealId);
                updateStmt.executeUpdate();
            }
            else
            {
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, mealId);
                insertStmt.setInt(3, rate);
                insertStmt.setString(4, comment);
                insertStmt.executeUpdate();
            }

            return true;

        }
        catch (SQLException e)
        {
            return false;
        }
    }


}
