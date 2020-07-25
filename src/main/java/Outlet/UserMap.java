package Outlet;

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
        StudentMap.reset();
        TeacherMap.reset();

        HashMap<Student, String> studentCIDMap = new HashMap<>(); // Maps a student object to their cid string. This is used b/c setCids() will load in new competitions, which requires all of the users to be loaded

        Connection conn = Conn.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                String email = rs.getString("email");
                String uname = rs.getString("uname");
                String tokenS = rs.getString("token");

                boolean isTeacher = rs.getBoolean("teacher");
                String cids = rs.getString("cids");
                String classString = rs.getString("class");
                short uid = rs.getShort("uid");

                BigInteger token;
                if(tokenS != null)
                    token = new BigInteger(tokenS, Character.MAX_RADIX);
                else
                    token = null;

                System.out.println("Loading user with uid = " + uid);
                User u = loadUser(email, uname, token, uid, isTeacher, cids, classString);
                if(!isTeacher) studentCIDMap.put((Student)u, cids);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        System.out.println("Teacher uid = " + getUserByUID((short) 1).uid);
        for(Student s: studentCIDMap.keySet()) {
            s.setCids(studentCIDMap.get(s));
        }
        return 1;
    }

    public static User loadUser(String email,String uname,BigInteger token,short uid,boolean isTeacher,String cids, String classString) {
        User user;
        if(isTeacher) {
            user = new Teacher();
            short[] list = gson.fromJson(cids, short[].class);
            ArrayList<Short> obj = ((Teacher)user).cids;
            for(short s: list) obj.add(s);

            ((Teacher) user).classCode = classString;
            user.email = email;
            user.uname = uname;
            user.token = token;
            user.uid = uid;

            TeacherMap.addTeacher((Teacher)user);
            System.out.println("Adding teacher with uid = " +user.uid);
        } else {
            user = new Student();
            ((Student) user).teacherId=Short.parseShort(classString);

            user.email = email;
            user.uname = uname;
            user.token = token;
            user.uid = uid;

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
    public static User getUserByUname(String uname) {
        Student student = StudentMap.getByUname(uname);
        if(student != null) return student;

        return TeacherMap.getByUname(uname);
    }
}