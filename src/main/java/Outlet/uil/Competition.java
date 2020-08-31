package Outlet.uil;

import Outlet.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.corba.se.spi.orbutil.fsm.Guard;

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
    public boolean isPublic;

    public EntryMap entries;
    ArrayList<FRQSubmission> frqSubmissions = new ArrayList<>();

    private void setTemplate(MCTest mc, FRQTest frq, String name, String whatItIs,String rules, String practice, short cid) {
        template = new Template(name, whatItIs, rules, practice, mc, frq, cid, this);
        template.updateScoreboard();entries = new EntryMap();
    }

    public Competition(Teacher teacher, short cid, boolean isPublic, String name, String whatItIs,String rules, String practice,
                       MCTest mc, FRQTest frq) {
        frq.setDirectories(cid, teacher.uid);

        this.teacher = teacher;
        this.isPublic = isPublic;
        setTemplate(mc, frq, name, whatItIs, rules, practice, cid);

        /* Now, create the folder */
        if(frq.exists) {
            frq.setDirectories(cid, teacher.uid);
            frq.createProblemDirectories();
            frq.initializeFiles();
        }
    }

    /* Returns a new competition object that has been inserted into the database */
    public static Competition createCompetition(Teacher teacher, boolean isPublic, String name, String whatItIs, String rules,
                                         String practice, MCTest mcTest, FRQTest frqTest) throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO competitions (uid, name, isPublic, whatItIs, " +
                "rules, practice, mcKey, mcCorrectPoints, mcIncorrectPoints, mcInstructions, mcTestLink," +
                "mcAnswers, mcOpens, mcTime, frqMaxPoints, frqIncorrectPenalty, frqProblemMap, frqStudentPack," +
                "frqJudgePacket, frqOpens, frqTime, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        stmt.setShort(1, teacher.uid);
        stmt.setString(2, name);
        stmt.setBoolean(3, isPublic);
        stmt.setString(4, whatItIs);
        stmt.setString(5, rules);
        stmt.setString(6, practice);


        if(mcTest.exists) {
            stmt.setString(7, gson.toJson(mcTest.KEY));
            stmt.setShort(8,mcTest.CORRECT_PTS);
            stmt.setShort(9, mcTest.INCORRECT_PTS);
            stmt.setString(10,mcTest.INSTRUCTIONS);
            stmt.setString(11,mcTest.TEST_LINK);
            stmt.setString(12,mcTest.ANSWERS);
            stmt.setString(13,mcTest.opens.DATE_STRING);
            stmt.setLong(14, mcTest.TIME);
        } else {
            stmt.setString(7, null);
            stmt.setShort(8, (short)0);
            stmt.setShort(9, (short)0);
            stmt.setString(10, null);
            stmt.setString(11, null);
            stmt.setString(12, null);
            stmt.setString(13, null);
            stmt.setLong(14, 0);
        }

        if(frqTest.exists) {
            stmt.setShort(15, frqTest.MAX_POINTS);
            stmt.setShort(16, frqTest.INCORRECT_PENALTY);
            stmt.setString(17, gson.toJson(frqTest.PROBLEM_MAP));
            stmt.setString(18, frqTest.STUDENT_PACKET);
            stmt.setString(19, frqTest.JUDGE_PACKET);
            stmt.setString(20, frqTest.opens.DATE_STRING);
            stmt.setLong(21, frqTest.TIME);
        } else {
            stmt.setShort(15, (short)0);
            stmt.setShort(16, (short)0);
            stmt.setString(17, null);
            stmt.setString(18, null);
            stmt.setString(19, null);
            stmt.setString(20, null);
            stmt.setLong(21, 0);
        }

        int type = 0;   // 0 if just MC, 1 if just FRQ, 2 if both
        if(!mcTest.exists && frqTest.exists) {
            type = 1;
        } else if(mcTest.exists && frqTest.exists){
            type = 2;
        }
        stmt.setShort(22, (short) type);

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

            Competition competition = new Competition(teacher, cid, isPublic, name, whatItIs, rules, practice, mcTest, frqTest);
            System.out.println("CID = " + cid + ", " + competition.template.cid);
            UIL.addCompetition(competition);

            teacher.cids.add(cid);
            teacher.updateUser(false);

            return competition;
        }
        return null;
    }

    /* Updates the competition in the database and the template */
    public void update(Teacher teacher, boolean isPublic, String name, String whatItIs, String rules,
                                                String practice, MCTest mcTest, FRQTest frqTest) throws SQLException {
        frqTest.setDirectories(template.cid, teacher.uid);
        frqTest.initializeFiles();

        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("UPDATE competitions SET uid=?, name=?, isPublic=?, whatItIs=?, " +
                        "rules=?, practice=?, mcKey=?, mcCorrectPoints=?, mcIncorrectPoints=?, mcInstructions=?, mcTestLink=?," +
                        "mcAnswers=?, mcOpens=?, mcTime=?, frqMaxPoints=?, frqIncorrectPenalty=?, frqProblemMap=?, frqStudentPack=?," +
                        "frqJudgePacket=?, frqOpens=?, frqTime=?, type=? WHERE cid=?",
                Statement.RETURN_GENERATED_KEYS);
        stmt.setShort(1, teacher.uid);
        stmt.setString(2, name);
        stmt.setBoolean(3, isPublic);
        stmt.setString(4, whatItIs);
        stmt.setString(5, rules);
        stmt.setString(6,practice);


        if(mcTest.exists) {
            stmt.setString(7, gson.toJson(mcTest.KEY));
            stmt.setShort(8,mcTest.CORRECT_PTS);
            stmt.setShort(9, mcTest.INCORRECT_PTS);
            stmt.setString(10,mcTest.INSTRUCTIONS);
            stmt.setString(11,mcTest.TEST_LINK);
            stmt.setString(12,mcTest.ANSWERS);
            stmt.setString(13,mcTest.opens.DATE_STRING);
            stmt.setLong(14, mcTest.TIME);
        } else {
            stmt.setString(7, null);
            stmt.setShort(8, (short)0);
            stmt.setShort(9, (short)0);
            stmt.setString(10, null);
            stmt.setString(11, null);
            stmt.setString(12, null);
            stmt.setString(13, null);
            stmt.setLong(14, 0);
        }

        if(frqTest.exists) {
            stmt.setShort(15, frqTest.MAX_POINTS);
            stmt.setShort(16, frqTest.INCORRECT_PENALTY);
            stmt.setString(17, gson.toJson(frqTest.PROBLEM_MAP));
            stmt.setString(18, frqTest.STUDENT_PACKET);
            stmt.setString(19, frqTest.JUDGE_PACKET);
            stmt.setString(20, frqTest.opens.DATE_STRING);
            stmt.setLong(21, frqTest.TIME);
        } else {
            stmt.setShort(15, (short)0);
            stmt.setShort(16, (short)0);
            stmt.setString(17, null);
            stmt.setString(18, null);
            stmt.setString(19, null);
            stmt.setString(20, null);
            stmt.setLong(21, 0);
        }

        int type = 0;   // 0 if just MC, 1 if just FRQ, 2 if both
        if(!mcTest.exists && frqTest.exists) {
            type = 1;
        } else if(mcTest.exists && frqTest.exists){
            type = 2;
        }
        stmt.setShort(22, (short) type);
        stmt.setShort(23, template.cid);
        stmt.executeUpdate();

        this.isPublic = isPublic;
        setTemplate(mcTest, frqTest, name, whatItIs, rules, practice, template.cid);
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
                                currentValue = submission.entry.frqResponses[submission.problemNumber - 1];
                            } else {
                                if(oldTakeNoPenalty) {  // We are now taking a penalty or gaining the points, so add the extreme
                                    submission.entry.frqResponses[submission.problemNumber - 1] = (short)((Math.abs(currentValue) + 1) * (currentValue % Math.abs(currentValue)));
                                    currentValue = submission.entry.frqResponses[submission.problemNumber - 1];
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
                writer.write("{\"mcHTML\":\"" + template.getFinishedMC(temp.scoreMC(user.uid, answers)) + "\"}");
                temp.update();
                template.updateScoreboard();
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

                FRQSubmission submission = template.frqTest.score(probNum, bytes, filePart.getSubmittedFileName(), user.uid, temp.tid);
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