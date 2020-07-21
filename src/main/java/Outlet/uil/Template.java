package Outlet.uil;

import Outlet.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * An html template for each UIL page. Just instantiate it, specify the needed parameters, and then each time someone
 * accesses the page call render(...) and the entire html page will be built AND sent using PrintWriter.
 */
public class Template {
    public final String name;   // DOES NOT include the "UIL" part
    public final String whatItIs;
    public final String rules;
    public final String practice;
    public final MCTest mcTest;
    public final FRQTest frqTest;
    public final Countdown opens;
    public final Countdown closes;
    public final short cid; // The competition id and the name of the competition's table
    public final String HEADERS;

    public String navBarHTML;   // For the competition-specific nav bar that goes underneath the header nav bar
    public String compOpenNavBar = "";   // HTML to append to navBarHTML if the competition is open
    public String scoreboardHTML;  // The scoreboard page html after the nav bars

    /**
     * The multiple choice page's html. First element is what to show if you haven't started yet,
     * second element is the multiple choice head, third element is the multiple choice body
     */
    public String[] mcHTML;
    public String[] frqHTML; // First element is what to show if you haven't started yet, second is the frq body
    public String answersHTML;  // Displays the answer sheets and testing materials.

    protected SortUILTeams sorter;    // Used to sort teams for the scoreboard

    private Competition competition;
    private final static int SCOREBOARD_UPDATE_INTERVAL = 10*60*1000;

    public Template(String n, String w, String r, String p, MCTest mc, FRQTest fr, Countdown op, Countdown cl, short cid, Competition competition){
        name = n;whatItIs = w;rules = r;practice = p;mcTest = mc;frqTest = fr;opens = op;closes = cl;this.cid = cid; this.sorter = new SortUILTeams();this.competition=competition;

        navBarHTML = "<ul id='upperHalf'><li id='nav_compname'>"+name+"</li><li onclick='showAbout();'>About</li><li onclick='showScoreboard();'>Scoreboard</li>";
        answersHTML = "<div id='answersColumn' class='column' style='display:none'><div class='row head-row'><h1>Answers</h1></div>";
        if(mc.exists) {
            mcHTML = new String[4];
            compOpenNavBar = "<li onclick='showMC();'>MC</li>";
            mcHTML[0] = "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<h1>Begin "+mcTest.NAME+"?</h1>" +
                    "<p class='subtitle'>Once you do, you will have " + mc.TIME_TEXT + " to finish.</p>" +
                    "<button id='mcBegin' onclick='beginMC()' class='chngButton'>Begin</button>" +
                    "</div>";
            mcHTML[1] = "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<h1>"+mcTest.NAME+"</h1>" +
                    "<p class='subtitle'><span>Instructions: </span>" + mcTest.INSTRUCTIONS + "<br><b>Test Packet: </b><a href='"+mcTest.TEST_LINK+"' class='link'>"+mcTest.TEST_LINK+"</a></p><div id='mcTestTimer'>";

            mcHTML[2] = "</div><table id='mcQuestions'><tr><th>#</th>";
            for(char c: mcTest.options) {
                mcHTML[2] += "<th>"+c+"</th>";
            }
            // mcHTML[2] += "<th>Skip</th><tr>";
            // short firstHalf = (short)Math.ceil(mcTest.NUM_PROBLEMS/2.0);
            for(int i=1; i<= mcTest.NUM_PROBLEMS; i++) {
                mcHTML[2] += "<tr class='mcQuestion'><td>"+i+"</td>";
                for(char c: mcTest.options){
                    mcHTML[2] +="<td><div class='mcBubble' onclick='setChoice("+i+",this)' data-val='"+c+"'></div></td>";
                }
            }
            mcHTML[2]+="<button class='chngButton' onclick='submitMC();'>Submit</button></div>";

            String answers = mcTest.ANSWERS;    // Default if the MC ANSWERS variable is not a link
            if(mcTest.ANSWERS_LINK) {   // If the variable is a link
                answers ="<a href='"+mcTest.ANSWERS+"' class='link'>"+mcTest.ANSWERS+"</a>";
            }
            answersHTML+="<div class='row'><h2>MC</h2><p><b>Test Packet: </b><a href='"+mcTest.TEST_LINK+"' class='link'>"+mcTest.TEST_LINK+"</a><br><b>Answers: </b>"+answers+"<p></div>";
        }
        frqHTML = new String[2];
        if(fr.exists) {
            compOpenNavBar += "<li onclick='showFRQ();'>FRQ</li>";
            frqHTML[0] = "<div id='frqColumn' class='column' style='display:none;'>" +
                    "<h1>Begin the "+frqTest.NAME+"?</h1>" +
                    "<p class='subtitle'>Once you do, you and your team will have " + frqTest.TIME_TEXT + " to finish.</p>" +
                    "<button id='mcBegin' onclick='beginFRQ()' class='chngButton'>Begin</button>" +
                    "</div>";
            frqHTML[1] =  "<p id='frqInst'><b>Problem Packet: </b><a href='"+frqTest.STUDENT_PACKET+"' class='link'>"+frqTest.STUDENT_PACKET+"</a><br>Choose a problem to submit:</p>" +
                        "<form id='submit' onsubmit='submitFRQ(); return false;' enctype='multipart/form-data'>" +
                        "<select id='frqProblem'>";
            for(int i=1; i<=frqTest.NUM_PROBLEMS;i++){
                frqHTML[1] += "<option value='"+i+"' id='frqProblem"+i+"'>"+frqTest.PROBLEM_MAP[i-1]+"</option>";
            }
            frqHTML[1] += "</select>" +
                    "<input type='file' accept='.java,.cpp,.py' id='frqTextfile'/>" +
                    "<button id='submitBtn' class='chngButton'>Submit</button>" +
                    "</form><p id='advice'>Confused? Review the <a href='#' class='link' onclick='showAbout();'>rules</a>.</p>";
            answersHTML+="<div class='row'><h2>FRQ</h2><p><b>Student Packet: </b><a href='"+frqTest.STUDENT_PACKET+
                    "' class='link'>"+frqTest.STUDENT_PACKET+"</a><br><b>Judge Packet: </b><a href='"+frqTest.JUDGE_PACKET+"' class='link'>"+frqTest.JUDGE_PACKET+"</a></p></div>";
        }
        answersHTML+="</div>";

        HEADERS = "<html><head><title>" + name + " - TXCSOpen</title>" +
                Dynamic.loadHeaders() +
                "<link rel='stylesheet' href='/css/uil_template.css'>" +
                "<script src='/js/uil.js'></script>" +
                "</head><body>";

        updateScoreboard();
        // Create a timer to update the scoreboard every SCOREBOARD_UPDATE_INTERVAL seconds
        /*Timer timer = new Timer();
        UpdateScoreboard updater = new UpdateScoreboard();
        updater.template = this;
        timer.schedule(updater, 0, SCOREBOARD_UPDATE_INTERVAL);
        updateScoreboard();*/
    }

