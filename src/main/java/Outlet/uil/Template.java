package Outlet.uil;

import Outlet.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
    public final boolean showScoreboard;

    public String navBarHTML;   // For the competition-specific nav bar that goes underneath the header nav bar
    private String scoreboardHTML;  // The scoreboard page html after the nav bars

    public JsonArray scoreboardData;  // The list of teams sorted and with team members and scores for each test
    public JsonArray teamCodeData;  // The list of team codes, with each index corresponding to the same index of the team in scoreboardData
    public JsonObject tempUserData;  // A list of temporary users' usernames and passwords.

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
    public Template(boolean published, String n, String description, MCTest mc, FRQTest fr, short cid, boolean showScoreboard,
                    Competition competition) {
        name = n;this.description = description;this.mcTest = mc;this.frqTest=fr;this.cid=cid;this.competition=competition;
        mcFirst = true;HEADERS="";this.sorter = new SortUILTeams();this.showScoreboard = showScoreboard;
    }

    public Template(String n, String description, MCTest mc, FRQTest fr, short cid, boolean showScoreboard, Competition competition){
        name = n;this.description = description;mcTest = mc;frqTest = fr;this.cid = cid; this.sorter = new SortUILTeams();
        this.showScoreboard = showScoreboard;
        this.competition=competition;

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
                name+"</li><li onclick='showAbout();' id='aboutNav' class='secondNavItem'>About</li>";
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
                    "<p class='subtitle'><span>Instructions: </span>" + StringEscapeUtils.escapeHtml4(mcTest.INSTRUCTIONS).replaceAll("\n","<br>") +
                    "<br><b>Test Packet: </b><a href='"+StringEscapeUtils.escapeHtml4(mcTest.TEST_LINK)+"' class='link' target='_blank'>link</a></p><div id='mcTestTimer'>";

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
            frqHTML =  "<p id='frqInst'><b>Problem Packet: </b><a href='"+StringEscapeUtils.escapeHtml4(frqTest.STUDENT_PACKET)+"' class='link' target='_blank'>link</a><br>Choose a problem to submit:</p>" +
                        "<form id='submit' onsubmit='submitFRQ(); return false;' enctype='multipart/form-data'>" +
                        "<select id='frqProblem'>";
            for(int i=1; i<=frqTest.PROBLEM_MAP.length;i++){
                frqHTML += "<option value='"+i+"' id='frqProblem"+i+"'>"+StringEscapeUtils.escapeHtml4(frqTest.PROBLEM_MAP[i-1].name)+"</option>";
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
        UserStatus userStatus = UserStatus.getCompeteStatus(uData, competition);

        String jsPDF = "";
        if(userStatus.admin) {
            jsPDF = "<script src='/js/html2canvas.min.js' defer></script>" +
                    "<script src='https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.3.0/jspdf.umd.min.js' defer></script>" +
                    "<script src='/js/htmldiff.js' defer></script>";
        }
        writer.write(HEADERS+ jsPDF +
                        // Dynamic.loadNav(request) +
                Dynamic.get_consoleHTML(1,getNavBarHTML(userStatus, competitionStatus) + "<div id='content'>" +
                        "<span id='columns'>" + getColumnsHTML(uData, userStatus, competitionStatus) +
                        "</span>"+ getRightBarHTML(uData, userStatus, competitionStatus)+"</div></body></html>", uData)
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
                "<div class='row2'>"+StringEscapeUtils.escapeHtml4(competition.teacher.getName()) +
                "<p class='right'>"+month+"/"+day+"</p></div>";
        if(competition.teacher.uid == u.uid) {
            html += "<div class='competition_controls mini_comp_controls' data-id='"+cid+"'><div class='tooltip-cnt competition_edit' style='display: block;'>" +
                    "<img src='/res/console/edit.svg'><p class='tooltip'>Edit</p></div></div>";
        } else if(u.teacher) {    // Check if the teacher is a judge of this competition
            Teacher teacher = (Teacher) u;
            if(teacher.judging.contains(competition)) {  // They are a judge
                    html += "<div class='competition_controls mini_comp_controls'><div class='tooltip-cnt competition_edit' style='display: block;'>" +
                            "<img src='/res/console/judge.svg'><p class='tooltip'>Judging</p></div></div>";
            }
        }

        return html + "</a>";
    }

    public String getRightBarHTML(User uData, UserStatus userStatus, CompetitionStatus competitionStatus) {
        String string = "<div id='leftBar'>";
        if(userStatus.admin || userStatus.signedUp) {
            if(mcTest.exists) {
                String written = "<div id='leftBarWritten'>";
                if(userStatus.admin) {
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
                    written += "<h2>Team</h2><p style='overflow:hidden'>"+StringEscapeUtils.escapeHtml4(entry.tname)+
                            "</p><h2>Team Code</h2><p>"+entry.password+"</p><h2>Test</h2>";
                    for(short uid: entry.uids) {
                        Student student = StudentMap.getByUID(uid);
                        short score = 0;
                        if(entry.mc.containsKey(uid)) {
                            MCSubmission submission = entry.mc.get(uid);
                            if(submission!=null) score = submission.scoringReport[0];
                        }
                        written += "<p>" + StringEscapeUtils.escapeHtml4(student.fname!=null?student.fname:"") +
                                " <span id='"+student.uid+"writtenScore'>" +
                                score + "</span>pts</p>";
                    }
                }
                written += "</div>";
                string += written;
            }

            if(frqTest.exists) {
                String handsOn = "<div id='leftBarHandsOn'><h2>Hands-On</h2>";
                if(userStatus.admin) {
                    handsOn += "<p><span id='handsOnSubmissionCount'></span> submissions</p><p><span id='handsOnSubmissionAverage'></span> average</p>";
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

            if(userStatus.admin) {
                string += "<div id='leftBarBottom'><p><span id='numTeams'></span> teams</p>" +
                                                  "<p><span id='numUsers'></span> students</p></div>";
            } else if(userStatus.signedUp) {
                UILEntry entry = ((Student)uData).cids.get(cid);
                int rank = sortedTeams.indexOf(entry.tid) + 1;

                string += "<div id='leftBarBottom'>";
                if(showScoreboard) {
                    string +=
                            "<p id='bottomRank'>" + ordinal(rank) + "</p>" +
                                    "<p>out of <span id='bottomOutOf'>" + competition.entries.tidMap.values().size() +
                                    "</span> teams</p></div>";
                }
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
                "<div id='signUpIsAlternateCnt'>I am a written specialist<input name='isAlternate' id='signUpIsAlternate' type='checkbox'></div>" +
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
                "<div id='aboutInfo'><h2>Author</h2><p>"+StringEscapeUtils.escapeHtml4(teacher.getName())+"</p>"+
                school;
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
            html += "<li style='list-style-type:none;'>" + StringEscapeUtils.escapeHtml4(student.getName());
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
        return "<tr onclick='showMCSubmission("+student.uid+");'><td>" + StringEscapeUtils.escapeHtml4(student.getName()) +
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
        } else if(userStatus.teacher && userStatus.admin) {  // This is the teacher who made this competition
            String html =  "<div id='mcColumn' class='column mcSubmissionList' style='display:none;'>" +
                    "<div id='mcColumn_submissionList'><div class='row head-row'>" +
                    "<h1>Written</h1>" +
                    "</div>" +
                    "<div class='row'>" +
                    "<p>Test Packet: <a class='link' target='_blank' href='" + StringEscapeUtils.escapeHtml4(mcTest.TEST_LINK) + "'>link</a></p>" +
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
                    "<p>Test Packet: <a class='link' target='_blank' href='" + StringEscapeUtils.escapeHtml4(mcTest.TEST_LINK) + "'>link</a></p>" +
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
                            StringEscapeUtils.escapeHtml4(mcTest.KEY[i-1][0])+"</p></td>";
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
    public String getFinishedMCHelper(MCSubmission submission, short uid, boolean isTeacher, CompetitionStatus status) {
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
                    correctAnswer = "<p class='mcTextCorrectAnswer'>"+ StringEscapeUtils.escapeHtml4(mcTest.KEY[i-1][0])+"</p>";
                }
                String tempAnswer = answer;
                if(tempAnswer.equals(MCTest.SKIP_CODE)) tempAnswer = "";
                html += "<td colspan='5'><input type='text' class='mcText' value='"+StringEscapeUtils.escapeHtml4(tempAnswer)+"' disabled>"+
                        correctAnswer+"</td>";
            }
            String mcResult = "";
            if(isTeacher) mcResult = "onchange='changeMCJudgement(this,"+uid+","+i+")'";
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

        return html + getFinishedMCHelper(submission, uid,false, status) + "</div></div>";
    }

    public String getSmallFRQ(int i, FRQSubmission submission) {
        return "<tr onclick='showFRQSubmission(this,"+i+")'><td>" + StringEscapeUtils.escapeHtml4(frqTest.PROBLEM_MAP[submission.problemNumber-1].name) +
                "</td><td>" + StringEscapeUtils.escapeHtml4(submission.entry.tname) + "</td><td id='showFRQSubmission"+i+"'>" + submission.getResultString() +
                "</td></tr>";
    }

    public String getFRQHTML(User u, UserStatus userStatus, CompetitionStatus competitionStatus) {
        if(!frqTest.exists || competitionStatus.frqBefore) return "";

        if(userStatus.teacher) {
            if(userStatus.admin) {   // This is the teacher who created this competition
                String html =  "<div id='frqColumn' class='column frqSubmissionList' style='display:none;'>" +
                        "<div style='flex-grow:1' id='frqSubmissions'><div class='row head-row'>" +
                        "<h1>Hands-On</h1>" +
                        "</div>" +
                        "<div class='row'><audio src='/blip.mp3' preload='auto' controls='none' style='display:none' id='playBell'></audio>" +
                        "<p>Most recent problems are first.<br>Test Packet: <a class='link' target='_blank' href='" + StringEscapeUtils.escapeHtml4(frqTest.STUDENT_PACKET) + "'>link</a></p>" +
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
                if(userStatus.alt) {
                    return "<div id='frqColumn' class='column' style='display:none;'>" +
                            "<h1 class='forbiddenPage'>Written specialists cannot compete in the Hands-On.</h1>" +
                            "</div>";
                }
                else return getRunningFRQ(entry);
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
        if ((userStatus.signedUp || userStatus.admin) && (competitionStatus.frqDuring || competitionStatus.frqFinished) &&
                (competitionStatus.mcDuring || competitionStatus.mcFinished)) {
            String html = "<div id='clarificationsColumn' class='column' style='display:none;'><h1>Clarifications</h1>";

            if (!user.teacher) {
                html += "<textarea maxlength='255' oninput='inputMaxLength(this)' id='clarification_input' " +
                        "placeholder='Ask a question.'></textarea><button onclick='sendClarification()' class='chngButton'>Send Clarification</button>";
            }

            html += "<div class='clarification_group'>";

            boolean noClarifications = true;
            for (int i=competition.clarifications.size()-1;i>=0;i--) {
                Clarification clarification = competition.clarifications.get(i);
                String askerName = "";
                Student asker = StudentMap.getByUID(clarification.uid);
                if(asker != null) {
                    UILEntry entry = asker.cids.get(cid);
                    if(entry != null) {
                        askerName = " - " + entry.tname;
                    }
                }
                if(clarification.responded) {  // Only show responded clarifications to non creators
                    html += "<div class='clarification' id='clarification_"+clarification.index+"'><h3>Question"+
                            askerName+"</h3><span>" +
                            StringEscapeUtils.escapeHtml4(clarification.question) +
                            "</span><h3>Answer</h3><span>" + StringEscapeUtils.escapeHtml4(clarification.response) + "</span></div>";
                    noClarifications = false;
                } else if (userStatus.admin) {    // Not yet responded, so add in the response textarea
                    html += "<div class='clarification' id='clarification_"+clarification.index+"'><h3>Question"+
                            askerName+"</h3><span>" + StringEscapeUtils.escapeHtml4(clarification.question) +
                            "</span><h3>Answer</h3><span><textarea maxlength='255' oninput='inputMaxLength(this)' placeholder='Send a response.'></textarea><button " +
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
        if(userStatus.admin || competition.template.showScoreboard)
            nav += "<li onclick='showScoreboard();' class='secondNavItem' id='scoreboardNav'>Scoreboard</li>" +
                    "<script>requestLoadScoreboard()</script>";

        String postfix = "";    // The controls to start and stop portions of the competition. Only for the creator.
        if(userStatus.creator) {
            if(mcTest.exists) {
                if(competitionStatus.mcDuring) postfix += "<button onclick='stopWritten()' class='chngButton'>Stop Written</button>";
                else postfix += "<button onclick='startWritten()' class='chngButton'>Start Written</button>";
            }
            if(frqTest.exists) {
                if(competitionStatus.frqDuring) postfix += "<button onclick='stopHandsOn()' class='chngButton'>Stop Hands-On</button>";
                else postfix += "<button onclick='startHandsOn()' class='chngButton' >Start Hands-On</button>";
            }
        }

        if((!frqTest.exists || competitionStatus.frqBefore) && (!mcTest.exists || competitionStatus.mcBefore)) {
            if(mcFirst) return nav + postfix + "<li id='countdownCnt'>Written opens in <p id='countdown'>" + mcTest.opens + "</p></li></ul>";
            else return nav + postfix + "<li id='countdownCnt'>Hands-On opens in <p id='countdown'>" + frqTest.opens + "</p></li></ul>";
        } else {
            if(userStatus.signedUp || userStatus.admin) {
                if (mcTest.exists && !competitionStatus.mcBefore) nav += MC_HEADER;
                if (frqTest.exists && !competitionStatus.frqBefore) nav += FRQ_HEADER;
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
                return nav + postfix + "<li id='countdownCnt'>Written ends in <p id='countdown'>" + closesScript + "</p></li></ul>";
            } else if (!competitionStatus.mcDuring && competitionStatus.frqDuring) {
                String closesScript = frqTest.closes.getScript("updateNav()");

                return nav + postfix + "<li id='countdownCnt'>Hands-On ends in <p id='countdown'>" + closesScript + "</p></li></ul>";
            } else if (competitionStatus.mcFinished && competitionStatus.frqBefore) {
                return nav + postfix + "<li id='countdownCnt'>Hands-On opens in <p id='countdown'>" + frqTest.opens + "</p></li></ul>";
            } else if (competitionStatus.mcBefore && competitionStatus.frqFinished) {
                return nav + postfix + "<li id='countdownCnt'>Written opens in <p id='countdown'>" + mcTest.opens + "</p></li></ul>";
            } else if (competitionStatus.mcFinished && competitionStatus.frqFinished) {
                return nav + postfix + "<li id='countdownCnt'>The competition has ended!</li></ul>";
            } else {
                String closesScript;
                if(userStatus.teacher || userStatus.finishedMC)  {   // They are a teacher
                    closesScript = closes.getScript("updateNav()");
                } else {
                    closesScript = closes.getScript("");
                }
                return nav + postfix + "<li id='countdownCnt'>Competition closes in <p id='countdown'>" + closesScript + "</ul>";  // MC and FRQ at the same time.
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
                        UserStatus status = UserStatus.getCompeteStatus(socket.user, competition);
                        socket.sendLoadScoreboardData(status);
                    }
                    scoreboardSocketScheduled = false;
                    task.cancel();
                    task.purge();
                }
            }, 1000);
        }

        // This will store the json scoreboard information
        scoreboardData = new JsonArray();
        teamCodeData = new JsonArray();
        tempUserData = new JsonObject();

        competition.entries.allEntries.sort(sorter);
        sortedTeams.clear();
        for (UILEntry entry : competition.entries.allEntries) {
            sortedTeams.add(entry.tid);


            // entry.getMCScore();

            /*teamList += "<tr><td>" + rank + "</td><td>" + StringEscapeUtils.escapeHtml4(entry.tname) + "</td>";
            if (competition.isPublic) teamList += "<td>" + StringEscapeUtils.escapeHtml4(entry.school) + "</td>";
            int mcScore = entry.getMCScore();
            teamList += "<td class='right'>" + ((frqTest.exists && mcTest.exists) ? mcScore : "") + "</td><td class='right'>" +
                    (frqTest.exists ? entry.frqScore : mcScore) + "</td>";
            if(frqTest.exists && mcTest.exists) teamList += "<td class='right'>" + (entry.frqScore + mcScore) + "</td></tr>";*/

            JsonObject entryJSON = new JsonObject();
            entryJSON.addProperty("tname", entry.tname);
            entryJSON.addProperty("school", entry.school);
            entryJSON.addProperty("tid", entry.tid);
            entryJSON.add("students", entry.getStudentJSON());
            if(frqTest.exists) {
                entryJSON.addProperty("frq", entry.frqScore);
                entryJSON.add("frqResponses", entry.getFRQJSON());
            }

            scoreboardData.add(entryJSON);
            teamCodeData.add(entry.password);
            for(short uid: entry.uids) {
                Student student = StudentMap.getByUID(uid);
                if(student.temp) {
                    JsonArray tempJSON = new JsonArray();
                    tempJSON.add(student.email);
                    tempJSON.add(student.password);
                    tempUserData.add(""+student.uid,tempJSON);
                }
            }
            // rank++;
        }

        // create HTML
        scoreboardHTML = "<div class='column' id='scoreboardColumn' style='display:none;'>" +
                "<div id='signUpBox' style='display:none'><div class='center'><h1>Create Team</h1>" +
                "<img src='/res/close.svg' id='signUpClose' onclick='hideSignup()'/>" +
                "<p id='errorBoxERROR'></p><p class='instruction'>Team Name</p><input name='teamCode' id='teamCode' maxlength='25' class='creatingTeam'>" +
                "<button class='chngButton' onclick='createTeam()'>Create</button></div></div>" +
                "<div id='selectStudent'><div class='center'><div class='leftBlock'>" +
                "<h1>Select Student</h1>" +
                "<div id='createStudent'><input maxLength='50' placeholder='First Name' id='fnameTemp'></input>" +
                "<input maxLength='50' placeholder='Last Name' class='half' id='lnameTemp'></input><input maxLength='100' placeholder='School' id='inputSchool'></input>" +
                "<button onclick='createTempStudent()' class='chngButton'>Create student</button></div>" +
                "<div id='studentSearch'><h3>Search for student</h3><input oninput='searchForStudent(this)'></input>" +
                "<div class='tableCnt'>" +
                "<table id='studentSearchTable'></table></div>" +
                "</div></div>" +
                "<div class='rightBlock'>" +
                "<img src='/res/close.svg' class='close' onclick='Team.closeSelectStudent()'/>" +
                "<b>From your class</b><div class='tableCnt'><table id='selectStudentFromClass'></table></div>" +
                "<b>From other teams</b><div class='tableCnt'><table id='selectSignedUpStudent'></table></div></div></div></div>" +

                // Section for selecting an existing student
                "<div id='selectGlobalTeam'><div class='center'><h1>Select Global Team</h1>" +
                "<img src='/res/close.svg' class='close' onclick='closeAddExistingTeam()'/>" +
                "<ul id='selectGlobalTeamList'></ul></div></div>" +

                // Delete team/student
                "<div id='deleteConfirmationCnt'><div class='center'><h1 id='deleteMessage'></h1><p id='deleteSubtitle'></p>" +
                "<button id='deleteButton' onclick='deleteTeamOrStudent()'>Yes, Delete</button>" +
                "<button onclick='closeDeleteConfirmation()'>Cancel</button>" +
                "</div></div>" +
                "<div id='teamListCnt' class='showGeneral'><h1>Scoreboard</h1><div id='scoreboardNav'>" +
                "<button onclick='showGeneralScoreboard()' id='showGeneralButton'>General</button>";
        if(mcTest.exists) scoreboardHTML += "<button onclick='showWrittenScoreboard()' id='showWrittenButton'>Written</button>";
        if(frqTest.exists) scoreboardHTML += "<button onclick='showHandsOnScoreboard()' id='showHandsOnButton'>Hands-On</button>";
        scoreboardHTML += "</div><button id='addExistingTeam' onclick='showAddExistingTeam()' class='creatorOnly chngButton'>Add Existing Team</button>" +
                "<button id='createTeam' onclick='showSignup()' class='creatorOnly chngButton'>Create Team</button>" +
                "<a id='downloadScoreboard' onclick='downloadScoreboard()' class='creatorOnly'>Download Scoreboard</a>" +
                "<a id='downloadRoster' onclick='downloadRoster()' class='creatorOnly'>Download Roster</a>" +
                "<div id='generalScoreboard'><table id='teamList'></table></div>";
        if(mcTest.exists) scoreboardHTML += "<div id='writtenScoreboard'><table id='writtenScoreboardTable'></table></div>";
        if(frqTest.exists) scoreboardHTML += "<div id='handsOnScoreboard'><table id='handsOnScoreboardTable'></table></div>";
        scoreboardHTML += "</div><div id='teamCnt'><h1 id='openTeamName'></h1>" +
                "<div id='teamControls'><img class='creatorOnly editTeam' id='deleteTeam' onclick='Team.showDeleteConfirmation()' src='/res/console/delete.svg'>" +
                "<img class='creatorOnly' id='editSaveTeam' onclick='Team.editSaveTeam()' src='/res/console/edit.svg'></div>" +
                "<div id='openTeamFeedbackCnt'></div><p class='creatorOnly'><span class='label'>Code:</span><span id='openTeamCode'></span></p>";
        if(mcTest.exists) scoreboardHTML += "<p><span class='label'>Written:</span><span id='openTeamWritten'></span></p>";
        if(frqTest.exists) scoreboardHTML += "<p><span class='label'>Hands-On:</span><span id='openTeamHandsOn'></span>";
        scoreboardHTML += "<h3>Primaries";
        if(mcTest.exists) scoreboardHTML += "<span style='float:right'>Written</span>";
        scoreboardHTML += "</h3><table id='openPrimariesList'></table><button id='addPrimaryCompetitor' " +
                "class='addCompetitor' onclick='Team.addPrimaryCompetitor()'>+</button>";
        if(competition.alternateExists) scoreboardHTML += "<h3>Written Specialist</h3><table id='openAlternateList'>" +
                "</table><button id='addAlternateCompetitor' class='addCompetitor' onclick='Team.addAlternateCompetitor()'>+</button>";
        scoreboardHTML += "</div></div>";
    }

    /***
     * Deletes a team's entry and updates the scoreboard.
     * @param entry
     */
    public void deleteEntry(UILEntry entry) {
        System.out.println("Deleting entry");

        Connection conn = Conn.getConnection();
        PreparedStatement stmt;
        try {
            stmt = conn.prepareStatement("DELETE FROM `c"+this.cid+"` WHERE tid=?");
            stmt.setShort(1,entry.tid);
            stmt.executeUpdate();

            // Now, update all of the students who are signed up for this team
            for(short uid: entry.uids) {
                Student s = StudentMap.getByUID(uid);
                s.cids.remove(cid);
            }
            entry.uids = new HashSet<>();
            competition.entries.delEntry(entry);

            // Remove frq submissions
            competition.frqSubmissions.removeIf(s -> (s.entry.tid == entry.tid));
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
    boolean alt;    // if they are the alternate
    boolean teacher;
    boolean admin;  // If they administrate this competition
    boolean creator;
    boolean judging;
    boolean finishedMC; // Whether or not they finished the MC. If they aren't signed up, this is always false.

    UserStatus(boolean signedUp, boolean alt, boolean teacher, boolean creator, boolean judging, boolean finishedMC) {
        this.signedUp = signedUp;
        this.alt = alt;
        this.teacher = teacher;
        this.admin = creator || judging;
        this.creator = creator;
        this.judging = judging;
        this.finishedMC = finishedMC;
    }

    public static UserStatus getCompeteStatus(User u, Competition competition) {
        boolean signedUp = true;
        boolean alt = false;
        boolean teacher = false;
        boolean creator = false;
        boolean judge = false;
        boolean finishedMC = false;

        if(u.teacher) {
            signedUp = false;
            teacher = true;
            if(((Teacher)u).competitions.contains(competition)){
                creator = true;
            } else if(((Teacher) u).judging.contains(competition)) {
                judge = true;
            }
        } else if(!((Student)u).cids.containsKey(competition.template.cid)) {
            signedUp = false;
        } else {
            UILEntry entry = ((Student)u).cids.get(competition.template.cid);
            finishedMC = entry.finishedMC(u.uid);

            if(entry.altUID == u.uid) { // They are this team's alternate
                alt = true;
            }
        }

        return new UserStatus(signedUp, alt, teacher, creator, judge, finishedMC);
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