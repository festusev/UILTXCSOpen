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

    protected SortUILTeams sorter;    // Used to sort teams for the scoreboard

    private final static int SCOREBOARD_UPDATE_INTERVAL = 10*60*1000;

    public Template(String n, String w, String r, String p, MCTest mc, FRQTest fr, Countdown op, Countdown cl, short cid, SortUILTeams sorter){
        name = n;whatItIs = w;rules = r;practice = p;mcTest = mc;frqTest = fr;opens = op;closes = cl;this.cid = cid; this.sorter = sorter;

        navBarHTML = "<ul id='upperHalf'><li id='nav_compname'>"+name+"</li><li onclick='showAbout();'>About</li><li onclick='showScoreboard();'>Scoreboard</li>";
        if(mc.exists) {
            mcHTML = new String[4];
            compOpenNavBar = "<li onclick='showMC();'>MC</li>";
            mcHTML[0] = "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<h1>Begin "+mcTest.NAME+"?</h1>" +
                    "<p class='subtitle'>Once you do, you will have " + mc.TIME_TEXT + " to finish.</p>" +
                    "<buttom id='mcBegin' onclick='beginMC()' class='chngButton'>Begin</button>" +
                    "</div>";
            mcHTML[1] = "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<h1>"+mcTest.NAME+"</h1>" +
                    "<p class='subtitle'><span>Instructions: </span>" + mcTest.INSTRUCTIONS + "</p><div id='mcTestTimer'>";

            mcHTML[2] = "</div><div id='mcQuestions'><ol class='mcColumn'>";
            short firstHalf = (short)Math.ceil(mcTest.NUM_PROBLEMS/2.0);
            for(int i=1; i<= firstHalf; i++) {    // Loop through the first half of questions to add answer bubbles
                mcHTML[2] += "<li class='mcQuestion'>";
                for(char c: mcTest.options){
                    mcHTML[2] +="<label for='"+i+c+"'>"+c+"</label><input type='radio' value='"+c+"' name='"+i+"' id='"+i+c+"'>";
                }
                mcHTML[2]+="</li>";
            }
            mcHTML[2]+="</ol><ol class='mcColumn' start='"+(firstHalf+1)+"'>";
            for(int i=firstHalf+1; i<=mcTest.NUM_PROBLEMS; i++) {    // Loop through the first 20 questions to add answer bubbles
                mcHTML[2] += "<li class='mcQuestion'>";
                for(char c: mcTest.options){
                    mcHTML[2] +="<label for='"+i+c+"'>"+c+"</label><input type='radio' value='"+c+"' name='"+i+"' id='"+i+c+"'>";
                }
                mcHTML[2]+="</li>";
            }
            mcHTML[2]+="</ol><button class='chngButton' onclick='submitMC();'>Submit</button></div></div>";
        }
        frqHTML = new String[2];
        if(fr.exists) {
            compOpenNavBar += "<li onclick='showFRQ();'>FRQ</li>";
            frqHTML[0] = "<div id='frqColumn' class='column' style='display:none;'>" +
                    "<h1>Begin the "+frqTest.NAME+"?</h1>" +
                    "<p class='subtitle'>Once you do, you and your team will have " + frqTest.TIME_TEXT + " to finish.</p>" +
                    "<buttom id='mcBegin' onclick='beginFRQ()' class='chngButton'>Begin</button>" +
                    "</div>";
            frqHTML[1] =  "<p id='frqInst'>Choose a problem to submit:</p>" +
                        "<form id='submit' onsubmit='submitFRQ(); return false;' enctype='multipart/form-data'>" +
                        "<select id='frqProblem'>";
            for(int i=1; i<=frqTest.NUM_PROBLEMS;i++){
                frqHTML[1] += "<option value='"+i+"' id='frqProblem"+i+"'>"+frqTest.PROBLEM_MAP[i-1]+"</option>";
            }
            frqHTML[1] += "</select>" +
                    "<input type='file' accept='.java,.cpp,.py' id='frqTextfile'/>" +
                    "<button id='submitBtn' class='chngButton'>Submit</button>" +
                    "</form><p id='advice'>Confused? Review the <a href='#' class='link' onclick='showAbout();'>rules</a>.</p>";
        }

        HEADERS = "<html><head><title>" + name + " - TXCSOpen</title>" +
                Dynamic.loadHeaders() +
                "<link rel='stylesheet' href='/css/uil_template.css'>" +
                "<script src='/js/uil.js'></script>" +
                "</head><body>";

        // Create a timer to update the scoreboard every SCOREBOARD_UPDATE_INTERVAL seconds
        Timer timer = new Timer();
        UpdateScoreboard updater = new UpdateScoreboard();
        updater.template = this;
        timer.schedule(updater, 0, SCOREBOARD_UPDATE_INTERVAL);
        updateScoreboard();
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
        } else if(u.tid <=0) {
            competeStatus = 2;  // They are logged in but have no team
        } else if(!u.team.comps.containsKey(cid)) {
            competeStatus = 3;  // They have not signed up
        }
        return competeStatus;
    }
    public String getColumnsHTML(User uData, int competeStatus){
        // First, we determine whether to put a "Sign Up" button, a message saying "Your team is signed up for this
        // competition", a message saying "You must belong to a team to sign up", or a message saying
        // "you must be logged in to sign up for this competition"
        String actMessage = "<button id='signUp' onclick='signUp()'>Sign Up</button>";
        if(competeStatus == 1){
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
        return about + scoreboardHTML + getMCHTML(uData, competeStatus) + getFRQHTML(uData, competeStatus);
    }
    public String getMCHTML(User u, int competeStatus){
        if(competeStatus == 1) {
            return "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<h1 class='forbiddenPage'>You must be logged in to compete</h1>" +
                    "</div>";
        } else if(competeStatus == 2) {
            return "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<h1 class='forbiddenPage'>You must belong to a team to compete</h1>" +
                    "</div>";
        } else if(competeStatus == 3) {
            return "<div id='mcColumn' class='column' style='display:none;'><div class='row'>" +
                    "<h1 class='forbiddenPage'>Sign up for this competition to compete</h1>" +
                    "<p class='subtitle' onclick='showAbout()' style='cursor:pointer'>Sign up in the <b>About</b> page</p>" +
                    "</div></div>";
        }
        UILEntry entry = u.team.comps.get(cid);
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

        CSEntry entry = (CSEntry) u.team.comps.get(cid);
        if(entry.finishedFRQ()) {
            return getFinishedFRQ(entry);
        } else if(entry.frqStarted>0) {
            return getRunningFRQ(entry);
        } else{
            return frqHTML[0];
        }
    }
    public String getRunningFRQ(CSEntry entry){
        return "<script>grabFRQProblemsTimer = setInterval(function() {grabFRQProblems()}, 1000*10);</script>" +
                "<div id='frqColumn' class='column' style='display:none'><div class='row head-row running-frq'>" +
                "<div id='frqSelection'>" +
                "<h1>"+frqTest.NAME+"</h1>" +
                "<div id='frqTimer'>"+frqTest.getTimer(entry.frqStarted)+"</div>"+
                frqHTML[1]+"</div>"+
                getFRQProblems(entry)+"</div></div>";
    }
    public String getFinishedFRQ(CSEntry entry){
        return "<div id='frqColumn' class='column' class='column' style='display:none'><div class='row head-row'>"+
                getFRQProblems(entry)+"</div></div>";
    }
    public String getFRQProblems(CSEntry entry){
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
        else return 1;
    }
    public String getNavBarHTML(int competeStatus){
        int status = getStatus();
        if(status == 0)
            return navBarHTML + "<li id='countdownCnt'>Competition opens in <p id='countdown'>" + opens +"</p></li></ul>";
        else if(status == 2)
            return navBarHTML + "<li id='countdownCnt'>The competition has ended!</li></ul>";
        else if(status == 1 && competeStatus == 0){
            return navBarHTML+compOpenNavBar+"<li id='countdownCnt'>Competition ends in <p id='countdown'>" + closes +"</p></li></ul>";
        }
        return navBarHTML + "<li id='countdownCnt'>Competition ends in <p id='countdown'>" + closes +"</p></li></ul>";
    }

    public void updateScoreboard(){
        ArrayList<Team> allTeams = Conn.getAllTeams();
        ArrayList<Team> teams = new ArrayList<>();
        for(Team t: allTeams) {
            if(t.comps.keySet().contains(cid)){
                teams.add(t);
            }
        }

        Collections.sort(teams, sorter);


        // The table row list of teams in order of points
        String teamList = "";
        int rank = 1;
        for(Team t: teams) {
            UILEntry entry = t.comps.get(cid);
            entry.getMCScore();
            double frqScore = 0;
            if(frqTest.exists){
                frqScore = ((CSEntry) entry).frqScore;
            }
            teamList+="<tr><td>" + rank + "</td><td>" + t.tname + "</td><td>" + t.affiliation + "</td>" +
                    "<td class='right'>"+((frqTest.exists&&mcTest.exists)?entry.getMCScore():"")+"</td><td class='right'>"+(frqTest.exists?((CSEntry) entry).frqScore:entry.getMCScore())+"</td></tr>";
            rank ++;
        }

        // create HTML
        scoreboardHTML = "<div class='column' id='scoreboardColumn' style='display:none;'><div class='row head-row'><h1>Scoreboard</h1>" +
                "<table id='teamList'><tr><th>#</th><th>Team</th><th>School</th><th class='right'>"+((frqTest.exists&&mcTest.exists)?"MC":"")+"</th><th class='right'>"+(frqTest.exists?"FRQ":"MC")+"</th>" +
                "</tr>" + teamList + "</table></div></div>";
    }


    /**
     * Checks if there is a 'normals' column in this database. If there isn't, adds a 'normals' column and calculates each
     * team's normalized score.
     * return a hashmap mapping the team's tid to its normalized score.
     * @return
     */
    public HashMap<Short, Double> end() throws SQLException {
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
            ArrayList<Team> allTeams = Conn.getAllTeams();
            ArrayList<UILEntry> teams = new ArrayList<>();
            for(Team t: allTeams) {
                if(t.comps.containsKey(this.cid)){
                    UILEntry comp = t.comps.get(this.cid);
                    teams.add(comp);

                    int score = comp.getScore();
                    if(max < score) max = score;
                }
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
            normalScoreMap.put(rs.getShort("tid"), rs.getDouble("normals"));
        }
        return normalScoreMap;
    }
}

class UpdateScoreboard extends TimerTask {
    public Template template;
    public void run() {
        template.updateScoreboard();
    }
}