    /** Renders the full page that they are on. Only called once per page visit, and sends all of the htmls hidden except
     * for the about one.
     * Also called on the client's updateNav() function, which happens if a timer runs out. In that case, returns an
     * updated version of the navigation bar.
    **/
    public void render(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User uData = Conn.getUser(request);

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        int competeStatus = getCompeteStatus(uData);
        writer.write(HEADERS+
                        Dynamic.loadNav(request) +
                getNavBarHTML(competeStatus) + "<span id='columns'>" + getColumnsHTML(uData, competeStatus) + "</span>" +
                "</body></html>"
        );
    }
    public int getCompeteStatus(User u){
        int competeStatus = 0;  // They have signed up
        if(u==null || u.token == null || !Conn.isLoggedIn(u.token)) {
            competeStatus = 1;  // They are not logged in
        } else if(u.teacher) {  // They are a teacher so they can't sign up
            competeStatus = 2;
        } else if(((Student)u).cids.containsKey(cid)) {
            competeStatus = 3;  // They have not signed up
        }
        return competeStatus;
    }
    public String getColumnsHTML(User uData, int competeStatus){
        // First, we determine whether to put a "Sign Up" button, a message saying "Your team is signed up for this
        // competition", a message saying "You must belong to a team to sign up", or a message saying
        // "you must be logged in to sign up for this competition"
        String actMessage = "<button id='signUp' onclick='signUp()'>Sign Up</button>";  // They haven't signed up yet
        if(competeStatus == 1){ // They are not logged in
            actMessage = "<h3 class='subtitle'>Log in to compete</h3>";
        } else if(competeStatus == 2) {
            actMessage = "<h3 class='subtitle'>Join a team to compete</h3>";
        } else if(competeStatus == 0) { // If they are already signed up for this competition
            actMessage = "<h3 class='subtitle'>Your team has signed up for this competition</h3>";
        }
        String about = "<div class='column' id='aboutColumn'>" +
                "<div class='row head-row'>" +
                "<h1>" + name + "</h1>" +
                actMessage + "" +
                "</div>" +
                "<div class='row'>" +
                "<h2>What it is</h2>" +
                "<p>" + whatItIs + "</p>" +
                "</div>" +
                "<div class='row'>" +
                "<h2>Rules</h2>" +
                "<p>" + rules + "</p>" +
                "</div>" +
                "<div class='row'>" +
                "<h2>Practice</h2>" +
                "<p>" + practice + "</p>" +
                "</div>" +
                "</div>";
        int status = getStatus();
        String answers = "";
        if(status == 2)  answers = answersHTML;
        return getFRQHTML(uData, competeStatus) + about + scoreboardHTML + answersHTML + getMCHTML(uData, competeStatus);
    }
    public String getMCHTML(User u, int competeStatus){
        if(competeStatus == 1) {
            return "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<h1 class='forbiddenPage'>You must be logged in to compete</h1>" +
                    "</div>";
        } else if(competeStatus == 2) { // They are a teacher
            return "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<h1 class='forbiddenPage'>Teachers cannot compete.</h1>" +
                    "</div>";
        } else if(competeStatus == 3) { // They are signed up
            return "<div id='mcColumn' class='column' style='display:none;'><div class='row'>" +
                    "<h1 class='forbiddenPage'>Sign up for this competition to compete</h1>" +
                    "<p class='subtitle' onclick='showAbout()' style='cursor:pointer'>Sign up in the <b>About</b> page</p>" +
                    "</div></div>";
        }
        Student s = (Student) u;
        UILEntry entry = s.cids.get(cid);
        if(!entry.mc.containsKey(u.uid))
            return mcHTML[0];

        if(entry.finishedMC(u.uid)) {
            return getFinishedMC(entry.mc.get(u.uid).scoringReport);
        } else
            return getRunningMC(entry.mc.get(u.uid).started);
    }
    public String getRunningMC(long started) {
        System.out.println("USER STARTED MC:"+started);
        return mcHTML[1]+mcTest.getTimer(started).toString()+mcHTML[2];
    }
    public String getFinishedMC(short[] scoringReport) {
        return "<div id='mcColumn' class='column' style='display:none;'><div class='row head-row'>" +
                "<h1>"+mcTest.NAME+": "+scoringReport[0]+"/"+mcTest.MAX_POINTS+"</h1>" +
                "<h3 class='subtitle'>"+scoringReport[1]+" correct, "+scoringReport[3]+" incorrect, "+scoringReport[2]+" skipped</h3>" +
                "</div></div>";
    }

