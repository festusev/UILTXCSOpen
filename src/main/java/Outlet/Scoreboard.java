package Outlet;

import Outlet.challenge.Challenge;
import Outlet.uil.CS;
import Outlet.uil.CalculatorApplications;
import Outlet.uil.Mathematics;
import Outlet.uil.NumberSense;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

        if(postNav.isEmpty())
            generateScoreboard();

        // create HTML
        writer.append("<html>\n" +
                "<head>\n" +
                "    <title>Scoreboard - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                "    <link rel=\"stylesheet\" href=\"./css/scoreboard.css\">\n" +
                "</head>\n" + Dynamic.loadNav(request) + postNav);
    }

    /**
     * Store the scoreboard html so that it doesn't need to be recreated every time
     */
    public static void generateScoreboard() {
        while(postNav.isEmpty()) {
            System.out.println("Generating Scoreboard");
            HashMap<Short, Double> firstCSScores = null;    // The archived scores of the first competition
            HashMap<Short, Double> challengeScores = null;
            HashMap<Short, Double> csScores = null;
            HashMap<Short, Double> mathScores;
            HashMap<Short, Double> numberSenseScores;
            HashMap<Short, Double> calcAppScores;
            try {
                // First, get the normal scores from the first competition
                Connection conn = Conn.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `c0`");

                ResultSet rs = stmt.executeQuery();
                firstCSScores = new HashMap<>();
                if (rs.next()) {
                    while (rs.next()) {
                        double normal = rs.getDouble("normals");
                        short tid = rs.getShort("tid");
                        firstCSScores.put(tid, normal);
                    }
                }

                /* First, make sure all of the competitions are initialized */
                if (!Challenge.initialized) {
                    Challenge.initialize();
                }
                if (!CS.initialized) {
                    CS.initialize();
                }
                if (!Mathematics.initialized) {
                    Mathematics.initialize();
                }
                if (!NumberSense.initialized) {
                    NumberSense.initialize();
                }
                if (!CalculatorApplications.initialized) {
                    CalculatorApplications.initialize();
                }

                if (Challenge.template.closes.done())
                    challengeScores = Challenge.template.end();
                else
                    challengeScores = new HashMap<>();

                if (CS.template.closes.done())
                    csScores = CS.template.end();
                else
                    csScores = new HashMap<>();

                if (Mathematics.template.closes.done())
                    mathScores = Mathematics.template.end();
                else
                    mathScores = new HashMap<>();

                if (NumberSense.template.closes.done())
                    numberSenseScores = NumberSense.template.end();
                else
                    numberSenseScores = new HashMap<>();

                if (CalculatorApplications.template.closes.done())
                    calcAppScores = CalculatorApplications.template.end();
                else
                    calcAppScores = new HashMap<>();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

            ArrayList<Team> teams = Conn.getAllTeams();

            ArrayList<NormalTeam> normalTeams = new ArrayList<>();
            for (Team t : teams) {
                double normalScore = 0;
                System.out.println("<< Team " + t.tid + " containsKey is " + t.comps.containsKey(0));
                if (firstCSScores.containsKey(t.tid)) {
                    normalScore += firstCSScores.get(t.tid);
                }
                if (t.comps.containsKey(1)) {
                    normalScore += csScores.get(t.tid);
                }
                if (t.comps.containsKey(2)) {
                    normalScore += challengeScores.get(t.tid);
                }
                if (t.comps.containsKey(3)) {
                    normalScore += mathScores.get(t.tid);
                }
                if (t.comps.containsKey(4)) {
                    normalScore += numberSenseScores.get(t.tid);
                }
                if (t.comps.containsKey(5)) {
                    normalScore += calcAppScores.get(t.tid);
                }
                normalTeams.add(new NormalTeam(t, normalScore));
            }
            Collections.sort(normalTeams);

            String teamList =
                    "<table id=\"teamList\"><tr><th>#</th><th>Team</th><th>Affiliation</th><th></th><th class=\"right\">Total Score</th>" +
                            "</tr>";
            int rank = 1;
            for (NormalTeam t : normalTeams) {
                teamList += "<tr><td>" + rank + "</td><td>" + t.team.tname + "</td><td>" + t.team.affiliation + "</td>" +
                        "<td></td><td class=\"right\">" + String.format("%.2f", t.normalScore) + "</td></tr>";
                rank++;
            }

            postNav = "<body><div class='column' id='scoreboardColumn'><div class='head-row'><h1>Scoreboard</h1><h3 class='subtitle'>Sum of each team's competition scores adjusted to a min-max normalization.</h3>" +
                    teamList + "</table></div></div>" + Dynamic.loadBigCopyright() +
                    "</body>\n" +
                    "</html>";
        }
    }
}

/**
 * A simple wrapper that adds in zScore and allows teams to be sorted by it
 */
class NormalTeam implements Comparable<NormalTeam>{
    Team team;
    double normalScore;
    public NormalTeam(Team team, double normalScore) {
        this.team = team; this.normalScore = normalScore;
    }
    public int compareTo(NormalTeam otherTeam){
        return Double.compare(otherTeam.normalScore,normalScore);
    }
}