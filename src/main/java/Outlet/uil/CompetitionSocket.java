package Outlet.uil;

import Outlet.*;
import Outlet.Websocket.*;
import com.google.gson.*;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
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

    private static Gson gson = new Gson();

    public void sendLoadScoreboardData(UserStatus status) {
        JsonObject response = new JsonObject();
        response.addProperty("action", "loadScoreboard");
        response.addProperty("isCreator", status.admin);
        response.addProperty("mcExists", competition.template.mcTest.exists);
        response.addProperty("frqExists", competition.template.frqTest.exists);
        response.addProperty("alternateExists", competition.alternateExists);
        response.addProperty("numNonAlts", competition.numNonAlts);
        response.add("teams", competition.template.scoreboardData);
        if(status.admin) {
            response.addProperty("numHandsOnSubmitted", competition.frqSubmissions.size());
            response.add("teamCodes", competition.template.teamCodeData);
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
                    object.addProperty("name", user.fname + " " + user.lname);
                    object.addProperty("question", clarification.question);
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
                        object.addProperty("name", user.fname + " " + user.lname);
                        object.addProperty("question", clarification.question);
                        object.addProperty("id", index);

                        competitionSocket.send(object.toString());
                    }
                }

                try {competition.update();} catch(Exception ignored) {}
            }
        } else if(action.equals("rc")) {
            if(status.admin || status.judging) {   // They are responding to a clarification
                System.out.println("Responding to a Clarification");
                Clarification clarification = competition.clarifications.get(data.get(1).getAsInt());

                if(clarification.responded) return; // Don't change a clarification that is already been responded to

                clarification.responded = true;
                clarification.response = data.get(2).getAsString();

                JsonObject object = new JsonObject();
                object.addProperty("action","ac");
                object.addProperty("index", clarification.index);
                object.addProperty("question", clarification.question);
                object.addProperty("answer", clarification.response);
                String stringified = object.toString();

                // Relay the clarification to all of the people who are connected to this competition and signed up
                ArrayList<CompetitionSocket> sockets = competitions.get(competition.template.cid);
                for(CompetitionSocket socket: sockets) {
                    UserStatus socketStatus = UserStatus.getCompeteStatus(socket.user, competition);

                    if(socketStatus.signedUp || socketStatus.admin) socket.send(stringified);
                }

                try {competition.update();} catch(Exception ignored) {}
            }
        } else if(action.equals("loadScoreboard")) {
            System.out.println("loading scoreboard");

            sendLoadScoreboardData(status);
        } else if(action.equals("saveTeam")) {
            if(status.admin) {
                System.out.println("Saving team");
                JsonArray teamArray = data.get(1).getAsJsonArray(); // A list of teams of the format: {tid: number, nonAlts:number[], alt:number}

                // First, check that no team has more than the maximum allowed non alts, and, if alts aren't allowed, no team has an alt.
                // Also, load the storedSubmissionMap
                HashMap<Short, MCSubmission> storedSubmissionMap = new HashMap<>(); // So that when we are swapping around users, we store their MCSubmission
                JsonObject[] teams = new JsonObject[teamArray.size()];
                for (int i=0,j=teamArray.size();i<j;i++) {
                    JsonObject team = teamArray.get(i).getAsJsonObject();
                    teams[i] = team;

                    JsonArray nonAlts = team.get("nonAlts").getAsJsonArray();
                    JsonElement alt = team.get("alt");
                    if(nonAlts.size() > competition.numNonAlts || (!alt.isJsonNull() && alt.getAsShort() >= 0 && !competition.alternateExists)) {
                        send("{\"action\":\"scoreboardOpenTeamFeedback\",\"isError\":true,\"msg\":\"Error while saving team\"}");
                        return;
                    };

                    if(competition.template.mcTest.exists) {
                        short tid = team.get("tid").getAsShort();
                        UILEntry entry = competition.entries.getByTid((tid));

                        for (short uid : entry.uids) {
                            storedSubmissionMap.put(uid, entry.mc.get(uid));
                        }
                    }
                }

                // Now update the teams
                for (JsonObject team: teams) {
                    short tid = team.get("tid").getAsShort();
                    JsonArray nonAlts = team.get("nonAlts").getAsJsonArray();
                    short alt = team.get("alt").getAsShort();

                    UILEntry entry = competition.entries.getByTid(tid);

                    Set<Short> newUIDs = new HashSet<>();
                    HashMap<Short, MCSubmission> newMCSubmissions = new HashMap<>();

                    // Set the non alternates
                    for(JsonElement uidE: nonAlts) {
                        short uid = uidE.getAsShort();
                        newUIDs.add(uid);

                        if(competition.template.mcTest.exists) {
                            MCSubmission submission = storedSubmissionMap.get(uid);
                            if(submission != null) newMCSubmissions.put(uid, submission);
                        }
                    }

                    // Set the alternate
                    if(competition.alternateExists) {
                        if(competition.template.mcTest.exists) {
                            MCSubmission submission = storedSubmissionMap.get(alt);
                            if(submission != null) newMCSubmissions.put(alt, submission);
                        }
                        if(alt>=0) newUIDs.add(alt);
                        entry.altUID = alt;
                    }

                    entry.uids = newUIDs;
                    entry.mc = newMCSubmissions;

                    for(short uid: entry.uids) {
                        Student student = StudentMap.getByUID(uid);
                        student.cids.put(competition.template.cid,entry);
                        student.updateUser(false);
                    }

                    entry.updateAll();
                }
                competition.template.updateScoreboard();;

                send("{\"action\":\"scoreboardOpenTeamFeedback\",\"isError\":false,\"msg\":\"Team saved successfully\"}");
            }
        } else if(action.equals("fetchGlobalTeams")) {  // Return a list of the global teams
            if(status.admin) {
                JsonArray array = new JsonArray();

                Set<Short> maps = Team.teams.keySet(); // Collection of uids
                for(short uid: maps) {
                    Collection<Team> teams = Team.teams.get(uid).values();

                    if(teams.size() > 0) {
                        Teacher teacher = TeacherMap.getByUID(uid);

                        JsonObject teacherJSON = new JsonObject();
                        teacherJSON.addProperty("uid", uid);
                        teacherJSON.addProperty("uname", teacher.fname + " " + teacher.lname);
                        teacherJSON.addProperty("school", teacher.school);

                        JsonArray teamsArray = new JsonArray();
                        for (Team team : teams) {
                            JsonObject teamJSON = new JsonObject();
                            teamJSON.addProperty("tid", team.tid);
                            teamJSON.addProperty("tname", team.name);

                            JsonArray nonAltStudents = new JsonArray();
                            for(Student student: team.nonAltStudents) {
                                JsonArray studentJSON = new JsonArray();
                                studentJSON.add(student.fname + " " + student.lname);
                                studentJSON.add(student.uid);

                                nonAltStudents.add(studentJSON);
                            }
                            teamJSON.add("nonAlts", nonAltStudents);

                            if(team.alternate != null) {
                                JsonArray studentJSON = new JsonArray();
                                studentJSON.add(team.alternate.fname + " " + team.alternate.lname);
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
            }
        } else if(action.equals("addExistingTeam")) {   // Adds a global team to the competition
            if(status.admin) {
                String tname = data.get(1).getAsString();
                short uid = data.get(2).getAsShort();
                short tid = data.get(3).getAsShort();

                System.out.println("Adding existing team");

                if(tname.isEmpty()) {
                    send("{\"action\":\"addExistingTeam\",\"error\":\"Team name is empty.\"}");
                    return;
                }

               HashMap<Short, Team> teamMap = Team.teams.get(uid);
                if(teamMap != null) {
                    Team team = teamMap.get(tid);
                    if(team != null) {
                        try {
                            for(UILEntry entry: competition.entries.allEntries) {
                                if(entry.tname.equals(tname)) {
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
                            boolean reloadScoreboard = false;   // If any of the students have been moved from other teams, just reload the scoreboard

                            ArrayList<Student> updateStudents = new ArrayList<>();  // We store students for updating after the team has been inserted. This way, we have the tid to update

                            for(Student student: team.nonAltStudents) {
                                if(entry.uids.size() > competition.numNonAlts) break;   // Only add as many students as this competition can take

                                MCSubmission storedMCSubmission = null;
                                if(status.signedUp) {    // This student is already signed up for this competition
                                    reloadScoreboard = true;
                                    UILEntry oldTeam = student.cids.get(competition.template.cid);

                                    if(competition.template.mcTest.exists) {
                                        storedMCSubmission = oldTeam.mc.get(student.uid);
                                    }

                                    oldTeam.leaveTeam(student);
                                    oldTeam.updateUIDS();
                                }

                                student.cids.put(competition.template.cid, entry);
                                updateStudents.add(student);
                                entry.uids.add(student.uid);

                                if(competition.template.mcTest.exists && storedMCSubmission != null) {
                                    entry.mc.put(student.uid, storedMCSubmission);
                                }
                            }
                            if(team.alternate != null && competition.alternateExists) {
                                MCSubmission storedMCSubmission = null;
                                if(team.alternate.cids.containsKey(competition.template.cid)) {    // This student is already signed up for this competition
                                    reloadScoreboard = true;
                                    UILEntry oldTeam = team.alternate.cids.get(competition.template.cid);

                                    if(competition.template.mcTest.exists) {
                                        storedMCSubmission = oldTeam.mc.get(team.alternate.uid);
                                    }

                                    oldTeam.leaveTeam(team.alternate);
                                    oldTeam.updateUIDS();
                                }

                                team.alternate.cids.put(competition.template.cid, entry);
                                team.alternate.updateUser(false);
                                updateStudents.add(team.alternate);
                                entry.uids.add(team.alternate.uid);
                                entry.altUID = team.alternate.uid;

                                if(competition.template.mcTest.exists && storedMCSubmission != null) {
                                    entry.mc.put(team.alternate.uid, storedMCSubmission);
                                }
                            }

                            entry.insert();
                            competition.entries.addEntry(entry);

                            for(Student update: updateStudents) {   // Now update all of the students
                                update.updateUser(false);
                            }

                            competition.template.updateScoreboard();

                            JsonObject entryJSON = new JsonObject();
                            entryJSON.addProperty("action", "addExistingTeam");

                            if(reloadScoreboard) {
                                entryJSON.addProperty("reload", "scoreboard");
                            } else {
                                entryJSON.addProperty("tname", entry.tname);
                                entryJSON.addProperty("school", entry.school);
                                entryJSON.addProperty("tid", entry.tid);
                                entryJSON.addProperty("code", entry.password);
                                entryJSON.add("students", entry.getStudentJSON());

                                if(competition.template.frqTest.exists) entryJSON.addProperty("frq", entry.frqScore);
                            }

                            send(entryJSON.toString());
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            send("{\"action\":\"addExistingTeam\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                            return;
                        }
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