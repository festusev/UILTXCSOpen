package Outlet;

import Outlet.uil.UILEntry;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final String DB_NAME = "uil";

    private static Gson gson = new Gson();

    private static ArrayList<User> users = new ArrayList<>();
    private static ArrayList<Team> teams = new ArrayList<>();

    //private static final //Logger //LOGGER = LogManager.get//Logger(Conn.class);

    private static final int VTOKEN_SIZE = 50;
    private static final int VCODE_SIZE = 6;
    public static void setHTMLHeaders(HttpServletResponse response) {
        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    }
    public static BigInteger getToken(HttpServletRequest request) {
        Cookie[] cookieList = request.getCookies();
        if(cookieList == null) return null;
        for(Cookie c: cookieList) {
            if(c.getName().equals("token")){    // If the token cookie exists
                return new BigInteger(c.getValue(), Character.MAX_RADIX);
            }
        }
        return null;
    }

    /** Remove the `token` cookie from the request **/
    public static void delToken(HttpServletRequest request, HttpServletResponse response, BigInteger token) {
        Cookie[] cookieList = request.getCookies();
        for(Cookie c: cookieList) {
            if(c.getName().equals("token")){    // If the token cookie exists
                c.setValue("");
                c.setPath("/");
                c.setMaxAge(0);
                response.addCookie(c);
            }
        }

        User temp = new User();
        temp.token = token;
        int index = Collections.binarySearch(users, temp);
        if(index>=0) {
            users.remove(index);
        }
    }

    /**
     * This is a dangerous method, be sure to never let it get in the hands of someone.
     * @return
     */
    public static User getUserByEmail(String email) {
        if(email == null || email.isEmpty()) return null;
        for(User u: users) {
            if(u.email.equals(email)) return u;
        }
        BigInteger token = generateToken();

        // First, we query the database for the password entry matching the email
        Connection con;
        PreparedStatement stmt;
        ResultSet rs;
        String storedFullHash;
        String uname;
        long start;
        String questions;
        short points;
        short uid = -1;
        short tid = -1;
        try {
            con = getConnection();
            if (con == null) return null; // If an error occurred making the connection
            stmt = con.prepareStatement("SELECT password, uname, uid, tid FROM users WHERE email=?");
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            if(rs.next()) { // A row matches this email
                storedFullHash = rs.getString("password");
                uname = rs.getString("uname");
                uid = rs.getShort("uid");
                tid = rs.getShort("tid");
            }
            else return null;     // If no row is found for this email
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        // Update the token in the db entry for this user
        try {
            stmt = con.prepareStatement("UPDATE users SET token=? WHERE email=?");
            stmt.setString(1, token.toString(Character.MAX_RADIX));
            stmt.setString(2, email);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        User u = null;
        try {
            u = loadUser(email, uname, token, uid, tid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return u;
    }
    public static User getUser(HttpServletRequest request) {
        return getUser(getToken(request));
    }
    public static User getUser(BigInteger token) {
        if(token == null) return null;
        User temp = new User();
        temp.token = token;
        int index = Collections.binarySearch(users, temp);
        if(temp.token != null && index >=0) return users.get(index);
        else {  // Load them from the database
            try {
                Connection con = getConnection();
                if (con == null) return null; // If an error occurred making the connection
                PreparedStatement stmt = con.prepareStatement("SELECT email,uname, uid, tid FROM users WHERE token=?");
                stmt.setString(1, temp.token.toString(Character.MAX_RADIX));
                //LOGGER.info(stmt.toString());
                ResultSet rs = stmt.executeQuery();
                if(rs.next()) { // A row matches this email
                    return loadUser(rs.getString("email"), rs.getString("uname"), temp.token, rs.getShort("uid"), rs.getShort("tid"));
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    public static Team getTeam(ResultSet rs) throws SQLException {
        Team team = new Team();
        if(rs.next()) { // A row matches this email
            team.tid = rs.getShort("tid");
            team.tname = rs.getString("name");
            team.affiliation = rs.getString("affiliation");
            team.setComps(rs.getString("comps"));
            team.setUids(rs.getString("uids"));

            Team temp = getLoadedTeam(team.tid);
            if(temp == null){
                teams.add(team);
                Collections.sort(teams);    // TODO: Make this more efficient
                return team;
            } else {
                return temp;
            }
        } else {
            return null;
        }
    }
    public static Team getLoadedTeam(short tid) {
        if(tid <=0) return null;
        Team temp = new Team();
        temp.tid = tid;
        int index = Collections.binarySearch(teams, temp);
        if(index >= 0) return teams.get(index);
        return null;
    }
    public static Team getLoadedTeam(String tname) {
        /*
        Note: It is faster to linear search this than to sort by tname, binary search it, then resort by tid.
         */
        Team team = null;
        for(Team t: teams){
            if(t.tname.equals(tname)){   // The team we are looking for
                team = t;
            }
        }
        return team;
    }
    public static boolean isLoggedIn(HttpServletRequest request){
        return getUser(request)!=null;
    }
    public static boolean isLoggedIn(BigInteger token){
        return getUser(token)!=null;
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
    public static User loadUser(String email, String uname, BigInteger token, short uid, short tid) throws SQLException {
        User user = new User();
        user.email = email;
        user.uname = uname;
        user.token = token;
        user.uid = uid;
        user.tid = tid;

        // If so, the user belongs to a team. We will search the current teams in the `teams` ArrayList, and if
        // no team is found, we will add the team to the `teams` lists.
        Team team = new Team();  // A team with the user's tid, used for searching if the team already exists
        team.tid = tid;

        if(tid != -1) { // If the user belongs to a team.
            System.out.println("-- Teams:" + teams + ", team:" + team);
            int teamIndex = Collections.binarySearch(teams, team);
            if(teamIndex<0) {    // If the team is not loaded into the team list
                /* Now we search the database for the team information based off of the tid. We will load it into
                   the team variable and then add that to the teams list. */
                Connection conn = getConnection();
                if (conn == null) return null; // If an error occurred making the connection
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM teams WHERE tid=?");
                stmt.setShort(1, tid);
                ResultSet rs = stmt.executeQuery();
                team = getTeam(rs);
            }
            else {    // The team is loaded into the team list, just search for it
                team = teams.get(teamIndex);
            }
        }
        user.team = team;
        users.add(user);
        Collections.sort(users);    // TODO: Make this more efficient
        return user;
    }

    /**
     * If idIsId, then search by tid. Otherwise, search by tname.
     * @param identifier
     * @return
     */
    private static int loadTeamHelper(Object identifier, boolean idIsId) {
        Connection conn = getConnection();
        PreparedStatement stmt;
        if(idIsId) {    // identifier is an integer
            if(getLoadedTeam((Short)identifier) != null ) return 0;    // The team is already loaded
            try {
                stmt = conn.prepareStatement("SELECT * FROM teams WHERE tid = ?");
                stmt.setShort(1,(Short)identifier);
                //LOGGER.info(stmt.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return -2;  // A java-side error
            }
        } else{
            if(getLoadedTeam((String)identifier) != null) return 0;     // The team is already loaded
            try {
                stmt = conn.prepareStatement("SELECT * FROM teams WHERE name = ?");
                stmt.setString(1, (String)identifier);
                //LOGGER.info(stmt.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return -2;  // A java-side error
            }

        }
        ResultSet rs;
        try {
            rs = stmt.executeQuery();
            if(!rs.next()) return -3;   // The team doesn't exist
            getTeam(rs);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;  // A mysql error
        }
    }
    public static int loadTeam(short tid) {
        return loadTeamHelper(tid, false);
    }
    public static int loadTeam(String tname) {
        return loadTeamHelper(tname, false);
    }
    public static Connection getConnection(){
        Connection conn = null;

        try{
            Class.forName(JDBC_DRIVER);	// Load up the Driver's class
            conn = DriverManager.getConnection("jdbc:mysql://"+DB_URL+":"+DB_PORT+"/"+DB_NAME+
                    "?autoReconnect=true&useSSL=false",USER,PASS);
        }catch(SQLException se){
            //Handle errors for JDBC
            //LOGGER.error("JDBC Error. Code="+se.getErrorCode()+". Cause="+se.getCause()
                    //+". Message="+se.getMessage());
            return null;
        }catch(Exception e){
            //Handle errors for Class.forName
            //LOGGER.error("Class.forName error (likely). Cause="+e.getCause()+". Message="+e.getMessage());
        }

        // If the connection to the server was unsuccessful
        if(conn == null) {
            //LOGGER.warn("CANNOT CONNECT TO MYSQL!!! AN ERROR WILL OCCUR IMMEDIATELY");
            throw new RuntimeException("Could not connect to MYSQL Database");
        }
        return conn;
    }
    public static BigInteger finishRegistration(String email, String password, String uname) throws SQLException {
        BigInteger token = generateToken();
        short uid = -1;
        // Make the actual update query
        try
        {
            // Establishing Connection
            Connection conn = getConnection();
            if(conn==null) return BigInteger.valueOf(-1); // If an error occurred making the connection
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users(email, password, uname, token) " +
                    "VALUES (?, ?, ?, ?)");
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, uname);
            stmt.setString(4, token.toString(Character.MAX_RADIX));

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
        loadUser(email, uname,  token, uid, (short) -1);
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
                String uname = rs.getString("uname");


                // Finally, purge the entry from the table
                stmt = conn.prepareStatement("DELETE FROM verification WHERE email = ? OR uname = ?");
                stmt.setString(1, email);
                stmt.setString(2, uname);
                stmt.executeUpdate();

                return finishRegistration(email, password, uname);    // Finally, return the browser token
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
     * @param uname
     * @param email
     * @param password
     * @return vtoken, -1, -2, or -3
     * @throws NoSuchAlgorithmException
     */
    public static int Register(String uname, String email, String password) throws NoSuchAlgorithmException, SQLException {
        BigInteger vtoken = new BigInteger(VTOKEN_SIZE, new Random());    // Generate the verification token (vToken)

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
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ? OR uname = ?");
            stmt.setString(1, email);
            stmt.setString(2, uname);
            ResultSet rs = stmt.executeQuery();
            boolean emailTaken = false; boolean unameTaken = false;
            while(rs.next()) {  // Loop through matching rows
                if(rs.getString("email").equals(email)) emailTaken = true;
                else if(rs.getString("uname").equals(uname)) unameTaken = true;
            }
            if(emailTaken) return -2;
            else if(unameTaken) return -3;

            // Purge any entries in the verification database which match the email
            stmt = conn.prepareStatement("DELETE FROM verification WHERE email = ? OR uname = ?");
            stmt.setString(1, email);   // TODO: Right now, if someone is in the process of registering with this email or uname, they will be interrupted by another person attempting to register with the same email or uname.
            stmt.setString(2, uname);
            stmt.executeUpdate();

            if(conn==null) return -1; // If an error occurred making the connection
            stmt = conn.prepareStatement("INSERT INTO verification(email, password, uname, expires, vtoken, code) " +
                    "VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setString(1, email);
            stmt.setString(2, passHashFull);
            stmt.setString(3, uname);
            stmt.setLong(4, System.currentTimeMillis() + 15*60*1000);   // Expires in 15 minutes
            stmt.setString(5, "");
            stmt.setString(6, code);

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

        // First, we query the database for the password entry matching the email
        Connection con;
        PreparedStatement stmt;
        ResultSet rs;
        String storedFullHash;
        String uname;
        long start;
        String questions;
        short points;
        short uid = -1;
        short tid = -1;
        try {
            con = getConnection();
            if (con == null) return BigInteger.valueOf(-1); // If an error occurred making the connection
            stmt = con.prepareStatement("SELECT password, uname, uid, tid FROM users WHERE email=?");
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            if(rs.next()) { // A row matches this email
                storedFullHash = rs.getString("password");
                uname = rs.getString("uname");
                uid = rs.getShort("uid");
                tid = rs.getShort("tid");
            }
            else return BigInteger.valueOf(-2);     // If no row is found for this email
        }
        catch (SQLException e) {
            e.printStackTrace();
            return BigInteger.valueOf(-1);
        }
        // Extract the salt from the storedFull Hash
        String storedSalt = storedFullHash.substring(0, storedFullHash.indexOf("."));
        String storedHashed = storedFullHash.substring(storedFullHash.indexOf(".")+1);
        String givenHashed = hashPassword(password, Base64.getDecoder().decode(storedSalt));  // The hash of the password supplied by the user

        // Compare the givenHashed and the storedHashed
        if(!storedHashed.equals(givenHashed)) { // If the password is incorrect, return -3
            return BigInteger.valueOf(-3);
        }

        // Update the token in the db entry for this user
        try {
            stmt = con.prepareStatement("UPDATE users SET token=? WHERE email=?");
            stmt.setString(1, token.toString(Character.MAX_RADIX));
            stmt.setString(2, email);
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return BigInteger.valueOf(-1);
        }

        loadUser(email, uname, token, uid, tid);
        return token;
    }
    // NOTE: DOES NOT remove the token from the cookie
    public static int delUser(User u) throws SQLException {
        Connection conn = getConnection();
        if(conn == null) return -1;

        // First, logout the user from the database
        logout(u.token);

        // Finally, remove the user from their team
        if(u.tid>=0){  // If they belong to a team
            u.team.removeUser(u);
            //LOGGER.debug("TEAM LENGTH: " + u.team.uids.length);
            if(u.team.uids.size()<=0) { // If so, remove the team from the scoreboard
                Scoreboard.generateScoreboard();
            }
        }

        // Next, remove the user's row from the database
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE uid=?");
        stmt.setShort(1, u.uid);
        int status = stmt.executeUpdate();

        return status;
    }
    public static int delTeam(Team team) {
        if(team == null) return -3;
        Connection conn = getConnection();

        try {
            Set<Short> keys = team.comps.keySet();
            for(short cid: keys) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM `c" + cid + "` WHERE tid= ?");
                stmt.setShort(1, team.tid);
                stmt.executeUpdate();
            }

            PreparedStatement stmt = conn.prepareStatement("DELETE FROM teams WHERE tid=?");
            stmt.setShort(1,team.tid);
            int index = Collections.binarySearch(teams, team);
            if(index>=0) teams.remove(index);
            team.setUids(new short[0]); // Set the uids to an empty array
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public static int createTeam(String tname, String affiliation, String password, User captain){
        Connection conn = getConnection();

        if(captain.tid>=0) {    // Captain already belongs to a team
            return -3;
        }
        try {
            short[] uids = {captain.uid};

            String hashedPassword = getHashedFull(password);
            Team team = new Team();
            team.tname = tname;
            team.affiliation = affiliation;
            team.setComps("[]");
            team.setUids(uids);
            int status = team.updateTeam(hashedPassword);    // Write it into the database
            if(status == -2) {  // this team name is already registered
                return -2;
            } else if(status != 0) return -1;   // A server error occurred

            // Add the team to the loaded teams list
            teams.add(team);

            // Get the team's id
            PreparedStatement stmt = conn.prepareStatement("SELECT tid FROM teams WHERE name=?");
            stmt.setString(1, tname);
            ResultSet rs = stmt.executeQuery();
            short tid = -1;
            if(rs.next()) tid = rs.getShort("tid");
            team.tid = tid;

            captain.tid = tid;
            captain.team = team;
            captain.updateUser(false);   // Change this in the database

            // Finally, update the scoreboard
            Scoreboard.generateScoreboard();

            return 0;
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Add a user to a team.
     * @param tname
     * @param pass
     * @param user
     * @return success code
     */
    public static int joinTeam(String tname, String pass, User user) throws SQLException, NoSuchAlgorithmException {
        int status = loadTeam(tname);    // Make sure the team is loaded in
        if(status == -3) return -3;  // The team doesn't exist
        else if(status !=0) return status;  // Some other error occurred
        Team team = getLoadedTeam(tname);
        if(team==null) {    // This team isn't loaded, so something went wrong
            return -8;
        }
        if(!team.verifyPassword(pass)){
            return -5;  // Incorrect password
        }
        int success = team.addUser(user);
        if(success == -2) return -2;    // If the team is full
        else if(success == 1) return 1; // If they belong to a team
        else if(success!=0) return -8;  // If a server error occurred

        user.tid = team.tid;
        user.team = team;
        user.updateUser(false);
        return 0;
    }
    /**
     * Returns a list of unames that belong to the team.
     * @return
     */
    public static HashSet<String> getTeamUsers(Team team) {
        String selection = "SELECT uname FROM users WHERE ";
        if(team == null || team.uids == null) return null;
        for(int i = 0; i<team.uids.size(); i++) {
            selection += "(uid = ?)";
            if(i < team.uids.size()-1) {    // Put OR in between the statements
                selection += " OR ";
            }
        }
        Connection conn = getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(selection);
            ArrayList<Short> uidsTemp = new ArrayList<>(team.uids);
            for(int c = 0; c<team.uids.size(); c++) {   // Set the uids as the ?
                stmt.setInt(c+1, uidsTemp.get(c));
            }
            HashSet<String> users = new HashSet<>();
            //LOGGER.info(stmt.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(rs.getString("uname"));
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }
    public static int logout(BigInteger token) throws SQLException {
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET token=? WHERE token=?");
            stmt.setNull(1, Types.CHAR);
            stmt.setString(2, token.toString(Character.MAX_RADIX));
            //LOGGER.info(stmt.toString());
            int result = stmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    public static ArrayList<Team> getAllTeams() {
        Connection conn = getConnection();
        ArrayList<Team> allTeams = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM teams");
            //LOGGER.info(stmt);
            ResultSet rs= stmt.executeQuery();
            Team t = getTeam(rs);
            while(t!=null) {
                allTeams.add(t);
                t = getTeam(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allTeams;
    }
}