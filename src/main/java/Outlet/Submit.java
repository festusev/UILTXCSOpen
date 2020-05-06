package Outlet;

import java.io.*;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/***
 * Shows a graph of the top 7 teams, then below lists the teams in order.
 * Created by Evan Ellis.
 */
@MultipartConfig
public class Submit extends HttpServlet{
    public static final long TIME_LIMIT = 1000*60*60*2;


    private static final String PAGE_NAME = "submit";
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User u = Conn.getUser(request);
        if(u==null || !Conn.isLoggedIn(u.token)){
            response.sendRedirect(request.getContextPath());
        }
        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.append("<html>\n" +
                        "<head>\n" +
                        "    <title>Submit - TXCSOpen</title>\n" +
                        "    <meta charset=\"utf-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                        "<link rel=\"icon\" type=\"image/png\" href=\"res/icon.png\">" +
                        "    <link rel=\"stylesheet\" href=\"./css/bootstrap.min.css\">\n" +
                        "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                        "    <link rel=\"stylesheet\" href=\"css/style.css\">\n" +
                        "    <link rel=\"stylesheet\" href=\"css/submit.css\">\n" +
                        "    <script src=\"./js/submit.js\"></script>\n" +
                        "</head>\n" +
                        "<body>\n" + Dynamic.loadLoggedInNav(request, PAGE_NAME));

