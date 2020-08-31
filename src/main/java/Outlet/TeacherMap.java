package Outlet;

import java.math.BigInteger;
import java.util.HashMap;

public class TeacherMap {
    private static HashMap<Short, Teacher> uidMap;
    private static HashMap<BigInteger, Teacher> tokenMap;
    private static HashMap<String, Teacher> emailMap;
    private static HashMap<String, Teacher> classCodeMap;

    public static void reset() {
        uidMap = new HashMap<>();
        tokenMap = new HashMap<>();
        emailMap = new HashMap<>();
        classCodeMap = new HashMap<>();
    }
    public static Teacher getByUID(short uid) {
        return uidMap.get(uid);
    }
    public static Teacher getByToken(BigInteger token) {
        return tokenMap.get(token);
    }
    public static Teacher getByEmail(String email) {
        return emailMap.get(email);
    }
    public static Teacher getByClassCode(String code) {return classCodeMap.get(code);}
    public static void addTeacher(Teacher teacher) {
        uidMap.put(teacher.uid, teacher);
        tokenMap.put(teacher.token, teacher);
        emailMap.put(teacher.email, teacher);
        classCodeMap.put(teacher.classCode, teacher);
    }
    public static void deleteTeacher(Teacher teacher) {
        uidMap.remove(teacher.uid);
        tokenMap.remove(teacher.token);
        emailMap.remove(teacher.email);
        classCodeMap.remove(teacher.classCode);
    }
}