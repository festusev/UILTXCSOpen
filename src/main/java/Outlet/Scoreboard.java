package Outlet;

import Outlet.challenge.Challenge;
import Outlet.uil.CS;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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
    private static boolean initialized = false;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if(!initialized) {
            Scoreboard.generateScoreboard();
            initialized = true;
        }

        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        if(preNav.isEmpty() || postNav.isEmpty())
            generateScoreboard();
        writer.append(preNav + Dynamic.loadNav(request) + postNav);
    }

    /**
     * Store the scoreboard html so that it doesn't need to be recreated every time
     */
    public static void generateScoreboard() {
        HashMap<Short, Double> challengeZScores = null;
        HashMap<Short, Double> csZScores = null;
        try {
            challengeZScores = Challenge.template.end();
            csZScores = CS.template.end();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        ArrayList<Team> teams = Conn.getAllTeams();

        ArrayList<ZTeam> zTeams = new ArrayList<>();
        for(Team t: teams) {
            int zScore = 0;
            if(t.comps.containsKey(1)) {
                zScore += csZScores.get(t.tid);
            }
            if(t.comps.containsKey(2)) {
                zScore += challengeZScores.get(t.tid);
            }
            zTeams.add(new ZTeam(t, zScore));
        }
        Collections.sort(zTeams);

        String teamList =
                "<table id=\"teamList\"><tr><th>#</th><th>Team</th><th>Affiliation</th><th></th><th class=\"right\">Total Score</th>" +
                "</tr>";
        int rank=1;
        for(ZTeam t: zTeams) {
            teamList += "<tr><td>" + rank + "</td><td>" + t.team.tname + "</td><td>" + t.team.affiliation + "</td>" +
                    "<td></td><td class=\"right\">" + t.zScore + "</td></tr>";
            rank++;
        }
        // create HTML
        preNav= "<html>\n" +
                        "<head>\n" +
                        "    <title>Scoreboard - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                        "    <link rel=\"stylesheet\" href=\"./css/scoreboard.css\">\n" +
                        "</head>\n";
        postNav =       "<body><div class='column' id='scoreboardColumn'><h1>Scoreboard</h1><h2>Sum of competition <a href='https://www.statisticshowto.com/probability-and-statistics/z-score/'>z-scores</a> for each team.</h2>" +
                        teamList + "</table></div>"+
                        "</body>\n" +
                        "</html>";
    }
}

/**
 * A simple wrapper that adds in zScore and allows teams to be sorted by it
 */
class ZTeam implements Comparable<ZTeam>{
    Team team;
    int zScore;
    public ZTeam(Team team, int zScore) {
        this.team = team; this.zScore = zScore;
    }
    public int compareTo(ZTeam otherTeam){
        return otherTeam.zScore - zScore;
    }
}
class SortTeams implements Comparator<Team>
{
    public int compare(Team a, Team b) {
        return 1; //b.getPts()-a.getPts();
    }
}