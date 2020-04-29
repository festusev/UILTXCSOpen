package Outlet;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Scanner;

/***
 * Client-side library, for reusable html.
 * Created by Evan Ellis.
 */
public class Dynamic {
    public static final String SERVER_ERROR = "Whoops! A server error occurred. Contact an admin if the problem continues.";
    public static final String DATETIME_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public static SimpleDateFormat sdf;

    public static final String CNTDWNCMP_DATE = "05/7/2020 00:00:00";
    public static final String CNTDWNCMP_TO = "Until the Competition Begins";
    public static final String CNTDWNCMP_OVER = "Compete Now!";

    public static Date cntdwnToCmp;     // Automatically set in the ContextListener on startup

    public static final String DROPDOWN = "<script>" +
            "function toggleDropdownNav(){\n" +
            "   var dropdownNav = document.getElementById(\"dropdownNavList\");" +
            "   if(dropdownNav.style.display == \"none\"){dropdownNav.style.display = \"block\";} else{dropdownNav.style.display = \"none\";} \n" +
            "}</script>" +
            "<div id=\"dropdownNav\"><img src=\"res/HamburgerIcon.svg\" onclick=\"toggleDropdownNav()\"/><ul id=\"dropdownNavList\" style=\"display:none;\">";
    public static final String RIGHT_FLAIR = "<img class=\"flair\" id=\"right_flair\" src=\"res/blue_flair.svg\">";
    public static final String LEFT_FLAIR = "<img class=\"flair\" id=\"left_flair\" src=\"res/orange_flair.svg\"/>";

