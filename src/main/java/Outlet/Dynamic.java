package Outlet;

import Outlet.uil.UILEntry;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/***
 * Client-side library, for reusable html.
 * Created by Evan Ellis.
 */
public class Dynamic {
    public static final String SERVER_ERROR = "Whoops! A server error occurred. Contact an admin if the problem continues.";

    public static final String DROPDOWN = "<script>" +
            "function toggleDropdownNav(){\n" +
            "   var dropdownNav = document.getElementById(\"dropdownNavList\");" +
            "   if(dropdownNav.style.display == \"none\"){dropdownNav.style.display = \"block\";} else{dropdownNav.style.display = \"none\";} \n" +
            "}</script>" +
            "<div id=\"dropdownNav\"><div class=\"topBar\"><img src=\"/res/HamburgerIcon.svg\" onclick=\"toggleDropdownNav()\" id=\"hamburger\"/><img src=\"/res/logo_dark_texasless.svg\" class=\"logo\" onclick=\"location.href='/'\"/></div><ul id=\"dropdownNavList\" style=\"display:none;\">";

    private static String announcement = ""; // The announcement pinned underneath the top bar

    public static void setAnnouncement(String stmt){
        if(stmt != null && !stmt.isEmpty())
            announcement = "<div id=\"announcement\">" + stmt + "</div>";
        else
            announcement = "";
    }
    public static String loadHeaders(){
        return "  <meta charset=\"utf-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "  <link rel=\"icon\" type=\"image/png\" href=\"/res/icon.png\">\n" +
                "  <link rel=\"stylesheet\" href=\"/css/style2.css\">" +
                "  <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>" +
                "  <script src=\"/js/websocket.js\"></script>";
    }
    public static String loadLoggedOutNav(){
        return  "    <ul id=\"top-bar\">\n" +
                "            <li class=\"nav-item\" id=\"logoCnt\">\n" +
                "                <img draggable=\"false\" src=\"/res/logo_dark_texasless.svg\" id=\"logo\" onclick=\"location.href='/'\"/>\n" +
                "      <li class=\"nav-item rightNav\">\n" +
                "        <a class=\"nav-link\" href=\"/login\">Login</a>\n" +
                "      </li>\n" +
                "      <li class=\"nav-item rightNav\">\n" +
                "        <a class=\"nav-link\" href=\"/register\">Register</a>\n" +
                "      </li>\n" +
                "        </ul>\n" +
                DROPDOWN +
                "      <li class=\"drop-nav-item\">\n" +
                "        <a class=\"nav-link\" href=\"/register\">Register</a>\n" +
                "      </li>\n" +
                "      <li class=\"drop-nav-item\">\n" +
                "        <a class=\"nav-link\" href=\"/login\">Login</a>\n" +
                "      </li>" +
                "       </ul></div>" +announcement;
    }
    public static String loadNav(HttpServletRequest request){
        if(Conn.isLoggedIn(request)) return loadLoggedInNav();
        return loadLoggedOutNav();
    }
    public static String loadLoggedInNav(){
        return  "    <ul id=\"top-bar\">\n" +
                "            <li class=\"nav-item\" id=\"logoCnt\">\n" +
                "                <img draggable=\"false\" src=\"/res/logo_dark_texasless.svg\" id=\"logo\" onclick=\"location.href='/'\"/>\n" +
                "            <li class=\"nav-item rightNav\">\n" +
                "                <a class=\"nav-link\" href=\"/logout\">Logout</a>\n" +
                "            </li>\n" +
                "            <li class=\"nav-item rightNav\">\n" +
                "                <a class=\"nav-link\" href=\"/console/competitions\">Profile</a>\n" +
                "            </li>\n" +
                "        </ul>\n" +
                DROPDOWN +
                "      <li class=\"drop-nav-item\">\n" +
                "        <a class=\"nav-link\" href=\"/console/competitions\">Profile</a>\n" +
                "      </li>\n" +
                "      <li class=\"drop-nav-item\">\n" +
                "        <a class=\"nav-link\" href=\"/logout\">Logout</a>\n" +
                "      </li></ul></div>" + announcement;
    }
    // Gets the bottom bar which has the copyright notice
    public static String loadCopyright(){
        return "<div id=\"copyright_notice\">© 2020. All rights reserved. <a href=\"/privacy-policy.jsp\">Privacy Policy.</a></div>";
    }
    public static String loadBigCopyright(){
        return "<div id=\"big_copyright\"><div id=\"copyright_contact\"><a href=\"https://twitter.com/TXCSOpen\"><img src=\"/res/contact/twitter.png\"/></a><a href=\"https://discord.gg/ukT4QnZ\"><img src=\"/res/contact/discord.png\"/></a><a href=\"mailto:contact@txcsopen.com\"><img src=\"/res/contact/gmail.png\"/></a></div><div id=\"copyright_privacy\"><a href=\"/privacy-policy.jsp\">Privacy Policy</a><a href=\"https://forms.gle/eKJSTFn7BTp6Gu538\">Report an Issue</a></div><div id=\"copyright_notice\">© 2020. All rights reserved.</div></div>";
    }

    /***
     * page is 0, 1, 2, 3, or 4, corresponding to upcoming, competitions, class, help, and profile respectively
     * @param page
     * @param content
     * @return
     */
    public static String get_consoleHTML(int page, String content, User user) {
        String leftBar = "<div id='left-bar'>" +
                            "<img src='/res/icon.png' id='tx'>";
        if(!user.temp)      leftBar += "<a href='/console/competitions' class='tooltip-cnt'>";
        else {  // Instead, have the competition trophy link to the competition this user is signed up for
            Student student = (Student) user;
            UILEntry entry = (UILEntry) student.cids.values().toArray()[0];
            leftBar += "<a href='/console/competitions?cid="+entry.competition.template.cid+"' class='tooltip-cnt'>";
        }
        leftBar +=          "<img src='/res/console/champion-winner-trophy.svg' "+(page==1?"class='selected'":"")+"/>" +
                            "<p class='tooltip'>Competitions</p></a>";
        if(!user.temp)      leftBar+="<a href='/console/class' class='tooltip-cnt'>" +
                            "<img src='/res/console/graduation-cap.svg' "+(page==2?"class='selected'":"")+"/>" +
                            "<p class='tooltip'>Class</p></a>";
        leftBar +=          "<a href='/console/help' class='tooltip-cnt' id='nav-help'>" +
                            "<img src='/res/console/question-mark-line.svg' "+(page==3?"class='selected'":"")+"/>" +
                            "<p class='tooltip'>Help</p></a><div id='nav-profile-cnt'>";
        if(user.temp)   leftBar+= "<a>";
        else            leftBar+= "<a href='/console/profile'>";

        leftBar += "<img src='/res/console/profile.svg' id='nav-profile' "+(page==4?"class='selected'":"")+"/></a><div>";
        if(!user.temp)  leftBar += "<a href='/console/profile' id='nav-my-settings'>My Settings</a>";
        leftBar += "<a href='/logout'>Logout</a></div></div></div><div id='right'>" + content + "</div>";
        return leftBar;
    }
}