    public String getFRQHTML(User u, int competeStatus) {
        if(!frqTest.exists) return "";
        if(competeStatus == 1) {
            return  "<div id='frqColumn' class='column' style='display:none;'>" +
                    "<h1 class='forbiddenPage'>You must be logged in to compete</h1>" +
                    "</div>";
        } else if(competeStatus == 2) {
            return "<div id='frqColumn' class='column' style='display:none;'>" +
                    "<h1 class='forbiddenPage'>You must belong to a team to compete</h1>" +
                    "</div>";
        } else if(competeStatus == 3) {
            return "<div id='frqColumn' class='column' style='display:none;'><div class='row'>" +
                    "<h1 class='forbiddenPage'>Sign up for this competition to compete</h1>" +
                    "<p class='subtitle' onclick='showAbout()' style='cursor:pointer'>Sign up in the <b>About</b> page</p>" +
                    "</div></div>";
        }

        UILEntry entry = ((Student)u).cids.get(cid);
        /*if(entry.finishedFRQ()) {
            return getFinishedFRQ(entry);
        } else if(entry.frqStarted>0) {
            return getRunningFRQ(entry);
        } else{
            return frqHTML[0];
        }*/
        return "";
    }
    public String getRunningFRQ(UILEntry entry){
        return "<script>grabFRQProblemsTimer = setInterval(function() {grabFRQProblems()}, 1000*10);</script>" +
                "<div id='frqColumn' class='column' style='display:none'><div class='row head-row running-frq'>" +
                "<div id='frqSelection'>" +
                "<h1>"+frqTest.NAME+"</h1>" +
                //"<div id='frqTimer'>"+frqTest.getTimer(entry.frqStarted)+"</div>"+
                frqHTML[1]+"</div>"+
                getFRQProblems(entry)+"</div></div>";
    }
    public String getFinishedFRQ(UILEntry entry){
        return "<div id='frqColumn' class='column' class='column' style='display:none'><div class='row head-row'>"+
                getFRQProblems(entry)+"</div></div>";
    }
    public String getFRQProblems(UILEntry entry){
        String problems = "<div id='frqProblems'><h1>Problems - " + entry.frqScore +"pts</h1>";
        for(int i=0; i<entry.frqResponses.length; i++) {
            problems+="<p>" + frqTest.PROBLEM_MAP[i] + " - ";
            short tries = entry.frqResponses[i];
            if(tries > 0) {
                problems += frqTest.calcScore(tries) + "pts";
            } else{
                problems += (tries*-1) + " tries";
            }
            problems+="</p>";
        }
        return problems += "</div>";
    }

