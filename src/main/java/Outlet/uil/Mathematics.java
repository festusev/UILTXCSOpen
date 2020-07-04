package Outlet.uil;
import Outlet.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

public class Mathematics extends HttpServlet{
    public static Template template;
    public static final short cid = 3;
    private static Gson gson = new Gson();
    public static boolean initialized = false;

    /***
     * MC test constants
     */
    public static final String[] KEY = {"a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a",
            "a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a","a",
            "a","a","a","a","a","a","a","a","a","a","a","a","a"}; // Multiple choice key
    public static final short[] MC_PROBLEM_MAP = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    public static final int MC_NUM_PROBLEMS = 60;
    public static final int CORRECT_PTS = 6;
    public static final int INCORRECT_PTS = -2;
    public static final int SKIPPED_PTS = 0;
    public static final String MC_NAME = "Test";
    public static final long MC_TIME = 40*60*1000;

    /**
     * Timing constants
     */
    private static String opensString = "08/07/2020 18:00:00";
    private static String closesString = "08/09/2020 18:00:00";
    private static Countdown opens;
    private static Countdown closes;
    public static final String MC_TIME_TEXT = "40 minutes";


    /***
     * Text constants like names and descriptions.
     */
    public static final String NAME = "UIL Math";
    public static final String WHAT_IT_IS = "A general math competition covering everything from algebra through " +
            "calculus with a little higher math sprinkled in :P. A 40-minute 60-question multiple choice test.";
    public static final String RULES =
            "<ol><li>Take the MC individually anytime during the 2-day period. Team members do not have to take it at the same time.</li>" +
            "<li>Each MC question correct is 6 pts and each incorrect is -2 pts. Skipped questions are 0pts.</li>" +
            "<li>You cannot use any resources except a calculator for the MC, and you should be ashamed if you have to (0_0)</li>";
    public static final String PRACTICE = "You can download practice MC tests from the <a href='/samples/mathematics_sample_packet.zip' class='link'>UIL Website.</a> Note that our test will cover a little bit more content to keep things interesting.";
    public static final String MC_INSTRUCTIONS = "Take these 60 questions in 40 minutes. If you can't reasonably eliminate any answers, leave the question blank. You are only allowed your brain and a calculator, feel shame if you have to cheat on this.";


