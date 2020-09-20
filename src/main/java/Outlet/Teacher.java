package Outlet;



import Outlet.uil.Competition;
import Outlet.uil.UIL;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;

public class Teacher extends User{
    public ArrayList<Short> cids = new ArrayList<>();
    public String classCode;
    public Teacher() {
        this.teacher = true;
    }
    public ArrayList<Competition> getCompetitions() {
        ArrayList<Competition> competitions = new ArrayList<>();
        for(short c: cids) {
            competitions.add(UIL.getCompetition(c));
        }
        return competitions;
    }

    /***
     * Update the class html for all students and the teacher who are connected to the profile page web socket.
     */
    public void updateClassHTML() {
        // Now tell all of the connected sockets to update their class
        Collection<Student> students = StudentMap.getByTeacher(uid).values();
        if(students.size() > 0) {
            JsonObject obj = new JsonObject();
            obj.addProperty("action", "updateStudentList");
            obj.addProperty("html", Class.getClassHTML(students.iterator().next(), this));
            String send = gson.toJson(obj);
            for (Student student : students) {
                ProfileSocket socket = ProfileSocket.connected.get(student.uid);
                if (socket != null) {
                    try {
                        socket.send(send);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        ProfileSocket socket = ProfileSocket.connected.get(uid);
        if(socket != null) {
            JsonObject obj = new JsonObject();
            obj.addProperty("action", "updateStudentList");
            obj.addProperty("html", Class.getClassHTML(this, this));
            try {
                socket.send(gson.toJson(obj));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * Sends to all of the connected student sockets info that says they should show the join team code.
     */
    public void updateDeletedClassHTML() {
        // Now tell all of the connected sockets to update their class
        Collection<Student> students = StudentMap.getByTeacher(uid).values();
        if(students.size() > 0) {
            for (Student student : students) {
                ProfileSocket socket = ProfileSocket.connected.get(student.uid);
                if (socket != null) {
                    try {
                        socket.send("{\"action\":\"showJoinClass\"}");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
