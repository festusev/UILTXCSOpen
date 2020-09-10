package Outlet.uil;

import Outlet.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;


/***
 * Manages teacher-created competitions, has post and get methods. Manages interfacing and uses a Template to render.
 */
public class Competition {
    public Template template;
    private static Gson gson = new Gson();

    public Teacher teacher;
    public boolean published;
    public boolean isPublic;

    public EntryMap entries;
    ArrayList<FRQSubmission> frqSubmissions = new ArrayList<>();

    private void setTemplate(boolean published, MCTest mc, FRQTest frq, String name, String description, short cid) {
        if(published) {
            template = new Template(name, description, mc, frq, cid, this);
            template.updateScoreboard();
            entries = new EntryMap();
        } else {
            template = new Template(false, name, description, mc, frq, cid, this);
            entries = new EntryMap();
        }
    }

    public Competition(Teacher teacher, short cid, boolean published, boolean isPublic, String name, String description,
                       MCTest mc, FRQTest frq) {
        frq.setDirectories(cid, teacher.uid);

        this.teacher = teacher;
        this.published = published;
        this.isPublic = isPublic;
        setTemplate(published, mc, frq, name, description, cid);

        /* Now, create the folder */
        if(frq.exists) {
            frq.setDirectories(cid, teacher.uid);
            frq.createProblemDirectories();
            frq.initializeFiles();
        }
    }

    /* Returns a new competition object that has been inserted into the database */
    public static Competition createCompetition(Teacher teacher, boolean published, boolean isPublic, String name,
                                                String description, MCTest mcTest, FRQTest frqTest) throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO competitions (uid, name, isPublic, description, " +
                "mcKey, mcCorrectPoints, mcIncorrectPoints, mcInstructions, mcTestLink," +
                "mcAnswers, mcOpens, mcTime, frqMaxPoints, frqIncorrectPenalty, frqProblemMap, frqStudentPack," +
                "frqJudgePacket, frqOpens, frqTime, type, published) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        stmt.setShort(1, teacher.uid);
        stmt.setString(2, name);
        stmt.setBoolean(3, isPublic);
        stmt.setString(4, description);


        if(mcTest.exists) {
            stmt.setString(5, gson.toJson(mcTest.KEY));
            stmt.setShort(6,mcTest.CORRECT_PTS);
            stmt.setShort(7, mcTest.INCORRECT_PTS);
            stmt.setString(8,mcTest.INSTRUCTIONS);
            stmt.setString(9,mcTest.TEST_LINK);
            stmt.setString(10,mcTest.ANSWERS);
            stmt.setString(11,mcTest.opens.DATE_STRING);
            stmt.setLong(12, mcTest.TIME);
        } else {
            stmt.setString(5, null);
            stmt.setShort(6, (short)0);
            stmt.setShort(7, (short)0);
            stmt.setString(8, null);
            stmt.setString(9, null);
            stmt.setString(10, null);
            stmt.setString(11, null);
            stmt.setLong(12, 0);
        }

        if(frqTest.exists) {
            stmt.setShort(13, frqTest.MAX_POINTS);
            stmt.setShort(14, frqTest.INCORRECT_PENALTY);
            stmt.setString(15, gson.toJson(frqTest.PROBLEM_MAP));
            stmt.setString(16, frqTest.STUDENT_PACKET);
            stmt.setString(17, frqTest.JUDGE_PACKET);
            stmt.setString(18, frqTest.opens.DATE_STRING);
            stmt.setLong(19, frqTest.TIME);
        } else {
            stmt.setShort(13, (short)0);
            stmt.setShort(14, (short)0);
            stmt.setString(15, null);
            stmt.setString(16, null);
            stmt.setString(17, null);
            stmt.setString(18, null);
            stmt.setLong(19, 0);
        }

        int type = 0;   // 0 if just MC, 1 if just FRQ, 2 if both
        if(!mcTest.exists && frqTest.exists) {
            type = 1;
        } else if(mcTest.exists && frqTest.exists){
            type = 2;
        }
        stmt.setShort(20, (short) type);
        stmt.setBoolean(21, published);

        System.out.println(stmt);
        stmt.execute();
        ResultSet rs = stmt.getGeneratedKeys();
        if(rs.next()) {
            short cid = (short)rs.getInt(1);

            // Now, create a table for the competition
            stmt = conn.prepareStatement("CREATE TABLE `c"+cid+"` (" +
                    "`tid` SMALLINT NOT NULL AUTO_INCREMENT UNIQUE," +
                    "`name` VARCHAR(25) NOT NULL UNIQUE," +
                    "`password` CHAR(153) NOT NULL," +
                    "`uids` TINYTEXT NOT NULL,`mc` TEXT NOT NULL," +
                    "`frqResponses` TINYTEXT NOT NULL,`frqScore` SMALLINT DEFAULT 0," +
                    "PRIMARY KEY (`tid`))");
            stmt.executeUpdate();

            Competition competition = new Competition(teacher, cid, published, isPublic, name, description, mcTest, frqTest);
            System.out.println("CID = " + cid + ", " + competition.template.cid);
            UIL.addCompetition(competition);

            teacher.cids.add(cid);
            teacher.updateUser(false);

            return competition;
        }
        return null;
    }

