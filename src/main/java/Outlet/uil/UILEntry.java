package Outlet.uil;

import Outlet.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static Outlet.Conn.getConnection;

public class UILEntry {
    public String tname;
    public Set<Short> uids; // The uids of all students, including the alternate
    public short altUID;  // The uid of the alternate. -1 if there is no alternate
    public short tid;
    public String school = "";  // Specified by the students
    public String password;

    // Maps uids to the corresponding MCSubmission
    public HashMap<Short, MCSubmission> mc;

    public Pair<Short, ArrayList<FRQSubmission>>[] frqResponses;    // All zeroes if they have not yet begun
    public short frqScore;
    public Competition competition;
    private static Gson gson = new Gson();

    /***
     * Used when creating a new team.
     * @param name
     * @param hashedPassword
     * @param competition
     * @param student
     */
    public UILEntry(String name, String hashedPassword, Competition competition, Student student) {
        this.tname =  name;
        if(student.teacherId >=0) {
            this.school = TeacherMap.getByUID(student.teacherId).school;
        }
        if(this.school.isEmpty()) this.school = "No School";
        this.password = hashedPassword;
        this.competition = competition;
        uids = new HashSet<>();
        uids.add(student.uid);
        altUID = -1;
        mc = new HashMap<>();
        frqResponses = new Pair[competition.template.frqTest.PROBLEM_MAP.length];
        for(int i=0;i<competition.template.frqTest.PROBLEM_MAP.length;i++) {
            frqResponses[i] = new Pair<>((short) 0, new ArrayList<>());
        }
        frqScore = 0;

        // competition.entries.addEntry(this);
    };

    /***
     * Used when a teacher creates a new team.
     * @param name
     * @param hashedPassword
     * @param competition
     */
    public UILEntry(String name, String hashedPassword, Competition competition) {
        this.tname =  name;
        this.school = "No School";

        this.password = hashedPassword;
        this.competition = competition;
        uids = new HashSet<>();
        altUID = -1;
        mc = new HashMap<>();
        frqResponses = new Pair[competition.template.frqTest.PROBLEM_MAP.length];
        for(int i=0;i<competition.template.frqTest.PROBLEM_MAP.length;i++) {
            frqResponses[i] = new Pair<>((short) 0, new ArrayList<>());
        }
        frqScore = 0;

        // competition.entries.addEntry(this);
    };

    public UILEntry(ResultSet rs, Competition comp) throws SQLException {
        tname = rs.getString("name");
        tid = rs.getShort("tid");
        competition = comp;
        password = rs.getString("password");

        setUids(rs.getString("uids"));
        altUID = rs.getShort("altUID");


        mc = new HashMap<>();

        if(comp.template.mcTest.exists) {
            JsonObject column = JsonParser.parseString(rs.getString("mc")).getAsJsonObject();
            Set<String> keys = column.keySet();
            for (String key : keys) {
                MCSubmission submission = MCSubmission.deserialize(column.get(key).getAsJsonArray());
                mc.put(Short.parseShort(key), submission);
            }
        }
        frqScore = 0;
        if(comp.template.frqTest.exists) {
            frqResponses = FRQSubmission.parseList(rs.getString("frqResponses"), this);

            for(Pair<Short, ArrayList<FRQSubmission>> problem: frqResponses) {
                if(problem.key > 0) frqScore += java.lang.Math.max(competition.template.frqTest.calcScore(problem.key), competition.template.frqTest.MIN_POINTS);
            }
        } else {
            frqResponses = new Pair[0];
        }
    }

    public void setUids(short[] temp) {
        uids = new HashSet<>();
        // boolean schoolSet = false;
        for(short uid: temp) {
            uids.add(uid);

            /*Student student = StudentMap.getByUID(uid);
            Teacher teacher = TeacherMap.getByUID(student.uid);
            if(teacher != null && !teacher.school.isEmpty() && !schoolSet) {
                schoolSet = true;
                this.school = teacher.school;
            }*/
        }
        //if(!schoolSet) {
        this.school = "No School";
        //}
    }
    public void setUids(String s) {
        short[] temp = gson. fromJson(s, short[].class);
        setUids(temp);
    }

    /***
     * Returns true if the competition has not started.
     * @return/
     */
    public boolean notStarted() {
        CompetitionStatus competitionStatus = new CompetitionStatus(competition.template.mcTest, competition.template.frqTest);
        if(!competitionStatus.frqBefore || !competitionStatus.mcBefore) return false;
        return true;
    }