    public static String VIEWCOUNTER_FILE = "viewCounter.txt";
    public static BufferedWriter viewCounter;
    public static int pageViews = 0;
    public static String loadLoggedOutNav(){
        return
                "  <nav id=\"top-bar\">\n" +
                        "    <ul id=\"left-nav\">\n" +
                        "      <li class=\"nav-item\" id=\"logoCnt\">\n" +
                        "        <img src=\"./res/logo_light_uil.svg\" id=\"logo\" onclick=\"location.href='index.jsp'\"/>\n" +
                        "      </li>\n" +
                        "      <li class=\"nav-item\">\n" +
                        "        <a class=\"nav-link\" href=\"scoreboard\">Scoreboard</a>\n" +
                        "      </li>\n" +
                        "      <li class=\"nav-item\">\n" +
                        "            <a class=\"nav-link\" href=\"rules\">Rules</a>\n" +
                        "       </li>\n" +
                        "      <li class=\"nav-item\">\n" +
                        "            <a class=\"nav-link\" href=\"/\">TXCSOpen</a>\n" +
                        "       </li>\n" +
                        "    </ul>\n" +
                        "    <ul id=\"right-nav\">\n" +
                        "      <li class=\"nav-item\">\n" +
                        "        <a class=\"nav-link\" href=\"register\">Register</a>\n" +
                        "      </li>\n" +
                        "      <li class=\"nav-item\">\n" +
                        "        <a class=\"nav-link\" href=\"login\">Login</a>\n" +
                        "      </li>\n" +
                        "    </ul>\n" +
                        "  </nav>" +
                        DROPDOWN +
                        "      <li class=\"drop-nav-item\">\n" +
                        "        <a class=\"nav-link\" href=\"scoreboard\">Scoreboard</a>\n" +
                        "      </li>\n" +
                        "      <li class=\"drop-nav-item\">\n" +
                        "            <a class=\"nav-link\" href=\"rules\">Rules</a>\n" +
                        "       </li>\n" +
                        "      <li class=\"drop-nav-item\">\n" +
                        "        <a class=\"nav-link\" href=\"register\">Register</a>\n" +
                        "      </li>\n" +
                        "      <li class=\"drop-nav-item\">\n" +
                        "        <a class=\"nav-link\" href=\"login\">Login</a>\n" +
                        "      </li></ul></div>";
    }
    public static String loadNav(HttpServletRequest request){
        if(Conn.isLoggedIn(request)) return loadLoggedInNav();
        return loadLoggedOutNav();
    }
    public static String loadLoggedInNav(){
        return  "    <nav id=\"top-bar\">\n" +
                "        <ul id=\"left-nav\">\n" +
                "            <li class=\"nav-item\" id=\"logoCnt\">\n" +
                "                <img src=\"./res/logo_light_uil.svg\" id=\"logo\" onclick=\"location.href='index.jsp'\"/>\n" +
                "            </li>\n" +
                "            <li class=\"nav-item\">\n" +
                "                <a class=\"nav-link\" href=\"scoreboard\">Scoreboard</a>\n" +
                "            </li>\n" +
                "            <li class=\"nav-item\">\n" +
                "                <a class=\"nav-link\" href=\"rules\">Rules</a>\n" +
                "            </li>\n" +
                "            <li class=\"nav-item\">\n" +
                "                 <a class=\"nav-link\" href=\"/\">TXCSOpen</a>\n" +
                "            </li>\n" +
                "            <li class=\"nav-item\">\n" +
                "                <a class=\"nav-link\" href=\"submit\">Submit</a>\n" +
                "            </li>\n" +
                "            <li class=\"nav-item\">\n" +
                "                <a class=\"nav-link\" href=\"multiple-choice\">Multiple Choice</a>\n" +
                "            </li>\n" +
                "        </ul>\n" +
                "        <ul id=\"right-nav\">\n" +
                "            <li class=\"nav-item\">\n" +
                "                <a class=\"nav-link\" href=\"console\">Profile</a>\n" +
                "            </li>\n" +
                "            <li class=\"nav-item\">\n" +
                "                <a class=\"nav-link\" href=\"logout\">Logout</a>\n" +
                "            </li>\n" +
                "        </ul>\n" +
                "    </nav>" +
                DROPDOWN +
                "      <li class=\"drop-nav-item\">\n" +
                "        <a class=\"nav-link\" href=\"scoreboard\">Scoreboard</a>\n" +
                "      </li>\n" +
                "      <li class=\"drop-nav-item\">\n" +
                "            <a class=\"nav-link\" href=\"rules\">Rules</a>\n" +
                "       </li>\n" +
                "       <li class=\"drop-nav-item\">\n" +
                "            <a class=\"nav-link\" href=\"submit\">Submit</a>\n" +
                "      </li>\n" +
                "      <li class=\"drop-nav-item\">\n" +
                "        <a class=\"nav-link\" href=\"console\">Profile</a>\n" +
                "      </li>\n" +
                "      <li class=\"drop-nav-item\">\n" +
                "        <a class=\"nav-link\" href=\"logout\">Logout</a>\n" +
                "      </li></ul></div>";
    }
    // Gets the bottom bar which has the copyright notice
    public static String loadCopyright(){
        return "<div id=\"copyright_notice\">© 2020. All rights reserved. <a href=\"privacy-policy.jsp\">Privacy Policy.</a></div>";
    }
    private static String getCntdwnScript(long now){
        return "// Set the date we're counting down to\n" +
                "var countdownDate = "+(cntdwnToCmp.getTime()-now)+";" +
                "var compOpen = false;" +
                "var cntdwnLoaded = new Date().getTime();" +
                "// Update the count down every 1 second\n" +
                "var x = setInterval(function() {\n" +
                "\n" +
                "    // Get today's date and time\n" +
                "    var now = new Date().getTime();\n" +
                "    // Find the distance between now and when the competition opens\n" +
                "    var distance = countdownDate - (now-cntdwnLoaded);\n" +
                "    // Time calculations for days, hours, minutes and seconds\n" +
                "    var days = Math.floor(distance / (1000 * 60 * 60 * 24));\n" +
                "    var hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));\n" +
                "    var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));\n" +
                "    var seconds = Math.floor((distance % (1000 * 60)) / 1000);\n" +
                "\n" +
                "    // Display the result in the element with id=\"demo\"\n" +
                "    document.getElementById(\"countdown\").innerHTML = days + \"<span>d</span> \" + hours + \"<span>h</span> \"\n" +
                "        + minutes + \"<span>m</span> \" + seconds + \"<span>s</span>\";\n" +
                "\n" +
                "    if(competitionDate - (now-cntdwnLoaded)< 0){" +
                "       clearInterval(x);" +
                "       document.getElementById(\"countdownCnt\").innerHTML = \""+ CNTDWNCMP_OVER +"\";\n" +
                "    }" +
                "}, 1000);";
    }
    // Gets a countdown to whatever we are counting down to
    public static String loadCountdown() {
        // First, get the difference between now and the date we are counting down to
        String cntTo = CNTDWNCMP_TO;

        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "America/Chicago" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        long now = zdt.toInstant().toEpochMilli();

        long diff = cntdwnToCmp.getTime()-now;    // Diff in milliseconds
        String regMsg = "";
        if(competitionOpen()){  // If the competition is open
            return "<div id=\"countdownCnt\">" + CNTDWNCMP_OVER + "</div>";
        }
        int diffDays = (int) (diff / (24 * 60 * 60 * 1000));
        int diffhours = (int) (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        int diffmin = (int) ((diff % (1000 * 60 * 60)) / (1000 * 60));
        int diffsec = (int) ((diff % (1000 * 60)) / 1000);

        return "<div id=\"countdownCnt\">" +
                "<div id=\"countdownCntReg\">"+regMsg+"</div>" +
                "<p id=\"countdown\">"+diffDays+"<span>d</span> "+diffhours+"<span>h</span> "+diffmin+"<span>m</span> "+diffsec+"<span>s</span>"+"</p>" +
                "<p id=\"countdownUntil\">" + cntTo + "</p>" +
                "</div>" +
                "<script>" +
                getCntdwnScript(now) +
                "</script>";
    }

    public static String loadTimer(String timerTo, long milli, String onTimerDone, boolean includeHour) {
        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "America/Chicago" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        long now = zdt.toInstant().toEpochMilli();

        if(milli <0){  // If the timer is over
            return "";
        }
        int diffhours = (int) (milli % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        int diffmin = (int) ((milli % (1000 * 60 * 60)) / (1000 * 60));
        int diffsec = (int) ((milli % (1000 * 60)) / 1000);

        String html =  "<div id=\"countdownCnt\">" +
                    "<p id=\"countdown\">";
        String hourScript = "";
        if(includeHour){
            html+=diffhours+"<span>h</span> ";
            hourScript = "hours +\"<span>h</span> \" +";
        }
        html+=diffmin+"<span>m</span> "+diffsec+"<span>s</span>"+"</p>" +
                "<p id=\"countdownUntil\">" + timerTo + "</p>" +
                "</div>" +
                "<script>" +
                "var seconds = "+milli+";" +
                "var cntdwnLoaded;" +
                "function startTimer(){" +
                "window.cntdwnLoaded = new Date().getTime();" +
                "// Update the count down every 1 second\n" +
                "var x = setInterval(function() {\n" +
                "\n" +
                "    // Get today's date and time\n" +
                "    var now = new Date().getTime();\n" +
                "    // Find the distance between now and when the timer ends\n" +
                "    var distance = window.seconds - (now-window.cntdwnLoaded);\n" +
                "    // Time calculations for days, hours, minutes and seconds\n" +
                "    var hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));\n" +
                "    var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));\n" +
                "    var seconds = Math.floor((distance % (1000 * 60)) / 1000);\n" +
                "\n" +
                "    // Display the result in the element with id=\"demo\"\n" +
                "    document.getElementById(\"countdown\").innerHTML = " + hourScript+ "minutes+\"<span>m</span> \" + seconds + \"<span>s</span>\";\n" +
                "\n" +
                "    if(window.seconds - (now-window.cntdwnLoaded)< 0){" +
                "       clearInterval(x);" +
                onTimerDone +
                "    }" +
                "}, 1000);}</script>";
        return html;
    }

    // Returns true if the competition has now opened
    public static boolean competitionOpen(){
        Date now = new Date();
        long diff = cntdwnToCmp.getTime() - now.getTime();
        if(diff < 0) return true;
        return false;
    }

    public static String loadRightFlair(){
        return RIGHT_FLAIR;
    }
    public static String loadLeftFlair(){
        return LEFT_FLAIR;
    }

    public static String loadErrorMsg(String message) {
        return "<div class='error'>ERROR: " + message + "</div>";
    }
    public static void addPageview(){
        pageViews++;
    }
}
