package Outlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Team {
    private static Gson gson = new Gson();

    public short tid;
    public Teacher teacher;
    public String name;
    public ArrayList<Student> nonAltStudents;
    public Student alternate; // The alternate student. This object is not in the "students" array list

    public static HashMap<Short, HashMap<Short, Team>> teams;   // Maps a teacher uid to a list of their Teams

    public Team(Teacher teacher, String name, ArrayList<Student> students, Student alternate) {
        this.teacher = teacher;
        this.name = name;
        this.nonAltStudents = students;
        this.alternate = alternate;
    }

    public void update(boolean insert) throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt;
        if(insert) {
            stmt = conn.prepareStatement("INSERT INTO teams (teacher, name, nonAltUids, alternate) " +
                    "VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        } else {
            stmt = conn.prepareStatement("UPDATE teams SET teacher = ?, name = ?, nonAltUids = ?, alternate = ? " +
                    "WHERE tid = ?");
        }
        stmt.setShort(1, teacher.uid);
        stmt.setString(2, name);

        JsonArray nonAltUids = new JsonArray();
        for(int i=0, j=nonAltStudents.size();i<j;i++) {
            nonAltUids.add(nonAltStudents.get(i).uid);
        }
        stmt.setString(3, nonAltUids.toString());
        if(alternate != null) stmt.setShort(4,  alternate.uid);
        else stmt.setShort(4,  (short)-1);

        if(!insert) stmt.setShort(5, tid);

        System.out.println(stmt);
        stmt.execute();
        if(insert) {    // Get the tid if we are inserting it
            ResultSet rs = stmt.getGeneratedKeys();
            if(rs.next()) {
                this.tid = rs.getShort(1);
            }
        }
    }

    public void delete() throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt;
        stmt = conn.prepareStatement("DELETE FROM teams WHERE tid=?");

        stmt.setShort(1, tid);

        System.out.println(stmt);
        stmt.execute();

        teams.get(teacher.uid).remove(tid);
    }

    public static void initialize() {
        teams = new HashMap<>();

        Connection conn = Conn.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM teams");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                short tid = rs.getShort("tid");
                short teacherId = rs.getShort("teacher");
                String name = rs.getString("name");
                String uidsS= rs.getString("nonAltUids");
                short alternateId = rs.getShort("alternate");

                Teacher teacher = TeacherMap.getByUID(teacherId);
                if(teacher == null) continue;

                short[] uids = null;
                try {
                    uids = gson.fromJson(uidsS, short[].class);
                } catch(Exception e) {
                    e.printStackTrace();
                    continue;
                }
                ArrayList<Student> students = new ArrayList<>();
                Student alternate = null;
                for(short uid: uids) {
                    Student student = StudentMap.getByUID(uid);
                    students.add(student);
                }
                if(alternateId > 0) alternate = StudentMap.getByUID(alternateId);

                Team team = new Team(teacher, name, students, alternate);
                team.setTID(tid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void setTID(short tid) {
        this.tid = tid;

        HashMap<Short, Team> teamListForThisTeacher;
        if(teams.containsKey(teacher.uid)) teamListForThisTeacher = teams.get(teacher.uid);
        else {
            teamListForThisTeacher = new HashMap<>();
            teams.put(teacher.uid, teamListForThisTeacher);
        }

        teamListForThisTeacher.put(tid, this);
    }
}
