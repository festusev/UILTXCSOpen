package Outlet.uil;

import Outlet.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class UILEntry {
    public String tname;
    public Set<Short> uids;
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


        mc = new HashMap<>();

        if(comp.template.mcTest.exists) {
            String column = rs.getString("mc").replace("\\u0027", "\"");
            column = column.substring(1, column.length() - 1);
            HashMap<String, ArrayList<Object[]>> temp = gson.fromJson(column, HashMap.class);
            temp = temp == null ? new HashMap<>() : temp;
            Set<String> keys = temp.keySet();
            for (String key : keys) {
                MCSubmission submission = MCSubmission.deserialize(temp.get(key));
                mc.put(Short.parseShort(key), submission);
            }
        }
        if(comp.template.frqTest.exists) {
            frqResponses = FRQSubmission.parseList(rs.getString("frqResponses"), this);

            frqScore = Short.parseShort(rs.getString("frqScore"));
        } else {
            frqResponses = new Pair[0];
            frqScore = 0;
        }
    }

    public void setUids(short[] temp) {
        uids = new HashSet<>();
        boolean schoolSet = false;
        for(short uid: temp) {
            uids.add(uid);

            Student student = StudentMap.getByUID(uid);
            Teacher teacher = TeacherMap.getByUID(student.uid);
            if(teacher != null && !teacher.school.isEmpty() && !schoolSet) {
                schoolSet = true;
                this.school = teacher.school;
            }
        }
        if(!schoolSet) {
            this.school = "No School";
        }
    }
    public void setUids(String s) {
        short[] temp = gson.fromJson(s, short[].class);
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

    public int leaveTeam(Student u) {
        // They don't belong to this team
        if(u.cids.get(competition.template.cid).tid != tid || !uids.contains(u.uid))
            return -2;
        if(notStarted()) return -3;   // Cannot leave team unless before the competition has started

        uids.remove(u.uid);
        int status = update();

        u.cids.remove(competition.template.cid);
        if(u.updateUser(false) != 0) return -1;
        if(uids.size()<=0) {   // If The team only has one user, delete the team.
            delete();
        }

        ArrayList<ClassSocket> list = ClassSocket.classes.get(tid);
        for(ClassSocket socket: list) {
            JsonObject obj = new JsonObject();
            obj.addProperty("action", "updateTeam");
            obj.addProperty("html", competition.template.getTeamMembers(StudentMap.getByUID(socket.user.uid), this));
            try {
                socket.send(gson.toJson(obj));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return status;
    }

    public void delete() {
        competition.template.deleteEntry(tid);
        uids = new HashSet<>();
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
            String statement = "UPDATE `c"+competition.template.cid+"` SET mc=?,frqResponses=?,frqScore=? WHERE tid=?";
            PreparedStatement stmt = conn.prepareStatement(statement);

            if(competition.template.mcTest.exists) {
                stmt.setString(1, gson.toJson(stringifyMC()));
            } else {
                stmt.setString(1, "{}");
            }
            if(competition.template.frqTest.exists) {
                stmt.setString(2, FRQSubmission.stringifyList(frqResponses));
                stmt.setShort(3, frqScore);
            } else {
                stmt.setString(2, "[]");
                stmt.setShort(3, (short)0);
            }
            stmt.setShort(4, tid);
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
            PreparedStatement stmt = conn.prepareStatement("UPDATE `c"+competition.template.cid+"` SET uids=? WHERE tid=?");
            stmt.setString(1, gson.toJson(uids));
            stmt.setShort(2, tid);
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
            String statement = "INSERT INTO `c"+competition.template.cid+"` (name, password, uids, mc, frqResponses, frqScore) VALUES (?,?,?,?,?,?)";

            PreparedStatement stmt = conn.prepareStatement(statement);
            stmt.setString(1, tname);
            stmt.setString(2, password);
            stmt.setString(3, gson.toJson(uids));

            if(competition.template.mcTest.exists) {
                stmt.setString(4, stringifyMC());
            } else {
                stmt.setString(4, "{}");
            }
            if(competition.template.frqTest.exists) {
                stmt.setString(5, FRQSubmission.stringifyList(frqResponses));
                stmt.setShort(6, frqScore);
            } else {
                stmt.setString(5, "{}");
                stmt.setShort(6, (short)0);
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
        String mcStringified = "{";
        Set<Short> keys = mc.keySet();
        int i = 0;
        for (short key : keys) {
            mcStringified += key + ":" + mc.get(key).serialize();
            if (i < keys.size() - 1) mcStringified += ",";
            i++;
        }
        mcStringified += "}";
        return mcStringified;
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
        else if(result.result == FRQSubmission.Result.RIGHT_ANSWER) {
            //System.out.println("--ADDING SUCCESSFUL FRQ RUN, # tries = " +frqResponses[probNum] + " score = "+java.lang.Math.max(CS.template.frqTest.calcScore(frqResponses[probNum]), competition.frqTest.MIN_POINTS));
            problem.key = (short)(java.lang.Math.abs(problem.key) + 1);
            frqScore += java.lang.Math.max(competition.template.frqTest.calcScore(problem.key), competition.template.frqTest.MIN_POINTS);
        } else update = false;

        if(update) update();
    }

    public boolean finishedMC(short uid) {
        if(!competition.template.mcTest.exists) return true;

        if(mc.containsKey(uid)) {
            MCSubmission submission = mc.get(uid);
            return submission.finished;
        } else {
            return false;
        }
    }

    public int getMCScore() {
        if(!competition.template.mcTest.exists) return 0;

        int score = 0;
        for(short i: mc.keySet()){
            score += mc.get(i).scoringReport[0];
        }
        return score;
    }

    public int getScore() {
        int frq = 0;
        int mc = 0;
        if(competition.template.mcTest.exists) mc = getMCScore();
        if(competition.template.frqTest.exists) frq = frqScore;
        return frq + mc;
    }
}