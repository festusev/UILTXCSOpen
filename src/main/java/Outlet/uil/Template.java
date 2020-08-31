package Outlet.uil;

import Outlet.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * An html template for each UIL page. Just instantiate it, specify the needed parameters, and then each time someone
 * accesses the page call render(...) and the entire html page will be built AND sent using PrintWriter.
 */
public class Template {
    public final String name;   // DOES NOT include the "UIL" part
    public final String description;
    public final MCTest mcTest;
    public final FRQTest frqTest;
    public final boolean mcFirst;   // Whether the mcTest is first.
    public final short cid; // The competition id and the name of the competition's table
    public final String HEADERS;
    public final String MC_HEADER = "<li onclick='showMC();'>Written</li>";
    public final String FRQ_HEADER = "<li onclick='showFRQ();'>Hands-On</li>";

    public String navBarHTML;   // For the competition-specific nav bar that goes underneath the header nav bar
    public String scoreboardHTML;  // The scoreboard page html after the nav bars

    public Countdown opens;
    public Countdown closes;

    /**
     * The multiple choice page's html. First element is what to show if you haven't started yet,
     * second element is the multiple choice head, third element is the multiple choice body
     */
    public String[] mcHTML;
    public String frqHTML; // First element is what to show if you haven't started yet, second is the frq body
    // public String answersHTML;  // Displays the answer sheets and testing materials.

    ArrayList<Short> sortedTeams = new ArrayList<>();   // array of tids
    protected SortUILTeams sorter;    // Used to sort teams for the scoreboard

    private Competition competition;

