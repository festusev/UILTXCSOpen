package Outlet;

import Outlet.uil.UILEntry;
import com.google.gson.Gson;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Team implements Comparable<Team> {
    public String tname;
    public String affiliation;
    public HashMap<Short, UILEntry> comps;    // List of the competitions this team is registered for, referenced by the competition's id
    public Set<Short> uids;  // The user at index 0 is the team captain and has special privileges
    public short tid;
    private static Gson gson = new Gson();

    //private static final //Logger //LOGGER = LogManager.get//Logger(Team.class);

    @Override
    public int compareTo(Team team) {
        return tid - team.tid;
    }

    public void setUids(short[] temp) {
        uids = new HashSet<>();
        for(short uid: temp) {
            uids.add(uid);
        }
    }
    public void setUids(String s) {
        short[] temp = gson.fromJson(s, short[].class);
        setUids(temp);
    }
    public int updateTeam(){
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("UPDATE teams SET affiliation=?, uids=? WHERE tid = ?");
            String uidString = gson.toJson(uids);

            stmt.setString(1, affiliation);
            stmt.setString(2, uidString);
            stmt.setShort(3, tid);

            //LOGGER.info(stmt.toString());
            stmt.executeUpdate();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public int updateTeam(String hashedPassword){
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("INSERT INTO teams(name, password, affiliation, comps, uids) VALUES (?, ?, ?, ?, ?)");

            stmt.setString(1, tname);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, affiliation);
            stmt.setString(4, gson.toJson(comps.keySet()));
            stmt.setString(5, gson.toJson(uids));

            //LOGGER.info(stmt.toString());
            stmt.executeUpdate();
            return 0;
        } catch (SQLException e) {
            System.out.println("--- ERROR MESSAGE: " + e.getMessage());
            if(e.getMessage().contains("for key 'name'")) return -2;    // If so, this team already exists
            e.printStackTrace();
            return -1;
        }
    }
    public int updateComps(){
        Connection conn = Conn.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE teams SET comps=? WHERE tid=?");
            stmt.setString(1, gson.toJson(comps.keySet()));
            stmt.setShort(2, tid);

            stmt.executeUpdate();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public void setComps(String s) {
        short[] temp = gson.fromJson(s, short[].class);
        comps = new HashMap<>();
        for(short comp: temp) {
            comps.put(comp, UILEntry.loadUILEntry(comp, tid));
        }
    }
    public int addUser(User u) {

        if(uids.size() >= 3) {  // The team is full
            return -2;
        }
        uids.add(u.uid);
        return updateTeam();
    }
    public void removeUser(User u) {
        if(uids.size()-1<=0) {   // If The team only has one user, delete the team.
            Conn.delTeam(this);
        } else {
            uids.remove(u.uid);

            updateTeam();   // Make it permanent.
        }
    }

    public int changePassword(String hashedPassword) {
        Connection conn = Conn.getConnection();
        if(conn==null) return -1;   // Error making connection;
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE teams SET password = ? WHERE tid=?");
            stmt.setString(1, hashedPassword);
            stmt.setShort(2, tid);
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
            PreparedStatement stmt = conn.prepareStatement("SELECT password FROM teams WHERE tid=?");
            stmt.setShort(1, tid);
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