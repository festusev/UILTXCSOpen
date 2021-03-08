package Outlet;

import Outlet.uil.Competition;
import Outlet.uil.UIL;
import Outlet.uil.UILEntry;
import com.google.gson.Gson;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static Outlet.Class.*;
import static java.lang.Class.forName;

/***
 * A class containing all of the necessary functions for communicating with the server.
 * Created by Evan Ellis.
 */
public class Conn {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "comp.curhndcalh3n.us-east-2.rds.amazonaws.com";
    private static final int DB_PORT = 3306;
    private static final String USER = "admin";
    private static final String PASS = "1pcdEy31lxTSp6x$";	// TODO Possibly in the future have an admin type this in.
    // private static final String DB_NAME = "uil";
    private static final String DB_NAME = "comptest";

    private static Gson gson = new Gson();

    private static final int VTOKEN_SIZE = 50;
    private static final int VCODE_SIZE = 6;
    public static void setHTMLHeaders(HttpServletResponse response) {
        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    }
    public static BigInteger getTokenByString(String s){
        return new BigInteger(s, Character.MAX_RADIX);
    }
    public static BigInteger getToken(HttpServletRequest request) {
        Cookie[] cookieList = request.getCookies();
        if(cookieList == null) return null;
        for(Cookie c: cookieList) {
            if(c.getName().equals("token")){    // If the token cookie exists
                return getTokenByString(c.getValue());
            }
        }
        return null;
    }

    /** Remove the `token` cookie from the request and from the user object **/
    public static void delToken(HttpServletRequest request, HttpServletResponse response, User u) {
        Cookie[] cookieList = request.getCookies();
        for(Cookie c: cookieList) {
            if(c.getName().equals("token")){    // If the token cookie exists
                c.setValue("");
                c.setPath("/");
                c.setMaxAge(0);
                response.addCookie(c);
            }
        }

        u.token = null;
    }

