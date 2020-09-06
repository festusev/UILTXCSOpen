package Outlet;

import Outlet.uil.UIL;
import Outlet.uil.UILEntry;
import com.google.gson.JsonObject;

import java.io.IOException;
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
        String[] dict = s.split(",");   // formatted like ['1:2', '3:49']

        cids = new HashMap<>();
        for(String entry: dict) {
            System.out.println("Entry="+entry);
            if(entry.isEmpty()) continue;
            String[] temp = entry.split(":");
            short cid = Short.parseShort(temp[0]);
            short tid = Short.parseShort(temp[1]);
            cids.put(cid, UILEntry.loadUILEntry(tid, cid));
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
