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

public class CS extends HttpServlet{
    public static Template template;
    public static final short cid = 1;
    private static Gson gson = new Gson();
    public static boolean initialized = false;

    /***
     * MC test constants
     */
    public static final String[] KEY = {"d","c","e","a","a","b","e","c","e","d","b","a","c","a","d","b","e","c","c","a",
            "d","b","e","d","a","d","e","b","c","d","c","a","b","b","e","b","a","c","a","b"}; // Multiple choice key
    public static final short[] MC_PROBLEM_MAP = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    public static final int MC_NUM_PROBLEMS = 40;
    public static final int CORRECT_PTS = 6;
    public static final int INCORRECT_PTS = -2;
    public static final int SKIPPED_PTS = 0;
    public static final String MC_NAME = "Test";
    public static final long MC_TIME = 45*60*1000;

    /***
     * FRQ test constants
     */
    public static final String DIR = cid+"_cs/";    // Both the score and the testcases directories
    public static final short FRQ_NUM_PROBLEMS = 12;
    public static final short MAX_POINTS = 60;
    public static final short INCORRECT_PENALTY = 5;
    public static final String[] FRQ_PROBLEM_MAP = {"1. Abril", "2. Brittany", "3. Emmanuel", "4. Guowei", "5. Ina", "6. Josefa", "7. Kenneth", "8. Magdalena", "9. Noah", "10. Ramiro", "11. Seema", "12. Wojtek", "13. Least Least Common Multiple Sum", "14. Constellations", "15. Power Walking", "16. A Long Piece of String", "17. Really Mean Question", "18. Pattern Finding"};
    private static final String[] DAT_MAP = new String[]{"abril.dat", "brittany.dat", "emmanuel.dat", "guowei.dat", "ina.dat", "josefa.dat", "kenneth.dat", "magdalena.dat", "noah.dat", "ramiro.dat", "seema.dat", "wojtek.dat", "llcms.dat", "constellations.dat", "powerwalking.dat", "longstring.dat", "rmq.dat", "patternfinding.dat"};
    public static final String FRQ_NAME = "Programming";
    public static final long FRQ_TIME = 2*60*60*1000;

    /**
     * Timing constants
     */
    private static String opensString = "07/31/2020 18:00:00";
    private static String closesString = "08/02/2020 18:00:00";
    private static Countdown opens;
    private static Countdown closes;
    public static final String MC_TIME_TEXT = "45 minutes";
    public static final String FRQ_TIME_TEXT = "2 hours";

    /***
     * Text constants like names and descriptions.
     */
    public static final String NAME = "UIL CS";
    public static final String WHAT_IT_IS = "A challenging and unique computer science competition, testing skills in" +
            " general programming and Java syntax. Consists of a 45-minute 40-question multiple choice test and a two-hour" +
            " 12-question algorithmic programming contest. ";
    public static final String RULES = "<ol><li>Take the MC individually anytime during the 2-day period. Team members do not have to take it at the same time.</li>" +
            "<li>Each MC question correct is 6 pts and each incorrect is -2 pts. Skipped questions are 0pts.</li>" +
            "<li>You cannot use any resources for the MC.</li>" +
            "<li>Take the Programming section with your team anytime during the 2-day period.</li>" +
            "<li>Each Programming question has a max score of 60 pts where each incorrect submission reduces that value by 5pts.</li>" +
            "<li>You may use textbooks or offline resources for the Programming but don't google problems.</li>" +
            "<li>You can program in Java 11, Python 3, or C++ 17.</li>";
    public static final String PRACTICE = "You can download the MC and Programming solutions from our last UIL CS <a href='/samples/cs_sample_packet.zip' class='link'>here.</a> You can also visit <a href='https://www.uiltexas.org/academics/stem/computer-science' class='link'>official UIL</a> to view their sample tests.";
    public static final String MC_INSTRUCTIONS = "Take these 40 questions in 45 minutes. If you can't reasonably eliminate any answers, leave the question blank. You are only allowed your brain, Google will not help you.";


