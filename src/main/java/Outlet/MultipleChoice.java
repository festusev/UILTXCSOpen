package Outlet;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/***
 * Administers the multiple choice test. The test is 45 minutes long, and users can take it asynchronously anytime they
 * want.
 * Created by Evan Ellis.
 */
public class MultipleChoice extends HttpServlet{
    private static final char[] key = {'d','c','e','a','a','b','e','c','e','d','b','a','c','a','d','b','e','c','c','a','d','b','e','d','a','d','e','b','c','d','c','a','b','b','e','b','a','c','a','b'};
    public static final int CORRECT_PTS = 6;
    public static final int INCORRECT_PTS = -2;
    public static final int SKIPPED_PTS = 0;
    public static final int NUM_PROBLEMS = 40;

    private static Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User u = Conn.getUser(request);
        if(u== null || !Conn.isLoggedIn(u.token)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        System.out.println("--- START: "  + u.start + "---");
        PrintWriter writer = response.getWriter();
        String body = "<style>#copyright_notice{position:fixed;}body{overflow:hidden;}</style><div class=\"forbidden\">The Test is Closed Until the Competition Begins.<p class=\"forbiddenRedirect\"><a class=\"link\" href=\"console\">Click Here to Go back.</a></p></div>";
        if(u.start > 0) {
            body = "<style>#copyright_notice{position:fixed;}body{overflow:hidden;}</style><div class=\"forbidden\">You've already taken the test.<p class=\"forbiddenRedirect\"><a class=\"link\" href=\"console\">Click Here to Go back.</a></p></div>";
        } else if(u.tid <0) {
            body = "<div class=\"forbidden\">You must belong to a team to submit.<p class=\"forbiddenRedirect\"><a class=\"link\" href=\"console\">Join a team here.</a></p></div>";
        } else if(Dynamic.competitionOpen()) { // Load the multiple choice form
            body =  "   <div id=\"beginWarning\"><div id=\"warningCnt\">" +
                    "       <p id=\"warningHeader\">Are you sure you want to begin?</p>" +
                    "       <p id=\"warningSubtitle\">Once you do you will have 45 minutes to complete the test.</p>" +
                    "       <button id=\"beginBtn\" onclick=\"begin()\">Begin</button>" +
                    "       <a id=\"goBackBtn\" href=\"console\">Go Back</a></div>" +
                    "   </div>" +
                    "   <div class=\"row\" id=\"upperHalf\">\n" +
                    "        <div class=\"center\">\n" +
                    "            <div id=\"body-header\">\n" +
                    "                Multiple Choice\n" +
                    "            </div>\n" +
                    "        </div>" +
                    Dynamic.loadTimer("Remaining", 45*60*1000, "submit();", false) +
                    "    </div>\n" +
                    "    <div id=\"centerColumn\">" +
                    "       <p id=\"instructions\"><span>Instructions:</span> Take this test in 45 minutes without any aid. When you're done, submit your answers for scoring. You can take this anytime today, so please don't share it with anyone.</p>" +
                    "        <ol class=\"column\">";
            char[] options = new char[]{'a', 'b', 'c', 'd', 'e'};
            for(short i=1; i<=20; i++) {    // Loop through the first 20 questions to add answer bubbles
                body += "<li>";
                for(char c: options){
                    body +="<label for=\""+i+c+"\">"+c+"</label><input type=\"radio\" value=\""+c+"\" name=\""+i+"\" id=\""+i+c+"\">";
                }
                body+="</li>";
            }
            body+="</ol><ol class=\"column\" start=\"21\">";
            for(short i=21; i<=40; i++) {    // Loop through the first 20 questions to add answer bubbles
                body += "<li>";
                for(char c: options){
                    body +="<label for=\""+i+c+"\">"+c+"</label><input type=\"radio\" value=\""+c+"\" name=\""+i+"\" id=\""+i+c+"\">";
                }
                body+="</li>";
            }
            body+="</ol><button class=\"chngButton\" onclick=\"submit();\">Submit</button></div>";
        }
        writer.write("<html>\n" +
                "<head>\n" +
                "    <title>Multiple Choice - TXCSOpen</title>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <link rel=\"icon\" type=\"image/png\" href=\"res/icon.png\">" +
                "    <link rel=\"stylesheet\" href=\"./css/bootstrap.min.css\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                "    <link rel=\"stylesheet\" href=\"./css/style.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"./css/multiple-choice.css\">\n" +
                "    <script src=\"./js/multiple-choice.js\"></script>" +
                "</head>\n" +
                "<body>\n" +
                body +
                Dynamic.loadCopyright() +
                "</body></html>");
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User u = Conn.getUser(request);
        if(!Dynamic.competitionOpen() || u ==null || !Conn.isLoggedIn(u.token)) {
            return;
        }
        String s = request.getParameter("started");
        if(s != null) {
            Long started = Long.parseLong(s);
            if(started <= 0) return;
            u.start = started;
            try {
                System.out.println("--- Current Points: " + u.points + " Started: " + u.start + " ---");
                u.updateUser(false);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        // If we are not setting started, then we are taking a mc submission
        if(System.currentTimeMillis() - u.start > 1000*60*47) { // If so, they have exceeded the time limit. Giving them 2 extra minutes in case of technical issues
            writer.write("{\"error\":\"Time limit exceeded. Submission forfeited. Your score is -80.\"}");
        }
        char[] answers = gson.fromJson(request.getParameter("answers"), char[].class);  // An array of length 40 containing all of their answers in order
        ArrayList<Short> questions = new ArrayList<>();   // The questions they got correct
        int skipped = 0;
        for(short i =0; i<answers.length; i++){
            if(key[i] == answers[i]) questions.add((short) (i+1));
            else if(answers[i]=='z') skipped ++;
        }
        short[] qArray = new short[questions.size()];   // So that we can store it
        for(short i=0;i<qArray.length;i++) {
            qArray[i]=questions.get(i);
        }
        short score = (short)(qArray.length*CORRECT_PTS + skipped*SKIPPED_PTS + (40-qArray.length - skipped)*INCORRECT_PTS);
        u.addScoringReport(score, qArray);

        writer.write("{\"scored\":\"You got " + qArray.length + " correct and " + (40 - qArray.length) + " incorrect. Your final score is " + score + "\"}");

        // Finally, update the scoreboard
        Scoreboard.generateScoreboard();
    }
}