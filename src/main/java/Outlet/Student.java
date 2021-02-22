package Outlet;

import Outlet.uil.UIL;
import Outlet.uil.UILEntry;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class Student extends User{
    public HashMap<Short, UILEntry> cids = new HashMap<>();    // Maps the cid of each competition they are competing in to the UILEntry of their team in that competition
    public short teacherId;
    public Student() {
        this.teacher = false;
    }
    public void setCids(String s) {
        System.out.println("CIDS= "+ s);
        s = s.substring(s.indexOf("{")+1, s.indexOf("}"));
        String[] dict = s.split(",");   // formatted like {'1:2', '3:49'}

        cids = new HashMap<>();
        for(String entryS: dict) {
            System.out.println("Entry="+entryS);
            if(entryS.isEmpty()) continue;
            String[] temp = entryS.split(":");
            short cid = Short.parseShort(temp[0]);
            short tid = Short.parseShort(temp[1]);
            UILEntry entry = UILEntry.loadUILEntry(tid, cid);
            if(entry != null) cids.put(cid, entry);
        }
    }
    public void joinClass(Teacher teacher) {
        StudentMap.deleteStudent(this);
        teacherId = teacher.uid;
        StudentMap.addStudent(this);
        updateUser(false);

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

        updateUser(false);

        for(short cid:cids.keySet()) {
            if(teacher.cids.contains(cid) && !UIL.getCompetition(cid).isPublic) {    // This competition is the teachers and is private
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
}