    public Template(String n, String description, MCTest mc, FRQTest fr, short cid, Competition competition){
        name = n;this.description = description;mcTest = mc;frqTest = fr;this.cid = cid; this.sorter = new SortUILTeams();this.competition=competition;

        if(mcTest.exists && !frqTest.exists) {
            mcFirst = true;
            opens = mcTest.opens;
            closes = mcTest.closes;
        } else if(!mcTest.exists && frqTest.exists) {
            mcFirst = false;
            opens = frqTest.opens;
            closes = frqTest.closes;
        } else {
            long difference = fr.opens.date.getTime() - mc.opens.date.getTime();
            if (difference > 0) {
                mcFirst = true;
                opens = mcTest.opens;
                closes = frqTest.closes;
            } else {
                mcFirst = false;
                opens = mcTest.opens;
                closes = frqTest.closes;
            }
        }

        navBarHTML = "<ul id='upperHalf'><li id='nav_compname' onclick='location.href=\"/uil\"' style='cursor:pointer'>"+name+"</li><li onclick='showAbout();'>About</li><li onclick='showScoreboard();'>Scoreboard</li>";
        // answersHTML = "<div id='answersColumn' class='column' style='display:none'><div class='row head-row'><h1>Answers</h1></div>";
        if(mc.exists) {
            mcHTML = new String[2];
            /*mcHTML[0] = "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<h1>Begin "+mcTest.NAME+"?</h1>" +
                    "<p class='subtitle'>Once you do, you will have " + mc.TIME_TEXT + " to finish.</p>" +
                    "<button id='mcBegin' onclick='beginMC()' class='chngButton'>Begin</button>" +
                    "</div>";*/
            mcHTML[0] = "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<h1>"+mcTest.NAME+"</h1>" +
                    "<p class='subtitle'><span>Instructions: </span>" + mcTest.INSTRUCTIONS + "<br><b>Test Packet: </b>" +
                    "<a href='"+mcTest.TEST_LINK+"' class='link'>link</a></p><div id='mcTestTimer'>";

            mcHTML[1] = "</div><button class='chngButton' onclick='submitMC();'>Submit</button><table id='mcQuestions'><tr><th>#</th>";
            for(char c: mcTest.options) {
                mcHTML[1] += "<th>"+c+"</th>";
            }
            // mcHTML[2] += "<th>Skip</th><tr>";
            // short firstHalf = (short)Math.ceil(mcTest.NUM_PROBLEMS/2.0);
            for(int i=1; i<= mcTest.NUM_PROBLEMS; i++) {
                mcHTML[1] += "<tr class='mcQuestion'><td>" + i + "</td>";
                if (mcTest.KEY[i - 1][1].equals("0")) {   // This is a MC problem
                    for (char c : mcTest.options) {
                        mcHTML[1] += "<td><div class='mcBubble' onclick='setChoice(" + i + ",this)' data-val='" + c + "'></div></td>";
                    }
                } else {    // This is an SAQ problem
                    mcHTML[1] += "<td colspan='5'><input type='text' class='mcText' onchange='setSAQChoice(" + i + ",this)'></td>";
                }
            }
            mcHTML[1]+="</table></div>";

            // answersHTML+="<div class='row'><h2>MC</h2><p><b>Test Packet: </b><a href='"+mcTest.TEST_LINK+"' class='link'>link</a><br><b>Answers: </b>"+answers+"<p></div>";
        }
        frqHTML = "";
        if(fr.exists) {
            frqHTML =  "<p id='frqInst'><b>Problem Packet: </b><a href='"+frqTest.STUDENT_PACKET+"' class='link'>link</a><br>Choose a problem to submit:</p>" +
                        "<form id='submit' onsubmit='submitFRQ(); return false;' enctype='multipart/form-data'>" +
                        "<select id='frqProblem'>";
            for(int i=1; i<=frqTest.PROBLEM_MAP.length;i++){
                frqHTML += "<option value='"+i+"' id='frqProblem"+i+"'>"+frqTest.PROBLEM_MAP[i-1]+"</option>";
            }
            frqHTML += "</select>" +
                    "<input type='file' accept='.java,.cpp,.py' id='frqTextfile'/>" +
                    "<button id='submitBtn' class='chngButton'>Submit</button>" +
                    "</form><p id='advice'>Confused? Review the <a href='#' class='link' onclick='showAbout();'>rules</a>.</p>";
            // answersHTML+="<div class='row'><h2>FRQ</h2><p><b>Student Packet: </b><a href='"+frqTest.STUDENT_PACKET+
            //         "' class='link'>link</a><br><b>Judge Packet: </b><a href='"+frqTest.JUDGE_PACKET+"' class='link'>link</a></p></div>";
        }
        // answersHTML+="</div>";

        HEADERS = "<html><head><title>" + name + " - TXCSOpen</title>" +
                Dynamic.loadHeaders() +
                "<link rel='stylesheet' href='/css/uil_template.css'>" +
                "<script src='/js/uil.js'></script>" +
                "</head><body>";

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
        System.out.println(">>>> RENDERING");
        User uData = UserMap.getUserByRequest(request);

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        CompetitionStatus competitionStatus = new CompetitionStatus(mcTest, frqTest);
        UserStatus userStatus = UserStatus.getCompeteStatus(uData, cid);
        writer.write(HEADERS+
                        Dynamic.loadNav(request) +
                getNavBarHTML(userStatus, competitionStatus) + "<span id='columns'>" + getColumnsHTML(uData, userStatus, competitionStatus) + "</span>" +
                "</body></html>"
        );
    }

