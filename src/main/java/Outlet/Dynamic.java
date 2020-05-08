package Outlet;

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
    public static final String DATETIME_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public static SimpleDateFormat sdf;

    public static final String CNTDWNCMP_DATE = "04/7/2020 00:00:00";
    public static final String CNTDWNCMP_TO = "Until the Competition Begins";
    public static final String CNTDWNCMP_OVER = "Compete Now!";

    public static Date cntdwnToCmp;     // Automatically set in the ContextListener on startup

    public static final String CNTDWNMCENDS_DATE = "04/8/2020 00:00:00";    // The countdown to when the multiple choice closes
    public static Date cntdwnToMCOver;

    public static final String CNTDWNCMPENDS_DATE = "05/9/2020 00:00:00";    // The countdown to when the multiple choice closes
    public static Date cntdwnToCMPOver;

    public static final String DROPDOWN = "<script>" +
            "function toggleDropdownNav(){\n" +
            "   var dropdownNav = document.getElementById(\"dropdownNavList\");" +
            "   if(dropdownNav.style.display == \"none\"){dropdownNav.style.display = \"block\";} else{dropdownNav.style.display = \"none\";} \n" +
            "}</script>" +
            "<div id=\"dropdownNav\"><img src=\"res/HamburgerIcon.svg\" onclick=\"toggleDropdownNav()\"/><ul id=\"dropdownNavList\" style=\"display:none;\">";
    public static final String RIGHT_FLAIR = "<img class=\"flair\" id=\"right_flair\" src=\"res/blue_flair.svg\">";
    public static final String LEFT_FLAIR = "<img class=\"flair\" id=\"left_flair\" src=\"res/orange_flair.svg\"/>";

    private static String announcement = ""; // The announcement pinned underneath the top bar

    private static final String GA_URL = "https://www.google-analytics.com/collect";

    public static void setAnnouncement(String stmt){
        if(stmt != null && !stmt.isEmpty())
            announcement = "<div id=\"announcement\">" + stmt + "</div>";
        else
            announcement = "";
    }
    public static String loadLoggedOutNav(HttpServletRequest request, String pageName){
        addPageView(request, pageName);
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
                        "      </li></ul></div>" +announcement;
    }
    public static String loadNav(HttpServletRequest request, String pageName){
        if(Conn.isLoggedIn(request)) return loadLoggedInNav(request, pageName);
        return loadLoggedOutNav(request, pageName);
    }
    public static String loadLoggedInNav(HttpServletRequest request, String pageName){
        addPageView(request, pageName);
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
                "                <a class=\"nav-link\" href=\"programming\">Programming</a>\n" +
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
                "            <a class=\"nav-link\" href=\"programming\">Programming</a>\n" +
                "      </li>\n" +
                "      <li class=\"drop-nav-item\">\n" +
                "        <a class=\"nav-link\" href=\"console\">Profile</a>\n" +
                "      </li>\n" +
                "      <li class=\"drop-nav-item\">\n" +
                "        <a class=\"nav-link\" href=\"logout\">Logout</a>\n" +
                "      </li></ul></div>" + announcement;
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
                "    if(countdownDate - (now-cntdwnLoaded)< 0){" +
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
                "       document.getElementById(\"countdownUntil\").innerHTML = \"Times Up!\";" +
                "       document.getElementById(\"countdown\").innerHTML = \"0<span>m</span> 0<span>s</span>\";" +
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

    /**
     * Returns 0 if the MC section hasn't begun, 1 if it is currently open, and 2 if it has closed.
     * @return
     */
    public static int mcOpen(){
        Date now = new Date();
        long time = now.getTime();
        long endsDiff = cntdwnToMCOver.getTime() - time;  // The difference between now and when the competition ends
        long startsDiff = cntdwnToCmp.getTime() - time; // The difference between now and when the multiple choice ends
        if(startsDiff > 0) return 0;    // It hasn't started
        else if(endsDiff <0) return 2;  // It is over
        return 1;   //  It is open
    }

    public static int frqOpen(){
        Date now = new Date();
        long time = now.getTime();
        long endsDiff = cntdwnToCMPOver.getTime() - time;  // The difference between now and when the competition ends
        long startsDiff = cntdwnToMCOver.getTime() - time; // The difference between now and when the multiple choice ends
        if(startsDiff > 0) return 0;    // It hasn't started
        else if(endsDiff <0) return 2;  // It is over
        return 1;   //  It is open
    }

    public static String loadRightFlair(){
        return RIGHT_FLAIR;
    }
    public static String loadLeftFlair(){
        return LEFT_FLAIR;
    }

    public static void addPageView(HttpServletRequest request, String pageName){
        /*User u = Conn.getUser(request);
        short id = u == null ? Short.MAX_VALUE: u.uid;
        try {
            String urlParameters = "v=1&tid=UA-143422338-1&cid="+id+"&t=pageview&dp=%2F"+request.getHeader("referer")+"&ds=server&aip=1&uip="+getClientIpAddr(request)+"&ua="+ URLEncoder.encode(request.getHeader("User-Agent"),"UTF-8")+"&z="+(int)(Math.random()*10000);
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            URL myURL = new URL(GA_URL);
            HttpsURLConnection gaConn = (HttpsURLConnection) myURL.openConnection();

            gaConn.setDoInput(true);
            gaConn.setDoOutput(true);
            gaConn.setRequestMethod("POST");
            gaConn.setRequestProperty("User-Agent", "Java client");
            gaConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            DataOutputStream wr = new DataOutputStream(gaConn.getOutputStream());
            wr.write(postData);

            StringBuilder content;

            BufferedReader in = new BufferedReader(new InputStreamReader(gaConn.getInputStream()));

            String line;
            content = new StringBuilder();

            while ((line = in.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
    //Credit to https://gist.github.com/c0rp-aubakirov/a4349cbd187b33138969
    public static String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
