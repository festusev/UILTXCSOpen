package Outlet.uil;

import Outlet.Conn;
import Outlet.Countdown;
import Outlet.Student;
import com.google.gson.Gson;

import javax.xml.transform.Result;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UILEntry {
    public String tname;
    public Set<Short> uids;
    public short tid;
    public String affiliation;  // Specified by the students

    // Maps uids to the corresponding MCSubmission
    public HashMap<Short, MCSubmission> mc;

    public short[] frqResponses;    // All zeroes if they have not yet begun
    public short frqScore;
    public Competition competition;
    private static Gson gson = new Gson();

    public UILEntry(ResultSet rs, Competition comp) throws SQLException {
        tname = rs.getString("name");
        tid = rs.getShort("tid");
        affiliation = rs.getString("affiliation");
        competition = comp;

        setUids(rs.getString("uids"));

        if(comp.template.mcTest.exists) {
            mc = new HashMap<>();
            String column = rs.getString("mc").replace("\\u0027", "\"");
            column = column.substring(1, column.length() - 1);
            HashMap<String, ArrayList<Object[]>> temp = gson.fromJson(column, HashMap.class);
            temp = temp == null ? new HashMap<>() : temp;
            Set<String> keys = temp.keySet();
            for (String key : keys) {
                MCSubmission submission = MCSubmission.deserialize(temp.get(key));
                mc.put(Short.parseShort(key), submission);
            }
        }
        if(comp.template.frqTest.exists) {
            frqResponses = gson.fromJson(rs.getString("frqResponses"), short[].class);
            frqScore = Short.parseShort(rs.getString("frqScore"));
        }
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

    public int leaveTeam(Student u) {
        // They don't belong to this team
        if(u.cids.get(competition.template.cid).tid != tid || !uids.contains(u.uid))
            return -2;
        if(UILEntry.compRunning(competition.template.cid)) return -3;

        uids.remove(u.uid);
        int status = update();
        try {
            u.cids.remove(competition.template.cid);
            u.updateUser(false);
            if(uids.size()<=0) {   // If The team only has one user, delete the team.
                delete();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return status;
    }

    public void delete() {
        competition.template.deleteEntry(tid);
        uids = new HashSet<>();
    }

    /**
     * Returns true if the competition 'cid' is currently running.
     * @param cid
     * @return
     */
    public static boolean compRunning(short cid) {
        Template template = getTemplate(cid);

        return template.opens.done() && !template.closes.done();
    }

    public static Template getTemplate(short cid) {
        return UIL.getCompetition(cid).template;
    }

    public static UILEntry loadUILEntry(short tid, short cid) {
        try {
            return UIL.getCompetition(cid).getEntry(tid);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int update() { // Updates entry in the database
        Connection conn = Conn.getConnection();
        try {
            String statement = "UPDATE `c"+competition.template.cid+"` SET ";
            if(competition.template.mcTest.exists) {
                statement += "mc=?";
            }
            if(competition.template.frqTest.exists) {
                if(competition.template.mcTest.exists) statement+=",";
                statement += "frqResponses=?,frqScore=?";
            }
            statement += " WHERE tid=?";
            PreparedStatement stmt = conn.prepareStatement(statement);

            short startIndex = 1;   // Keeps track of where the frqTest should start inserting parameters
            if(competition.template.mcTest.exists) {
                String mcStringified = "{";
                Set<Short> keys = mc.keySet();
                int i = 0;
                for (short key : keys) {
                    mcStringified += key + ":" + mc.get(key).serialize();
                    if (i < keys.size() - 1) mcStringified += ",";
                    i++;
                }
                mcStringified += "}";
                stmt.setString(1, gson.toJson(mcStringified));
                startIndex = 2;
            }
            if(competition.template.frqTest.exists) {
                stmt.setString(startIndex, gson.toJson(frqResponses));
                stmt.setShort(++startIndex, frqScore);
            }
            stmt.setShort(++startIndex, tid);
            stmt.executeUpdate();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Returns the time started, even if they have begun the MC.
     * @param uid
     * @return
     */
    public long beginMC(short uid) {    // Returns the time started, even if they have already begun
        if(!competition.template.mcTest.exists) return -1;

        if(mc.keySet().contains(uid))
            return mc.get(uid).started;

        long now = Countdown.getNow();
        mc.put(uid, new MCSubmission(competition.template.mcTest.NUM_PROBLEMS,now));
        return now;
    }

    public long beginFRQ(){
        if(!competition.template.frqTest.exists) return -1;

        /*if(frqStarted > 0) return frqStarted;
        else {
            frqStarted = Countdown.getNow();
            update();
            return frqStarted;
        }*/
        return 0;
    }

    /**
     * Returns an array scoring report. First element is the total score,
     * second element is the number correct, third element is the number skipped
     * fourth element is the number incorrect
     * @param uid
     * @param answers
     * @return short[3]
     */
    public short[] scoreMC(short uid, String[] answers) {
        if(!competition.template.mcTest.exists) return new short[]{0,0,0,0};

        MCSubmission entry = mc.get(uid);
        entry.answers = answers;
        entry.scoringReport = competition.template.mcTest.score(answers);
        entry.finished = Countdown.getNow();
        return entry.scoringReport;
    }

    // if Greater than zero, add an incorrect run, if less than zero, add nothing,
    // if equal to zero, add a correct run
    public void addFRQRun(int status, int probNum) {
        if(!competition.template.frqTest.exists) return;

        probNum--;  // We only use probNum for indexes
        if(status>0) frqResponses[probNum]--;
        else if(status==0) {
            //System.out.println("--ADDING SUCCESSFUL FRQ RUN, # tries = " +frqResponses[probNum] + " score = "+java.lang.Math.max(CS.template.frqTest.calcScore(frqResponses[probNum]), competition.frqTest.MIN_POINTS));
            frqResponses[probNum] = (short)(java.lang.Math.abs(frqResponses[probNum]) + 1);
            frqScore += java.lang.Math.max(competition.template.frqTest.calcScore(frqResponses[probNum]), competition.template.frqTest.MIN_POINTS);
        }
        if(status >= 0) update();
    }

    public boolean finishedMC(short uid) {
        if(!competition.template.mcTest.exists) return true;

        if(mc.keySet().contains(uid)) {
            MCSubmission submission = mc.get(uid);
            if(submission.finished >0 || (submission.started+competition.template.mcTest.TIME)< Countdown.getNow()) return true;
        }
        return false;
    }

    public boolean finishedFRQ(){
        if(!competition.template.frqTest.exists) return true;

        //if(frqStarted > 0 && (frqStarted+competition.frqTest.TIME)<Countdown.getNow()) return true;
        return false;
    }

    public int getMCScore() {
        if(!competition.template.mcTest.exists) return 0;

        int score = 0;
        for(short i: mc.keySet()){
            score += mc.get(i).scoringReport[0];
        }
        return score;
    }



    public int getScore() {
        int frq = 0;
        int mc = 0;
        if(competition.template.mcTest.exists) mc = getMCScore();
        if(competition.template.frqTest.exists) frq = frqScore;
        return frq + mc;
    }
}

