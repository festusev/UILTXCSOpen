package Outlet;
import Outlet.uil.UILEntry;
import com.google.gson.Gson;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Set;

public class User implements Comparable<User>{
    public String email;
    public String fname;
    public String lname;
    public String school;
    public BigInteger token;
    public String password;
    public short uid;
    public boolean teacher;

    protected static Gson gson = new Gson();
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
    public int updateUser(boolean insert) {
        // Make the actual update query
        try
        {
            // Establishing Connection
            Connection con = Conn.getConnection();
            if(con==null) return -1; // If an error occurred making the connection
            PreparedStatement stmt;
            if(insert)  {
                stmt = con.prepareStatement("INSERT INTO users(email, fname, lname, affiliation, token, cids, class) VALUES (?, ?, ?, ?, ?)");
            } else {
                stmt = con.prepareStatement("UPDATE users SET email=?, fname=?, lname=?, school=?, token=?, cids=?, class=? WHERE uid=?");
                stmt.setShort(6, uid);
            }
            String cidsString = "";
            String classString = "";
            if(teacher) {
                cidsString = gson.toJson(((Teacher) this).cids);
                classString = ((Teacher)this).classCode;
            } else {
                HashMap<Short, UILEntry> cids = ((Student) this).cids;
                cidsString = "{";
                Set<Short> temp = cids.keySet();
                for(short cid: temp) {
                    cidsString += cid+":"+cids.get(cid).tid+",";
                }
                if(temp.size() > 0) {   // If we added in an entry, remove the final comma
                    cidsString = cidsString.substring(0, cidsString.length()-1);
                }
                cidsString += "}";
                classString = ""+((Student)this).teacherId;
            }

            stmt.setString(1, email);
            stmt.setString(2, fname);
            stmt.setString(3, lname);
            stmt.setString(4, school);
            stmt.setString(5, token.toString(Character.MAX_RADIX));
            stmt.setString(6, cidsString);
            stmt.setString(7, classString);
            stmt.setShort(8, uid);
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
        password = hashedPassword;

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
        // Extract the salt from the storedFull Hash
        String storedSalt = this.password.substring(0, this.password.indexOf("."));
        String storedHashed = this.password.substring(this.password.indexOf(".")+1);
        String givenHashed = null;  // The hash of the password supplied by the user
        try {
            givenHashed = Conn.hashPassword(password, Base64.getDecoder().decode(storedSalt));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        if(givenHashed.equals(storedHashed)) return true;
        return false;
    }
}
