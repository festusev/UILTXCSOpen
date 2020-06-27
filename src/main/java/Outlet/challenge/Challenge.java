package Outlet.challenge;

import Outlet.*;
import Outlet.uil.*;
import com.google.gson.Gson;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.Comparator;

public class Challenge extends HttpServlet {
    public static ChallengeTemplate template;
    public static final short CID = 2;
    private static Gson gson = new Gson();
    static boolean  initialized = false;

    /***
     * FRQ test constants
     */
    public static final String DIR = CID +"_challenge/";    // Both the score and the testcases directories
    public static final String FRQ_NAME = "Challenge";

    /**
     * Timing constants
     */
    private static String opensString = "09/11/2019 10:50:32";
    private static String closesString = "09/15/2020 10:50:32";
    private static Countdown opens;
    private static Countdown closes;

    /***
     * Text constants like names and descriptions.
     */
    public static final String NAME = "TXCSOpen Challenge";
    public static final String WHAT_IT_IS = "A difficult but rewarding competition.";
    public static final String RULES = "You may use the internet, but do not post anything competition-specific online.";
    public static final String PRACTICE = "Brush up on algorithms and the like.";


    public static void initialize() {
        opens = new Countdown(opensString, "countdown");
        closes = new Countdown(closesString, "countdown");
        template = new ChallengeTemplate(NAME, WHAT_IT_IS, RULES, PRACTICE, opens, closes, CID, new SortChallengeTeams());

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
        } if(user.team.comps.containsKey(CID)) { // If their team is already signed up for this competition
            if(template.getStatus() == 1) {
                ChallengeEntry temp = (ChallengeEntry) user.team.comps.get(CID);
                if(action.equals("grabFRQProblems")) {
                    writer.write("{\"frqProblemsHTML\":\""+template.getFRQProblems(temp)+"\"}");
                } else if(action.equals("submitFRQ")){
                    Part filePart = request.getPart("textfile");
                    InputStream fileContent = filePart.getInputStream();

                    byte[] bytes = new byte[fileContent.available()];
                    fileContent.read(bytes);

                    int status = ScoreEngine.score(P7zipCompression.decompress(bytes), temp);
                    switch(status){
                        case 0:
                            writer.write("{\"status\":\"success\",\"scored\":\"Correct answer!\"}");
                            return;
                        case 1:
                            writer.write("{\"status\":\"error\",\"error\":\"Output format error.\"}");
                            return;
                        case 2:
                            writer.write("{\"status\":\"error\",\"error\":\"Wrong answer.\"}");
                            return;
                        default:
                            writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                            return;
                    }
                }
            } else {
                writer.write("{\"status\":\"error\",\"error\":\"\"}");
            }
        } else if(action.equals("signup")) {    // Their team is not signed up for this competition
            try {
                user.team.comps.put(CID,ChallengeEntry.signup(user.tid, request.getParameter("eligible").equals("true")));
                user.team.updateComps();
                writer.write("{\"status\":\"success\",\"updatedHTML\":\""+template.getColumnsHTML(user, template.getCompeteStatus(user))+"\"}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}

class SortChallengeTeams extends SortUILTeams implements Comparator<Team>
{
    public int compare(Team a, Team b) {
        ChallengeEntry aEntry = (ChallengeEntry) a.comps.get(Challenge.CID);
        ChallengeEntry bEntry = (ChallengeEntry) b.comps.get(Challenge.CID);

        int diffWon = bEntry.won - aEntry.won;
        if(diffWon!=0) return diffWon;
        else {
            double diffLoc = aEntry.locality - bEntry.locality;
            if(diffLoc>0) return 1;
            else if(diffLoc<0) return -1;
            return 0;
        }
    }
}
class P7zipCompression {
    public static String decompress(final byte[] compressed) throws IOException {
        SevenZFile sevenZFile = new SevenZFile(new SeekableInMemoryByteChannel(compressed));
        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        if(entry!=null){
            byte[] content = new byte[(int) entry.getSize()];
            sevenZFile.read(content, 0, content.length);
            return new String(content);
        }
        sevenZFile.close();
        return null;
    }
}