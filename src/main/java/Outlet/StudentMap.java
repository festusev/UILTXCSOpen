package Outlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class StudentMap {
    private static HashMap<Short, Student> uidMap;
    private static HashMap<Short, HashMap<Short, Student>> teacherMap;  // Maps teacher ids to a hashmap that maps student ids to students
    private static HashMap<Short, JsonArray> teacherJSONMap;    // Maps teacher ids to a json object that stores the students in the class.
    private static HashMap<BigInteger, Student> tokenMap;
    private static HashMap<String, Student> emailMap;
    private static HashMap<String, HashMap<Short, Student>> prefixList;  // Maps prefixes of names to a map of the students that have those prefixes. Lowercase

    public static void reset() {
        uidMap = new HashMap<>();
        teacherMap = new HashMap<>();
        tokenMap = new HashMap<>();
        emailMap = new HashMap<>();
        teacherJSONMap = new HashMap<>();
        prefixList = new HashMap<>();
    }
    public static Student getByUID(short uid) {
        return uidMap.get(uid);
    }
    public static HashMap<Short,Student> getByTeacher(short uid) {
        HashMap<Short, Student> map = teacherMap.get(uid);
        if(map==null) return new HashMap<>();
        return map;
    }
    public static JsonArray getJSONByTeacher(short uid) {
        return teacherJSONMap.get(uid);
    }
    public static Student getByToken(BigInteger token) {
        return tokenMap.get(token);
    }
    public static Student getByEmail(String email) {
        return emailMap.get(email);
    }
    public static HashMap<Short, Student> getByPrefix(String prefix) {
        return prefixList.get(prefix.toLowerCase());
    }
    private static ArrayList<String> getPrefixes(String name) { // Gets a list of the prefixes of this name
        ArrayList<String> prefixes = new ArrayList<>();
        String lastPrefix = "";
        char[] chars = name.toCharArray();
        for(char c: chars) {
            lastPrefix += c;
            prefixes.add(lastPrefix);
        }
        return prefixes;
    }
    public static void addStudent(Student student) {
        ArrayList<String> prefixes = getPrefixes((student.getName()).toLowerCase());
        for(String prefix: prefixes) {
            HashMap<Short, Student> storedPrefixes= prefixList.get(prefix);
            if(storedPrefixes != null) {
                storedPrefixes.put(student.uid, student);
            } else {
                storedPrefixes = new HashMap<>();
                storedPrefixes.put(student.uid, student);
                prefixList.put(prefix, storedPrefixes);
            }
        }
        uidMap.put(student.uid, student);
        if(teacherMap.containsKey(student.teacherId)) { // If other students are registered to this teacher
            teacherMap.get(student.teacherId).put(student.uid, student);
            JsonArray array = teacherJSONMap.get(student.teacherId);
            JsonArray data = new JsonArray();
            data.add(student.getName());
            data.add(student.uid);

            if(array != null) {
                array.add(data);
            } else {
                array = new JsonArray();
                array.add(data);
                teacherJSONMap.put(student.teacherId, array);
            }
        } else {
            HashMap<Short, Student> temp = new HashMap<>();
            temp.put(student.uid, student);
            teacherMap.put(student.teacherId, temp);

            JsonArray array = new JsonArray();
            JsonArray data = new JsonArray();
            data.add(student.getName());
            data.add(student.uid);
            array.add(data);
            teacherJSONMap.put(student.teacherId, array);
        }
        tokenMap.put(student.token, student);
        emailMap.put(student.email, student);
    }
    public static void deleteStudent(Student student) {
        ArrayList<String> prefixes = getPrefixes((student.getName()).toLowerCase());
        for(String prefix: prefixes) {
            HashMap<Short, Student> storedPrefixes= prefixList.get(prefix);
            if(storedPrefixes != null) {
                storedPrefixes.remove(student.uid);
            }
        }

        uidMap.remove(student.uid);
        teacherMap.get(student.teacherId).remove(student.uid);
        tokenMap.remove(student.token);
        emailMap.remove(student.email);

        JsonArray array = teacherJSONMap.get(student.teacherId);
        for(int i=0,j=array.size();i<j;i++) {
            JsonArray data = array.get(i).getAsJsonArray();

            if(data.get(1).getAsShort() == student.uid) {
                array.remove(i);
                break;
            }
        }
    }
}