    public static boolean isLoggedIn(HttpServletRequest request){
        User u =  UserMap.getUserByRequest(request);
        if(u != null && u.token != null) return u.token.equals(Conn.getToken(request));
        else return false;
    }
    public static boolean isLoggedIn(BigInteger token){
        User u = UserMap.getUserByToken(token);
        if(u == null || u.token == null) return false;
        return u.token.equals(token);
    }
    public static BigInteger  generateToken() {
        return new BigInteger(255, new Random());  // How the user will remain logged in;
    }
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom(); // TODO: seed this somehow
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }
    public static String hashPassword(String pass, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);

        byte[] hash = md.digest(pass.getBytes());

        // Convert byte array into signum representation
        // BigInteger class is used, to convert the resultant byte array into its signum representation
        BigInteger inputDigestBigInt = new BigInteger(1, hash);

        // Convert the input digest into hex value
        String hashtext = inputDigestBigInt.toString(16);

        return hashtext;
    }
    public static String getHashedFull(String password) throws NoSuchAlgorithmException {
        byte[] salt = generateSalt();
        return Base64.getEncoder().encodeToString(salt) + "." + hashPassword(password, salt);
    }

    public static Connection getConnection(){
        Connection conn;
        for(int i=0;i<100;i++) {
            try {
                forName(JDBC_DRIVER);    // Load up the Driver's class
                conn = DriverManager.getConnection("jdbc:mysql://" + DB_URL + ":" + DB_PORT + "/" + DB_NAME +
                        "?autoReconnect=true&useSSL=false", USER, PASS);
                if(conn!=null) return conn;
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }
    public static BigInteger finishRegistration(String email, String password, String fname, String lname, String school, boolean isTeacher, boolean temp) throws SQLException {
        BigInteger token = generateToken();
        short uid = -1;
        String classString;
        // Make the actual update query
        try
        {
            // Establishing Connection
            Connection conn = getConnection();
            if(conn==null) return BigInteger.valueOf(-1); // If an error occurred making the connection
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users(email, password, fname, lname, school, token, teacher, class, temp) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, fname);
            stmt.setString(4, lname);
            stmt.setString(5, school);
            stmt.setString(6, token.toString(Character.MAX_RADIX));
            stmt.setBoolean(7, isTeacher);
            if(isTeacher) { // Teachers' 'cids' string is a json list of the cids of the competitions they created
                /**
                 * Generate the random 6 character code
                 */
                int leftLimit = 48; // numeral '0'
                int rightLimit = 90; // letter 'Z'
                Random random = new Random();

                do {
                    classString = random.ints(leftLimit, rightLimit + 1)
                            .filter(i -> (i <= 57 || i >= 65) && (i <= 90))
                            .limit(6)
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString();
                } while(TeacherMap.getByClassCode(classString) != null);
            } else {    // Students' 'cids' string is a json dictionary mapping the cid of each competition they've signed up for to their tid in that competition
                classString = "-1";
            }

            stmt.setString(8, classString);    // This will be used to join their classes
            stmt.setBoolean(9, temp);

            int success = stmt.executeUpdate();
            if(success<=0) return BigInteger.valueOf(-1);   // If an error occurs

            // Get uid
            stmt = conn.prepareStatement("SELECT uid FROM users WHERE email=?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) uid = rs.getShort("uid");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            if(e.getMessage().contains("for key 'email'")) return BigInteger.valueOf(-2); // If the error message is about the user having the same email as another
            else if(e.getMessage().contains("for key 'uname'")) return BigInteger.valueOf(-3);  // If the error message is about the user having the same uname as another
            else return BigInteger.valueOf(-1);  // If an error occurred making the connection that is not one of the above
        }
        UserMap.loadUser(email, fname, lname, school,  token, uid, isTeacher, classString, password, temp);
        return token;
    }

    // Flushes the verification table. Called every 15 minutes by VerificationFlusher
    public static void flushVerificationTable(){
        Connection conn = getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM verification WHERE expires<?");
            stmt.setLong(1, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Transfers the user's information into the user database, finishing registration.
     * Takes in the token that is sent to the user's email, and returns a randomly-generated
     * logged-in token that will be stored as a cookie
     * @param format
     * @return bToken
     */
    private static BigInteger verifyHelper(String format, String value){
        // First, retrieve the information from the `verification` database row that matches vToken
        Connection conn = getConnection();
        if(conn==null) return BigInteger.valueOf(-1);   // If an error occurred making the connection
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM verification WHERE " + format);
            stmt.setString(1, value);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) { // A row matches this vToken
                if(rs.getLong("expires")<System.currentTimeMillis()) {  // If the token has expired. Should be already deleted, but stuff happens
                    // Remove this row from the database
                    stmt = conn.prepareStatement("DELETE FROM verification WHERE " + format);
                    stmt.setString(1, value);
                    stmt.executeUpdate();
                    return BigInteger.valueOf(-2);  // The token has expired
                }

                String email = rs.getString("email");
                String password = rs.getString("password");
                String fname = rs.getString("fname");
                String lname = rs.getString("lname");
                String school = rs.getString("school");
                boolean isTeacher = rs.getBoolean("teacher");

                // Finally, purge the entry from the table
                stmt = conn.prepareStatement("DELETE FROM verification WHERE email = ?");
                stmt.setString(1, email);
                stmt.executeUpdate();

                return finishRegistration(email, password, fname, lname, school, isTeacher, false);    // Finally, return the browser token
            } else {    // no vToken matches this one in the database
                return BigInteger.valueOf(-2);  // The token has expired. Should be already deleted, but stuff happens
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return BigInteger.valueOf(-1);
        }
    }
    public static BigInteger verifyToken(String vToken){
        return verifyHelper("vToken = ?", vToken);
    }
    public static BigInteger verifyCode(String code){
        return verifyHelper("code = ?", code);
    }

    public static boolean verifyResetCode(String code, String email) {
        // First, retrieve the information from the `verification` database row that matches vToken
        Connection conn = getConnection();
        if(conn==null) return false;   // If an error occurred making the connection
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM reset_password WHERE code=? AND email=?");
            stmt.setString(1, code);
            stmt.setString(2,email);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) { // A row matches this vToken
                if(rs.getLong("expires")<System.currentTimeMillis()) {  // If the token has expired. Should be already deleted, but stuff happens
                    // Remove this row from the database
                    stmt = conn.prepareStatement("DELETE FROM reset_password WHERE code=? AND email=?");
                    stmt.setString(1, code);
                    stmt.setString(2,email);
                    stmt.executeUpdate();
                    return false;  // The token has expired
                }

                return true;    // Finally, return the browser token
            } else {    // no vToken matches this one in the database
                return false;  // The token has expired. Should be already deleted, but stuff happens
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /***
     * Sends a resetPassword email to the user and stores their email along with a vToken, code, and expiration date
     * in the 'reset_password' database.
     * @param email
     * @return
     */
    public static int ResetPassword(String email) {
        /**
         * Generate the random 8 character code
         */
        int leftLimit = 48; // numeral '0'
        int rightLimit = 90; // letter 'Z'
        Random random = new Random();

        String code = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90))
                .limit(VCODE_SIZE)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();


        // Make the actual update query
        try
        {
            // First, check if the email or username is already take (or both)
            Connection conn = getConnection();
            if(conn==null) {
                System.out.println("--ERROR MAKING CONNECTION--");
                return -1; // If an error occurred making the connection
            }
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            boolean emailTaken = false;
            while(rs.next()) {  // Loop through matching rows
                if(rs.getString("email").equals(email)) emailTaken = true;
            }
            if(!emailTaken) return -2;

            // Purge any entries in the 'reset_pass' database which match the email
            stmt = conn.prepareStatement("DELETE FROM reset_password WHERE email = ?");
            stmt.setString(1, email);   // TODO: Right now, if someone is in the process of registering with this email or uname, they will be interrupted by another person attempting to register with the same email or uname.
            stmt.executeUpdate();

            if(conn==null) return -1; // If an error occurred making the connection
            stmt = conn.prepareStatement("INSERT INTO reset_password(email, expires, vtoken, code) VALUES (?, ?, ?, ?)");
            stmt.setString(1, email);
            stmt.setLong(2, System.currentTimeMillis() + 15*60*1000);   // Expires in 15 minutes
            stmt.setString(3, "");
            stmt.setString(4, code);

            int success = stmt.executeUpdate();
            if(success<0) return -1;   // If some error occurred

            // And now we send the email asynchronously
            new Thread(() -> {
                SendMail.sendVerification(email, code);
            }).start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return -1;  // If an error occurred making the connection that is not one of the above
        }
        return 0;
    }

    /**
     * First, checks if the email or username is already taken. Then:
     * Puts the user's information into the verification database, along with a code and a token.
     * If a server error occurs, it returns -1. If the email is already taken, it returns -2.
     * If the username is already taken, it returns -3.
     * @param email
     * @param password
     * @return vtoken, -1, -2, or -3
     * @throws NoSuchAlgorithmException
     */
    public static int Register(String fname, String lname, String affiliation, String email, String password, boolean isTeacher) throws NoSuchAlgorithmException {
        /**
         * Generate the random 8 character code
         */
        int leftLimit = 48; // numeral '0'
        int rightLimit = 90; // letter 'Z'
        Random random = new Random();

        String code = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90))
                .limit(VCODE_SIZE)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();


        String passHashFull = getHashedFull(password);
        // Make the actual update query
        try
        {
            // First, check if the email or username is already take (or both)
            Connection conn = getConnection();
            if(conn==null) {
                System.out.println("--ERROR MAKING CONNECTION--");
                return -1; // If an error occurred making the connection
            }

            if(UserMap.getUserByEmail(email)!=null) return -2;

            // Purge any entries in the verification database which match the email
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM verification WHERE email = ?");
            stmt.setString(1, email);   // TODO: Right now, if someone is in the process of registering with this email or uname, they will be interrupted by another person attempting to register with the same email or uname.
            stmt.executeUpdate();

            stmt = conn.prepareStatement("INSERT INTO verification(email, password, fname, lname, school, teacher, expires, vtoken, code) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, email);
            stmt.setString(2, passHashFull);
            stmt.setString(3, fname);
            stmt.setString(4, lname);
            stmt.setString(5, affiliation);
            stmt.setBoolean(6,isTeacher);
            stmt.setLong(7, System.currentTimeMillis() + 15*60*1000);   // Expires in 15 minutes
            stmt.setString(8, "");
            stmt.setString(9, code);

            int success = stmt.executeUpdate();
            if(success<0) return -1;   // If some error occurred

            // And now we send the email asynchronously
            new Thread(() -> {
                SendMail.sendVerification(email, code);
            }).start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return -1;  // If an error occurred making the connection that is not one of the above
        }
        return 0;
    }

    public static int resendVerification(String email) {
        Connection conn = getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("SELECT * FROM verification WHERE email = ?");
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                String code = rs.getString("code");
                String vtoken = rs.getString("vtoken");

                // And now we send the email asynchronously
                new Thread(() -> {
                    SendMail.sendVerification(email, code);
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    /**
     * Resends the reset password verification email
     * @param email
     * @return
     */
    public static int resendResetVerification(String email) {
        Connection conn = getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("SELECT * FROM reset_password WHERE email = ?");
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                String code = rs.getString("code");

                // And now we send the email asynchronously
                new Thread(() -> {
                    SendMail.sendVerification(email, code);
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    /**
     * Returns -1 if a server error occurred, -2 if the email doesn't match a user, and -3 if the password is incorrect
     * @param email
     * @param password
     * @return token, -1, -2
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     */
    public static BigInteger Login(String email, String password) throws NoSuchAlgorithmException, SQLException {
        BigInteger token = generateToken();

        User user = UserMap.getUserByEmail(email);
        if(user == null) return BigInteger.valueOf(-2);

        if(!user.verifyPassword(password)) { // If the password is incorrect, return -3
            return BigInteger.valueOf(-3);
        }

        // Update the token in the db entry for this user
        Connection conn = getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET token=? WHERE email=?");
            stmt.setString(1, token.toString(Character.MAX_RADIX));
            stmt.setString(2, email);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return BigInteger.valueOf(-1);
        }
        UserMap.delUser(user);
        user.token = token;
        UserMap.addUser(user);

        return token;
    }

    public static int logout(BigInteger token) {
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET token=? WHERE token=?");
            stmt.setNull(1, Types.CHAR);
            stmt.setString(2, token.toString(Character.MAX_RADIX));
            stmt.executeUpdate();
            return 0;
        } catch(SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}