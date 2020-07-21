package Outlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/***
 * The first page the user sees when they log in. Contains user and team configurations.
 * Created by Evan Ellis.
 */
public class Console extends HttpServlet{
    private static final String PAGE_NAME = "console";
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User uData = Conn.getUser(request); // We need to display the user's data
        if(uData == null || uData.token == null || !Conn.isLoggedIn(uData.token)){
            response.sendRedirect(request.getContextPath());
            return;
        }
        // Get the user's team, if they belong to one
        //Team uTeam = Conn.getLoadedTeam(uData.tid);

        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        // TODO: Alan make this like the profile wireframes

        // Create the team section html
        String tName = "";
        String tSection = "";
        String tPoints = "";
        /*if(uTeam != null) {
            tName = uTeam.tname + " - " + uTeam.affiliation;
            tSection = "    <div class=\"row\">\n" +
                    "        <div id=\"teamDiv\" class=\"rowCenter\">\n" +
                    "            <div id=\"memConf\" class=\"sec\">\n" +
                    "                <p class=\"secTitle\" id=\"memConfTitle\">Current Team Members<button onclick=\"leaveTeam()\">Leave</button></p>\n" +
                    "                <div class=\"secBody\" id=\"leaveTeamBox\">\n" +
                    "                    <ul id=\"memList\">\n";
            HashSet<String> users = Conn.getTeamUsers(uTeam);
            for(String uname: users) {
                tSection += "                        <li class=\"memName\">" + uname + "</li>\n";
            }
            tSection +=        "                    </ul>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "            <div id=\"teamPass\" class=\"sec\">\n" +
                    "                <p class=\"secTitle\" id=\"teamPassTitle\">Update Team Password</p>\n" +
                    "                <form class=\"secBody\" onsubmit=\"updateTeamPass(); return false;\" id=\"newTeamPassBox\">\n" +
                    "                    <label for=\"curTeamPass\">Current Team Password:</label>\n" +
                    "                    <input type=\"password\" id=\"curTeamPass\" name=\"curTeamPass\"/>\n" +
                    "                    <label for=\"newTeamPass\">New Team Password:</label>\n" +
                    "                    <input type=\"password\" id=\"newTeamPass\" name=\"newTeamPass\"/>\n" +
                    "                    <label for=\"confNewTeamPass\">Confirm New Password:</label>\n" +
                    "                    <input type=\"password\" id=\"confNewTeamPass\" name=\"confNewTeamPass\"/>\n" +
                    "                    <button class=\"chngButton\" id=\"teamPassButton\">Update Team Password</button>\n" +
                    "                </form>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "    </div>\n";
        } else {    // They do not belong to a team*/
            tName = "No Team";
            tPoints = "<p id=\"distWon\"></p>";
            tSection = "    <div class=\"row\">\n" +
                    "        <div id=\"teamDiv\" class=\"rowCenter\">\n" +
                    "            <div id=\"memConf\" class=\"sec\" style=\"height:auto\">\n" +
                    "                <p class=\"secTitle\" id=\"memConfTitle\">Join Team</p>\n" +
                    "                <form class=\"secBody\" id=\"joinTeamBox\" onsubmit=\"joinTeam(); return false;\">\n" +
                    "                    <label for=\"joinTeamName\">Team Name</label>\n" +
                    "                    <input type=\"text\" id=\"joinTeamName\" name=\"joinTeamName\" maxlength=\"25\"/>\n" +
                    "                    <label for=\"joinTeamPass\">Team Password</label>\n" +
                    "                    <input type=\"password\" id=\"joinTeamPass\" name=\"joinTeamPass\"/>\n" +
                    "                    <button class=\"chngButton\" id=\"teamPassJoin\">Join Team</button>\n" +
                    "                </form>\n" +
                    "            </div>\n" +
                    "            <div id=\"teamPass\" class=\"sec\">\n" +
                    "                <p class=\"secTitle\" id=\"teamPassTitle\">Create Team</p>\n" +
                    "                <form action=\"create-team\" id=\"createTeamBox\" onsubmit=\"createTeam(); return false;\" class=\"secBody\">\n" +
                    "                    <label for=\"newTeamName\">Team Name:</label>\n" +
                    "                    <input type=\"text\" id=\"newTeamName\" name=\"newTeamName\" maxlength=\"25\"/>\n" +
                    "                    <label for=\"affiliation\">Affiliation:</label>\n" +
                    "                    <input type=\"text\" id=\"affiliation\" name=\"affiliation\" maxlength=\"25\"/>\n" +
                    "                    <label for=\"newTeamPass\">New Team Password:</label>\n" +
                    "                    <input type=\"password\" id=\"newTeamPass\" name=\"newTeamPass\"/>\n" +
                    "                    <label for=\"confNewTeamPass\">Confirm New Password:</label>\n" +
                    "                    <input type=\"password\" id=\"confNewTeamPass\" name=\"confNewTeamPass\"/>\n" +
                    "                    <button class=\"chngButton\" id=\"teamPassCreate\">Create Team</button>\n" +
                    "                </form>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "    </div>\n";
        //}

        // create HTML form
        PrintWriter writer = response.getWriter();
        writer.append(
                "<html>\n" +
                "<head>\n" +
                "    <title>Console - TXCSOpen</title>\n" + Dynamic.loadHeaders()+
                "    <link rel=\"stylesheet\" href=\"./css/console.css\">\n" +
                "    <script src=\"./js/console.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                Dynamic.loadLoggedInNav() +
                "    <div class=\"row\" id=\"infoRow\">\n" +
                "        <div id=\"userInfo\">\n" +
                "            <p id=\"username\">" +uData.uname + "</p>\n" +
                "            <p id=\"team\">" + tName + "</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                        tSection +
                "    <div class=\"row\" id=\"userRow\">\n" +
                "        <div id=\"userDiv\" class=\"rowCenter\">\n" +
                "            <div id=\"myPass\" class=\"sec\">\n" +
                "                <p class=\"secTitle\">Update User Password</p>\n" +
                "                <form class=\"secBody\" onsubmit=\"updateUserPass(); return false;\"  id=\"newUserPassBox\">\n" +
                "                    <label for=\"curUserPass\">Current Password:</label>\n" +
                "                    <input type=\"password\" id=\"curUserPass\" name=\"curUserPass\"/>\n" +
                "                    <label for=\"newUserPass\">New Password:</label>\n" +
                "                    <input type=\"password\" id=\"newUserPass\" name=\"newUserPass\"/>\n" +
                "                    <label for=\"confNewUserPass\">Confirm New Password:</label>\n" +
                "                    <input type=\"password\" id=\"confNewUserPass\" name=\"confNewUserPass\"/>\n" +
                "                    <button class=\"chngButton\" id=\"myPassButton\">Update Password</button>\n" +
                "                </form>\n" +
                "            </div>\n" +
                "            <div id=\"delAccount\" class=\"sec\">\n" +
                "                <p class=\"secTitle\" id=\"delTitle\">Delete Account</p>\n" +
                "                <form class=\"secBody\" onsubmit=\"delUser();return false;\" id=\"delUserBox\">\n" +
                "                    <p id=\"delWarning\">Deleting your account will permanently remove you from the competition.</p>\n" +
                "                    <label for=\"curPass\">Password:</label>\n" +
                "                    <input type=\"password\" id=\"delUserPass\" name=\"delUserPass\"/>\n" +
                "                    <button class=\"chngButton\" id=\"delButton\">Delete Account</button>\n" +
                "                </form>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>");
    }
}