package Outlet;

import Outlet.uil.UIL;
import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/***
 * Manages a lot of private hashmaps mapping token, uid, students, teachers, and more to User objects
 */
public class UserMap {
    private static Gson gson = new Gson();

    /***
     * Loads every single user from the database into the maps.
     * @return
     */
    public static int initialize() {
        StudentMap.reset(); // Initialize the StudentMap class
        TeacherMap.reset(); // Initialize the TeacherMap class

        Connection conn = Conn.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                String email = rs.getString("email");
                String fname = rs.getString("fname");
                String lname = rs.getString("lname");
                String school = rs.getString("school");
                String tokenS = rs.getString("token");

                boolean isTeacher = rs.getBoolean("teacher");
                String cids = rs.getString("cids");
                String classString = rs.getString("class");
                short uid = rs.getShort("uid");
                String password = rs.getString("password");

                BigInteger token;
                if(tokenS != null)
                    token = new BigInteger(tokenS, Character.MAX_RADIX);
                else
                    token = null;

                User u = loadUser(email, fname, lname, school, token, uid, isTeacher, cids, classString, password);
                if(isTeacher) { // Add this teacher object to each of its competitions
                    Teacher teacher = (Teacher) u;
                    for(short cid: teacher.cids) {
                        UIL.getCompetition(cid).setTeacher(teacher);
                    }
                }
                if(!isTeacher) ((Student)u).setCids(cids);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        return 1;
    }

    public static User loadUser(String email,String fname, String lname, String school, BigInteger token,short uid,boolean isTeacher,String cids, String classString, String password) {
        User user;
        System.out.println("CIDS = " + cids);
        if(isTeacher) {
            user = new Teacher();
            short[] list = gson.fromJson(cids, short[].class);
            ArrayList<Short> obj = ((Teacher)user).cids;
            for(short s: list) {
                obj.add(s);
                UIL.getCompetition(s).setTeacher((Teacher)user);
            }

            ((Teacher) user).classCode = classString;
            user.email = email;
            user.fname = fname;
            user.lname = lname;
            user.school = school;
            user.token = token;
            user.uid = uid;
            user.password = password;

            TeacherMap.addTeacher((Teacher)user);
            System.out.println("Adding teacher with uid = " +user.uid);
        } else {
            user = new Student();
            ((Student) user).teacherId=Short.parseShort(classString);

            user.email = email;
            user.fname = fname;
            user.lname = lname;
            user.school = school;
            user.token = token;
            user.uid = uid;
            user.password = password;

            StudentMap.addStudent((Student)user);
        }


        return user;
    }

    public static void addUser(User u) {
        if(u.teacher) TeacherMap.addTeacher((Teacher)u);
        else StudentMap.addStudent((Student)u);
    }
    public static void delUser(User u) {
        if(u.teacher) TeacherMap.deleteTeacher((Teacher)u);
        else StudentMap.deleteStudent((Student)u);
    }
    public static User getUserByUID(short uid) {
        Student student = StudentMap.getByUID(uid);
        if(student != null) return student;

        return TeacherMap.getByUID(uid);
    }
    public static User getUserByToken(BigInteger token) {
        Student student = StudentMap.getByToken(token);
        if(student != null) return student;

        return TeacherMap.getByToken(token);
    }
    public static User getUserByRequest(HttpServletRequest request) {
        return getUserByToken(Conn.getToken(request));
    }
    public static User getUserByEmail(String email) {
        Student student = StudentMap.getByEmail(email);
        if(student != null) return student;

        return TeacherMap.getByEmail(email);
    }
}