    public static void initialize() {
        MCTest mc = new MCTest(KEY, MC_PROBLEM_MAP, MC_NUM_PROBLEMS, CORRECT_PTS, INCORRECT_PTS, SKIPPED_PTS, MC_NAME, MC_TIME_TEXT, MC_INSTRUCTIONS, MC_TIME);
        FRQTest frq = new FRQTest();
        opens = new Countdown(opensString, "countdown");
        closes = new Countdown(closesString, "countdown");
        template = new Template(NAME, WHAT_IT_IS, RULES, PRACTICE, mc, frq, opens, closes, cid, new SortMathTeams());

        initialized = true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if(!initialized) initialize();
        template.render(request,response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if(!initialized) initialize();

        User user = Conn.getUser(request);
        if(user == null || user.token == null || !Conn.isLoggedIn(user.token)){ // They are not logged in, return nothing
            return;
        }

        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();


        if (action.equals("updatePage")) {  // Reloads the columns portion of the page
            writer.write("{\"updatedHTML\":\""+template.getColumnsHTML(user, template.getCompeteStatus(user))+"\"}");
            return;
        } if(user.team.comps.keySet().contains(cid)) { // If their team is already signed up for this competition
            if(template.getStatus() == 1) {
                MathEntry temp = (MathEntry) user.team.comps.get(cid);
                if (action.equals("beginMC")) { // Even if they have already begun, will send them the MC test
                    if (!temp.finishedMC(user.uid)) {
                        writer.write("{\"status\":\"success\",\"mcHTML\":\""+template.getRunningMC(temp.beginMC(user.uid))+"\"}");
                        temp.update();
                        return;
                    }
                } else if (action.equals("submitMC")) {
                    String[] answers = gson.fromJson(request.getParameter("answers"), String[].class);
                    writer.write("{\"mcHTML\":\"" + template.getFinishedMC(temp.scoreMC(user.uid, answers)) + "\"}");
                    temp.update();
                    template.updateScoreboard();
                    return;
                }
            } else {
                writer.write("{\"status\":\"error\",\"error\":\"\"}");
            }
        } else if(action.equals("signup")) {    // Their team is not signed up for this competition
            try {
                user.team.comps.put(cid,MathEntry.signup(user.tid));
                user.team.updateComps();
                template.updateScoreboard();
                writer.write("{\"status\":\"success\",\"updatedHTML\":\""+template.getColumnsHTML(user, template.getCompeteStatus(user))+"\"}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}

class MathEntry extends UILEntry{
    public final short tid;
    private static Gson gson = new Gson();

    public MathEntry(ResultSet rs) throws SQLException {
        super(rs.getShort("tid"));
        tid = super.tid;

        mc = new HashMap<>();
        System.out.println("MC: " + rs.getString("mc"));
        String column = rs.getString("mc").replace("\\u0027","\"");
        column = column.substring(1, column.length()-1);
        System.out.println("COLUMN:"+column);
        HashMap<String, ArrayList<Object[]>> temp = gson.fromJson(column, HashMap.class);
        temp = temp == null ? new HashMap<>(): temp;
        System.out.println("TEMP Length:" +temp.size());
        Set<String> keys = temp.keySet();
        for(String key: keys) {
            //System.out.println("TESTING:"+temp.get(key).size()+",FIRST_ELEMENT:"+temp.get(key).get(0)+",FIRST_ELEMENT_TYPE:"+temp.get(key).get(0).getClass());
            MCSubmission submission = MCSubmission.deserialize(temp.get(key));
            mc.put(Short.parseShort(key),submission);
        }
    }
    public MathEntry(short tid, HashMap<Short, MCSubmission> mc) {
        super(tid);
        this.tid = tid; this.mc = mc;
    }

    /**
     * Loads an EXISTING MathEntry from the database. Does not create a new one.
     * @param tid
     */
    public static MathEntry loadEntry(short tid) throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `c"+Mathematics.cid+"` WHERE tid=?");
        stmt.setShort(1,tid);
        ResultSet rs = stmt.executeQuery();
        if(rs.next()) {
            return new MathEntry(rs);
        }
        return null;
    }

    /**
     * Signs ths user up in the competition's database. Note that it DOES NOT update the teams
     * database.
     * @param tid
     * @return
     * @throws SQLException
     */
    public static MathEntry signup(short tid) throws SQLException {
        MathEntry newEntry = new MathEntry(tid, new HashMap<>());

        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO `c" + Mathematics.cid + "` VALUES (?, ?)");
        stmt.setShort(1, newEntry.tid);
        stmt.setString(2, "{}");
        stmt.executeUpdate();

        return newEntry;
    }

    // Updates the entry in the database
    public void update(){
        Connection conn = Conn.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE `c"+Mathematics.cid+"` SET mc=? WHERE tid=?");
            String mcStringified = "{";
            Set<Short> keys = mc.keySet();
            int i=0;
            for(short key:keys) {
                mcStringified+=key+":"+mc.get(key).serialize();
                if(i < keys.size()-1) mcStringified += ",";
                i++;
            }
            mcStringified += "}";
            stmt.setString(1, gson.toJson(mcStringified));
            stmt.setShort(2, tid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns a sorted array of Teams which are signed up for Math.
     * @return
     */
    public static ArrayList<Team> getAllEntries(){
        ArrayList<Team> allTeams = Conn.getAllTeams();
        ArrayList<Team> teams = new ArrayList<>();
        for(Team t: allTeams) {
            if(t.comps.keySet().contains(Mathematics.cid)){
                teams.add(t);
            }
        }
        return teams;
    }

    /**
     * Returns the time started, even if they have begun the MC.
     * @param uid
     * @return
     */
    public long beginMC(short uid) {
        if(mc.keySet().contains(uid))
            return mc.get(uid).started;

        long now = Countdown.getNow();
        mc.put(uid, new MCSubmission(Mathematics.MC_NUM_PROBLEMS,now));
        return now;
    }

    /**
     * Returns an array scoring report. First element is the total score,
     * second element is the number correct, third element is the number skipped
     * fourth element is the number incorrect
     * @param uid
     * @param answers
     * @return short[3]
     */
    public short[] scoreMC(short uid, String[] answers){
        MCSubmission entry = mc.get(uid);
        entry.answers = answers;
        entry.scoringReport = Mathematics.template.mcTest.score(answers);
        entry.finished = Countdown.getNow();
        return entry.scoringReport;
    }

    public int getMCScore(){
        int score = 0;
        for(short i: mc.keySet()){
            score += mc.get(i).scoringReport[0];
        }
        return score;
    }

    public int getScore(){
        return getMCScore();
    }
}

/**
 * Sorts teams that belong to the Math competition by their Math score
 */
class SortMathTeams extends SortUILTeams implements Comparator<Team>
{
    public int compare(Team a, Team b) {
        return b.comps.get(Mathematics.cid).getScore()-a.comps.get(Mathematics.cid).getScore();
    }
}