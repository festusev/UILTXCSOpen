package Outlet.uil;

import Outlet.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
    public final String MC_HEADER = "<li onclick='showMC();' id='writtenNav' class='secondNavItem'>Written</li>";
    public final String FRQ_HEADER = "<li onclick='showFRQ();' id='handsOnNav' class='secondNavItem'>Hands-On</li>";

    public String navBarHTML;   // For the competition-specific nav bar that goes underneath the header nav bar
    private String scoreboardHTML;  // The scoreboard page html after the nav bars

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
    private boolean scoreboardSocketScheduled = false;  // Whether a timer has been set to send out the updated scoreboard to connected sockets
    private Gson gson = new Gson();

    private Competition competition;

    private boolean scoreboardInitialized = false;  // Whether or not the scoreboard has been initialized

    // Used whenever the competition is not published. 'published' should always be false.
    public Template(boolean published, String n, String description, MCTest mc, FRQTest fr, short cid, Competition competition) {
        name = n;this.description = description;this.mcTest = mc;this.frqTest=fr;this.cid=cid;this.competition=competition;
        mcFirst = true;HEADERS="";this.sorter = new SortUILTeams();
    }

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
            long opensDifference = fr.opens.date.getTime() - mc.opens.date.getTime();
            if (opensDifference > 0) {
                mcFirst = true;
                opens = mcTest.opens;
            } else {
                mcFirst = false;
                opens = mcTest.opens;
            }

            long closesDifference = fr.closes.date.getTime() - mc.closes.date.getTime();
            if (closesDifference > 0) {
                closes = frqTest.closes;
            } else {
                closes = mcTest.closes;
            }
        }

        navBarHTML = "<ul id='upperHalf'><li id='nav_compname'>"+
                name+"</li><li onclick='showAbout();' id='aboutNav' class='secondNavItem'>About</li><li onclick='showScoreboard();' " +
                "class='secondNavItem' id='scoreboardNav'>Scoreboard</li>";
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
                    "<a href='"+mcTest.TEST_LINK+"' class='link' target='_blank'>link</a></p><div id='mcTestTimer'>";

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
                    mcHTML[1] += "<td colspan='5'><input type='text' maxlength='40' class='mcText' onchange='setSAQChoice(" + i + ",this)'></td>";
                }
            }
            mcHTML[1]+="</table></div>";

            // answersHTML+="<div class='row'><h2>MC</h2><p><b>Test Packet: </b><a href='"+mcTest.TEST_LINK+"' class='link'>link</a><br><b>Answers: </b>"+answers+"<p></div>";
        }
        frqHTML = "";
        if(fr.exists) {
            frqHTML =  "<p id='frqInst'><b>Problem Packet: </b><a href='"+frqTest.STUDENT_PACKET+"' class='link' target='_blank'>link</a><br>Choose a problem to submit:</p>" +
                        "<form id='submit' onsubmit='submitFRQ(); return false;' enctype='multipart/form-data'>" +
                        "<select id='frqProblem'>";
            for(int i=1; i<=frqTest.PROBLEM_MAP.length;i++){
                frqHTML += "<option value='"+i+"' id='frqProblem"+i+"'>"+frqTest.PROBLEM_MAP[i-1].name+"</option>";
            }
            frqHTML += "</select>" +
                    "<input type='file' accept='.java,.cpp,.py' id='frqTextfile'/>" +
                    "<button id='submitBtn' class='chngButton'>Submit</button>" +
                    "</form><p id='advice'>Confused? Review the <a href='#' class='link' target='_blank' onclick='showAbout();'>rules</a>.</p>";
            // answersHTML+="<div class='row'><h2>FRQ</h2><p><b>Student Packet: </b><a href='"+frqTest.STUDENT_PACKET+
            //         "' class='link'>link</a><br><b>Judge Packet: </b><a href='"+frqTest.JUDGE_PACKET+"' class='link'>link</a></p></div>";
        }
        // answersHTML+="</div>";

        HEADERS = "<html><head><title>" + name + " - TXCSOpen</title>" +
                Dynamic.loadHeaders() +
                "<link rel='stylesheet' href='/css/console/console.css'>" +
                "<link rel='stylesheet' href='/css/console/uil_template.css'>" +
                //"<script src='/js/diff/base.js'></script>" +
                //"<script src='/js/diff/line.js'></script>" +
                "<script src='/js/console/uil.js'></script>" +
                "</head><body>";

        // Cre a timer to update the scoreboard every SCOREBOARD_UPDATE_INTERVAL seconds
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
                        // Dynamic.loadNav(request) +
                Dynamic.get_consoleHTML(1,getNavBarHTML(userStatus, competitionStatus) + "<div id='content'>" +
                        "<span id='columns'>" + getColumnsHTML(uData, userStatus, competitionStatus) +
                        "</span>"+ getRightBarHTML(uData, userStatus, competitionStatus)+"</div></body></html>")
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
        if(u == null || u.teacher || closes.done()) {
            signupText = "";
        } else if(((Student)u).cids.containsKey(cid)){
            signupColor = "grey";
            signupText = "Signed up";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(opens.date);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String html =  "<a class='competition mini_competition' href='/console/competitions?cid="+cid+"'>" +
                "<div class='row1'>"+StringEscapeUtils.escapeHtml4(name)+"<p class='right' style='color:"+signupColor+"'>"+signupText+"</p></div>" +
                "<div class='row2'>"+StringEscapeUtils.escapeHtml4(competition.teacher.fname) + " " + StringEscapeUtils.escapeHtml4(competition.teacher.lname) +
                "<p class='right'>"+month+"/"+day+"</p></div>";
        if(competition.teacher.uid == u.uid) {
            html += "<div class='competition_controls mini_comp_controls' data-id='"+cid+"'><div class='tooltip-cnt competition_edit' style='display: block;'>" +
                    "<img src='/res/console/edit.svg'><p class='tooltip'>Edit</p></div></div>";
        }

        return html + "</a>";
    }

    public String getRightBarHTML(User uData, UserStatus userStatus, CompetitionStatus competitionStatus) {
        String string = "<div id='leftBar'>";
        if(userStatus.creator || userStatus.signedUp) {
            if(mcTest.exists) {
                String written = "<div id='leftBarWritten'>";
                if(userStatus.creator) {
                    written += "<h2>Written</h2>";
                    int submissionCount = 0;
                    int submissionTotal = 0;    // All of the scores added up
                    for(UILEntry entry: competition.entries.tidMap.values()) {
                        Set<Short> uids = entry.mc.keySet();
                        for (short uid : uids) {
                            MCSubmission submission = entry.mc.get(uid);
                            if (submission != null) {
                                submissionCount++;
                                submissionTotal += submission.scoringReport[0];
                            }
                        }
                    }
                    int submissionAverage = 0;
                    if(submissionCount > 0) submissionAverage = submissionTotal/submissionCount;
                    written += "<p id='writtenSubmissionCount'>" + submissionCount + " submitted</p>";
                    written += "<p id='writtenAverage'>" + Math.round(submissionAverage) + " average</p>";
                } else {
                    UILEntry entry = ((Student)uData).cids.get(cid);
                    written += "<h2>Team</h2><p style='overflow:hidden'>"+entry.tname+"</p><h2>Team Code</h2><p>"+entry.password+"</p><h2>Test</h2>";
                    for(short uid: entry.uids) {
                        Student student = StudentMap.getByUID(uid);
                        short score = 0;
                        if(entry.mc.containsKey(uid)) {
                            MCSubmission submission = entry.mc.get(uid);
                            if(submission!=null) score = submission.scoringReport[0];
                        }
                        written += "<p>" + StringEscapeUtils.escapeHtml4(student.fname) + " <span id='"+student.uid+"writtenScore'>" +
                                score + "</span>pts</p>";
                    }
                }
                written += "</div>";
                string += written;
            }

            if(frqTest.exists) {
                String handsOn = "<div id='leftBarHandsOn'><h2>Hands-On</h2>";
                if(userStatus.creator) {
                    int submissionCount = 0;
                    int submissionTotal = 0;
                    for(UILEntry entry: competition.entries.tidMap.values()) {
                        for(Pair<Short, ArrayList<FRQSubmission>> problem: entry.frqResponses) {
                            if(problem.key != 0) submissionCount ++;
                        }
                        submissionTotal += entry.frqScore;
                    }

                    handsOn += "<p><span id='handsOnSubmissionCount'>" + submissionCount + "</span> submissions</p>";
                    int average = competition.entries.tidMap.values().size();
                    if(average > 0) average = Math.round(submissionTotal/average);
                    handsOn += "<p><span id='handsOnSubmissionAverage'>" + average + "</span> average</p>";
                } else {
                    int solved = 0;
                    int wrong = 0;
                    int untried = 0;

                    UILEntry entry = ((Student)uData).cids.get(cid);
                    for(Pair<Short, ArrayList<FRQSubmission>> problem: entry.frqResponses) {
                        if(problem.key == 0) untried++;
                        else if(problem.key > 0) solved++;
                        else wrong++;
                    }

                    handsOn += "<p id='handsOnSolved'>" + solved + " solved</p>" +
                               "<p id='handsOnWrong'>" + wrong + " wrong</p>" +
                               "<p id='handsOnUntried'>" + untried + " untried</p>" +
                               "<p id='handsOnScore'>" + entry.frqScore + " pts</p>";
                }
                handsOn += "</div>";
                string += handsOn;
            }

            if(userStatus.creator) {
                int numStudents = 0;
                for(UILEntry entry: competition.entries.tidMap.values()) {
                    numStudents += entry.uids.size();
                }
                string += "<div id='leftBarBottom'><p><span id='numTeams'>"+competition.entries.tidMap.values().size()+"</span> teams</p>" +
                                                  "<p><span id='numUsers'>"+numStudents+"</span> students</p></div>";
            } else if(userStatus.signedUp) {
                UILEntry entry = ((Student)uData).cids.get(cid);
                int rank = sortedTeams.indexOf(entry.tid) + 1;

                string += "<div id='leftBarBottom'>";
                string += 
                        "<p id='bottomRank'>" + ordinal(rank) + "</p>" +
                        "<p>out of <span id='bottomOutOf'>" + competition.entries.tidMap.values().size() +
                        "</span> teams</p></div>";
            }
        } /*else if(userStatus.teacher) {
            string += "<div id='leftBarBottom'><a href='/profile' class='bottomLeftLink'>Create Competition</a></div>";
        }*/ else {    // They are a student but not signed up
            // string += "<div id='leftBarBottom'><p onclick='showSignup()' class='bottomLeftLink'>Sign Up</p></div>";
            return "";
        }
        return string + "</div>";
    }

    public String getColumnsHTML(User uData, UserStatus userStatus, CompetitionStatus competitionStatus){
        // First, we determine whether to put a "Sign Up" button, a message saying "Your team is signed up for this
        // competition", a message saying "You must belong to a team to sign up", or a message saying
        // "you must be logged in to sign up for this competition"
        String actMessage = "<button id='signUp' onclick='showSignup()'>Sign Up</button><div id='signUpBox' style='display:none'><div class='center'><h1>Join Team</h1>" +
                "<img src='/res/close.svg' id='signUpClose' onclick='hideSignup()'/>" +
                "<p id='errorBoxERROR'></p><p class='instruction'>Enter team join code:</p><input name='teamCode' id='teamCode' oninput='codeEntered(this)' maxlength='6'>" +
                "<p id='toggleCreateTeam' onclick='toggleCreateTeam(event)'>or create a new team.</p></div></div>";  // They haven't signed up yet

        if(closes.done()) {
            actMessage = "";
        } else if(userStatus.teacher) {
            actMessage = "<h3 class='subtitle'>Teacher's can't compete</h3>";
        } else if(userStatus.signedUp) { // If they are already signed up for this competition
            actMessage = "<h3 class='subtitle'>You have signed up for this competition</h3>";
        }
        Teacher teacher = competition.teacher;
        String school = "";
        if(!teacher.school.isEmpty()) school = "<h2>School</h2><p>"+StringEscapeUtils.escapeHtml4(teacher.school)+"</p>";
        String escapedDescription = StringEscapeUtils.escapeHtml4(description);
        escapedDescription = escapedDescription.replaceAll("\n","<br>");
        String about = "<div class='column' id='aboutColumn'>" +
                "<div id='aboutDescription'>" +
                "<div id='aboutHead'><h1>" + StringEscapeUtils.escapeHtml4(name) + "</h1>" + actMessage + "</div>" +
                //"<div class='row' id='aboutDescriptionRow'>" +
                "<p>" + escapedDescription + "</p></div>" +
                "<div id='aboutInfo'><h2>Author</h2><p>"+StringEscapeUtils.escapeHtml4(teacher.fname)+" "+
                StringEscapeUtils.escapeHtml4(teacher.lname)+"</p>"+school;
        if(mcTest.exists) {
            about += "<h2>Written</h2><p>"+mcTest.opens.DATE_STRING+"<br>"+(mcTest.TIME/(1000*60))+" min<br>"+mcTest.KEY.length+" questions</p>";
        }
        if(frqTest.exists) {
            about += "<h2>Hands-On</h2><p>"+frqTest.opens.DATE_STRING+"<br>"+(frqTest.TIME/(1000*60))+" min<br>"+frqTest.PROBLEM_MAP.length+" questions</p>";
        }
        about += "</div></div>";
        return getFRQHTML(uData, userStatus, competitionStatus) + about + getScoreboardHTML() + /*answers +*/
                getMCHTML(uData, userStatus, competitionStatus) + getClarificationHTML(uData, userStatus, competitionStatus); /*getTeamHTML(uData, userStatus)*/
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

    public String getTeamMembers(User u, UILEntry team) {
        String html = "";
        for(short uid: team.uids) {
            Student student = StudentMap.getByUID(uid);
            html += "<li style='list-style-type:none;'>" + StringEscapeUtils.escapeHtml4(student.fname) + " " + StringEscapeUtils.escapeHtml4(student.lname);
            if (uid == u.uid && team.notStarted()) html += "<span onclick='leaveTeam()' id='leaveTeam'>Leave</span>";
            html+="</li>";
        }
        return html;
    }

    public String getTeamHTML(User u, UserStatus userStatus) {
        if(!userStatus.signedUp) return "";    // They don't belong to a team

        UILEntry team = ((Student)u).cids.get(cid);
        String html = "<div id='teamColumn' class='column' style='display:none;'><p id='teamName'>"+StringEscapeUtils.escapeHtml4(team.tname)+"<span>"+
                ordinal(sortedTeams.indexOf(team.tid)+1)+"</span></p>" +
                "<p id='teamJoinCode'>Join Code: "+team.password+"</p><div><b>Members:</b><ul id='teamMembers'>";
        html += getTeamMembers(u, team);
        html+="</ul></div>";
        /*if(frqTest.exists) {
            html+="<div id='frqProblems'>";
        }*/

        return html+"</div>";
    }
    public String getSmallMC(Student student, UILEntry entry, MCSubmission submission) {
        return "<tr onclick='showMCSubmission("+entry.tid+","+student.uid+");'><td>" + StringEscapeUtils.escapeHtml4(student.fname + " " + student.lname) +
                "</td><td>" + StringEscapeUtils.escapeHtml4(entry.tname) + "</td><td>" + submission.scoringReport[0] +
                "</td></tr>";
    }
    public String getMCHTML(User u, UserStatus userStatus, CompetitionStatus competitionStatus){
        if(!mcTest.exists) return "";

        if(userStatus.signedUp && !userStatus.teacher) {
            Student s = (Student) u;
            UILEntry entry = s.cids.get(cid);
            /*if (!entry.mc.containsKey(u.uid)) {
                return mcHTML[0];
            } else*/
            if (userStatus.finishedMC) {
                return getFinishedMC(entry.mc.get(u.uid), entry.tid, u.uid, competitionStatus);
            } else if(competitionStatus.mcBefore) {
                return "<div id='mcColumn' class='column mcSubmissionList' style='display:none;'>" +
                        "<div id='mcColumn_submissionList'><div class='row head-row'>" +
                        "<h1>Written</h1></div><div class='row'><p>The written section will start soon.</p></div></div></div>";
            } else if(competitionStatus.mcDuring) {
                return getRunningMC();
            } else if(competitionStatus.mcFinished) {   // The multiple choice is over, and they didn't take it
                return "<div id='mcColumn' class='column mcSubmissionList' style='display:none;'>" +
                        "<div id='mcColumn_submissionList'><div class='row head-row'>" +
                        "<h1>Written</h1></div><div class='row'><p>The written section has closed.</p></div></div></div>";
            }
        } else if(userStatus.teacher && userStatus.creator) {  // This is the teacher who made this competition
            String html =  "<div id='mcColumn' class='column mcSubmissionList' style='display:none;'>" +
                    "<div id='mcColumn_submissionList'><div class='row head-row'>" +
                    "<h1>Written</h1>" +
                    "</div>" +
                    "<div class='row'>" +
                    "<p>Test Packet: <a class='link' target='_blank' href='" + mcTest.TEST_LINK + "'>link</a></p>" +
                    "<p><b>Submissions:</b></p>";

            html += "<table id='mcSubmissions'><tr id='mcSubmissionsTr'><th>Name</th><th>Team</th><th>Score</th></tr>";

            for(UILEntry entry: competition.entries.tidMap.values()) {
                Set<Short> uids = entry.mc.keySet();
                for (short uid : uids) {
                    MCSubmission submission = entry.mc.get(uid);
                    if (submission != null) {
                        Student student = StudentMap.getByUID(uid);
                        html += getSmallMC(student, entry, submission);
                    }
                }
            }
            html += "</table></div></div></div>";

            return html;
        } else if(competitionStatus.mcFinished) {
            String html =  "<div id='mcColumn' class='column' style='display:none;'>" +
                    "<div class='row head-row'>" +
                    "<h1>Written</h1>" +
                    "</div>" +
                    "<div class='row'>" +
                    "<p>Test Packet: <a class='link' target='_blank' href='" + mcTest.TEST_LINK + "'>link</a></p>" +
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
                    html += "<td colspan='5'><input type='text' class='mcText' disabled><p class='mcTextCorrectAnswer'>"+
                            mcTest.KEY[i-1][0]+"</p></td>";
                }
            }
            html += "</table></div></div>";

            return html;
        }
        return "";
        /*else {
            if(userStatus.teacher) { // They are a teacher
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
        }*/
    }
    public String getRunningMC() {
        return mcHTML[0]+mcTest.getTimer().toString()+mcHTML[1];
    }
    public String getFinishedMCHelper(MCSubmission submission, short tid, short uid, boolean isTeacher, CompetitionStatus status) {
        if(status.mcDuring && !isTeacher) return "";    // If they aren't a teacher, don't let them see the finished mc.

        String html = "<table id='mcQuestions'><tr><th>#</th>";
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
                String tempAnswer = answer;
                if(tempAnswer.equals(MCTest.SKIP_CODE)) tempAnswer = "";
                html += "<td colspan='5'><input type='text' class='mcText' value='"+tempAnswer+"' disabled>"+correctAnswer+"</td>";
            }
            String mcResult = "";
            if(isTeacher) mcResult = "onchange='changeMCJudgement(this,"+tid+","+uid+","+i+")'";
            else mcResult = "disabled";
             // Add in a "correct"/"incorrect"/"skipped" dropdown
            html += "<td><select class='changeMCResult' "+mcResult+">";
            String correctString = "";
            String incorrectString = "";
            String skippedString = "";
            if(answer.equals(mcTest.KEY[i-1][0])) correctString = " selected";
            else if(answer.equals(MCTest.SKIP_CODE)) skippedString = " selected";
            else incorrectString = " selected";
            html += "<option "+correctString+">Correct</option><option "+incorrectString+">Incorrect</option><option "+
                    skippedString+">Skipped</option></select></td></tr>";
        }
        html += "</table>";
        return html;
    }
    public String getFinishedMC(MCSubmission submission, short tid, short uid, CompetitionStatus status) {
        if(!status.mcFinished) return "<div id='mcColumn' class='column' style='display:none;'><div class='row head-row'><h1>Written</h1><p>Written scores are hidden until the competition closes.</p></div></div>";
        String html =  "<div id='mcColumn' class='column' style='display:none;'>" +
                        "<div class='row head-row'>" +
                        "<h1>Written</h1>" +
                        "<h3>"+submission.scoringReport[0]+"/"+mcTest.MAX_POINTS+"</h3>" +
                        "</div>" +
                        "<div class='row'>" +
                        "<p>Test Packet: <a href='" + mcTest.TEST_LINK + "' class='link' target='_blank'>link</a></p>" +
                        "<p>Correct: "+submission.scoringReport[1]+"</p>" +
                        "<p>Incorrect: "+submission.scoringReport[3]+"</p>" +
                        "<p>Skipped: "+submission.scoringReport[2]+"</p><br>" +
                        "<p>Scoring Report</p>";

        return html + getFinishedMCHelper(submission, tid, uid,false, status) + "</div></div>";
    }

    public String getSmallFRQ(int i, FRQSubmission submission) {
        return "<tr onclick='showFRQSubmission("+i+")'><td>" + StringEscapeUtils.escapeHtml4(frqTest.PROBLEM_MAP[submission.problemNumber-1].name) +
                "</td><td>" + StringEscapeUtils.escapeHtml4(submission.entry.tname) + "</td><td id='showFRQSubmission"+i+"'>" + submission.getResultString() +
                "</td></tr>";
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
                        "<p>Test Packet: <a class='link' target='_blank' href='" + frqTest.STUDENT_PACKET + "'>link</a></p>" +
                        "<p><b>Submissions:</b></p>";

                html += "<table id='frqSubmissionsTable'><tr id='frqSubmissionsTr'><th>Problem</th><th>Team</th><th>Result</th></tr>";

                String rows = "";
                for(int i=0, j=competition.frqSubmissions.size(); i<j; i++) {
                    FRQSubmission submission = competition.frqSubmissions.get(i);
                    rows = getSmallFRQ(i, submission) + rows;
                }
                html += rows + "</table></div></div></div>";

                return html;
            } else return "<div id='frqColumn' class='column' style='display:none;'><h1 class='forbiddenPage'>Teachers cannot compete.</h1></div>";
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
        return /*"<script>grabFRQProblemsTimer = setInterval(function() { QProblems()}, 1000*10);</script>" +*/
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
        String problems = "<div id='frqProblems'><b>Problems - " + entry.frqScore +"pts</b>";
        for(int i=0; i<entry.frqResponses.length; i++) {
            problems+="<p>" + StringEscapeUtils.escapeHtml4(frqTest.PROBLEM_MAP[i].name) + " - ";
            Pair<Short, ArrayList<FRQSubmission>> problem = entry.frqResponses[i];
            if(problem.key > 0) {
                problems += frqTest.calcScore(problem.key) + "pts";
            } else{
                problems += (problem.key*-1) + " tries";
            }
            problems+="</p>";
        }
        return problems + "</div>";
    }

    public String getClarificationHTML(User user, UserStatus userStatus, CompetitionStatus competitionStatus) {
        // System.out.println("SignedUp="+userStatus.signedUp+", Creator="+userStatus.creator+", FRQDuring="+competitionStatus.frqDuring+
        //        ", FRQFinished="+competitionStatus.frqFinished+", MCDuring="+competitionStatus.mcDuring+", MCFinished="+competitionStatus.mcFinished);
        if ((userStatus.signedUp || userStatus.creator) && (competitionStatus.frqDuring || competitionStatus.frqFinished) &&
                (competitionStatus.mcDuring || competitionStatus.mcFinished)) {
            String html = "<div id='clarificationsColumn' class='column' style='display:none;'><h1>Clarifications</h1>";

            if (!user.teacher) {
                html += "<textarea maxlength='255' id='clarification_input' placeholder='Ask a question.'></textarea><button onclick='sendClarification()' class='chngButton'>Send Clarification</button>";
            }

            html += "<div class='clarification_group'>";

            boolean noClarifications = true;
            for (int i=competition.clarifications.size()-1;i>=0;i--) {
                Clarification clarification = competition.clarifications.get(i);
                if(clarification.responded) {  // Only show responded clarifications to non creators
                    String askerName = "";
                    if(userStatus.creator) {
                        Student asker = StudentMap.getByUID(clarification.uid);
                        if (asker != null) askerName = " - " + asker.fname + " " + asker.lname;
                    }

                    html += "<div class='clarification'><h3>Question"+askerName+"</h3><span>" + clarification.question +
                            "</span><h3>Answer</h3><span>" + clarification.response + "</span></div>";
                    noClarifications = false;
                } else if (userStatus.creator) {    // Not yet responded, so add in the response textarea
                    String askerName = "";
                    Student asker = StudentMap.getByUID(clarification.uid);
                    if (asker != null) askerName = " - " + asker.fname + " " + asker.lname;

                    html += "<div class='clarification'><h3>Question"+askerName+"</h3><span>" + clarification.question +
                            "</span><h3>Answer</h3><span><textarea placeholder='Send a response.'></textarea><button " +
                            "onclick='answerClarification(this, "+i+")' class='chngButton'>Send</button></span></div>";

                    noClarifications = false;
                }
            }

            if(noClarifications) {
                html += "There are no clarifications.";
            }

            return html + "</div></div>";
        }
        return "";
    }

    public String getNavBarHTML(UserStatus userStatus, CompetitionStatus competitionStatus) {
        String nav = navBarHTML;
        /*if(userStatus.signedUp) {
            nav += "<li id='team' onclick='showTeam()'>Team</li>";
        }*/
        if((!frqTest.exists || competitionStatus.frqBefore) && (!mcTest.exists || competitionStatus.mcBefore)) {
            if(mcFirst) return nav + "<li id='countdownCnt'>Written opens in <p id='countdown'>" + mcTest.opens + "</p></li></ul>";
            else return nav + "<li id='countdownCnt'>Hands-On opens in <p id='countdown'>" + frqTest.opens + "</p></li></ul>";
        } else {
            if(userStatus.signedUp || userStatus.creator) {
                if (mcTest.exists) nav += MC_HEADER;
                if (frqTest.exists) nav += FRQ_HEADER;
                if((competitionStatus.frqDuring || competitionStatus.frqFinished) && (competitionStatus.mcDuring || competitionStatus.mcFinished))
                    nav += "<li id='clarificationNav' onclick='showClarifications()' class='secondNavItem'>Clarifications</li>";
            }

            if (competitionStatus.mcDuring && !competitionStatus.frqDuring) {
                String closesScript;
                if(userStatus.teacher)  {   // They are a teacher, so when the test closes, update the nav.
                    closesScript = mcTest.closes.getScript("updateNav()");
                } else {    // They are not a teacher, so when the test closes, do nothing
                    closesScript = mcTest.closes.getScript("");
                }
                return nav + "<li id='countdownCnt'>Written ends in <p id='countdown'>" + closesScript + "</p></li></ul>";
            } else if (!competitionStatus.mcDuring && competitionStatus.frqDuring) {
                String closesScript = frqTest.closes.getScript("updateNav()");

                return nav + "<li id='countdownCnt'>Hands-On ends in <p id='countdown'>" + closesScript + "</p></li></ul>";
            } else if (competitionStatus.mcFinished && competitionStatus.frqBefore) {
                return nav + "<li id='countdownCnt'>Hands-On opens in <p id='countdown'>" + frqTest.opens + "</p></li></ul>";
            } else if (competitionStatus.mcBefore && competitionStatus.frqFinished) {
                return nav + "<li id='countdownCnt'>Written opens in <p id='countdown'>" + mcTest.opens + "</p></li></ul>";
            } else if (competitionStatus.mcFinished && competitionStatus.frqFinished) {
                return nav + "<li id='countdownCnt'>The competition has ended!</li></ul>";
            } else {
                String closesScript;
                if(userStatus.teacher || userStatus.finishedMC)  {   // They are a teacher
                    closesScript = closes.getScript("updateNav()");
                } else {
                    closesScript = closes.getScript("");
                }
                return nav + "<li id='countdownCnt'>Competition closes in <p id='countdown'>" + closesScript + "</ul>";  // MC and FRQ at the same time.
            }
        }
    }

    // Makes sure that the scoreboard has been initialized
    public String getScoreboardHTML() {
        if(!scoreboardInitialized) {
            updateScoreboard();
            scoreboardInitialized = true;
        }
        return scoreboardHTML;
    }

    public void updateScoreboard(){
        /*try {
            allTeams = competition.getAllEntries();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }*/

        if(!scoreboardSocketScheduled) {    // Schedule a timer to send the updated scoreboard to the connected sockets
            scoreboardSocketScheduled = true;
            Timer task = new Timer();
            task.schedule(new TimerTask() {
                @Override
                public void run() {
                    ArrayList<CompetitionSocket> sockets = CompetitionSocket.competitions.get(cid);
                    if(sockets == null) return;

                    for(CompetitionSocket socket: sockets) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("action", "updateScoreboard");
                        obj.addProperty("html", getScoreboardHTML());

                        try {
                            socket.send(gson.toJson(obj));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    scoreboardSocketScheduled = false;
                    task.cancel();
                    task.purge();
                }
            }, 1000);
        }

        competition.entries.allEntries.sort(sorter);

        // The table row list of teams in order of points
        String teamList = "";
        int rank = 1;
        sortedTeams.clear();
        for(UILEntry entry: competition.entries.allEntries) {
            sortedTeams.add(entry.tid);
            entry.getMCScore();
            teamList += "<tr><td>" + rank + "</td><td>" + StringEscapeUtils.escapeHtml4(entry.tname) + "</td>";
            if(competition.isPublic) teamList += "<td>" + StringEscapeUtils.escapeHtml4(entry.school) + "</td>";
            teamList += "<td class='right'>"+((frqTest.exists&&mcTest.exists)?entry.getMCScore():"")+"</td><td class='right'>"+
                        (frqTest.exists?entry.frqScore:entry.getMCScore())+"</td></tr>";
            rank++;
        }

        // create HTML
        scoreboardHTML = "<div class='column' id='scoreboardColumn' style='display:none;'><h1>Scoreboard</h1>" +
                "<table id='teamList'><tr><th>#</th><th>Team</th>";
        if(competition.isPublic) scoreboardHTML += "<th>School</th>";
        scoreboardHTML += "<th class='right'>"+((frqTest.exists&&mcTest.exists)?"Written":"")+"</th><th class='right'>"+
                (frqTest.exists?"Hands-On":"Written")+"</th>" +
                "</tr>" + teamList + "</table></div>";
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
    boolean signedUp;
    boolean teacher;
    boolean creator;
    boolean finishedMC; // Whether or not they finished the MC. If they aren't signed up, this is always false.

    UserStatus(boolean signedUp, boolean teacher, boolean creator, boolean finishedMC) {
        this.signedUp = signedUp;
        this.teacher = teacher;
        this.creator = creator;
        this.finishedMC = finishedMC;
    }

    public static UserStatus getCompeteStatus(User u, short cid) {
        boolean signedUp = true;
        boolean teacher = false;
        boolean creator = false;
        boolean finishedMC = false;

        if(u.teacher) {
            signedUp = false;
            teacher = true;
            if(((Teacher)u).cids.contains(cid)){
                creator = true;
            }
        } else if(!((Student)u).cids.containsKey(cid)) {
            signedUp = false;
        } else {
            UILEntry entry = ((Student)u).cids.get(cid);
            finishedMC = entry.finishedMC(u.uid);
        }

        return new UserStatus(signedUp, teacher, creator, finishedMC);
    }
}

class CompetitionStatus {
    public final boolean mcBefore;
    public final boolean mcDuring;
    public final boolean mcOverflow;    // If the multiple choice is in the threshold when submissions are accepted
    public final boolean mcFinished;

    public final boolean frqBefore;
    public final boolean frqDuring;
    public final boolean frqOverflow;    // If the frq is in the threshold when submissions are accepted
    public final boolean frqFinished;

    public static final long OVERFLOW_LENGTH = 1000*60;   // The length of time (milli) after submissions close when they are still accepted
    CompetitionStatus(MCTest mcTest, FRQTest frqTest) {
        if(mcTest.exists) {
            if (!mcTest.opens.done()) {
                mcBefore = true;
                mcDuring = false;
                mcOverflow = false;
                mcFinished = false;
            } else if (!mcTest.closes.done()) {
                mcBefore = false;
                mcDuring = true;
                mcOverflow = false;
                mcFinished = false;
            } else if (mcTest.closes.inOverflow(OVERFLOW_LENGTH)) {
                mcBefore = false;
                mcDuring = false;
                mcOverflow = true;
                mcFinished = true;
            } else {
                mcBefore = false;
                mcDuring = false;
                mcOverflow = false;
                mcFinished = true;
            }
        } else {
            mcBefore = false;
            mcDuring = false;
            mcOverflow = false;
            mcFinished = true;
        }

        if(frqTest.exists) {
            if (!frqTest.opens.done()) {
                frqBefore = true;
                frqDuring = false;
                frqOverflow = false;
                frqFinished = false;
            } else if (!frqTest.closes.done()) {
                frqBefore = false;
                frqDuring = true;
                frqOverflow = false;
                frqFinished = false;
            } else if (frqTest.closes.inOverflow(OVERFLOW_LENGTH)) {
                frqBefore = false;
                frqDuring = false;
                frqOverflow = true;
                frqFinished = true;
            } else {
                frqBefore = false;
                frqDuring = false;
                frqOverflow = false;
                frqFinished = true;
            }
        } else {
            frqBefore = false;
            frqDuring = false;
            frqOverflow = false;
            frqFinished = true;
        }
        System.out.println("mcBefore="+mcBefore+",mcDuring="+mcDuring+",mcInOverflow="+mcOverflow+",mcFinished="+mcFinished+
                ",frqBefore="+frqBefore+",frqDuring="+frqDuring+",frqOverflow="+frqOverflow+",frqFinished="+frqFinished);
    }
}