    public int leaveTeam(Student u ) {
        // They don't belong to this team
        UILEntry tempEntry = u.cids.get(competition.template.cid);
        if(tempEntry == null || tempEntry.tid != tid || !uids.contains(u.uid))
            return -2;
        // if(notStarted()) return -3;   // Cannot leave team unless before the competition has started

        uids.remove(u.uid);
        if(altUID == u.uid) altUID = -1;

        int status;

        u.cids.remove(competition.template.cid);

        if(mc.containsKey(u.uid)) {
            mc.remove(u.uid);
            status = update();
        } else {
            status = updateUIDS();
        }

        ArrayList<ClassSocket> list = ClassSocket.classes.get(tid);
        if(list != null) {
            for (ClassSocket socket : list) {
                try {
                    if(u.temp && socket.user.uid == u.uid) {    // They are a temporary user, and this is their socket
                        socket.send("[\"action\":\"competitionDeleted\"]");
                    } else {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("action", "updateTeam");
                        obj.addProperty("html", competition.template.getTeamMembers(StudentMap.getByUID(socket.user.uid), this));

                        socket.send(gson.toJson(obj));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(u.temp) {    // They are a temporary user, so delete their account
            Connection conn = getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement("DELETE FROM users WHERE uid=?");
                stmt.setShort(1, u.uid);
                status = stmt.executeUpdate();
                UserMap.delUser(u);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return status;
    }

    public static Template getTemplate(short cid) {
        return UIL.getCompetition(cid).template;
    }

    public static UILEntry loadUILEntry(short tid, short cid) {
        try {
            return Objects.requireNonNull(UIL.getCompetition(cid)).getEntry(tid);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int update() { // Updates entry in the database
        Connection conn = Conn.getConnection();
        try {
            String statement = "UPDATE `c"+competition.template.cid+"` SET mc=?,frqResponses=? WHERE tid=?";
            PreparedStatement stmt = conn.prepareStatement(statement);

            if(competition.template.mcTest.exists) {
                stmt.setString(1, stringifyMC());
            } else {
                stmt.setString(1, "{}");
            }
            if(competition.template.frqTest.exists) {
                stmt.setString(2, FRQSubmission.stringifyList(frqResponses));
            } else {
                stmt.setString(2, "[]");
            }
            stmt.setShort(3, tid);
            stmt.executeUpdate();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int updateUIDS() {
        Connection conn = Conn.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE `c"+competition.template.cid+"` SET uids=?, altUID = ? WHERE tid=?");
            stmt.setString(1, gson.toJson(uids));
            stmt.setShort(2, altUID);
            stmt.setShort(3, tid);
            stmt.executeUpdate();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int updateAll() {
        Connection conn = Conn.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE `c"+competition.template.cid+"` SET uids=?,altUID=?," +
                    "mc=?,frqResponses=? WHERE tid=?");

            stmt.setString(1, gson.toJson(uids));
            stmt.setShort(2, altUID);
            if(competition.template.mcTest.exists) {
                stmt.setString(3, stringifyMC());
            } else {
                stmt.setString(3, "{}");
            }
            if(competition.template.frqTest.exists) {
                stmt.setString(4, FRQSubmission.stringifyList(frqResponses));
            } else {
                stmt.setString(4, "[]");
            }
            stmt.setShort(5, tid);
            stmt.executeUpdate();

            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /***
     * Inserts the entry into the database and retrieves and updates the tid
     * @return
     */
    public int insert(){
        Connection conn = Conn.getConnection();
        try {
            String statement = "INSERT INTO `c"+competition.template.cid+"` (name, password, uids, altUID, mc, frqResponses" +
                    ") VALUES (?,?,?,?,?, ?)";

            PreparedStatement stmt = conn.prepareStatement(statement);
            stmt.setString(1, tname);
            stmt.setString(2, password);
            stmt.setString(3, gson.toJson(uids));
            stmt.setShort(4, altUID);

            if(competition.template.mcTest.exists) {
                stmt.setString(5, stringifyMC());
            } else {
                stmt.setString(6, "{}");
            }
            if(competition.template.frqTest.exists) {
                stmt.setString(6, FRQSubmission.stringifyList(frqResponses));
            } else {
                stmt.setString(6, "{}");
            }
            stmt.executeUpdate();

            stmt = conn.prepareStatement("SELECT tid FROM `c"+competition.template.cid+"` WHERE name=?");
            stmt.setString(1, tname);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                tid = rs.getShort("tid");
            } else {
                return -1;
            }

            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String stringifyMC() {
        JsonObject obj = new JsonObject();
        Set<Short> keys = mc.keySet();
        for (short key : keys) {
            MCSubmission submission = mc.get(key);
            if(submission != null) obj.add(""+key, submission.serialize());
        }
        return obj.toString();

        /*String mcStringified = "{";
        Set<Short> keys = mc.keySet();
        int i = 0;
        for (short key : keys) {
            mcStringified += key + ":" + mc.get(key).serialize();
            if (i < keys.size() - 1) mcStringified += ",";
            i++;
        }
        mcStringified += "}";
        return mcStringified;*/
    }

    /**
     * Returns an array scoring report. First element is the total score,
     * second element is the number correct, third element is the number skipped
     * fourth element is the number incorrect
     * @param uid
     * @param answers
     * @return short[3]
     */
    public MCSubmission scoreMC(short uid, String[] answers) {
        if(!competition.template.mcTest.exists) return new MCSubmission(answers,new short[]{0,0,0,0}, true);

        MCSubmission entry = new MCSubmission(answers, competition.template.mcTest.score(answers), true);
        mc.put(uid, entry);
        update();
        return entry;
    }

    // if Greater than zero, add an incorrect run, if less than zero, add nothing,
    // if equal to zero, add a correct run
    public void addFRQRun(FRQSubmission result, int probNum) {
        if(!competition.template.frqTest.exists) return;

        probNum--;  // We only use probNum for indexes
        boolean update = true;
        Pair<Short, ArrayList<FRQSubmission>> problem = frqResponses[probNum];
        problem.value.add(result);

        if(result.takePenalty()) problem.key--;
        else if(result.result == FRQSubmission.Result.CORRECT) {
            //System.out.println("--ADDING SUCCESSFUL FRQ RUN, # tries = " +frqResponses[probNum] + " score = "+java.lang.Math.max(CS.template.frqTest.calcScore(frqResponses[probNum]), competition.frqTest.MIN_POINTS));
            problem.key = (short)(java.lang.Math.abs(problem.key) + 1);
            frqScore += java.lang.Math.max(competition.template.frqTest.calcScore(problem.key), competition.template.frqTest.MIN_POINTS);
        } else update = false;

        if(update) update();
    }

    // Recalculates the score from a single FRQProblem. Looks for the first correct FRQProblem, and ignores every submission
    // after it.
    public void recalculateFRQScore(int probNum) {
        Pair<Short, ArrayList<FRQSubmission>> problem = frqResponses[probNum];

        // The old number of points that this problem contributed to the total frq score
        int oldScore = 0;
        if(problem.key > 0 ) oldScore = java.lang.Math.max(competition.template.frqTest.calcScore(problem.key), competition.template.frqTest.MIN_POINTS);

        int numTries = 0;   // The new number of tries. If positive, the problem is solved
        for(int i=0;i<problem.value.size();i++) {
            FRQSubmission submission = problem.value.get(i);
            if(submission.takePenalty()) numTries--;
            else if(submission.result == FRQSubmission.Result.CORRECT) {
                numTries = -numTries + 1;
                break;
            }
        }
        problem.key = (short) numTries;

        frqScore = (short)(frqScore - oldScore);
        if(numTries > 0)    // It has been solved
            frqScore = (short)(frqScore + java.lang.Math.max(competition.template.frqTest.calcScore(problem.key), competition.template.frqTest.MIN_POINTS));
    }

    public boolean finishedMC(short uid) {
        if(!competition.template.mcTest.exists) return true;

        if(mc.containsKey(uid)) {
            MCSubmission submission = mc.get(uid);
            if(submission != null) return submission.finished;
            else return false;
        } else {
            return false;
        }
    }

    public int getMCScore() {
        if(!competition.template.mcTest.exists) return 0;

        int score = 0;

        if(mc.size() <= competition.numNonAlts) {   // In this case, just add up everyone's scores.
            for(short i: mc.keySet()){
                MCSubmission submission = mc.get(i);
                if(submission != null) score += submission.scoringReport[0];
            }
        } else {    // In this case, just drop the lowest score
            int uidOfLowest = 0;
            int lowest = Integer.MAX_VALUE;
            for(short uid: mc.keySet()) {
                MCSubmission submission = mc.get(uid);
                if(submission == null) continue;

                short thisScore = submission.scoringReport[0];
                if(thisScore < lowest) {
                    uidOfLowest = uid;
                    lowest = thisScore;
                }
            }

            for(short uid: mc.keySet()) {
                MCSubmission submission = mc.get(uid);
                if(submission == null) continue;

                if(uid != uidOfLowest) score += submission.scoringReport[0];
            }
        }
        return score;
    }

    // Returns a JSON list of objects formatted: {tid: tid, nonAlts: [["name", uid, mcScore]], alt: ["name",uid,mcScore]}. if there is no mcTest, there is no mcScore
    public JsonObject getStudentJSON() {
        JsonObject obj = new JsonObject();

        JsonArray array = new JsonArray();
        for(short uid: uids) {
            if(uid == altUID) continue; // This is the alt

            Student student = StudentMap.getByUID(uid);
            if(student == null) continue;

            JsonArray studentData = student.getJSON();

            if(competition.template.mcTest.exists && mc.containsKey(uid)) {
                MCSubmission submission = mc.get(uid);
                if(submission != null) studentData.add(submission.scoringReport[0]);
            }
            array.add(studentData);
        }
        obj.add("nonAlts", array);

        if(altUID >= 0 && competition.template.frqTest.exists) {    // Alternates exist and this team has one
            Student student = StudentMap.getByUID(altUID);
            if(student != null) {
                JsonArray studentData = student.getJSON();

                if (competition.template.mcTest.exists && mc.containsKey(altUID)) {
                    MCSubmission submission = mc.get(altUID);
                    if (submission != null) studentData.add(submission.scoringReport[0]);
                }

                obj.add("alt", studentData);
            }
        }

        return obj;
    }

    public int getScore() {
        int frq = 0;
        int mc = 0;
        if(competition.template.mcTest.exists) mc = getMCScore();
        if(competition.template.frqTest.exists) frq = frqScore;
        return frq + mc;
    }
}