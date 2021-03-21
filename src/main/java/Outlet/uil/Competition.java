package Outlet.uil;

import Outlet.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import static Outlet.Conn.getConnection;


/***
 * Manages teacher-created competitions, has post and get methods. Manages interfacing and uses a Template to render.
 */
public class Competition {
    public Template template;
    private static Gson gson = new Gson();

    public Teacher teacher;
    private short[] judges;   // The list of judges this teacher has added. Judges can help grade competitions, but can't edit them

    public boolean published;
    public boolean isPublic;
    public boolean alternateExists; // If teams can have alternates
    public short numNonAlts;    // The maximum number of non alternate competitors on each team

    public EntryMap entries;
    ArrayList<FRQSubmission> frqSubmissions = new ArrayList<>();

    public ArrayList<Clarification> clarifications;

    public void setTemplate(boolean published, MCTest mc, FRQTest frq, String name, String description, short cid, boolean showScoreboard) {
        if(published) {
            template = new Template(name, description, mc, frq, cid, showScoreboard, this);
            // entries = new EntryMap();
            // template.updateScoreboard();
        } else {
            template = new Template(false, name, description, mc, frq, cid, showScoreboard, this);
            // entries = new EntryMap();
        }
    }

    public Competition(short cid, boolean published, boolean isPublic, String name, String description,
                       boolean alternateExists, short numNonAlts, MCTest mc, FRQTest frq, ArrayList<Clarification> clarifications,
                       short[] judges, boolean showScoreboard) {
        this.teacher = null;
        this.published = published;
        this.isPublic = isPublic;
        this.alternateExists = alternateExists;
        this.numNonAlts = numNonAlts;
        this.judges = judges;

        entries = new EntryMap();
        setTemplate(published, mc, frq, name, description, cid, showScoreboard);


        this.clarifications = clarifications;
    }

    public Competition(Teacher teacher, short cid, boolean published, boolean isPublic, String name, String description,
                       boolean alternateExists, short numNonAlts, MCTest mc, FRQTest frq, ArrayList<Clarification> clarifications,
                       short[] judges, boolean showScoreboard) {
        this(cid, published, isPublic, name, description, alternateExists, numNonAlts, mc, frq,clarifications, judges, showScoreboard);
        setTeacher(teacher);
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;

        /* Now, create the folder */
        if(template.frqTest.exists) {
            template.frqTest.setDirectories(template.cid, teacher.uid);
            template.frqTest.createProblemDirectories();
            template.frqTest.initializeFiles();
        }
    }