    public static void initialize() {
        MCTest mc = new MCTest(KEY, MC_PROBLEM_MAP, MC_NUM_PROBLEMS, CORRECT_PTS, INCORRECT_PTS, SKIPPED_PTS, MC_NAME, MC_TIME_TEXT, MC_INSTRUCTIONS, MC_TIME);
        FRQTest frq = new FRQTest(DIR, DIR, FRQ_NUM_PROBLEMS, MAX_POINTS, INCORRECT_PENALTY, FRQ_PROBLEM_MAP, FRQ_NAME, FRQ_TIME_TEXT, FRQ_TIME, DAT_MAP);
        opens = new Countdown(opensString, "countdown");
        closes = new Countdown(closesString, "countdown");
        template = new Template(NAME, WHAT_IT_IS, RULES, PRACTICE, mc, frq, opens, closes, cid, new SortCSTeams());

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
                    CSEntry temp = (CSEntry) user.team.comps.get(cid);
                    if(action.equals("grabFRQProblems")) {
                        writer.write("{\"frqProblemsHTML\":\""+template.getFRQProblems(temp)+"\"}");
                    } else if (action.equals("beginMC")) { // Even if they have already begun, will send them the MC test
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
                    } else if (action.equals("beginFRQ")) {
                        if (!temp.finishedFRQ()) {
                            temp.beginFRQ();
                            writer.write("{\"status\":\"success\",\"frqHTML\":\""+template.getRunningFRQ(temp)+"\"}");
                        }
                    } else if(action.equals("submitFRQ")){
                        Part filePart = request.getPart("textfile");
                        InputStream fileContent = filePart.getInputStream();

                        byte[] bytes = new byte[fileContent.available()];
                        fileContent.read(bytes);

                        short probNum = Short.parseShort(request.getParameter("probNum"));

                        if(temp.frqResponses[probNum-1] > 0){
                            writer.write("{\"status\":\"error\",\"error\":\"You've already gotten this problem.\"}");
                            return;
                        }

                        int status = template.frqTest.score(probNum, bytes, filePart.getSubmittedFileName(), user.uid, user.tid);
                        temp.addFRQRun(status, probNum);
                        switch(status){
                            case 0:
                                writer.write("{\"status\":\"success\",\"scored\":\"You gained points!\"}");
                                template.updateScoreboard();
                                return;
                            case 1:
                                writer.write("{\"status\":\"error\",\"error\":\"Compile-time error.\"}");
                                template.updateScoreboard();
                                return;
                            case 2:
                                writer.write("{\"status\":\"error\",\"error\":\"Runtime error\"}");
                                template.updateScoreboard();
                                return;
                            case 3:
                                writer.write("{\"status\":\"error\",\"error\":\"Time limit exceeded.\"}");
                                template.updateScoreboard();
                                return;
                            case 4:
                                writer.write("{\"status\":\"error\",\"error\":\"Wrong answer.\"}");
                                template.updateScoreboard();
                                return;
                            default:
                                writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                                return;
                        }
                    } else if(action.equals("finishFRQ")) {
                        writer.write("{\"frqHTML\":\""+template.getFinishedFRQ(temp)+"\"}");
                        return;
                    }
                } else {
                    writer.write("{\"status\":\"error\",\"error\":\"\"}");
                }
        } else if(action.equals("signup")) {    // Their team is not signed up for this competition
            try {
                user.team.comps.put(cid,CSEntry.signup(user.tid));
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

class CSEntry extends UILEntry{
    public final short tid;
    private static final short FRQ_MIN = 5;    // You get a minimum of 5 points from the frq

    public short[] frqResponses;    // All zeroes if they have not yet begun
    public short frqScore;
    public long frqStarted; // Default is zero if they haven't started
    private static Gson gson = new Gson();

    public CSEntry(ResultSet rs) throws SQLException {
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

        frqResponses = gson.fromJson(rs.getString("frqResponses"), short[].class);
        frqScore = Short.parseShort(rs.getString("frqScore"));
        frqStarted = Long.parseLong(rs.getString("frqStarted"));
    }
    public CSEntry(short tid, HashMap<Short, MCSubmission> mc, short[] frqResponses, short frqScore, long frqStarted) {
        super(tid);
        this.tid = tid; this.mc = mc; this.frqResponses = frqResponses; this.frqScore = frqScore; this.frqStarted = frqStarted;
    }

    /**
     * Loads an EXISTING CSEntry from the database. Does not create a new one.
     * @param tid
     */
    public static CSEntry loadEntry(short tid) throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `c"+CS.cid+"` WHERE tid=?");
        stmt.setShort(1,tid);
        ResultSet rs = stmt.executeQuery();
        if(rs.next()) {
            return new CSEntry(rs);
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
    public static CSEntry signup(short tid) throws SQLException {
        CSEntry newEntry = new CSEntry(tid, new HashMap<>(), new short[CS.FRQ_NUM_PROBLEMS], (short) 0, 0);

        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO `c" + CS.cid + "` VALUES (?, ?, ?, ?, ?)");
        stmt.setShort(1, newEntry.tid);
        stmt.setString(2, "{}");
        stmt.setString(3, gson.toJson(newEntry.frqResponses));
        stmt.setShort(4, newEntry.frqScore);
        stmt.setLong(5, newEntry.frqStarted);
        stmt.executeUpdate();

        return newEntry;
    }

    // Updates the entry in the database
    public void update(){
        Connection conn = Conn.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE `c"+CS.cid+"` SET mc=?,frqResponses=?,frqScore=?,frqStarted=? WHERE tid=?");
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
            stmt.setString(2, gson.toJson(frqResponses));
            stmt.setShort(3, frqScore);
            stmt.setLong(4, frqStarted);
            stmt.setShort(5, tid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns a sorted array of Teams which are signed up for CS.
     * @return
     */
    public static ArrayList<Team> getAllEntries(){
        ArrayList<Team> allTeams = Conn.getAllTeams();
        ArrayList<Team> teams = new ArrayList<>();
        for(Team t: allTeams) {
            if(t.comps.keySet().contains(CS.cid)){
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
        mc.put(uid, new MCSubmission(CS.MC_NUM_PROBLEMS,now));
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
        entry.scoringReport = CS.template.mcTest.score(answers);
        entry.finished = Countdown.getNow();
        return entry.scoringReport;
    }

    public long beginFRQ(){
        if(frqStarted > 0) return frqStarted;
        else {
            frqStarted = Countdown.getNow();
            update();
            return frqStarted;
        }
    }

    // if Greater than zero, add an incorrect run, if less than zero, add nothing,
    // if equal to zero, add a correct run
    public void addFRQRun(int status, int probNum) {
        probNum--;  // We only use probNum for indexes
        if(status>0) frqResponses[probNum]--;
        else if(status==0) {
            frqScore += java.lang.Math.max(CS.template.frqTest.calcScore(frqResponses[probNum]), FRQ_MIN);
            frqResponses[probNum] = (short)(java.lang.Math.abs(frqResponses[probNum]) + 1);
        }
        if(status >= 0) update();
    }

    public boolean finishedFRQ(){
        if(frqStarted > 0 && (frqStarted+CS.FRQ_TIME)<Countdown.getNow()) return true;
        return false;
    }

    public int getMCScore(){
        int score = 0;
        for(short i: mc.keySet()){
            score += mc.get(i).scoringReport[0];
        }
        return score;
    }

    public int getScore(){
        return getMCScore() + frqScore;
    }
}

/**
 * Sorts teams that belong to the CS competition by their CS score
 */
class SortCSTeams extends SortUILTeams implements Comparator<Team>
{
    public int compare(Team a, Team b) {
        return b.comps.get(CS.cid).getScore()-a.comps.get(CS.cid).getScore();

    }
}