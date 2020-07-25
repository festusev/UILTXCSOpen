package Outlet;

import java.math.BigInteger;
import java.util.HashMap;

public class StudentMap {
    private static HashMap<Short, Student> uidMap;
    private static HashMap<Short, HashMap<Short, Student>> teacherMap;
    private static HashMap<BigInteger, Student> tokenMap;
    private static HashMap<String, Student> emailMap;
    private static HashMap<String, Student> unameMap;

    public static void reset() {
        uidMap = new HashMap<>();
        teacherMap = new HashMap<>();
        tokenMap = new HashMap<>();
        emailMap = new HashMap<>();
        unameMap = new HashMap<>();
    }
    public static Student getByUID(short uid) {
        return uidMap.get(uid);
    }
    public static HashMap<Short,Student> getByTeacher(short uid) {
        return teacherMap.get(uid);
    }
    public static Student getByToken(BigInteger token) {
        return tokenMap.get(token);
    }
    public static Student getByEmail(String email) {
        return emailMap.get(email);
    }
    public static Student getByUname(String uname) {
        return unameMap.get(uname);
    }
    public static void addStudent(Student student) {
        uidMap.put(student.uid, student);
        if(teacherMap.containsKey(student.teacherId)) { // If other students are registered to this teacher
            teacherMap.get(student.teacherId).put(student.uid, student);
        } else {
            HashMap<Short, Student> temp = new HashMap<>();
            temp.put(student.uid, student);
            teacherMap.put(student.teacherId, temp);
        }
        tokenMap.put(student.token, student);
        emailMap.put(student.email, student);
        unameMap.put(student.uname, student);
    }
    public static void deleteStudent(Student student) {
        uidMap.remove(student.uid);
        teacherMap.remove(student.teacherId);
        tokenMap.remove(student.token);
        emailMap.remove(student.email);
        unameMap.remove(student.uname);
    }
}