    /***
     * Gets a minified summary of the competition including name, date, who created it, and if you have signed up.
     * @param u
     * @return
     */
    public String getMiniHTML(User u)
    {
        String signupColor = "green";
        String signupText = "Sign Up";
        if(u == null || u.teacher) {
            signupText = "";
        } else if(((Student)u).cids.containsKey(cid)){
            signupColor = "grey";
            signupText = "Signed up";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(opens.date);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return "<div class='competition' onclick='location.href=\"/uil?cid="+cid+"\"'>" +
                "<div class='row1'>"+StringEscapeUtils.escapeHtml4(name)+"<p class='right' style='color:"+signupColor+"'>"+signupText+"</p></div>" +
                "<div class='row2'>Created by "+StringEscapeUtils.escapeHtml4(competition.teacher.fname) + " " + StringEscapeUtils.escapeHtml4(competition.teacher.lname) +
                "<p class='right'>"+month+"/"+day+"</p></div>";
    }

    public String getColumnsHTML(User uData, UserStatus userStatus, CompetitionStatus competitionStatus){
        // First, we determine whether to put a "Sign Up" button, a message saying "Your team is signed up for this
        // competition", a message saying "You must belong to a team to sign up", or a message saying
        // "you must be logged in to sign up for this competition"
        String actMessage = "<button id='signUp' onclick='showSignup()'>Sign Up</button><div id='signUpBox' style='display:none'><div class='center'><h1>Join Team</h1>" +
                "<p id='errorBoxERROR'></p><p class='instruction'>Enter team join code:</p><input name='teamCode' id='teamCode' oninput='codeEntered(this)' maxlength='6'>" +
                "<p id='toggleCreateTeam' onclick='toggleCreateTeam()'>or create a new team.</p></div></div>";  // They haven't signed up yet
        if(!userStatus.loggedIn){ // They are not logged in
            actMessage = "<h3 class='subtitle'>Log in to compete</h3>";
        } else if(userStatus.teacher) {
            actMessage = "<h3 class='subtitle'>Teacher's cannot compete</h3>";
        } else if(userStatus.signedUp) { // If they are already signed up for this competition
            actMessage = "<h3 class='subtitle'>You have signed up for this competition</h3>";
        }
        String about = "<div class='column' id='aboutColumn'>" +
                "<div class='row head-row'>" +
                "<h1>" + StringEscapeUtils.escapeHtml4(name) + "</h1>" +
                actMessage + "" +
                "</div>" +
                "<div class='row'>" +
                //"<h2>What it is</h2>" +
                "<p>" + StringEscapeUtils.escapeHtml4(description) + "</p>" +
                "</div>" +
                /*"<div class='row'>" +
                "<h2>Rules</h2>" +
                "<p>" + StringEscapeUtils.escapeHtml4(rules) + "</p>" +
                "</div>" +
                /*"<div class='row'>" +
                "<h2>Practice</h2>" +
                "<p>" + practice + "</p>" +
                "</div>" +*/
                "</div>";
        // String answers = "";
        // if(competitionStatus.mcFinished && competitionStatus.frqFinished)  answers = answersHTML;
        return getFRQHTML(uData, userStatus, competitionStatus) + about + scoreboardHTML + /*answers +*/
                getMCHTML(uData, userStatus, competitionStatus) + getTeamHTML(uData, userStatus);
    }

    /***
     * Converts a number to its ordinal version. 1 goest to 1st, 2 goes to 2nd, etc...
     * @param i
     * @return
     */
    public static String ordinal(int i) {
        String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];

        }
    }

    public String getTeamHTML(User u, UserStatus userStatus) {
        if(!userStatus.signedUp) return "";    // They don't belong to a team

        UILEntry team = ((Student)u).cids.get(cid);
        String html = "<div id='teamColumn' class='column' style='display:none;'><p id='teamName'>"+StringEscapeUtils.escapeHtml4(team.tname)+"<span>"+
                ordinal(sortedTeams.indexOf(team.tid)+1)+"</span></p>" +
                "<p id='teamJoinCode'>Join Code: "+team.password+"</p><div id='teamMembers'><b>Members:</b><ul>";
        for(short uid: team.uids) {
            Student student = StudentMap.getByUID(uid);
            html+="<li>"+StringEscapeUtils.escapeHtml4(student.fname)+" "+StringEscapeUtils.escapeHtml4(student.lname);
            if(uid == u.uid && team.notStarted()) html += "<span onclick='leaveTeam()' id='leaveTeam'>Leave</span>";
            /*if(mcTest.exists) { // List the student's mc score
                if(team.mc.containsKey(student.uid))
                    html+= " - " + team.mc.get(student.uid).scoringReport[0];
                else
                    html+=" - Hasn't taken mc";
            }*/
            html+="</li>";

        }
        html+="</ul></div>";
        /*if(frqTest.exists) {
            html+="<div id='frqProblems'>";
        }*/

        return html+"</div>";
    }
    public String getMCHTML(User u, UserStatus userStatus, CompetitionStatus competitionStatus){
        if(!mcTest.exists || competitionStatus.mcBefore) return "";

        if(userStatus.signedUp && userStatus.loggedIn && !userStatus.teacher) {
            Student s = (Student) u;
            UILEntry entry = s.cids.get(cid);
            /*if (!entry.mc.containsKey(u.uid)) {
                return mcHTML[0];
            } else*/
            if (entry.finishedMC(u.uid)) {
                return getFinishedMC(entry.mc.get(u.uid));
            } else {
                return getRunningMC();
            }
        } else if(userStatus.teacher && userStatus.creator) {  // This is the teacher who made this competition
            String html =  "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<div class='row head-row'>" +
                    "<h1>Written</h1>" +
                    "</div>" +
                    "<div class='row'>" +
                    "<p>Test Packet: <a class='link' href='" + mcTest.TEST_LINK + "'>link</a></p>" +
                    "<p><b>Submissions:</b></p>";

            html += "<table id='mcSubmissions'><tr><th>Name</th><th>Team</th><th>Score</th></tr>";

            for(UILEntry entry: competition.entries.tidMap.values()) {
                Set<Short> uids = entry.mc.keySet();
                for (short uid : uids) {
                    MCSubmission submission = entry.mc.get(uid);
                    if (submission != null) {
                        Student student = StudentMap.getByUID(uid);
                        html += "<tr><td>" + StringEscapeUtils.escapeHtml4(student.fname + " " + student.lname) +
                                "</td><td>" + StringEscapeUtils.escapeHtml4(entry.tname) + "</td><td>" + submission.scoringReport[0] +
                                "</td></tr>";
                    }
                }
            }
            html += "</table></div></div>";

            return html;
        } else if(competitionStatus.mcFinished) {
            String html =  "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<div class='row head-row'>" +
                    "<h1>Written</h1>" +
                    "</div>" +
                    "<div class='row'>" +
                    "<p>Test Packet: <a class='link' href='" + mcTest.TEST_LINK + "'>link</a></p>" +
                    "<p>Answer Key</p>";

            html += "<table id='mcQuestions'><tr><th>#</th>";
            for(char c: mcTest.options) {
                html += "<th>"+c+"</th>";
            }
            html += "</tr>";
            for(int i=1; i<= mcTest.NUM_PROBLEMS; i++) {
                html += "<tr class='mcQuestion'><td>" + i + "</td>";
                if (mcTest.KEY[i - 1][1].equals("0")) {   // This is a MC problem
                    for (char c : mcTest.options) {
                        String className = "";
                        if(mcTest.KEY[i-1][0].charAt(0) == c) className = "mcSelected";

                        html += "<td><div class='mcBubble "+className+"' data-val='" + c + "' style='cursor:unset'></div></td>";
                    }
                } else {    // This is an SAQ problem
                    html += "<td colspan='5'><input type='text' class='mcText'><p class='mcTextCorrectAnswer'>"+
                            mcTest.KEY[i-1][0]+"</p></td>";
                }
            }
            html += "</table></div></div>";

            return html;
        } else {
            if(!userStatus.loggedIn) {
                return "<div id='mcColumn' class='column' style='display:none;'>" +
                        "<h1 class='forbiddenPage'>You must be logged in to compete</h1>" +
                        "</div>";
            } else if(userStatus.teacher) { // They are a teacher
                return "<div id='mcColumn' class='column' style='display:none;'>" +
                        "<h1 class='forbiddenPage'>Teachers cannot compete.</h1>" +
                        "</div>";
            } else { // They are not signed up
                if(!opens.done()) {
                    return "<div id='mcColumn' class='column' style='display:none;'><div class='row'>" +
                            "<h1 class='forbiddenPage'>Sign up for this competition to compete</h1>" +
                            "<p class='subtitle' onclick='showAbout()' style='cursor:pointer'>Sign up in the <b>About</b> page</p>" +
                            "</div></div>";
                } else {    // You can't sign up if the competition has begun
                    return "<div id='mcColumn' class='column' style='display:none;'>" +
                            "<h1 class='forbiddenPage'>Sign up has closed.</h1>" +
                            "</div>";
                }
            }
        }
    }
    public String getRunningMC() {
        return mcHTML[0]+mcTest.getTimer().toString()+mcHTML[1];
    }
    public String getFinishedMC(MCSubmission submission) {
        String html =  "<div id='mcColumn' class='column' style='display:none;'>" +
                        "<div class='row head-row'>" +
                        "<h1>Written</h1>" +
                        "<h3>"+submission.scoringReport[0]+"/"+mcTest.MAX_POINTS+"</h3>" +
                        "</div>" +
                        "<div class='row'>" +
                        "<p>Test Packet: " + mcTest.TEST_LINK + "</p>" +
                        "<p>Correct: "+submission.scoringReport[1]+"</p>" +
                        "<p>Incorrect: "+submission.scoringReport[3]+"</p>" +
                        "<p>Skipped: "+submission.scoringReport[2]+"</p><br>" +
                        "<p>Scoring Report</p>";

        html += "<table id='mcQuestions'><tr><th>#</th>";
        for(char c: mcTest.options) {
            html += "<th>"+c+"</th>";
        }
        html += "</tr>";
        for(int i=1; i<= mcTest.NUM_PROBLEMS; i++) {
            html += "<tr class='mcQuestion'><td>" + i + "</td>";
            String answer = submission.answers[i-1];
            if (mcTest.KEY[i - 1][1].equals("0")) {   // This is a MC problem
                for (char c : mcTest.options) {
                    String className = "";
                    if ((""+c).equals(answer)) className = "mcSelected";
                    else if(mcTest.KEY[i-1][0].charAt(0) == c) className = "mcCorrectAnswer";

                    html += "<td><div class='mcBubble "+className+"' data-val='" + c + "' style='cursor:unset'></div></td>";
                }
            } else {    // This is an SAQ problem
                String correctAnswer = "";
                if(!answer.equals(mcTest.KEY[i-1][0])) {
                    correctAnswer = "<p class='mcTextCorrectAnswer'>"+ mcTest.KEY[i-1][0]+"</p>";
                }
                html += "<td colspan='5'><input type='text' class='mcText' value='"+answer+"'>"+correctAnswer+"</td>";
            }
        }
        html += "</table></div></div>";

        return html;
    }

    public String getFRQHTML(User u, UserStatus userStatus, CompetitionStatus competitionStatus) {
        if(!frqTest.exists || competitionStatus.frqBefore) return "";

        if(userStatus.teacher) {
            if(userStatus.creator) {   // This is the teacher who created this competition
                String html =  "<div id='frqColumn' class='column frqSubmissionList' style='display:none;'>" +
                        "<div style='flex-grow:1' id='frqSubmissions'><div class='row head-row'>" +
                        "<h1>Hands-On</h1>" +
                        "</div>" +
                        "<div class='row'>" +
                        "<p>Test Packet: <a class='link' href='" + frqTest.STUDENT_PACKET + "'>link</a></p>" +
                        "<p><b>Submissions:</b></p>";

                html += "<table><tr><th>Problem</th><th>Team</th><th>Result</th></tr>";

                String rows = "";
                for(int i=0, j=competition.frqSubmissions.size(); i<j; i++) {
                    FRQSubmission submission = competition.frqSubmissions.get(i);
                    rows = "<tr onclick='showFRQSubmission("+i+")'><td>" + StringEscapeUtils.escapeHtml4(frqTest.PROBLEM_MAP[submission.problemNumber-1]) +
                            "</td><td>" + StringEscapeUtils.escapeHtml4(submission.entry.tname) + "</td><td id='showFRQSubmission"+i+"'>" + submission.getResultString() +
                            "</td></tr>" + rows;
                }
                html += rows + "</table></div></div></div>";

                return html;
            } else return "<div id='frqColumn' class='column' style='display:none;'><h1 class='forbiddenPage'>Teachers cannot compete.</h1></div>";
        } else if(!userStatus.loggedIn) {
            return  "<div id='frqColumn' class='column' style='display:none;'>" +
                    "<h1 class='forbiddenPage'>You must be logged in to compete</h1>" +
                    "</div>";
        } else if(!userStatus.signedUp) {
            if(!opens.done()) {
                return "<div id='frqColumn' class='column' style='display:none;'><div class='row'>" +
                        "<h1 class='forbiddenPage'>Sign up for this competition to compete</h1>" +
                        "<p class='subtitle' onclick='showAbout()' style='cursor:pointer'>Sign up in the <b>About</b> page</p>" +
                        "</div></div>";
            } else {
                return "<div id='frqColumn' class='column' style='display:none;'>" +
                        "<h1 class='forbiddenPage'>Sign up has closed.</h1>" +
                        "</div>";
            }
        } else {
            UILEntry entry = ((Student) u).cids.get(cid);
            if (competitionStatus.frqFinished) {
                return getFinishedFRQ(entry);
            } else if (competitionStatus.frqDuring) {
                return getRunningFRQ(entry);
            } else return "";   // This shouldn't happen
        }
    }
    public String getRunningFRQ(UILEntry entry){
        return "<script>grabFRQProblemsTimer = setInterval(function() { QProblems()}, 1000*10);</script>" +
                "<div id='frqColumn' class='column' style='display:none'><div class='row head-row running-frq'>" +
                "<div id='frqSelection'>" +
                "<h1>"+StringEscapeUtils.escapeHtml4(frqTest.NAME)+"</h1>" +
                "<div id='frqTimer'>"+frqTest.getTimer()+"</div>"+
                frqHTML+"</div>"+
                getFRQProblems(entry)+"</div></div>";
    }
    public String getFinishedFRQ(UILEntry entry){
        return "<div id='frqColumn' class='column' class='column' style='display:none'><div class='row head-row'>"+
                getFRQProblems(entry)+"</div></div>";
    }
    public String getFRQProblems(UILEntry entry){
        String problems = "<div id='frqProblems'><h1>Problems - " + entry.frqScore +"pts</h1>";
        for(int i=0; i<entry.frqResponses.length; i++) {
            problems+="<p>" + StringEscapeUtils.escapeHtml4(frqTest.PROBLEM_MAP[i]) + " - ";
            short tries = entry.frqResponses[i];
            if(tries > 0) {
                problems += frqTest.calcScore(tries) + "pts";
            } else{
                problems += (tries*-1) + " tries";
            }
            problems+="</p>";
        }
        return problems + "</div>";
    }

    public String getNavBarHTML(UserStatus userStatus, CompetitionStatus competitionStatus){
        String nav = navBarHTML;
        if(userStatus.signedUp) {
            nav += "<li id='team' onclick='showTeam()'>Team</li>";
        }
        if((!frqTest.exists || competitionStatus.frqBefore) && (!mcTest.exists || competitionStatus.mcBefore)) {
            if(mcFirst) return nav + "<li id='countdownCnt'>Written opens in <p id='countdown'>" + mcTest.opens + "</p></li></ul>";
            else return nav + "<li id='countdownCnt'>Hands-On opens in <p id='countdown'>" + frqTest.opens + "</p></li></ul>";
        } else {
            if(competitionStatus.mcFinished && mcTest.exists) nav += MC_HEADER;
            if(competitionStatus.frqFinished && frqTest.exists) nav += FRQ_HEADER;

            if (competitionStatus.mcDuring && !competitionStatus.frqDuring) {
                if(!userStatus.teacher || userStatus.creator)
                    return nav + MC_HEADER + "<li id='countdownCnt'>Written ends in <p id='countdown'>" + mcTest.closes + "</p></li></ul>";
                else
                    return nav + "<li id='countdownCnt'>Written ends in <p id='countdown'>" + mcTest.closes + "</p></li></ul>";
            } else if (!competitionStatus.mcDuring && competitionStatus.frqDuring) {
                if(!userStatus.teacher || userStatus.creator)
                    return nav + FRQ_HEADER + "<li id='countdownCnt'>Hands-On ends in <p id='countdown'>" + frqTest.closes + "</p></li></ul>";
                else
                    return nav + "<li id='countdownCnt'>Hands-On ends in <p id='countdown'>" + frqTest.closes + "</p></li></ul>";
            } else if (competitionStatus.mcFinished && competitionStatus.frqBefore) {
                return nav + "<li id='countdownCnt'>Hands-On opens in <p id='countdown'>" + frqTest.opens + "</p></li></ul>";
            } else if (competitionStatus.mcBefore && competitionStatus.frqFinished) {
                return nav + "<li id='countdownCnt'>Written opens in <p id='countdown'>" + mcTest.opens + "</p></li></ul>";
            } else if (competitionStatus.mcFinished && competitionStatus.frqFinished) {
                return nav + "<li id='countdownCnt'>The competition has ended!</li></ul>";
            } else return "";  // This shouldn't happen
        }
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
        sortedTeams.clear();
        for(UILEntry entry: allTeams) {
            sortedTeams.add(entry.tid);
            entry.getMCScore();
            teamList += "<tr><td>" + rank + "</td><td>" + StringEscapeUtils.escapeHtml4(entry.tname) + "</td>";
            if(competition.isPublic) teamList += "<td>" + StringEscapeUtils.escapeHtml4(entry.school) + "</td>";
            teamList += "<td class='right'>"+((frqTest.exists&&mcTest.exists)?entry.getMCScore():"")+"</td><td class='right'>"+
                        (frqTest.exists?entry.frqScore:entry.getMCScore())+"</td></tr>";
            rank++;
        }

        // create HTML
        scoreboardHTML = "<div class='column' id='scoreboardColumn' style='display:none;'><div class='row head-row'><h1>Scoreboard</h1>" +
                "<table id='teamList'><tr><th>#</th><th>Team</th>";
        if(competition.isPublic) scoreboardHTML += "<th>School</th>";
        scoreboardHTML += "<th class='right'>"+((frqTest.exists&&mcTest.exists)?"Written":"")+"</th><th class='right'>"+
                (frqTest.exists?"Hands-On":"Written")+"</th>" +
                "</tr>" + teamList + "</table></div></div>";
    }

    /***
     * Deletes a team's entry and updates the scoreboard.
     * @param tid
     */
    public void deleteEntry(short tid) {
        System.out.println("Deleting entry");
        Connection conn = Conn.getConnection();
        PreparedStatement stmt;
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
}

