package Outlet;
import Outlet.uil.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/***
 * The first page the user sees when they log in. Contains user and team configurations.
 * Created by Evan Ellis.
 */
public class Profile extends HttpServlet{
    protected static Gson gson = new Gson();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User u = UserMap.getUserByRequest(request);
        if(u==null || u.token == null){
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String action = request.getParameter("action");
        System.out.println("action="+action+"&teacher="+u.teacher);
        if(action != null && action.equals("getCompetitions") && u.teacher) {
            JsonArray listJ = new JsonArray();
            // String json = "[";
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");

            int c = 0;
            int length = ((Teacher)u).cids.size();
            for(short cid: ((Teacher)u).cids) {
                Competition competition = UIL.getCompetition(cid);
                JsonObject compJ = new JsonObject();
                compJ.addProperty("cid", cid);
                compJ.addProperty("name", competition.template.name);
                compJ.addProperty("published", competition.published);
                compJ.addProperty("isPublic", competition.isPublic);
                compJ.addProperty("description", StringEscapeUtils.escapeHtml4(competition.template.description));
                compJ.addProperty("numNonAlts", competition.numNonAlts);
                compJ.addProperty("alternateExists", competition.alternateExists);

                if(competition.template.mcTest.exists) {
                    JsonObject writtenJ = new JsonObject();
                    writtenJ.addProperty("opens", competition.template.mcTest.opens.DATE_STRING);
                    writtenJ.addProperty("time", (competition.template.mcTest.TIME / (1000 * 60)));

                    JsonArray writtenAnswersJ = new JsonArray();
                    for (int i = 0, j = competition.template.mcTest.KEY.length; i < j; i++) {
                        JsonArray answerJ = new JsonArray();
                        answerJ.add(StringEscapeUtils.escapeHtml4(competition.template.mcTest.KEY[i][0]));
                        answerJ.add(competition.template.mcTest.KEY[i][1]);
                        writtenAnswersJ.add(answerJ);
                    }
                    writtenJ.add("answers", writtenAnswersJ);
                    writtenJ.addProperty("instructions", StringEscapeUtils.escapeHtml4(competition.template.mcTest.INSTRUCTIONS));
                    writtenJ.addProperty("testLink", StringEscapeUtils.escapeHtml4(competition.template.mcTest.TEST_LINK));
                    writtenJ.addProperty("correctPoints", competition.template.mcTest.CORRECT_PTS);
                    writtenJ.addProperty("incorrectPoints", competition.template.mcTest.INCORRECT_PTS);
                    compJ.add("written", writtenJ);
                }
                if(competition.template.frqTest.exists) {
                    JsonObject handsOnJ = new JsonObject();
                    handsOnJ.addProperty("opens", competition.template.frqTest.opens.DATE_STRING);
                    handsOnJ.addProperty("time", competition.template.frqTest.TIME / (1000 * 60));

                    JsonArray problemsJ = new JsonArray();
                    for (int i = 0, j = competition.template.frqTest.PROBLEM_MAP.length; i < j; i++) {
                        JsonArray problemJ = new JsonArray();
                        FRQProblem problem = competition.template.frqTest.PROBLEM_MAP[i];
                        problemJ.add(StringEscapeUtils.escapeHtml4(problem.name));
                        problemJ.add(problem.input);
                        problemJ.add(problem.output);
                        problemsJ.add(problemJ);
                    }
                    handsOnJ.add("problems", problemsJ);
                    handsOnJ.addProperty("studentPacketLink", StringEscapeUtils.escapeHtml4(competition.template.frqTest.STUDENT_PACKET));
                    handsOnJ.addProperty("maxPoints",competition.template.frqTest.MAX_POINTS);
                    handsOnJ.addProperty("incorrectPenalty", competition.template.frqTest.INCORRECT_PENALTY);
                    compJ.add("handsOn", handsOnJ);
                }
                listJ.add(compJ);
                c++;
            }
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");

            PrintWriter writer = response.getWriter();
            writer.write(new Gson().toJson(listJ));
            return;
        } else {
            Conn.setHTMLHeaders(response);

            String right = //"<div id='profile_cnt'>" +
                    "<div id='Profile'>" +
                    "<h1>Profile</h1><div class='profile_cmpnt half'><h2>First Name</h2><input type='text' name='fname' id='fname' value='" + StringEscapeUtils.escapeHtml4(u.fname) + "'/></div>" +
                    "<div class='profile_cmpnt half'><h2>Last Name</h2><input type='text' name='lname' id='lname' value='" + StringEscapeUtils.escapeHtml4(u.lname) + "'></div>";
            String delUserPassText = "Your information cannot be recovered.";    // The warning text that we display when the user tries to delete their account.
            if (u.teacher) {
                String school = "";
                if(u.school != null) school = StringEscapeUtils.escapeHtml4(u.school);
                right += "<div class='profile_cmpnt full'><h2>School</h2><input type='text' name='school' id='school' value='" + school + "'></div>";
                delUserPassText = "Your class and competitions will be permanently deleted.";
            }
            right += "<div class='profile_cmpnt full'><h2><b>Change Password</b></h2><h2>Old Password</h2>" +
                    "<input type='password' name='oldPassword' id='oldPassword'/><h2>New Password</h2>" +
                    "<input type='password' name='newPassword' id='newPassword'/></div>" +
                    "<div class='profile_cmpnt full'><span onclick='saveChanges()' id='saveChanges'>Save Changes</span></div>" +
                    "<div class='profile_cmpnt full'><span onclick='document.getElementById(\"delUserPasswordCnt\").style.display=\"block\";' id='delAccount'>Delete Account</span></div>" +
                    "<div id='delUserPasswordCnt' style='display:none'><div class='center'><h1>Are you sure?</h1><p>" + delUserPassText +
                    "</p><label for='delPass'>Retype your password:</label><input name='delPass' type='password' id='delUserPassword'/>" +
                    "<button onclick='delUser()'>Yes, delete my account.</button><a onclick='hideDelUser()'>Cancel</a></div></div>";
            if(u.teacher) right+="</div>";


            // create HTML form
            PrintWriter writer = response.getWriter();
            writer.append("<html>\n" +
                    "<head>\n" +
                    "    <title>Profile - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                    "    <link rel=\"stylesheet\" href=\"/css/console/console.css\">\n" +
                    "    <link rel=\"stylesheet\" href=\"/css/console/profile.css\">\n" +
                    "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                    "    <script src=\"/js/console/profile.js\"></script>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    Dynamic.get_consoleHTML(4, right) +
                    "</body>\n" +
                    "</html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if(!Conn.isLoggedIn(request)){
            return;
        }
        User u = UserMap.getUserByRequest(request);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        System.out.println("doing post");

        String action = request.getParameter("action");
        if(action == null) {
            action = partToString(request.getPart("action"));
            if(action == null) {
                response.setStatus(405);
                return;
            }
        }
        if(action.equals("saveChanges")) {   // They are updating their user's information
            String fname = request.getParameter("fname");
            String lname = request.getParameter("lname");
            String school = request.getParameter("school");
            String oldPassword = request.getParameter("oldPassword");
            String newPassword = request.getParameter("newPassword");

            System.out.println("fname="+fname+", lname="+lname+", school="+school+", oldPassword="+oldPassword+", newPassword="+newPassword);

            boolean oldPassValid = oldPassword!=null && !oldPassword.isEmpty();
            boolean newPassValid = newPassword!=null && !newPassword.isEmpty();
            if(oldPassValid || newPassValid) {
                if(oldPassValid && !newPassValid) {
                    writer.write("{\"error\":\"New password is not specified\"}");
                    return;
                } else if(!oldPassValid && newPassValid) {
                    writer.write("{\"error\":\"Old password is not specified\"}");
                    return;
                } else if(u.verifyPassword(oldPassword)) {
                    try {
                        u.changePassword(Conn.getHashedFull(newPassword));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                        return;
                    }
                } else {
                    writer.write("{\"error\":\"Password is incorrect.\"}");
                    return;
                }
            }
            if(!fname.isEmpty()) {
                u.fname = fname;
            } else {
                writer.write("{\"error\":\"First name cannot be empty.\"}");
                return;
            }
            if(!lname.isEmpty()) {
                u.lname = lname;
            } else {
                writer.write("{\"error\":\"Last name cannot be empty.\"}");
                return;
            }
            u.school = school;  // School can be empty

            if(u.updateUser(false) == 0) {
                writer.write("{\"success\":\"Changes saved.\"}");
                return;
            } else {
                writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                return;
            }
        }
    }

    public static String partToString(Part part) {
        if(part == null) return null;
        try {
            InputStream stream = part.getInputStream();
            byte[] partBytes = new byte[stream.available()];
            stream.read(partBytes);
            return new String(partBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}