package yemekhanetakip.db;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher
{
    //hashes the password
    public static String hashPassword(String plainPassword)
    {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    //checks the hashed password
    public static boolean checkPassword(String plainPassword, String hashedPassword)
    {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}