class UpdateScoreboard extends TimerTask {
    public Template template;
    public void run() {
        template.updateScoreboard();
    }
}

class UserStatus {
    boolean loggedIn;
    boolean signedUp;
    boolean teacher;
    boolean creator;

    UserStatus(boolean loggedIn, boolean signedUp, boolean teacher, boolean creator) {
        this.loggedIn = loggedIn;
        this.signedUp = signedUp;
        this.teacher = teacher;
        this.creator = creator;
    }

    public static UserStatus getCompeteStatus(User u, short cid) {
        boolean loggedIn = true;
        boolean signedUp = true;
        boolean teacher = false;
        boolean creator = false;

        if(u == null || !Conn.isLoggedIn(u.token)) {
            loggedIn = false;
            signedUp = false;
        } else if(u.teacher) {
            signedUp = false;
            teacher = true;
            if(((Teacher)u).cids.contains(cid)){
                creator = true;
            }
        } else if(!((Student)u).cids.containsKey(cid)) {
            signedUp = false;
        }
        return new UserStatus(loggedIn, signedUp, teacher, creator);
    }
}

class CompetitionStatus {
    public final boolean mcBefore;
    public final boolean mcDuring;
    public final boolean mcFinished;

    public final boolean frqBefore;
    public final boolean frqDuring;
    public final boolean frqFinished;

    CompetitionStatus(MCTest mcTest, FRQTest frqTest) {
        if(mcTest.exists) {
            if (!mcTest.opens.done()) {
                mcBefore = true;
                mcDuring = false;
                mcFinished = false;
            } else if (!mcTest.closes.done()) {
                mcBefore = false;
                mcDuring = true;
                mcFinished = false;
            } else {
                mcBefore = false;
                mcDuring = false;
                mcFinished = true;
            }
        } else {
            mcBefore = false;
            mcDuring = false;
            mcFinished = true;
        }

        if(frqTest.exists) {
            if (!frqTest.opens.done()) {
                frqBefore = true;
                frqDuring = false;
                frqFinished = false;
            } else if (!frqTest.closes.done()) {
                frqBefore = false;
                frqDuring = true;
                frqFinished = false;
            } else {
                frqBefore = false;
                frqDuring = false;
                frqFinished = true;
            }
        } else {
            frqBefore = false;
            frqDuring = false;
            frqFinished = true;
        }
        System.out.println("mcBefore="+mcBefore+",mcDuring="+mcDuring+",mcFinished="+mcFinished+",frqBefore="+frqBefore+",frqDuring="+frqDuring+",frqFinished="+frqFinished);
    }
}