    public void unPublish() throws SQLException {
        published = false;
        UIL.unPublish(this);

        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("UPDATE competitions SET published=? WHERE cid=?");
        stmt.setBoolean(1, false);
        stmt.setShort(2, template.cid);
        stmt.executeUpdate();
    }

    /* Updates the competition in the database and the template */
    public void update(Teacher teacher, boolean published, boolean isPublic, String name, String description, MCTest mcTest, FRQTest frqTest) throws SQLException {
        frqTest.setDirectories(template.cid, teacher.uid);
        frqTest.initializeFiles();

        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("UPDATE competitions SET uid=?, name=?, isPublic=?, description=?, " +
                        "mcKey=?, mcCorrectPoints=?, mcIncorrectPoints=?, mcInstructions=?, mcTestLink=?," +
                        "mcAnswers=?, mcOpens=?, mcTime=?, frqMaxPoints=?, frqIncorrectPenalty=?, frqProblemMap=?, frqStudentPack=?," +
                        "frqJudgePacket=?, frqOpens=?, frqTime=?, type=?, published=? WHERE cid=?",
                Statement.RETURN_GENERATED_KEYS);
        stmt.setShort(1, teacher.uid);
        stmt.setString(2, name);
        stmt.setBoolean(3, isPublic);
        stmt.setString(4,description);


        if(mcTest.exists) {
            stmt.setString(5, gson.toJson(mcTest.KEY));
            stmt.setShort(6,mcTest.CORRECT_PTS);
            stmt.setShort(7, mcTest.INCORRECT_PTS);
            stmt.setString(8,mcTest.INSTRUCTIONS);
            stmt.setString(9,mcTest.TEST_LINK);
            stmt.setString(10,mcTest.ANSWERS);
            stmt.setString(11,mcTest.opens.DATE_STRING);
            stmt.setLong(12, mcTest.TIME);
        } else {
            stmt.setString(5, null);
            stmt.setShort(6, (short)0);
            stmt.setShort(7, (short)0);
            stmt.setString(8, null);
            stmt.setString(9, null);
            stmt.setString(10, null);
            stmt.setString(11, null);
            stmt.setLong(12, 0);
        }

        if(frqTest.exists) {
            stmt.setShort(13, frqTest.MAX_POINTS);
            stmt.setShort(14, frqTest.INCORRECT_PENALTY);
            stmt.setString(15, gson.toJson(frqTest.PROBLEM_MAP));
            stmt.setString(16, frqTest.STUDENT_PACKET);
            stmt.setString(17, frqTest.JUDGE_PACKET);
            stmt.setString(18, frqTest.opens.DATE_STRING);
            stmt.setLong(19, frqTest.TIME);
        } else {
            stmt.setShort(13, (short)0);
            stmt.setShort(14, (short)0);
            stmt.setString(15, null);
            stmt.setString(16, null);
            stmt.setString(17, null);
            stmt.setString(18, null);
            stmt.setLong(19, 0);
        }

        int type = 0;   // 0 if just MC, 1 if just FRQ, 2 if both
        if(!mcTest.exists && frqTest.exists) {
            type = 1;
        } else if(mcTest.exists && frqTest.exists){
            type = 2;
        }
        stmt.setShort(20, (short) type);
        stmt.setBoolean(21, published);
        stmt.setShort(22, template.cid);
        stmt.executeUpdate();

        this.isPublic = isPublic;
        setTemplate(published, mcTest, frqTest, name, description, template.cid);
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

        CompetitionStatus competitionStatus = new CompetitionStatus(template.mcTest, template.frqTest);
        if (action.equals("updatePage")) {  // Reloads the columns portion of the page
            writer.write("{\"updatedHTML\":\""+StringEscapeUtils.escapeHtml4(template.getColumnsHTML(userBefore,
                    UserStatus.getCompeteStatus(userBefore, template.cid), competitionStatus))+"\"}");
            return;
        }
        if(userBefore.teacher) {
            if(((Teacher)userBefore).cids.contains(template.cid)) {
                if(action.equals("showFRQSubmission")) {
                    int id = Integer.parseInt(request.getParameter("id"));
                    if (frqSubmissions.size() > id) {
                        FRQSubmission submission = frqSubmissions.get(id);
                        JsonObject compJ = new JsonObject();
                        compJ.addProperty("name", StringEscapeUtils.escapeHtml4(template.frqTest.PROBLEM_MAP[submission.problemNumber - 1]));
                        compJ.addProperty("team", StringEscapeUtils.escapeHtml4(submission.entry.tname));
                        compJ.addProperty("result", submission.getCondensedResult());

                        if (submission.showInput())
                            compJ.addProperty("input", StringEscapeUtils.escapeHtml4(submission.input));
                        if (submission.showOutput())
                            compJ.addProperty("output", StringEscapeUtils.escapeHtml4(submission.output));
                        writer.write(new Gson().toJson(compJ));
                    }
                } else if(action.equals("changeFRQJudgement")) {
                    int id = Integer.parseInt(request.getParameter("id"));
                    int newResultId = Integer.parseInt(request.getParameter("judgeId"));

                    if (frqSubmissions.size() > id) {
                        FRQSubmission submission = frqSubmissions.get(id);

                        boolean oldShowOutput = submission.showOutput();
                        boolean oldTakePenalty = submission.takePenalty();
                        boolean oldTakeNoPenalty = submission.noPenalty();
                        FRQSubmission.Result oldResult = submission.result;
                        switch (newResultId) {
                            case 0:
                                submission.result = FRQSubmission.Result.WRONG_ANSWER;
                                break;
                            case 1:
                                submission.result = FRQSubmission.Result.RIGHT_ANSWER;
                                break;
                            case 2:
                                submission.result = FRQSubmission.Result.SERVER_ERROR;
                                break;
                        }
                        if(oldTakePenalty != submission.takePenalty() || oldTakeNoPenalty != submission.noPenalty()) {    // The result has changed
                            submission.overrideShowOutput = true;   // Don't let them circumvent the show output rule
                            submission.overriddenShowOutput = oldShowOutput;

                            short currentValue = submission.entry.frqResponses[submission.problemNumber - 1];
                            if(submission.result == FRQSubmission.Result.SERVER_ERROR) {    // We switched it to not giving a penalty, so reduce the extreme
                                submission.entry.frqResponses[submission.problemNumber - 1] = (short)((Math.abs(currentValue) - 1) * (currentValue % Math.abs(currentValue)));
                            } else {
                                if(oldTakeNoPenalty && currentValue != 0) {  // We are now taking a penalty or gaining the points, so add the extreme
                                    submission.entry.frqResponses[submission.problemNumber - 1] = (short)((Math.abs(currentValue) + 1) * (currentValue % Math.abs(currentValue)));
                                    currentValue = submission.entry.frqResponses[submission.problemNumber - 1];
                                } else if(oldTakeNoPenalty) {

                                }

                                if (submission.result == FRQSubmission.Result.RIGHT_ANSWER) {    // They switched it to right answer
                                    currentValue = (short) Math.abs(currentValue);
                                    submission.entry.frqResponses[submission.problemNumber - 1] = currentValue;
                                    submission.entry.frqScore += template.frqTest.calcScore(currentValue);
                                } else if (oldResult == FRQSubmission.Result.RIGHT_ANSWER) {     // They switched it from right answer
                                    submission.entry.frqScore -= template.frqTest.calcScore(currentValue);
                                    submission.entry.frqResponses[submission.problemNumber - 1] = (short) (-1 * Math.abs(currentValue));
                                } else return;
                            }

                            submission.entry.update();
                            template.updateScoreboard();
                        }
                    }
                } else if(action.equals("showMCSubmission")) {
                    short tid = Short.parseShort(request.getParameter("tid"));
                    short uid = Short.parseShort(request.getParameter("uid"));
                    UILEntry entry = null;
                    Student student = StudentMap.getByUID(uid);
                    try {
                        entry = getEntry(tid);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    if (entry != null && student != null) {
                        JsonObject compJ = new JsonObject();
                        MCSubmission submission = entry.mc.get(uid);
                        if(!submission.finished) return;

                        compJ.addProperty("user", StringEscapeUtils.escapeHtml4(student.fname + " " + student.lname));
                        compJ.addProperty("team", StringEscapeUtils.escapeHtml4(entry.tname));
                        compJ.addProperty("answers", template.getFinishedMCHelper(submission));
                        compJ.addProperty("scoringReport", gson.toJson(submission.scoringReport));

                        writer.write(new Gson().toJson(compJ));
                    }
                }
            }
            return;
        }

        Student user = (Student) userBefore;
        if(user.cids.containsKey(template.cid)) { // If their team is already signed up for this competition
            UILEntry temp = user.cids.get(template.cid);
            if(action.equals("leaveTeam")) {
                int status = temp.leaveTeam(user);
                if(status != 0) { ;
                    writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                    return;
                } else {

                }
            } else if(!competitionStatus.frqBefore && action.equals("grabFRQProblems")) {
                writer.write("{\"frqProblemsHTML\":\""+template.getFRQProblems(temp)+"\"}");
            } else if (competitionStatus.mcDuring && action.equals("submitMC")) {
                String[] answers = gson.fromJson(request.getParameter("answers"), String[].class);
                MCSubmission submission = temp.scoreMC(user.uid, answers);
                writer.write("{\"mcHTML\":\"" + template.getFinishedMC(submission) + "\"}");
                temp.update();
                template.updateScoreboard();

                // Send it to the teacher
                CompetitionSocket socket = CompetitionSocket.connected.get(((Student)user).teacherId);
                if(socket != null) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("action", "addSmallMC");
                    obj.addProperty("html", template.getSmallMC(user,temp, submission));
                    socket.send(gson.toJson(obj));
                }
                return;
            } else if(competitionStatus.frqDuring && action.equals("submitFRQ")){
                Part filePart = request.getPart("textfile");
                InputStream fileContent = filePart.getInputStream();

                byte[] bytes = new byte[fileContent.available()];
                fileContent.read(bytes);

                short probNum = Short.parseShort(request.getParameter("probNum"));

                if(temp.frqResponses[probNum-1] > 0){
                    writer.write("{\"status\":\"error\",\"error\":\"You've already gotten this problem.\"}");
                    return;
                }

                String fname = filePart.getSubmittedFileName();
                if(!fname.matches("^[a-zA-Z0-9.]*$")) {   // Make sure it doesn't have commands
                    writer.write("{\"status\":\"error\",\"error\":\"The file name must be alphanumeric.\"}");
                    return;
                }

                FRQSubmission submission = template.frqTest.score(probNum, bytes, fname, user.uid, temp.tid);
                submission.entry = temp;
                temp.addFRQRun(submission, probNum);
                frqSubmissions.add(submission);
                if(submission.result == FRQSubmission.Result.RIGHT_ANSWER) {
                    writer.write("{\"status\":\"success\",\"scored\":\"You gained points!\"}");
                    template.updateScoreboard();
                } else if(submission.result == FRQSubmission.Result.COMPILETIME_ERROR) {
                    writer.write("{\"status\":\"error\",\"error\":\"Compile-time error.\"}");
                    template.updateScoreboard();
                } else if(submission.result == FRQSubmission.Result.RUNTIME_ERROR) {
                    writer.write("{\"status\":\"error\",\"error\":\"Runtime error\"}");
                    template.updateScoreboard();
                } else if(submission.result == FRQSubmission.Result.EXCEEDED_TIME_LIMIT) {
                    writer.write("{\"status\":\"error\",\"error\":\"Time limit exceeded.\"}");
                    template.updateScoreboard();
                } else if(submission.result == FRQSubmission.Result.WRONG_ANSWER) {
                    writer.write("{\"status\":\"error\",\"error\":\"Wrong answer.\"}");
                    template.updateScoreboard();
                } else if(submission.result == FRQSubmission.Result.SERVER_ERROR) {
                    writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                } else if(submission.result == FRQSubmission.Result.EMPTY_FILE) {
                    writer.write("{\"status\":\"error\",\"error\":\"Empty file.\"}");
                } else if(submission.result == FRQSubmission.Result.UNCLEAR_FILE_TYPE) {
                    writer.write("{\"status\":\"error\",\"error\":\"Unclear file type. Files must end in .java, .py, or .cpp.\"}");
                }

                // Send it to the teacher
                CompetitionSocket socket = CompetitionSocket.connected.get(user.teacherId);
                if(socket != null) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("action", "addSmallFRQ");
                    obj.addProperty("html", template.getSmallFRQ(frqSubmissions.indexOf(submission), submission));
                    socket.send(gson.toJson(obj));
                }

                // Update all of their team member's frqProblems
                for(short uid: temp.uids) {
                    socket = CompetitionSocket.connected.get(uid);
                    System.out.println("Looking at uid="+uid);
                    if(socket != null) {
                        System.out.println("Socket != null for uid="+uid);
                        JsonObject obj = new JsonObject();
                        obj.addProperty("action", "updateFRQProblems");
                        obj.addProperty("html", template.getFRQProblems(temp));
                        socket.send(gson.toJson(obj));
                    }
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
            try {
                Collection<UILEntry> teams = getAllEntries();
                System.out.println("Num teams = " + teams.size() + " given code = " + code);
                UILEntry entry = entries.getByPassword(code);
                if(entry != null) {
                    if(entry.uids.size() >= 3) {    // This team is full
                        writer.write("{\"status\":\"error\",\"error\":\"Team is full.\"}");
                        return;
                    }
                    user.cids.put(this.template.cid, entry);
                    user.updateUser(false);
                    entry.uids.add(user.uid);
                    entry.updateUIDS();
                    template.updateScoreboard();
                    writer.write("{\"status\":\"success\",\"reload\":\"/uil\"}");

                    // Tell the existing team members to get a new html
                    for(short uid: entry.uids) {
                        if(uid == user.uid) continue;

                        CompetitionSocket socket = CompetitionSocket.connected.get(uid);
                        if (socket != null) {
                            JsonObject obj = new JsonObject();
                            obj.addProperty("action", "updateTeam");
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
                Collection<UILEntry> teams = getAllEntries();
                for(UILEntry entry: teams) {
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
                user.updateUser(false);
                template.updateScoreboard();
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
            entries.addEntry(entry);
            return entry;
        }
        return null;
    }

    /***
     * Loads in entirely NEW UILEntries, so even if one is already in the UILEntry hashmap it will be a different object.
     * @return
     */
    public ArrayList<UILEntry> getAllEntries() throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `c"+template.cid+"`");
        ResultSet rs = stmt.executeQuery();
        ArrayList<UILEntry> entries = new ArrayList<>();
        while(rs.next()) {
            entries.add(new UILEntry(rs, this));
        }
        return entries;
    }

    // Deletes this competition. Does NOT update the teacher, but does update the students. Deletes the Hands-On testcases as well
    public void delete() {
        Collection<UILEntry> values = entries.tidMap.values();
        for(UILEntry entry: values) {
            for(short uid: entry.uids) {
                Student s = StudentMap.getByUID(uid);
                s.cids.remove(this.template.cid);
                s.updateUser(false);
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

        if(template.frqTest.exists) template.frqTest.deleteTestcaseDir();

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
    public HashMap<Short, UILEntry> tidMap = new HashMap<>();    // Maps a teams tid to its UILEntry
    public HashMap<String, UILEntry> passwordMap = new HashMap<>();    // Maps a teams password to its UILEntry

    public UILEntry getByTid(short tid) {
        return tidMap.get(tid);
    }

    public UILEntry getByPassword(String password) {
        return passwordMap.get(password.toUpperCase());
    }

    public void addEntry(UILEntry entry) {
        tidMap.put(entry.tid, entry);
        passwordMap.put(entry.password.toUpperCase(), entry);
    }

    public void delEntry(UILEntry entry) {
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