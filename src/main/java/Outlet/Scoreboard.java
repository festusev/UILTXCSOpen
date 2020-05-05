package Outlet;

import com.sun.org.apache.xpath.internal.operations.Mult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/***
 * Shows a graph of the top 7 teams, then below lists the teams in order.
 * Created by Evan Ellis.
 */
public class Scoreboard extends HttpServlet{
    private static String preNav="";   // The scoreboard html before the dynamic navigation
    private static String postNav="";   // The scoreboard html after the dynamic navigation
    private static final short NUMGRAPHED = 7;  // The number of teams to graph
    private static final double START_DATE = 5;
    //private static final Logger LOGGER = LogManager.getLogger(Scoreboard.class);
    private static final String PAGE_NAME = "scoreboard";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        if(preNav.isEmpty() || postNav.isEmpty())
            generateScoreboard();
        writer.append(preNav + Dynamic.loadNav(request, PAGE_NAME) + postNav);
    }

    /**
     * Store the scoreboard html so that it doesn't need to be recreated every time
     */
    public static void generateScoreboard() {
        //LOGGER.info("--- GENERATING SCOREBOARD ----");
        // Get a json encoded string of all of the teams
        ArrayList<Team> teams = Conn.getAllTeams();
        Collections.sort(teams, new SortTeams());

        int maxTestSum = MultipleChoice.NUM_PROBLEMS * MultipleChoice.CORRECT_PTS;
        int maxProbSum = ScoreEngine.NUM_PROBLEMS * ScoreEngine.MAX_POINTS;
        double maxScore = maxProbSum + maxTestSum;

        // The table row list of teams in order of points
        String teamList = "";
        int rank = 1;
        for(Team t: teams) {
            teamList+="<tr><td>" + rank + "</td><td>" + t.tname + "</td><td>" + t.affiliation + "</td>" +
                    "<td class=\"bar\"><div class=\"testSum\" style=\"width:calc(50% * "+(t.testSum/maxScore)+");\" title=\""+t.testSum+"\"></div><div class=\"probSum\" style=\"width:"+
                    "calc(50% *"+(t.getProblemScore()/maxScore)+");\" title=\""+t.getProblemScore()+"\"></div></td></tr>";
            rank ++;
        }

        String script = ""; // Set to empty string if registration is not open
        String body = "";
        if(Dynamic.competitionOpen()) {
            body = "<div id=\"upperHalf\" class=\"row\"><p id=\"center\">Scoreboard</p></div>" +
                    "<div id=\"column\"><table id=\"teamList\"><tr><th>Rank</th><th>Team Name</th><th>Affiliation</th><th>MC Score - Programming Score</th>" +
                    "</tr>" + teamList + "</table></div>";
        } else {
            body = "<style>#copyright_notice{position:fixed;}body{overflow:hidden;}</style><div class=\"forbidden\">The Scoreboard is Closed Until the Competition Begins.<p class=\"forbiddenRedirect\"><a class=\"link\" href=\"index.jsp\">Click Here to Go back.</a></p></div>";
        }

        // create HTML
        preNav= "<html>\n" +
                        "<head>\n" +
                        "    <title>Scoreboard - TXCSOpen</title>\n" +
                        "    <meta charset=\"utf-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                        "<link rel=\"icon\" type=\"image/png\" href=\"res/icon.png\">" +
                        "    <link rel=\"stylesheet\" href=\"./css/bootstrap.min.css\">\n" +
                        "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                        "    <link rel=\"stylesheet\" href=\"./css/style.css\">\n" +
                        "    <link rel=\"stylesheet\" href=\"./css/scoreboard.css\">\n" +
                        "</head>\n" +
                        "<body>\n";
        postNav=        body+
                        Dynamic.loadLeftFlair() +
                        "</body>\n" +
                        "</html>";
    }
}
class SortTeams implements Comparator<Team>
{
    public int compare(Team a, Team b) {
        return b.getPts()-a.getPts();
    }
}