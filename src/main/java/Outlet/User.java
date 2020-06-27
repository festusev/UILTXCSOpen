package Outlet;
import com.google.gson.Gson;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class User implements Comparable<User>{
    public String email;
    public String uname;
    public Team team;
    public BigInteger token;
    public short uid;
    public short tid;

    private static Gson gson = new Gson();
    //private static final //Logger //LOGGER = LogManager.get//Logger(User.class);
    @Override
    public int compareTo(User user) {
        if(token == null || user.token == null) return user.uid - uid;  // If we are comparing by uid
        return token.compareTo(user.token);
    }

    /**
     * Make the database entry referred to by the uid is equal to this user class.
     * WILL NOT UPDATE the uid.
     * @param insert, if true insert, if false update
     * @return The status code.
     */
    public int updateUser(boolean insert) throws NoSuchAlgorithmException, SQLException {
        // Make the actual update query
        try
        {
            // Establishing Connection
            Connection con = Conn.getConnection();
            if(con==null) return -1; // If an error occurred making the connection
            String query;
            PreparedStatement stmt;
            if(insert)  {
                stmt = con.prepareStatement("INSERT INTO users(email, uname, token, tid) VALUES (?, ?, ?, ?)");
                stmt.setString(3, token.toString(Character.MAX_RADIX));
                stmt.setShort(4, tid);
            } else {
                stmt = con.prepareStatement("UPDATE users SET email=?, uname=?, token=?, tid=? WHERE uid=?");
                stmt.setString(3, token.toString(Character.MAX_RADIX));
                stmt.setShort(4, tid);
                stmt.setShort(5, uid);
            }
            stmt.setString(1, email);
            stmt.setString(2, uname);
            //LOGGER.info(stmt.toString());

            System.out.println(stmt.toString());
            stmt.executeUpdate();
            return 0;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            if(e.getMessage().contains("for key 'email'")) return -2; // If the error message is about the user having the same email as another
            else if(e.getMessage().contains("for key 'uname'")) return -3;  // If the error message is about the user having the same uname as another
            else return -1;  // If an error occurred making the connection that is not one of the above
        }
    }
    public int changePassword(String hashedPassword) {
        Connection conn = Conn.getConnection();
        if(conn==null) return -1;   // Error making connection;
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password = ? WHERE uid=?");
            stmt.setString(1, hashedPassword);
            stmt.setShort(2, uid);
            //LOGGER.info(stmt.toString());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public boolean verifyPassword(String password) {
        Connection conn = Conn.getConnection();
        if(conn==null) return false;

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE uid=?");
            stmt.setShort(1, uid);
            //LOGGER.info(stmt.toString());
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                String stored = rs.getString("password");
                // Extract the salt from the storedFull Hash
                String storedSalt = stored.substring(0, stored.indexOf("."));
                String storedHashed = stored.substring(stored.indexOf(".")+1);
                String givenHashed = Conn.hashPassword(password, Base64.getDecoder().decode(storedSalt));  // The hash of the password supplied by the user
                if(givenHashed.equals(storedHashed)) return true;
                return false;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }
}
