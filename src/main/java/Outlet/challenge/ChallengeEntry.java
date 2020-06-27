package Outlet.challenge;
import Outlet.Conn;
import Outlet.Team;
import Outlet.uil.UILEntry;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChallengeEntry extends UILEntry {
    public final short tid;

    public boolean eligible;
    public int won;    // Number of districts they have won
    public double locality;

    private static Gson gson = new Gson();
    public ChallengeEntry(ResultSet rs) throws SQLException {
        super(rs.getShort("tid"));
        tid = super.tid;

        eligible = rs.getBoolean("eligible");
        won = rs.getInt("won");
        locality = rs.getDouble("locality");
    }
    public ChallengeEntry(short tid, boolean eligible, int won, double locality) {
        super(tid);
        this.tid = tid; this.eligible=eligible;this.won=won;this.locality=locality;
    }

    /**
     * Loads an EXISTING CSEntry from the database. Does not create a new one.
     * @param tid
     */
    public static ChallengeEntry loadEntry(short tid) throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `c"+ Challenge.CID +"` WHERE tid=?");
        stmt.setShort(1,tid);
        ResultSet rs = stmt.executeQuery();
        if(rs.next()) {
            return new ChallengeEntry(rs);
        }
        return null;
    }

    /**
     * Signs up this team in the competition's database. Note that it DOES NOT update the teams
     * database.
     * @param tid
     * @return
     * @throws SQLException
     */
    public static ChallengeEntry signup(short tid, boolean eligible) throws SQLException {
        ChallengeEntry newEntry = new ChallengeEntry(tid, eligible, 0, 0);

        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO `c" + Challenge.CID + "` VALUES (?, ?, ?, ?, ?)");
        stmt.setShort(1, newEntry.tid);
        stmt.setBoolean(2, eligible);
        stmt.setInt(3, 0);
        stmt.setDouble(4, 0);
        stmt.executeUpdate();

        return newEntry;
    }

    // Updates the entry in the database
    public void update(){
        Connection conn = Conn.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE `c"+Challenge.CID +"` SET eligible=?,won=?,locality=? WHERE tid=?");
            stmt.setBoolean(1, eligible);
            stmt.setInt(2, won);
            stmt.setDouble(3, locality);
            stmt.setLong(4, tid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public long beginMC(short uid) {
        return 0;
    }

    @Override
    public short[] scoreMC(short uid, String[] answers) {
        return new short[0];
    }

    @Override
    public int getMCScore() {
        return 0;
    }

    @Override
    public int getScore() {
        return 0;
    }

    /**
     * Returns a sorted array of Teams which are signed up for the Challenge.
     * @return
     */
    public static ArrayList<Team> getAllEntries(){
        ArrayList<Team> allTeams = Conn.getAllTeams();
        ArrayList<Team> teams = new ArrayList<>();
        for(Team t: allTeams) {
            if(t.comps.keySet().contains(Challenge.CID)){
                teams.add(t);
            }
        }
        return teams;
    }


    // if Greater than zero, add an incorrect run, if less than zero, add nothing,
    // if equal to zero, add a correct run
    public void addFRQRun(int won, double locality) {
        this.won = won;
        this.locality = locality;
        update();
    }
}