        long now = (new Date()).getTime();
        int frqStatus = Dynamic.frqOpen();
        if(frqStatus == 0) {
            writer.append("<style>#copyright_notice{position:fixed;}body{overflow:hidden;}</style><div class=\"forbidden\">The Programming Section Will Open On May 8th<p class=\"forbiddenRedirect\"><a class=\"link\" href=\""+request.getContextPath()+"/console\">Click Here to Go back.</a></p></div>");
        } else if(frqStatus == 2){
            writer.append("<style>#copyright_notice{position:fixed;}body{overflow:hidden;}</style><div class=\"forbidden\">The Competition Has Closed<p class=\"forbiddenRedirect\"><a class=\"link\" href=\""+request.getContextPath()+"/console\">Click Here to Go back.</a></p></div>");
        } else if(u.team.start > 0 && now - u.team.start > TIME_LIMIT) {
            writer.append("<style>#copyright_notice{position:fixed;}body{overflow:hidden;}</style><div class=\"forbidden\">Your Team Has Already Competed<p class=\"forbiddenRedirect\"><a class=\"link\" href=\""+request.getContextPath()+"/console\">Click Here to Go back.</a></p></div>");
        } else if(u.tid >= 0) {    // If the user belongs to a team
            String problems = "";
            String problemStatusList = "<ol id=\"problemStatusList\">";  // The list of where the team is on each problem. Displayed to the right
            int numProblems = ScoreEngine.NUM_PROBLEMS;
            for(int i=1; i<=numProblems;i++){
                problems += "  <option value=\""+i+"\">"+ScoreEngine.PROBLEM_MAP[i-1]+"</option>\n";
                short status = u.team.getProblemStatus((short)i);
                String statusQuote = "";
                if(status > 0) {    // They've solved it
                    statusQuote = "Solved (" + (ScoreEngine.MAX_POINTS - (status-1)*5) + "pts)";
                } else {    // It's still unsolved
                    statusQuote = Math.abs(status) + " tries";
                }
                problemStatusList += "<li>" + ScoreEngine.PROBLEM_MAP[i-1] + " - " + statusQuote + "</li>";
            }
            problemStatusList += "</ol>";

            String beginWarning = "   <div id=\"beginWarning\"><div id=\"warningCnt\">" +
                    "       <p id=\"warningHeader\">Are you sure you want to begin?</p>" +
                    "       <p id=\"warningSubtitle\">Once you do you and your team will have 2 hours to compete.</p>" +
                    "       <button id=\"beginBtn\" onclick=\"begin()\">Begin</button>" +
                    "       <a id=\"goBackBtn\" href=\"console\">Go Back</a></div>" +
                    "   </div>";
            long diff = 0;
            if(u.team.start > 0) {
                beginWarning = "   <div id=\"beginWarning\" style=\"display:none;\"><div id=\"warningCnt\">" +
                                "       <p id=\"warningHeader\">Are you sure you want to begin?</p>" +
                                "       <p id=\"warningSubtitle\">Once you do you and your team will have 2 hours to compete.</p>" +
                                "       <button id=\"beginBtn\" onclick=\"begin()\">Begin</button>" +
                                "       <a id=\"goBackBtn\" href=\"console\">Go Back</a></div>" +
                                "   </div>" +
                                "   <script>document.addEventListener(\"DOMContentLoaded\", function(event) {startTimer();});</script>";
                diff = now-u.team.start;
            }
            writer.append(
                        beginWarning+
                        "<div id=\"centerBox\"><div id=\"submissionLeft\"><p id=\"submitHeader\">Submit</p>" +
                        Dynamic.loadTimer("Remaining", TIME_LIMIT - diff, "location.reload();", true) +
                        "<p id=\"inst\">Choose a problem to submit:</p>" +
                        "<form id=\"submit\" onsubmit=\"submit(); return false;\" enctype=\"multipart/form-data\">" +
                        "<select id=\"problem\">\n" +
                        problems +
                        "</select>" +
                        "<input type=\"file\" accept=\".java,.cpp,.py\" id=\"textfile\"/>" +
                        "<button id=\"submitBtn\" class=\"chngButton\">Submit</button>" +
                        "</form><p id=\"advice\">Download the <a target=\"_blank\" href=\"ProgrammingFiles/programmingPacket.pdf\"" +
                        " class=\"link\" >problems</a> and the <a href=\"ProgrammingFiles/StudentData.zip\" class=\"link\">data files</a>. Be sure to take input from System.in. Confused? Reread the <a target=\"_blank\" href=\"rules\" class=\"link\">rules</a>.</p></div>" +
                        "<div id=\"submissionRight\"><div id=\"rightTitle\">Problems</div>" + problemStatusList);
        } else {    // Otherwise, display a message saying they must be part of a team to submit
            writer.append("<div class=\"forbidden\">You must belong to a team to submit.<p class=\"forbiddenRedirect\"><a class=\"link\" href=\"console\">Join a team here.</a></p></div>");
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int frqStatus = Dynamic.frqOpen();
        if(frqStatus != 1) {
            response.sendRedirect(request.getContextPath());
            return;
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        User u = Conn.getUser(request);
        if(u != null && !Conn.isLoggedIn(u.token)){
            writer.write("{\"reload\":\"" +request.getContextPath() + "\"}");
            return;
        }

        Team t = u.team;
        if (u == null) { // User isn't logged in
            writer.append("{\"error\":\"User isn't logged in.\"}");
            return;
        } else if (t == null || u.tid < 0) {
            writer.append("{\"error\":\"User doesn't belong to a team.\"}");
            return;
        }
        String s = request.getParameter("started");
        if(t.start > 0 && System.currentTimeMillis() - t.start > TIME_LIMIT + 2*60*1000) { // If so, they have exceeded the time limit. Giving them 2 extra minutes in case of technical issues
            writer.write("{\"error\":\"Time limit exceeded. Submission forfeited.\"}");
            return;
        }

        if(s != null) {
            Long started = Long.parseLong(s);
            if(started <= 0) return;
            t.start = started;
            t.updateTeam();
            return;
        }

        // If we are not setting started, then we are taking a file submission
        Part filePart = request.getPart("textfile");
        InputStream fileContent = filePart.getInputStream();

        byte[] bytes = new byte[fileContent.available()];
        fileContent.read(bytes);

        short probNum = Short.parseShort(request.getParameter("probNum"));
        if(t.problemSolved(probNum)) {
            writer.write("{\"error\":\"You've already solved that problem silly!\"}");
            return;
        }

        int status =  ScoreEngine.score(probNum, bytes, filePart.getSubmittedFileName(), u.uid, u.tid);
        boolean success = status == 0;
        if(status >= 0) {
            short beforePoints = t.getProblemScore();   // The points before the submission. Used to calculate the points gained.
            t.addRun(probNum, success);
            int serverStatus = t.updateTeam();

            if (serverStatus < 0 || status < 0) {
                writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                return;
            }
            if(success) {
                // Update the scoreboard
                Scoreboard.generateScoreboard();

                writer.write("{\"success\":\"You gained "+(t.getProblemScore() - beforePoints)+" points!\"}");
            } else if(status == 1) {
                writer.write("{\"error\":\"A compile time error occurred.\"}");
            } else if(status == 2) {
                writer.write("{\"error\":\"A runtime error occurred. Be sure to use STDIN.\"}");
            } else if(status == 3) {
                writer.write("{\"error\":\"Time limit exceeded.\"}");
            } else if(status == 4) {
                writer.write("{\"error\":\"Wrong answer.\"}");
            }
        }
        else {
            writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
            return;
        }
    }
}