    /* Returns a new competition object that has been inserted into the database */
    public static Competition createCompetition(Teacher teacher, boolean published, boolean isPublic, String name,
                                                String description, boolean alternateExists, short numNonAlts,
                                                MCTest mcTest, FRQTest frqTest, short[] judges, boolean showScoreboard) throws SQLException {
        Connection  conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO competitions (uid, name, isPublic, description, " +
                "alternateExists, numNonAlts, mcKey, mcCorrectPoints, mcIncorrectPoints, mcInstructions, mcTestLink," +
                "mcOpens, mcTime, frqMaxPoints, frqIncorrectPenalty, frqProblemMap, frqStudentPack," +
                "frqJudgePacket, frqOpens, frqTime, type, published, clarifications,judges, showScoreboard, frqAutoGrade," +
                        "dryRunExists,dryRunStudentPacket,mcAutoGrade,mcGraded) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'[]',?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        stmt.setShort(1, teacher.uid);
        stmt.setString(2, name);
        stmt.setBoolean(3, isPublic);
        stmt.setString(4, description);
        stmt.setBoolean(5, alternateExists);
        stmt.setShort(6, numNonAlts);


        if(mcTest.exists) {
            stmt.setString(7, gson.toJson(mcTest.KEY));
            stmt.setShort(8,mcTest.CORRECT_PTS);
            stmt.setShort(9, mcTest.INCORRECT_PTS);
            stmt.setString(10,mcTest.INSTRUCTIONS);
            stmt.setString(11,mcTest.TEST_LINK);
            // stmt.setString(10,mcTest.ANSWERS);
            stmt.setString(12,mcTest.opens.DATE_STRING);
            stmt.setLong(13, mcTest.TIME);
        } else {
            stmt.setString(7, null);
            stmt.setShort(8, (short)0);
            stmt.setShort(9, (short)0);
            stmt.setString(10, null);
            stmt.setString(11, null);
            // stmt.setString(10, null);
            stmt.setString(12, null);
            stmt.setLong(13, 0);
        }

        if(frqTest.exists) {
            stmt.setShort(14, frqTest.MAX_POINTS);
            stmt.setShort(15, frqTest.INCORRECT_PENALTY);
            stmt.setString(16, FRQProblem.toJsonArray(frqTest.PROBLEM_MAP));
            stmt.setString(17, frqTest.STUDENT_PACKET);
            stmt.setString(18, frqTest.JUDGE_PACKET);
            stmt.setString(19, frqTest.opens.DATE_STRING);
            stmt.setLong(20, frqTest.TIME);
            stmt.setBoolean(25, frqTest.AUTO_GRADE);
        } else {
            stmt.setShort(14, (short)0);
            stmt.setShort(15, (short)0);
            stmt.setString(16, null);
            stmt.setString(17, null);
            stmt.setString(18, null);
            stmt.setString(19, null);
            stmt.setLong(20, 0);
            stmt.setBoolean(25,true);
        }

        int type = 0;   // 0 if just MC, 1 if just FRQ, 2 if both
        if(!mcTest.exists && frqTest.exists) {
            type = 1;
        } else if(mcTest.exists && frqTest.exists){
            type = 2;
        }
        stmt.setShort(21, (short) type);
        stmt.setBoolean(22, published);
        stmt.setString(23, gson.toJson(judges));
        stmt.setBoolean(24, showScoreboard);
        stmt.setBoolean(26, frqTest.DRYRUN_EXISTS);
        stmt.setString(27, frqTest.DRYRUN_STUDENT_PACKET);
        stmt.setBoolean(28, mcTest.AUTO_GRADE);
        stmt.setBoolean(29, mcTest.graded);

        System.out.println(stmt);
        stmt.execute();
        ResultSet rs = stmt.getGeneratedKeys();
        if(rs.next()) {
            short cid = (short)rs.getInt(1);

            // First, delete the table if it already exists
            stmt = conn.prepareStatement("DROP TABLE IF EXISTS `c" + cid + "`;");
            stmt.executeUpdate();

            // Now, create a table for the competition
            stmt = conn.prepareStatement("CREATE TABLE `c"+cid+"` (" +
                    "`tid` SMALLINT NOT NULL AUTO_INCREMENT UNIQUE," +
                    "`name` VARCHAR(25) NOT NULL UNIQUE," +
                    "`password` CHAR(153) NOT NULL," +
                    "`uids` TINYTEXT NOT NULL," +
                    "`altUID` SMALLINT NOT NULL," +
                    "`mc` TEXT NOT NULL," +
                    "`frqResponses` MEDIUMTEXT NOT NULL," +
                    "`individual` BOOLEAN NOT NULL DEFAULT FALSE," +
                    "PRIMARY KEY (`tid`))");
            System.out.println(stmt);
            stmt.executeUpdate();

            Competition competition = new Competition(teacher, cid, published, isPublic, name, description,alternateExists,
                    numNonAlts, mcTest, frqTest, new ArrayList<>(), new short[0], showScoreboard);
            competition.setJudges(judges);  // We do this so that the teacher objects are updated with this competition that they are judging
            System.out.println("CID = " + cid + ", " + competition.template.cid);
            UIL.addCompetition(competition);

            teacher.competitions.add(competition);

            return competition;
        }
        return null;
    }

    public void updateDB(String name, String description, boolean alternateExists, short numNonAlts, MCTest mcTest,
                         FRQTest frqTest, short[] judges, boolean showScoreboard) throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("UPDATE competitions SET uid=?, name=?, isPublic=?, description=?, " +
                        "alternateExists=?, numNonAlts=?, mcKey=?, mcCorrectPoints=?, mcIncorrectPoints=?, mcInstructions=?, mcTestLink=?," +
                        "mcOpens=?, mcTime=?, frqMaxPoints=?, frqIncorrectPenalty=?, frqProblemMap=?, frqStudentPack=?," +
                        "frqJudgePacket=?, frqOpens=?, frqTime=?, type=?, published=?, clarifications=?, judges=?, showScoreboard=?, " +
                        "frqAutoGrade=?, dryRunExists=?, dryRunStudentPacket=?, mcAutoGrade=?, mcGraded=? WHERE cid=?",
                Statement.RETURN_GENERATED_KEYS);
        stmt.setShort(1, teacher.uid);
        stmt.setString(2, name);
        stmt.setBoolean(3, isPublic);
        stmt.setString(4,description);
        stmt.setBoolean(5, alternateExists);
        stmt.setShort(6, numNonAlts);

        if(mcTest.exists) {
            stmt.setString(7, gson.toJson(mcTest.KEY));
            stmt.setShort(8,mcTest.CORRECT_PTS);
            stmt.setShort(9, mcTest.INCORRECT_PTS);
            stmt.setString(10,mcTest.INSTRUCTIONS);
            stmt.setString(11,mcTest.TEST_LINK);
            // stmt.setString(10,mcTest.ANSWERS);
            stmt.setString(12,mcTest.opens.DATE_STRING);
            stmt.setLong(13, mcTest.TIME);
        } else {
            stmt.setString(7, null);
            stmt.setShort(8, (short)0);
            stmt.setShort(9, (short)0);
            stmt.setString(10, null);
            stmt.setString(11, null);
            // stmt.setString(10, null);
            stmt.setString(12, null);
            stmt.setLong(13, 0);
        }

        if(frqTest.exists) {
            stmt.setShort(14, frqTest.MAX_POINTS);
            stmt.setShort(15, frqTest.INCORRECT_PENALTY);
            stmt.setString(16, FRQProblem.toJsonArray(frqTest.PROBLEM_MAP));
            stmt.setString(17, frqTest.STUDENT_PACKET);
            stmt.setString(18, frqTest.JUDGE_PACKET);
            stmt.setString(19, frqTest.opens.DATE_STRING);
            stmt.setLong(20, frqTest.TIME);
            stmt.setBoolean(26, frqTest.AUTO_GRADE);
        } else {
            stmt.setShort(14, (short)0);
            stmt.setShort(15, (short)0);
            stmt.setString(16, null);
            stmt.setString(17, null);
            stmt.setString(18, null);
            stmt.setString(19, null);
            stmt.setLong(20, 0);
            stmt.setBoolean(26, true);
        }

        int type = 0;   // 0 if just MC, 1 if just FRQ, 2 if both
        if(!mcTest.exists && frqTest.exists) {
            type = 1;
        } else if(mcTest.exists && frqTest.exists){
            type = 2;
        }
        stmt.setShort(21, (short) type);
        stmt.setBoolean(22, published);
        stmt.setString(23, Clarification.toJson(this.clarifications).toString());
        stmt.setString(24, gson.toJson(judges));
        stmt.setBoolean(25, showScoreboard);
        stmt.setBoolean(27, frqTest.DRYRUN_EXISTS);
        stmt.setString(28, frqTest.DRYRUN_STUDENT_PACKET);
        stmt.setBoolean(29, mcTest.AUTO_GRADE);
        stmt.setBoolean(30, mcTest.graded);
        stmt.setShort(31, template.cid);
        stmt.executeUpdate();
    }

