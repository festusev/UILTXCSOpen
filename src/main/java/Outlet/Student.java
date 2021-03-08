package Outlet;

import Outlet.uil.UIL;
import Outlet.uil.UILEntry;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

public class Student extends User{
    public HashMap<Short, UILEntry> cids = new HashMap<>();    // Maps the cid of each competition they are competing in to the UILEntry of their team in that competition
    public short teacherId;
    public Student() {
        this.teacher = false;
    }

    public void joinClass(Teacher teacher) {
        StudentMap.deleteStudent(this);
        teacherId = teacher.uid;
        StudentMap.addStudent(this);
        updateUser();

        teacher.updateClassHTML();
    }
    public void leaveClass(Teacher teacher) {
        HashMap<Short, Team> teamMap = Team.teams.get(teacher.uid);
        if(teamMap != null) {
            Collection<Team> teams = teamMap.values();

            for(Team team: teams) {
                boolean updated = false;    // Whether this student was in this team or not
                for(int i=0,j=team.nonAltStudents.size();i<j;i++) {
                    if(team.nonAltStudents.get(i).uid == uid) {
                        team.nonAltStudents.remove(i);
                        updated = true;
                    }
                }

                if(team.alternate != null && team.alternate.uid == uid) {
                    team.alternate = null;
                    updated = true;
                }

                if(updated) {
                    try {
                        team.update(false);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }

        StudentMap.deleteStudent(this);
        teacherId = -1;
        StudentMap.addStudent(this);

        updateUser();

        for(short cid:cids.keySet()) {
            if(teacher.competitions.contains(cid) && !UIL.getCompetition(cid).isPublic) {    // This competition is the teachers and is private
                cids.get(cid).leaveTeam(this);
            }
        }

        teacher.updateClassHTML();

        // Now, tell the student that they have left the class
        ProfileSocket socket = ProfileSocket.connected.get(uid);
        if(socket != null) {
            try {
                socket.send("{\"action\":\"showJoinClass\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public JsonArray getJSON() {
        JsonArray student = new JsonArray();
        student.add(getName());
        student.add(uid);

        return student;
    }
}