    /**
     * Returns 0 if the competition has not yet begun, 1 if the competition is currently running,
     * and 2 if the competition is over.
     * @return
     */
    public int getStatus(){
        if(!opens.done()) return 0;
        else if(!closes.done()) return 1;
        else return 2;
    }
    public String getNavBarHTML(int competeStatus){
        int status = getStatus();
        System.out.println("Status is " + status);
        if(status == 0)
            return navBarHTML + "<li id='countdownCnt'>Competition opens in <p id='countdown'>" + opens +"</p></li></ul>";
        else if(status == 2)
            return navBarHTML + "<li id='answers' onclick='showAnswers()'>Answers</li><li id='countdownCnt'>The competition has ended!</li></ul>";
        else if(status == 1 && competeStatus == 0){
            return navBarHTML+compOpenNavBar+"<li id='countdownCnt'>Competition ends in <p id='countdown'>" + closes +"</p></li></ul>";
        }
        return navBarHTML + "<li id='countdownCnt'>Competition ends in <p id='countdown'>" + closes +"</p></li></ul>";
    }

    public void updateScoreboard(){
        ArrayList<UILEntry> allTeams;
        try {
            allTeams = competition.getAllEntries();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        Collections.sort(allTeams, sorter);


        // The table row list of teams in order of points
        String teamList = "";
        int rank = 1;
        for(UILEntry entry: allTeams) {
            entry.getMCScore();
            teamList+="<tr><td>" + rank + "</td><td>" + entry.tname + "</td><td>" + entry.affiliation + "</td>" +
                    "<td class='right'>"+((frqTest.exists&&mcTest.exists)?entry.getMCScore():"")+"</td><td class='right'>"+(frqTest.exists?entry.frqScore:entry.getMCScore())+"</td></tr>";
            rank++;
        }

        // create HTML
        scoreboardHTML = "<div class='column' id='scoreboardColumn' style='display:none;'><div class='row head-row'><h1>Scoreboard</h1>" +
                "<table id='teamList'><tr><th>#</th><th>Team</th><th>School</th><th class='right'>"+((frqTest.exists&&mcTest.exists)?"MC":"")+"</th><th class='right'>"+(frqTest.exists?"FRQ":"MC")+"</th>" +
                "</tr>" + teamList + "</table></div></div>";
    }


    /***
     * Deletes a team's entry and updates the scoreboard.
     * @param tid
     */
    public void deleteEntry(short tid) {
        System.out.println("Deleting entry");
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("DELETE FROM `c"+this.cid+"` WHERE tid=?");
            stmt.setShort(1,tid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            updateScoreboard();
            //Scoreboard.generateScoreboard();
        }
    }

    /**
     * Checks if there is a 'normals' column in this database. If there isn't, adds a 'normals' column and calculates each
     * team's normalized score.
     * return a hashmap mapping the team's tid to its normalized score.
     * @return
     */
    /*public HashMap<Short, Double> end() throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `c"+this.cid+"`");

        ResultSet rs = stmt.executeQuery();
        try {
            if(rs.next())
                rs.getDouble("normals");
        } catch (Exception e) { // In this case there is no z-score column so we add it
            stmt = conn.prepareStatement("ALTER TABLE `c"+this.cid+"` ADD COLUMN normals DOUBLE");
            stmt.executeUpdate();

            int max = 0;    // The max score of this competition
            ArrayList<UILEntry> allTeams = competition.getAllEntries();
            ArrayList<UILEntry> teams = new ArrayList<>();
            for(UILEntry entry: allTeams) {
                teams.add(entry);

                int score = entry.getScore();
                if(max < score) max = score;
            }

            HashMap<Short, Double> normalScores = new HashMap<>();
            for(UILEntry t: teams) {
                normalScores.put(t.tid, ((double)t.getScore())/max*100);
            }

            String updateMysql = "UPDATE `c"+this.cid+"` SET normals = (case ";
            for(short i:normalScores.keySet()) {
                updateMysql += "when tid = '" + i + "' then " + normalScores.get(i) + " ";
            }
            updateMysql += "end) WHERE 1=1;";

            stmt = conn.prepareStatement(updateMysql);
            stmt.executeUpdate();

            return normalScores;
        }
        rs.first();
        HashMap<Short, Double> normalScoreMap = new HashMap<>();
        while(rs.next()) {
            short sTid = rs.getShort("tid");
            double sNormals = rs.getDouble("normals");
            System.out.println("TID = "+sTid+", NORMAL="+sNormals);
            normalScoreMap.put(sTid, sNormals);
        }
        return normalScoreMap;
    }*/
}

class UpdateScoreboard extends TimerTask {
    public Template template;
    public void run() {
        template.updateScoreboard();
    }
}