    public void update() throws SQLException {
        updateDB(template.name, template.description, alternateExists,numNonAlts, template.mcTest, template.frqTest, judges,
                template.showScoreboard);
    }

    /* Updates the competition in the database and the template */
    public void update(Teacher teacher, boolean published, boolean isPublic, boolean alternateExists, short numNonAlts,
                       String name, String description, MCTest mcTest, FRQTest frqTest, short[] judges, boolean showScoreboard) throws SQLException {
        frqTest.setDirectories(template.cid, teacher.uid);
        frqTest.initializeFiles();

        updateDB(name, description, alternateExists, numNonAlts, mcTest, frqTest, judges, showScoreboard);

        this.isPublic = isPublic;

        this.alternateExists = alternateExists;
        this.numNonAlts = numNonAlts;
        this.setJudges(judges);
        setTemplate(published, mcTest, frqTest, name, description, template.cid, showScoreboard);
    }

    public void setJudges(short[] judges) {
        for(short uid: this.judges) {
            Teacher teacher = TeacherMap.getByUID(uid);
            teacher.judging.remove(this);
        }

        this.judges = judges;
        for(short uid: this.judges) {
            Teacher teacher = TeacherMap.getByUID(uid);
            teacher.judging.add(this);
        }
    }

    public short[] getJudges() {
        return judges;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        template.render(request,response);
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User userBefore = UserMap.getUserByRequest(request);
        if(userBefore == null || !Conn.isLoggedIn(userBefore.token)){ // They are not logged in, return nothing
            return;
        }

        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        UserStatus userStatus = UserStatus.getCompeteStatus(userBefore, this);
        CompetitionStatus competitionStatus = new CompetitionStatus(template.mcTest, template.frqTest);
        if (action.equals("updatePage")) {  // Reloads the columns portion of the page
            writer.write("{\"updatedHTML\":\""+StringEscapeUtils.escapeHtml4(template.getColumnsHTML(userBefore,
                    UserStatus.getCompeteStatus(userBefore, this), competitionStatus))+"\"}");
            return;
        }
        if(userStatus.teacher) {
            if(userStatus.admin) {
                if(action.equals("showFRQSubmission")) {
                    int id = Integer.parseInt(request.getParameter("id"));
                    if (frqSubmissions.size() > id) {
                        FRQSubmission submission = frqSubmissions.get(id);
                        FRQProblem problem = template.frqTest.PROBLEM_MAP[submission.problemNumber];
                        JsonObject compJ = new JsonObject();
                        compJ.addProperty("name", StringEscapeUtils.escapeHtml4(problem.name));
                        compJ.addProperty("team", StringEscapeUtils.escapeHtml4(submission.entry.tname));
                        compJ.addProperty("result", submission.getResultString());

                        if (submission.showInput())
                            compJ.addProperty("input", StringEscapeUtils.escapeHtml4(submission.input).replaceAll("\r?\n","<br>"));
                        if (submission.showOutput()) {
                            compJ.addProperty("output", StringEscapeUtils.escapeHtml4(submission.output).replaceAll("\r?\n", "<br>"));
                            if(problem.outputFile == null) {
                                template.frqTest.loadOutputFile(submission.problemNumber, problem);
                            }
                            compJ.addProperty("outputFile", problem.outputFile.replaceAll("\r?\n", "<br>"));
                        }

                        compJ.addProperty("graded", submission.graded);
                        writer.write(new Gson().toJson(compJ));
                    }
                } else if(action.equals("changeFRQJudgement")) {
                    int id = Integer.parseInt(request.getParameter("id"));
                    int newResultId = Integer.parseInt(request.getParameter("judgeId"));

                    if (frqSubmissions.size() > id) {
                        FRQSubmission submission = frqSubmissions.get(id);

                        boolean oldShowOutput = submission.showOutput();
                        //boolean oldTakePenalty = submission.takePenalty();
                        //boolean oldTakeNoPenalty = submission.noPenalty();
                        //FRQSubmission.Result oldResult = submission.result;
                        switch (newResultId) {
                            case 0:
                                submission.result = FRQSubmission.Result.CORRECT;
                                break;
                            case 1:
                                submission.result = FRQSubmission.Result.INCORRECT;
                                break;
                            case 3:
                                submission.result = FRQSubmission.Result.COMPILETIME_ERROR;
                                break;
                            case 4:
                                submission.result = FRQSubmission.Result.RUNTIME_ERROR;
                                break;
                            case 5:
                                submission.result = FRQSubmission.Result.EMPTY_FILE;
                                break;
                            case 6:
                                submission.result = FRQSubmission.Result.EXCEEDED_TIME_LIMIT;
                                break;
                            case 7:
                                submission.result = FRQSubmission.Result.UNCLEAR_FILE_TYPE;
                                break;
                            case 8:
                                submission.result = FRQSubmission.Result.PACKAGE_ERROR;
                                break;
                            case 9:
                                submission.result = FRQSubmission.Result.FORMAT_ERROR;
                                break;
                            case 2:
                            default:
                                submission.result = FRQSubmission.Result.SERVER_ERROR;
                            /*case 2:
                                submission.result = FRQSubmission.Result.SERVER_ERROR;
                                break;*/
                        }

                        submission.entry.recalculateFRQScore(submission.problemNumber);
                            /*Pair<Short, ArrayList<FRQSubmission>> problem = submission.entry.frqResponses[submission.problemNumber - 1];
                            short currentValue = problem.key;
                            //if(submission.result == FRQSubmission.Result.SERVER_ERROR) {    // We switched it to not giving a penalty, so reduce the extreme
                            //    problem.key = (short)((Math.abs(currentValue) - 1) * (currentValue % Math.abs(currentValue)));
                            //} else {
                            if(oldTakeNoPenalty && currentValue != 0) {  // We are now taking a penalty or gaining the points, so add the extreme
                                problem.key = (short)((Math.abs(currentValue) + 1) * (currentValue % Math.abs(currentValue)));
                                currentValue = problem.key;
                            }

                            if (submission.result == FRQSubmission.Result.CORRECT) {    // They switched it to right answer
                                currentValue = (short) Math.abs(currentValue);
                                problem.key = currentValue;
                                submission.entry.frqScore += template.frqTest.calcScore(currentValue);
                            } else if (oldResult == FRQSubmission.Result.CORRECT) {     // They switched it from right answer
                                submission.entry.frqScore -= template.frqTest.calcScore(currentValue);
                                problem.key = (short) (-1 * Math.abs(currentValue));
                            } else return;
                            //}*/

                        submission.entry.update();

                        if(submission.graded || template.frqTest.AUTO_GRADE) {
                            submission.entry.socketSendFRQProblems();
                            template.updateScoreboard();
                        }
                    }
                } else if(action.equals("showMCSubmission")) {
                    short uid = Short.parseShort(request.getParameter("uid"));
                    Student student = StudentMap.getByUID(uid);
                    if(student == null) return;

                    UILEntry entry = student.cids.get(template.cid);

                    if (entry != null) {
                        JsonObject compJ = new JsonObject();
                        MCSubmission submission = entry.mc.get(uid);
                        if(submission == null || !submission.finished) return;

                        compJ.addProperty("user", StringEscapeUtils.escapeHtml4(student.getName()));
                        compJ.addProperty("team", StringEscapeUtils.escapeHtml4(entry.tname));
                        compJ.addProperty("answers", template.getFinishedMCHelper(submission, uid, true, competitionStatus));
                        compJ.addProperty("scoringReport", gson.toJson(submission.scoringReport));

                        writer.write(new Gson().toJson(compJ));
                    }
                } else if(action.equals("changeMCJudgement")) {
                    short uid = Short.parseShort(request.getParameter("uid"));
                    String judgement = request.getParameter("judgement");
                    int probNum = Short.parseShort(request.getParameter("probNum")) - 1;

                    Student student = StudentMap.getByUID(uid);
                    UILEntry entry = student.cids.get(this.template.cid);
                    System.out.println("Changing MC Judgement, uid="+uid+", judgement="+judgement+", probNum="+probNum);
                    if(entry != null && entry.mc.containsKey(uid)) {
                        System.out.println("User in mc");
                        MCSubmission submission = entry.mc.get(uid);
                        if(submission != null && submission.finished) {
                            System.out.println("Finished");
                            if(judgement.equals("Correct")) {
                                submission.answers[probNum] = template.mcTest.KEY[probNum][0];
                            } else if(judgement.equals("Incorrect")) {
                                if(template.mcTest.KEY[probNum][0].equals("a")) submission.answers[probNum] = "b";
                                else submission.answers[probNum] = "a";
                            } else {
                                submission.answers[probNum] = MCTest.SKIP_CODE;
                            }
                            submission = entry.scoreMC(uid, submission.answers);
                            System.out.println("SCORING REPORT="+gson.toJson(submission.scoringReport));
                            template.updateScoreboard();

                            for(short teamMemberUID: entry.uids) {
                                CompetitionSocket socket = CompetitionSocket.connected.get(teamMemberUID);
                                if(socket != null)
                                    socket.send("[\"reScoreMC\",\""+uid+"\",\""+submission.scoringReport[0]+"\"]");
                            }
                        }
                    }
                } else if(action.equals("createteam")) {
                    String tname = request.getParameter("tname");
                    if(tname == null || tname.isEmpty()) {
                        writer.write("{\"status\":\"error\",\"error\":\"Team name is empty.\"}");
                        return;
                    }
                    try {

                        for(UILEntry entry: entries.allEntries) {
                            if(entry.tname.equals(tname)) {
                                writer.write("{\"status\":\"error\",\"error\":\"Team name is taken.\"}");
                                return;
                            }
                        }

                        int leftLimit = 48; // numeral '0'
                        int rightLimit = 90; // letter 'Z'
                        Random random = new Random();

                        String code;
                        do {
                            code = random.ints(leftLimit, rightLimit + 1)
                                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90))
                                    .limit(6)
                                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                    .toString();    // Get a 6-digit team code
                        } while (entries.getByPassword(code) != null);

                        UILEntry entry = new UILEntry(tname, code, this);
                        entry.insert();
                        entries.addEntry(entry);
                        template.updateScoreboard();

                        JsonObject data = new JsonObject();
                        data.addProperty("status", "success");
                        data.addProperty("tname", tname);
                        data.addProperty("code", entry.password);
                        data.addProperty("tid", entry.tid);
                        writer.write(data.toString());
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                        return;
                    }
                }
            }
            return;
        }

        Student user = (Student) userBefore;
        if(userStatus.signedUp) { // If their team is already signed up for this competition
            UILEntry temp = user.cids.get(template.cid);
            if(action.equals("leaveTeam")) {
                int status = temp.leaveTeam(user);
                if(status != 0) { ;
                    writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                    return;
                } else {

                }
            } else if(!competitionStatus.frqBefore && action.equals("grabFRQProblems")) {
                if(temp.altUID == user.uid) writer.write("{\"frqProblemsHTML\":\"\"}");
                else writer.write("{\"frqProblemsHTML\":\""+template.getFRQProblems(temp)+"\"}");
            } else if (action.equals("submitMC")) {
                if(competitionStatus.mcDuring || competitionStatus.mcOverflow) {    // submissions are open
                    String[] answers = gson.fromJson(request.getParameter("answers"), String[].class);
                    MCSubmission submission = temp.scoreMC(user.uid, answers);
                    writer.write("{\"mcHTML\":\"" + template.getFinishedMC(submission, temp.tid, user.uid, competitionStatus) + "\"}");
                    temp.update();
                    template.updateScoreboard();

                    // Send it to the teacher and judges
                    CompetitionSocket socket = CompetitionSocket.connected.get(teacher.uid);
                    if (socket != null) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("action", "addSmallMC");
                        obj.addProperty("html", template.getSmallMC(user, temp, submission));
                        socket.send(gson.toJson(obj));
                    }

                    for(short judgeUID: judges) {
                        socket = CompetitionSocket.connected.get(judgeUID);
                        if (socket != null) {
                            JsonObject obj = new JsonObject();
                            obj.addProperty("action", "addSmallMC");
                            obj.addProperty("html", template.getSmallMC(user, temp, submission));
                            socket.send(gson.toJson(obj));
                        }
                    }
                } else {    // Submissions are closed
                    writer.write("{\"mcHTML\":\"" + template.getMCHTML(user,UserStatus.getCompeteStatus(user, this), competitionStatus) + "\"}");
                }
                return;
            } else if(action.equals("submitFRQ")) {
                if(temp.altUID != user.uid && (competitionStatus.frqDuring || competitionStatus.frqOverflow)) {
                    short probNum = Short.parseShort(request.getParameter("probNum"));

                    if(template.frqTest.dryRunMode && probNum >= 1) { // It is in the dry run mode and they are submitting a non dry run
                        writer.write("{\"status\":\"error\",\"error\":\"You may only submit the dry run.\"}");
                        return;
                    } else if(!template.frqTest.dryRunMode && probNum <= 0) { // It isn't in the dry run mode and they are submitting a dry run
                        writer.write("{\"status\":\"error\",\"error\":\"The dry run has closed.\"}");
                        return;
                    } else if (temp.frqResponses[probNum].key > 0) {
                        writer.write("{\"status\":\"error\",\"error\":\"You've already gotten this problem.\"}");
                        return;
                    }

                    Part filePart = request.getPart("textfile");
                    InputStream fileContent = filePart.getInputStream();

                    byte[] bytes = new byte[fileContent.available()];
                    if(bytes.length > UIL.MAX_FILE_SIZE) {
                        writer.write("{\"status\":\"error\",\"error\":\"File size must be less than 1 MB.\"}");
                        return;
                    }

                    fileContent.read(bytes);

                    String fname = filePart.getSubmittedFileName();
                    if (fname == null || fname.isEmpty() || !fname.matches("^[a-zA-Z0-9.]*$")) {   // Make sure it doesn't have commands
                        writer.write("{\"status\":\"error\",\"error\":\"The file name must be alphanumeric.\"}");
                        return;
                    }

                    FRQSubmission submission = template.frqTest.score(probNum, bytes, fname, user.uid, temp.tid);
                    submission.entry = temp;
                    temp.addFRQRun(submission, probNum);
                    frqSubmissions.add(submission);

                    if(template.frqTest.AUTO_GRADE && submission.graded) {
                        /*if (submission.result == FRQSubmission.Result.CORRECT) {
                            writer.write("{\"status\":\"success\",\"scored\":\"You gained points!\"}");
                        } else if (submission.result == FRQSubmission.Result.COMPILETIME_ERROR) {
                            writer.write("{\"status\":\"error\",\"error\":\"Compile-time error.\"}");
                        } else if (submission.result == FRQSubmission.Result.RUNTIME_ERROR) {
                            writer.write("{\"status\":\"error\",\"error\":\"Runtime error\"}");
                        } else if (submission.result == FRQSubmission.Result.EXCEEDED_TIME_LIMIT) {
                            writer.write("{\"status\":\"error\",\"error\":\"Time limit exceeded.\"}");
                        } else if (submission.result == FRQSubmission.Result.INCORRECT) {
                            writer.write("{\"status\":\"error\",\"error\":\"Wrong answer.\"}");
                        } else if (submission.result == FRQSubmission.Result.SERVER_ERROR) {
                            writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                        } else if (submission.result == FRQSubmission.Result.EMPTY_FILE) {
                            writer.write("{\"status\":\"error\",\"error\":\"Empty file.\"}");
                        } else if (submission.result == FRQSubmission.Result.UNCLEAR_FILE_TYPE) {
                            writer.write("{\"status\":\"error\",\"error\":\"Unclear file type. Files must end in .java, .py, or .cpp.\"}");
                        }*/

                        template.updateScoreboard();

                        // Update all of their team member's frqProblems
                        temp.socketSendFRQProblems();
                    } //else {    // send a placeholder message saying that the teacher is grading it

                    writer.write("{\"status\":\"success\",\"scored\":\""+fname+" has been submitted.\"}");

                    // Send it to the teacher and the judges
                    CompetitionSocket socket = CompetitionSocket.connected.get(teacher.uid);
                    if (socket != null) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("action", "addSmallFRQ");
                        obj.addProperty("html", template.getSmallFRQ(frqSubmissions.indexOf(submission), submission));
                        socket.send(gson.toJson(obj));
                    }

                    for(short judgeUID: judges) {
                        socket = CompetitionSocket.connected.get(judgeUID);
                        if (socket != null) {
                            JsonObject obj = new JsonObject();
                            obj.addProperty("action", "addSmallFRQ");
                            obj.addProperty("html", template.getSmallFRQ(frqSubmissions.indexOf(submission), submission));
                            socket.send(gson.toJson(obj));
                        }
                    }
                } else {
                    writer.write("{\"status\":\"error\",\"error\":\"FRQ submissions are closed.\"}");
                    return;
                }
            } else if(competitionStatus.frqFinished && action.equals("finishFRQ")) {
                writer.write("{\"frqHTML\":\""+template.getFinishedFRQ(temp)+"\"}");
            }
        } else if(action.equals("jointeam")) {
            String code = request.getParameter("code");
            if(code == null || code.length() != 6) {
                writer.write("{\"status\":\"error\",\"error\":\"Team code must be 6 characters.\"}");
                return;
            }
            boolean isAlternate = request.getParameter("isAlternate").equals("true");
            try {
                UILEntry entry = entries.getByPassword(code);
                if(entry != null) {
                    if(entry.uids.size() > (numNonAlts + (alternateExists?1:0)) || entry.individual && entry.uids.size() >= 1) {    // This team is full
                        writer.write("{\"status\":\"error\",\"error\":\"Team is full.\"}");
                        return;
                    } else if(alternateExists && isAlternate && entry.altUID > 0) { // They are trying to sign up as the alternate and this team already has one
                        writer.write("{\"status\":\"error\",\"error\":\"This team already has an alternate.\"}");
                        return;
                    }

                    user.cids.put(this.template.cid, entry);
                    entry.uids.add(user.uid);
                    if(alternateExists && isAlternate) entry.altUID = user.uid;
                    entry.updateUIDS();
                    template.updateScoreboard();
                    writer.write("{\"status\":\"success\",\"reload\":\"/uil\"}");

                    // Tell the existing team members to get a new html
                    for(short uid: entry.uids) {
                        if(uid == user.uid) continue;

                        CompetitionSocket socket = CompetitionSocket.connected.get(uid);
                        if (socket != null) {
                            JsonObject obj = new JsonObject();
                            obj.addProperty("action","updateTeam");
                            obj.addProperty("html", template.getTeamMembers(StudentMap.getByUID(uid), entry));
                            try {
                                socket.send(gson.toJson(obj));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    return;
                }
                writer.write("{\"status\":\"error\",\"error\":\"Code is incorrect.\"}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                return;
            }
        } else if(action.equals("createteam")) {
            String tname = request.getParameter("tname");
            if(tname == null || tname.isEmpty()) {
                writer.write("{\"status\":\"error\",\"error\":\"Team name is empty.\"}");
                return;
            }
            try {

                for(UILEntry entry: entries.allEntries) {
                    if(entry.tname.equals(tname)) {
                        writer.write("{\"status\":\"error\",\"error\":\"Team name is taken.\"}");
                        return;
                    }
                }

                int leftLimit = 48; // numeral '0'
                int rightLimit = 90; // letter 'Z'
                Random random = new Random();

                String code;
                do {
                    code = random.ints(leftLimit, rightLimit + 1)
                            .filter(i -> (i <= 57 || i >= 65) && (i <= 90))
                            .limit(6)
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString();    // Get a 6-digit team code
                } while (entries.getByPassword(code) != null);

                UILEntry entry = new UILEntry(tname, code, this, user);
                entry.insert();
                entries.addEntry(entry);
                user.cids.put(template.cid, entry);
                template.updateScoreboard();

                JsonObject data = new JsonObject();
                data.addProperty("status", "success");
                data.addProperty("tname", tname);
                data.addProperty("tid", entry.tid);
                writer.write("{\"status\":\"success\"}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                return;
            }
        }
    }

    /***
     * Gets a UIL entry from the database or from the entries map if already loaded, using the specified team id.
     * @param tid
     * @return
     */
    public UILEntry getEntry(short tid) throws SQLException {
        UILEntry entry = entries.getByTid(tid);
        if(entry != null) return entry;

        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `c"+template.cid+"` WHERE tid=?");
        stmt.setShort(1,tid);
        ResultSet rs = stmt.executeQuery();
        if(rs.next()) {
            entry = new UILEntry(rs,this);
            for(Pair<Short, ArrayList<FRQSubmission>> pair: entry.frqResponses) {
                this.frqSubmissions.addAll(pair.value);
            }
            this.entries.addEntry(entry);
            return entry;
        }
        return null;
    }

    public void loadAllEntries() throws SQLException {
        entries = new EntryMap();

        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `c"+template.cid+"`");
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
            UILEntry entry = new UILEntry(rs, this);
            try {
                for(short uid: entry.uids) {
                    StudentMap.getByUID(uid).cids.put(template.cid, entry);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            entries.addEntry(entry);
            for(Pair<Short, ArrayList<FRQSubmission>> pair: entry.frqResponses) {
                this.frqSubmissions.addAll(pair.value);
            }
        }
    }

    // Deletes this competition. Does NOT update the teacher, but does update the students. Deletes the Hands-On testcases as well
    public void delete() {
        System.out.println("DELETING !!!");
        for(short uid: judges) {
            Teacher teacher = TeacherMap.getByUID(uid);
            teacher.judging.remove(this);
        }

        Collection<UILEntry> values = entries.tidMap.values();
        for(UILEntry entry: values) {
            for(short uid: entry.uids) {
                Student s = StudentMap.getByUID(uid);
                s.cids.remove(this.template.cid);
                if(s.temp) {
                    Connection conn = getConnection();
                    PreparedStatement stmt = null;
                    try {
                        stmt = conn.prepareStatement("DELETE FROM users WHERE uid=?");
                        stmt.setShort(1, s.uid);
                        stmt.executeUpdate();
                        UserMap.delUser(s);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        entries.tidMap.clear();
        entries.passwordMap.clear();

        Connection conn = Conn.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("DROP TABLE `c" + template.cid + "`");
            stmt.executeUpdate();

            stmt = conn.prepareStatement("DELETE FROM competitions WHERE uid=? and cid=?;");
            stmt.setShort(1, teacher.uid);
            stmt.setShort(2, template.cid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(template.frqTest.exists) template.frqTest.deleteDirectory(new File(template.frqTest.testcaseDirPath));

        // Finally, tell all of the people viewing this competition to stop viewing it
        ArrayList<CompetitionSocket> sockets = CompetitionSocket.competitions.get(template.cid);
        if(sockets != null) {
            for (CompetitionSocket socket : sockets) {
                JsonObject obj = new JsonObject();
                obj.addProperty("action", "competitionDeleted");
                try {
                    socket.send(gson.toJson(obj));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class EntryMap {
    public ArrayList<UILEntry> allEntries = new ArrayList<>();
    public HashMap<Short, UILEntry> tidMap = new HashMap<>();    // Maps a teams tid to its UILEntry
    public HashMap<String, UILEntry> passwordMap = new HashMap<>();    // Maps a teams password to its UILEntry

    public UILEntry getByTid(short tid) {
        return tidMap.get(tid);
    }

    public UILEntry getByPassword(String password) {
        return passwordMap.get(password.toUpperCase());
    }

    public void addEntry(UILEntry entry) {
        allEntries.add(entry);
        tidMap.put(entry.tid, entry);
        passwordMap.put(entry.password.toUpperCase(), entry);
    }

    public void delEntry(UILEntry entry) {
        allEntries.remove(entry);
        tidMap.remove(entry.tid, entry);
        passwordMap.remove(entry.password, entry);
    }
}

class SortCompByDate implements Comparator<Competition> {
    public int compare(Competition c1, Competition c2) {
        Countdown c1Opens;
        if(c1.template.mcFirst) c1Opens = c1.template.mcTest.opens;
        else c1Opens = c1.template.frqTest.opens;

        Countdown c2Opens;
        if(c2.template.mcFirst) c2Opens = c2.template.mcTest.opens;
        else c2Opens = c2.template.frqTest.opens;

        return c1Opens.date.compareTo(c2Opens.date);
    }
}