package Outlet.uil;

import Outlet.*;
import Outlet.Websocket.*;
import com.google.gson.*;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


@ServerEndpoint(value = "/console/sockets/c/{cid}",
        configurator = Configurator.class,
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class)
public class CompetitionSocket {
    private Session session;
    public User user;
    private Competition competition;    // The competition they are viewing

    public static HashMap<Short, CompetitionSocket> connected = new HashMap<>();    // Maps uid to socket
    public static HashMap<Short, ArrayList<CompetitionSocket>> competitions = new HashMap<>(); // Maps cid to a list of CompetitionSockets
    SimpleDateFormat sdf = new SimpleDateFormat(Countdown.DATETIME_FORMAT);

    private static Gson gson = new Gson();

    public void sendLoadScoreboardData(UserStatus status) {
        if(!competition.template.showScoreboard && !status.admin) { // If we aren't showing the scoreboard and this user isn't an admin, don't send the scoreboard
            return;
        }

        JsonObject response = new JsonObject();
        response.addProperty("action", "loadScoreboard");
        response.addProperty("isCreator", status.admin);
        response.addProperty("mcExists", competition.template.mcTest.exists);
        response.addProperty("frqExists", competition.template.frqTest.exists);
        response.addProperty("alternateExists", competition.alternateExists);
        response.addProperty("numNonAlts", competition.numNonAlts);
        if(competition.template.frqTest.exists) {
            response.addProperty("frqMaxPoints", competition.template.frqTest.MAX_POINTS);
            response.addProperty("frqIncorrectPenalty", competition.template.frqTest.INCORRECT_PENALTY);

            JsonArray problemMap = new JsonArray();
            for(FRQProblem problem: competition.template.frqTest.PROBLEM_MAP) {
                problemMap.add(problem.name);
            }
            response.add("frqProblemMap", problemMap);
        }
        response.add("teams", competition.template.scoreboardData);
        if(status.admin) {
            response.addProperty("numHandsOnSubmitted", competition.frqSubmissions.size());
            response.add("teamCodes", competition.template.teamCodeData);

            JsonArray studentsInClass = StudentMap.getJSONByTeacher(user.uid);
            if(studentsInClass == null) studentsInClass = new JsonArray();

            response.add("studentsInClass", studentsInClass);
            response.add("tempUsers", competition.template.tempUserData);
        }

        try {
            send(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("cid") String cidS) {
        System.out.println("OPEN!!! cid="+cidS);
        try{
            short cid = Short.parseShort(cidS);
            competition = UIL.getCompetition(cid);

            if(!competitions.containsKey(cid) || competitions.get(cid) == null) competitions.put(cid, new ArrayList<>());
            competitions.get(cid).add(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session = session;

        if(session.getUserProperties().containsKey("user")) {
            System.out.println("Found user");
            User u = (User) session.getUserProperties().get("user");
            user = u;
            if (u != null) connected.put(u.uid, this);
        }
    }

    @OnMessage
    public void onMessage(Session session, String message)
            throws IOException {
        System.out.println("Message="+message);

        UserStatus status = UserStatus.getCompeteStatus(user, competition);

        // String[] data = gson.fromJson(message, String[].class);
        JsonArray data = JsonParser.parseString(message).getAsJsonArray();
        String action = data.get(0).getAsString();
        if(action.equals("nc")) {
            if (!user.teacher && status.signedUp) {   // They are asking a new clarification
                System.out.println("New Clarification");
                Clarification clarification = new Clarification(user.uid, data.get(1).getAsString(), "", false);

                int index = competition.clarifications.size();  // The index of this clarification in the list
                competition.clarifications.add(clarification);

                CompetitionSocket teacherSocket = connected.get(competition.teacher.uid);
                if(teacherSocket != null) {
                    JsonObject object = new JsonObject();
                    object.addProperty("action", "nc");
                    object.addProperty("index", clarification.index);
                    object.addProperty("name", StringEscapeUtils.escapeHtml4(user.getName()));
                    object.addProperty("question", StringEscapeUtils.escapeHtml4(clarification.question));
                    object.addProperty("id", index);

                    teacherSocket.send(object.toString());
                }

                // Update the judges as well
                short[] judges = competition.getJudges();
                for(short judgeUID: judges) {
                    CompetitionSocket competitionSocket = connected.get(judgeUID);
                    if(competitionSocket != null) {
                        JsonObject object = new JsonObject();
                        object.addProperty("action", "nc");
                        object.addProperty("index", clarification.index);
                        object.addProperty("name", StringEscapeUtils.escapeHtml4(user.getName()));
                        object.addProperty("question", StringEscapeUtils.escapeHtml4(clarification.question));
                        object.addProperty("id", index);

                        competitionSocket.send(object.toString());
                    }
                }

                try {competition.update();} catch(Exception ignored) {}
            }
            return;
        } else if(action.equals("loadScoreboard")) {
            System.out.println("loading scoreboard");

            sendLoadScoreboardData(status);
            return;
        }
        if(status.admin) {
            if (action.equals("rc")) {
                System.out.println("Responding to a Clarification");
                Clarification clarification = competition.clarifications.get(data.get(1).getAsInt());

                if (clarification.responded)
                    return; // Don't change a clarification that is already been responded to

                clarification.responded = true;
                clarification.response = data.get(2).getAsString();

                JsonObject object = new JsonObject();
                object.addProperty("action", "ac");
                object.addProperty("index", clarification.index);
                object.addProperty("question", StringEscapeUtils.escapeHtml4(clarification.question));
                object.addProperty("answer", StringEscapeUtils.escapeHtml4(clarification.response));
                String stringified = object.toString();

                // Relay the clarification to all of the people who are connected to this competition and signed up
                ArrayList<CompetitionSocket> sockets = competitions.get(competition.template.cid);
                for (CompetitionSocket socket : sockets) {
                    UserStatus socketStatus = UserStatus.getCompeteStatus(socket.user, competition);

                    if (socketStatus.signedUp || socketStatus.admin) socket.send(stringified);
                }

                try {
                    competition.update();
                } catch (Exception ignored) {
                }
            } else if (action.equals("saveTeam")) {
                System.out.println("Saving team");

                JsonObject team = data.get(1).getAsJsonObject(); // A team of the format: {tid: number, nonAlts:number[], alt:number}

                JsonArray nonAlts = team.get("nonAlts").getAsJsonArray();
                JsonElement alt = team.get("alt");
                if (nonAlts.size() > competition.numNonAlts || (!alt.isJsonNull() && alt.getAsShort() >= 0 && !competition.alternateExists)) {
                    send("{\"action\":\"scoreboardOpenTeamFeedback\",\"isError\":true,\"msg\":\"Error while saving team\"}");
                    return;
                }
                ;

                short tid = team.get("tid").getAsShort();
                UILEntry entry = competition.entries.getByTid((tid));

                HashMap<Short, MCSubmission> newMC = new HashMap<>();
                ;

                Set<Short> newUIDs = new HashSet<>();
                for (JsonElement uidE : nonAlts) {    // Loop through the new non alt students
                    Student student = StudentMap.getByUID(uidE.getAsShort());
                    newUIDs.add(student.uid);

                    UserStatus studentStatus = UserStatus.getCompeteStatus(student, competition);
                    if (studentStatus.signedUp) {    // They are already signed up, so remove them from any previous team
                        UILEntry oldEntry = student.cids.get(competition.template.cid);
                        if (studentStatus.alt) {
                            oldEntry.altUID = -1;
                        }
                        oldEntry.uids.remove(student.uid);  // The old uids list should only store students that are being removed from the competition

                        if (oldEntry.tid != entry.tid) { // They were on a different team, so update that team now
                            if (competition.template.mcTest.exists) {
                                MCSubmission oldSubmission = oldEntry.mc.get(student.uid);
                                oldEntry.mc.remove(student.uid);
                                if (oldSubmission != null) newMC.put(student.uid, oldSubmission);
                            }
                            student.cids.put(competition.template.cid, entry);
                            oldEntry.updateAll();
                        } else {
                            MCSubmission submission = oldEntry.mc.get(student.uid);
                            if (submission != null) newMC.put(student.uid, submission);
                        }
                    } else {    // They were not already signed up, so add them to the team
                        student.cids.put(competition.template.cid, entry);
                    }
                }

                if (competition.alternateExists && !alt.isJsonNull()) {
                    short altUID = alt.getAsShort();
                    entry.altUID = altUID;

                    if (altUID >= 0) {
                        Student student = StudentMap.getByUID(altUID);
                        newUIDs.add(student.uid);

                        UserStatus studentStatus = UserStatus.getCompeteStatus(student, competition);
                        if (studentStatus.signedUp) {    // They are already signed up, so remove them from any previous team
                            UILEntry oldEntry = student.cids.get(competition.template.cid);
                            oldEntry.uids.remove(student.uid);

                            if (oldEntry.tid != entry.tid) { // They were on a different team, so update that team now
                                if (competition.template.mcTest.exists) {
                                    MCSubmission oldSubmission = oldEntry.mc.get(student.uid);
                                    oldEntry.mc.remove(student.uid);
                                    if (oldSubmission != null) newMC.put(student.uid, oldSubmission);
                                }

                                if (studentStatus.alt) {
                                    oldEntry.altUID = -1;
                                }

                                student.cids.put(competition.template.cid, entry);
                                oldEntry.updateAll();
                            } else {
                                MCSubmission submission = oldEntry.mc.get(student.uid);
                                if (submission != null) newMC.put(student.uid, submission);
                            }
                        } else {    // They were not already signed up, so add them to the team
                            student.cids.put(competition.template.cid, entry);
                        }
                    }
                }


                // If any students are left in the old uids list, remove them from the competition.
                for (short uid : entry.uids) {
                    Student delMe = StudentMap.getByUID(uid);
                    entry.leaveTeam(delMe);
                }

                entry.mc = newMC;
                entry.uids = newUIDs;

                entry.updateAll();

                competition.template.updateScoreboard();
                ;

                send("{\"action\":\"scoreboardOpenTeamFeedback\",\"isError\":false,\"msg\":\"Team saved successfully.\"}");
            } else if (action.equals("fetchGlobalTeams")) {  // Return a list of the global teams
                JsonArray array = new JsonArray();

                Set<Short> maps = Team.teams.keySet(); // Collection of uids
                for (short uid : maps) {
                    Collection<Team> teams = Team.teams.get(uid).values();

                    if (teams.size() > 0) {
                        Teacher teacher = TeacherMap.getByUID(uid);

                        JsonObject teacherJSON = new JsonObject();
                        teacherJSON.addProperty("uid", uid);
                        teacherJSON.addProperty("uname", teacher.getName());
                        teacherJSON.addProperty("school", teacher.school);

                        JsonArray teamsArray = new JsonArray();
                        for (Team team : teams) {
                            JsonObject teamJSON = new JsonObject();
                            teamJSON.addProperty("tid", team.tid);
                            teamJSON.addProperty("tname", team.name);

                            JsonArray nonAltStudents = new JsonArray();
                            for (Student student : team.nonAltStudents) {
                                JsonArray studentJSON = new JsonArray();
                                studentJSON.add(student.getName());
                                studentJSON.add(student.uid);

                                nonAltStudents.add(studentJSON);
                            }
                            teamJSON.add("nonAlts", nonAltStudents);

                            if (team.alternate != null) {
                                JsonArray studentJSON = new JsonArray();
                                studentJSON.add(team.alternate.getName());
                                studentJSON.add(team.alternate.uid);

                                teamJSON.add("alt", studentJSON);
                            }

                            teamsArray.add(teamJSON);
                        }
                        teacherJSON.add("teams", teamsArray);
                        array.add(teacherJSON);
                    }
                }


                JsonObject ret = new JsonObject();
                ret.addProperty("action", "loadGlobalTeams");
                ret.add("teachers", array);
                send(ret.toString());
            } else if (action.equals("addExistingTeam")) {   // Adds a global team to the competition
                String tname = data.get(1).getAsString();
                short uid = data.get(2).getAsShort();
                short tid = data.get(3).getAsShort();

                System.out.println("Adding existing team");

                if (tname.isEmpty()) {
                    send("{\"action\":\"addExistingTeam\",\"error\":\"Team name is empty.\"}");
                    return;
                }

                HashMap<Short, Team> teamMap = Team.teams.get(uid);
                if (teamMap != null) {
                    Team team = teamMap.get(tid);
                    if (team != null) {
                        try {
                            for (UILEntry entry : competition.entries.allEntries) {
                                if (entry.tname.equals(tname)) {
                                    send("{\"action\":\"addExistingTeam\",\"error\":\"Team name is taken.\"}");
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
                            } while (competition.entries.getByPassword(code) != null);

                            UILEntry entry = new UILEntry(tname, code, competition);
                            //boolean reloadScoreboard = false;   // If any of the students have been moved from other teams, just reload the scoreboard

                            for (Student student : team.nonAltStudents) {
                                if (entry.uids.size() >= competition.numNonAlts)
                                    break;   // Only add as many students as this competition can take

                                MCSubmission storedMCSubmission = null;
                                UserStatus studentStatus = UserStatus.getCompeteStatus(student, competition);
                                if (studentStatus.signedUp) {    // This student is already signed up for this competition
                                    //reloadScoreboard = true;
                                    UILEntry oldTeam = student.cids.get(competition.template.cid);

                                    if (competition.template.mcTest.exists) {
                                        storedMCSubmission = oldTeam.mc.get(student.uid);
                                    }

                                    oldTeam.leaveTeam(student);
                                    oldTeam.updateUIDS();
                                }

                                student.cids.put(competition.template.cid, entry);
                                entry.uids.add(student.uid);

                                if (competition.template.mcTest.exists && storedMCSubmission != null) {
                                    entry.mc.put(student.uid, storedMCSubmission);
                                }
                            }
                            if (team.alternate != null && competition.alternateExists) {
                                MCSubmission storedMCSubmission = null;
                                UserStatus studentStatus = UserStatus.getCompeteStatus(team.alternate, competition);
                                if (studentStatus.signedUp) {    // This student is already signed up for this competition
                                    //reloadScoreboard = true;
                                    UILEntry oldTeam = team.alternate.cids.get(competition.template.cid);

                                    if (competition.template.mcTest.exists) {
                                        storedMCSubmission = oldTeam.mc.get(team.alternate.uid);
                                    }

                                    oldTeam.leaveTeam(team.alternate);
                                    oldTeam.updateUIDS();
                                }

                                team.alternate.cids.put(competition.template.cid, entry);

                                entry.uids.add(team.alternate.uid);
                                entry.altUID = team.alternate.uid;

                                if (competition.template.mcTest.exists && storedMCSubmission != null) {
                                    entry.mc.put(team.alternate.uid, storedMCSubmission);
                                }
                            }

                            entry.insert();
                            competition.entries.addEntry(entry);
                            competition.template.updateScoreboard();

                            JsonObject entryJSON = new JsonObject();
                            entryJSON.addProperty("action", "addExistingTeam");

                            // if(reloadScoreboard) {
                            entryJSON.addProperty("reload", "scoreboard");
                            entryJSON.addProperty("tid", entry.tid);
                            /*} else {
                                entryJSON.addProperty("tname", entry.tname);
                                entryJSON.addProperty("school", entry.school);
                                entryJSON.addProperty("tid", entry.tid);
                                entryJSON.addProperty("code", entry.password);
                                entryJSON.add("students", entry.getStudentJSON());

                                if(competition.template.frqTest.exists) entryJSON.addProperty("frq", entry.frqScore);
                            }*/

                            send(entryJSON.toString());
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            send("{\"action\":\"addExistingTeam\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                            return;
                        }
                    }
                }
            } else if (action.equals("deleteTeam")) {
                short tid = data.get(1).getAsShort();
                UILEntry entry = competition.entries.getByTid(tid);
                competition.template.deleteEntry(entry);
            } else if (action.equals("deleteStudent")) {
                short tid = data.get(1).getAsShort();
                short uid = data.get(2).getAsShort();

                UILEntry entry = competition.entries.getByTid(tid);
                Student student = StudentMap.getByUID(uid);
                entry.leaveTeam(student);
            }
            // Return a list of students who match the given name. Max of 10 students.
            // Each student is an object of [name, uid, mc score,
            else if (action.equals("ssearch")) {
                String prefix = data.get(1).getAsString();  // The prefix of the name we are searching
                HashMap<Short, Student> map = StudentMap.getByPrefix(prefix);

                JsonArray array = new JsonArray(); // Array of [uname, uid, mcScore (if available), school]
                if (map != null) {
                    Collection<Student> searches = map.values();
                    int i = 0;
                    for (Student student : searches) {
                        if (i >= 10) break;    // Only show 10 students

                        JsonArray studentData = new JsonArray();
                        studentData.add(student.getName());
                        studentData.add(student.uid);

                        if (competition.template.mcTest.exists) {
                            UILEntry entry = student.cids.get(competition.template.cid);    // If they are already signed up for this competition
                            if (entry != null && entry.mc.containsKey(student.uid)) {
                                studentData.add(entry.mc.get(student.uid).scoringReport[0]);
                            }
                        }

                        array.add(studentData);
                        i++;
                    }
                }

                JsonObject ret = new JsonObject();
                ret.addProperty("action", "ssearch");
                ret.add("students", array);
                send(ret.toString());
            } else if (action.equals("createTempStudent")) { // Creates a temporary student account
                String fname = data.get(1).getAsString();
                String lname = data.get(2).getAsString();
                String school = data.get(3).getAsString();
                short tid = data.get(4).getAsShort();
                boolean isAlt = data.get(5).getAsBoolean();

                // First, check if the team has space for this user
                UILEntry entry = null;
                try {
                    entry = competition.getEntry(tid);
                    if (entry == null) return;

                    if (competition.alternateExists) {
                        if (entry.uids.size() >= (competition.numNonAlts + 1)) { // the team is entirely full
                            return;
                        } else if (isAlt && entry.altUID >= 0) { // There is already an alternate for this team
                            return;
                        }
                    } else if (entry.uids.size() >= competition.numNonAlts) {
                        return;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }

                String unameBase = (fname + lname).toLowerCase();
                String uname = unameBase;
                int x = 2;
                while (StudentMap.getByEmail(uname) != null) {
                    uname = unameBase + x;
                    x++;
                }


                int leftLimit = 48; // numeral '0'
                int rightLimit = 90; // letter 'Z'
                Random random = new Random();
                String password = random.ints(leftLimit, rightLimit + 1)
                        .filter(i -> (i <= 57 || i >= 65) && (i <= 90))
                        .limit(8)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString();
                try {
                    Conn.finishRegistration(uname, password, fname, lname, school, false, true);
                    Student student = StudentMap.getByEmail(uname);
                    student.cids.put(competition.template.cid, entry);
                    entry.uids.add(student.uid);
                    if (isAlt) entry.altUID = student.uid;

                    entry.updateUIDS();
                    competition.template.updateScoreboard();

                    JsonObject ret = new JsonObject();
                    ret.addProperty("action", "addTempStudent");
                    ret.addProperty("name", student.getName());
                    ret.addProperty("uname", student.email);
                    ret.addProperty("password", student.password);
                    ret.addProperty("isAlt", isAlt);
                    ret.addProperty("uid", student.uid);
                    ret.addProperty("tid", tid);

                    send(ret.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if(status.creator) {
                if(action.equals("stopWritten")) {      // Stop the written test
                    Date now = new Date(1); // Date at epoch
                    MCTest mcTest = new MCTest(true, sdf.format(now), competition.template.mcTest.KEY,
                            competition.template.mcTest.CORRECT_PTS, competition.template.mcTest.INCORRECT_PTS,
                            competition.template.mcTest.INSTRUCTIONS, competition.template.mcTest.TEST_LINK,
                            competition.template.mcTest.TIME);
                    try {
                        competition.update((Teacher)user, true, competition.isPublic, competition.alternateExists,
                                competition.numNonAlts, competition.template.name, competition.template.description,
                                mcTest, competition.template.frqTest, competition.getJudges(), competition.template.showScoreboard);
                        competition.template.updateScoreboard();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    broadcast("{\"action\":\"reload\"}");
                } else if(action.equals("startWritten")) {
                    MCTest mcTest = new MCTest(true, data.get(1).getAsString(), competition.template.mcTest.KEY,
                            competition.template.mcTest.CORRECT_PTS, competition.template.mcTest.INCORRECT_PTS,
                            competition.template.mcTest.INSTRUCTIONS, competition.template.mcTest.TEST_LINK,
                            competition.template.mcTest.TIME);
                    try {
                        competition.update((Teacher)user, true, competition.isPublic, competition.alternateExists,
                                competition.numNonAlts, competition.template.name, competition.template.description,
                                mcTest, competition.template.frqTest, competition.getJudges(), competition.template.showScoreboard);
                        competition.template.updateScoreboard();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    broadcast("{\"action\":\"reload\"}");
                } else if(action.equals("stopHandsOn")) {
                    Date now = new Date(1); // Date at epoch
                    FRQTest oldFRQ = competition.template.frqTest;
                    FRQTest frqTest = new FRQTest(true, sdf.format(now), oldFRQ.MAX_POINTS, oldFRQ.INCORRECT_PENALTY,
                            oldFRQ.PROBLEM_MAP, oldFRQ.STUDENT_PACKET, oldFRQ.JUDGE_PACKET, oldFRQ.TIME, oldFRQ.AUTO_GRADE);
                    try {
                        competition.update((Teacher)user, true, competition.isPublic, competition.alternateExists,
                                competition.numNonAlts, competition.template.name, competition.template.description,
                                competition.template.mcTest, frqTest, competition.getJudges(), competition.template.showScoreboard);
                        competition.template.updateScoreboard();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    broadcast("{\"action\":\"reload\"}");
                } else if(action.equals("startHandsOn")) {
                    FRQTest oldFRQ = competition.template.frqTest;
                    FRQTest frqTest = new FRQTest(true, data.get(1).getAsString(), oldFRQ.MAX_POINTS, oldFRQ.INCORRECT_PENALTY,
                            oldFRQ.PROBLEM_MAP, oldFRQ.STUDENT_PACKET, oldFRQ.JUDGE_PACKET, oldFRQ.TIME, oldFRQ.AUTO_GRADE);
                    try {
                        competition.update((Teacher)user, true, competition.isPublic, competition.alternateExists,
                                competition.numNonAlts, competition.template.name, competition.template.description,
                                competition.template.mcTest, frqTest, competition.getJudges(), competition.template.showScoreboard);
                        competition.template.updateScoreboard();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    broadcast("{\"action\":\"reload\"}");
                } else if(action.equals("publishGradedFRQ")) {  // Makes the result of an frq submission available to the team that submitted it
                    int id = data.get(1).getAsInt();
                    FRQSubmission submission = competition.frqSubmissions.get(id);
                    submission.graded = true;
                    submission.entry.recalculateFRQScore(submission.problemNumber-1);

                    submission.entry.update();
                    submission.entry.socketSendFRQProblems();
                    competition.template.updateScoreboard();
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        System.out.println("Closing session");
        if(this.user != null) connected.remove(this.user.uid);
        if(competition != null) competitions.get(competition.template.cid).remove(this);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        System.out.println("ERROR!!!");
        throwable.printStackTrace();
    }

    public void send(String msg) throws IOException {
        session.getBasicRemote().sendText(msg);
    }

    public static void broadcast(String message) {
        connected.values().forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}