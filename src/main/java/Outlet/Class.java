package Outlet;
import Outlet.uil.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.jdbc.StringUtils;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/***
 * The first page the user sees when they log in. Contains user and team configurations.
 * Created by Evan Ellis.
 */
public class Class extends HttpServlet {
    protected static Gson gson = new Gson();

    public static String getClassHTML(User u, Teacher teacher) {
        String html = "<div class='center'><div id='class'>";

        // Add in the class html
        if(teacher != null) {
            if (u.teacher) {
                html += "<p id='class_code'>Class Code: <b>" + ((Teacher) u).classCode + "</b></p>";
            } else {
                html += "<h2>Teacher: <b>" + StringEscapeUtils.escapeHtml4(teacher.fname + " " + teacher.lname) + "</b></h2><span onclick='leaveClass()' class='leaveClass'>Leave Class</span>";
            }
            html += "<script>loadClass();</script><h2>Students</h2>";

            Collection<Student> students = StudentMap.getByTeacher(teacher.uid).values();    // All of the students in the class
            if (students.size() <= 0) {
                html += "<div>No students.</div>";
            } else {
                html += "<ul id='studentList'>";
                /*for (Student s : students) {
                    html += "<li class='student'>" + StringEscapeUtils.escapeHtml4(s.fname + " " + s.lname);
                    if (u.teacher) html += "<span></span><span class='kick' onclick='kickStudent(this, "+s.uid+")'>Kick</span>";
                    html += "</li>";
                }*/
                html += "</ul>";
            }
        } else {    // They are not a teacher and do not belong to a class
            html += "<script>showJoinClass();</script>"; // I use an img tag here so that the script is executed when inserted using .innerHTML
        }
        html += "</div>";

        // Add in the team html
        if(teacher != null) {
            html += "<div id='teams'><h2>Teams</h2>";
            if(u.teacher) html += "<button id='newTeam' class='chngButton' onclick='newTeam()'>New</button>";

            html += "<ul id='teamList'></ul></div>";
        }

        return html += "</div>";
    }

    private JsonObject jsonifyStudent(Student student) {
        JsonObject studentObj = new JsonObject();
        studentObj.addProperty("name", StringEscapeUtils.escapeHtml4(student.fname + " " + student.lname));
        studentObj.addProperty("uid", student.uid);
        return studentObj;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User u = UserMap.getUserByRequest(request);
        if(u==null || u.token == null){
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String action = request.getParameter("action");
        System.out.println("action="+action+"&teacher="+u.teacher);

        Teacher teacher;
        if (u.teacher) teacher = (Teacher) u;
        else teacher = TeacherMap.getByUID(((Student) u).teacherId);

        Conn.setHTMLHeaders(response);

        // create HTML form
        PrintWriter writer = response.getWriter();
        writer.append("<html>\n" +
                "<head>\n" +
                "    <title>Class - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                "    <link rel=\"stylesheet\" href=\"/css/console/console.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"/css/console/class.css\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                "    <script src=\"/js/console/class.js\"></script>\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css\">\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/flatpickr\"></script>" +
                "</head>\n" +
                "<body>\n" +
                Dynamic.get_consoleHTML(2, getClassHTML(u, teacher)) +
                // Dynamic.loadNav(request) +
                // "<div id='changeInstructions' style='display:none;'></div>" +
                // "<div id='content'>" + nav + right + "</div>" +
                // Dynamic.loadCopyright() +
                "</body>\n" +
                "</html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if(!Conn.isLoggedIn(request)){
            return;
        }
        User u = UserMap.getUserByRequest(request);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        System.out.println("doing post");

        String action = request.getParameter("action");
        if(action == null) {
            action = partToString(request.getPart("action"));
            if(action == null) {
                response.setStatus(405);
                return;
            }
        }
        if(action.equals("joinClass") && !u.teacher) {
            String code = request.getParameter("code");
            if(code == null || code.length() != 6) {
                writer.write("{\"error\":\"Class Code must be 6 characters.\"}");
                return;
            }
            Teacher teacher = TeacherMap.getByClassCode(code.toUpperCase());
            if(teacher == null) {
                writer.write("{\"error\":\"Class Code is incorrect.\"}");
                return;
            }
            ((Student)u).joinClass(teacher);
            writer.write("{\"html\":\""+getClassHTML(u, teacher)+"\"}");
            return;
        } else if(action.equals("kickStudent") && u.teacher) {
            short uid = Short.parseShort(request.getParameter("uid"));
            Teacher teacher = (Teacher) u;
            Student student = StudentMap.getByUID(uid);
            if(student != null && student.teacherId == teacher.uid) {
                student.leaveClass(teacher);
            }
            return;
        } else if(action.equals("leaveClass") && !u.teacher) {
            Student student = (Student)u;
            Teacher teacher = TeacherMap.getByUID(student.teacherId);
            if(teacher != null) {
                student.leaveClass(teacher);
            }
        } else if(action.equals("getClass")) {  // Gets the class html
            JsonObject obj = new JsonObject();
            obj.addProperty("success", true);
            Teacher teacher;
            if(u.teacher) teacher = (Teacher) u;
            else teacher = TeacherMap.getByUID(((Student)u).teacherId);
            obj.addProperty("classHTML", getClassHTML(u, teacher));
            writer.write(gson.toJson(obj));
            return;
        } else if(action.equals("loadClass")) { // Returns a json object of the class
            Teacher teacher;
            if(u.teacher) teacher = (Teacher) u;
            else teacher = TeacherMap.getByUID(((Student)u).teacherId);

            JsonObject classData = new JsonObject();

            classData.addProperty("teacher", u.teacher);
            JsonArray studentList = new JsonArray();
            Collection<Student> students = StudentMap.getByTeacher(teacher.uid).values();
            for(Student student: students) {
                studentList.add(jsonifyStudent(student));
            }
            classData.add("studentList", studentList);

            JsonArray teamList = new JsonArray();
            if(Team.teams.containsKey(teacher.uid)) {
                Collection<Team> teams = Team.teams.get(teacher.uid).values();
                for(Team team: teams) {
                    JsonObject teamJson = new JsonObject();
                    teamJson.addProperty("tid", team.tid);
                    teamJson.addProperty("name", team.name);

                    JsonArray nonAltJson = new JsonArray(); // The array of non-alt students
                    for(Student student: team.nonAltStudents) {
                        nonAltJson.add(jsonifyStudent(student));
                    }
                    teamJson.add("nonAlt", nonAltJson);
                    if(team.alternate != null) {
                        teamJson.add("alt", jsonifyStudent(team.alternate));
                    }
                    teamList.add(teamJson);
                }
            }
            classData.add("teamList", teamList);

            writer.write(classData.toString());
        }
    }

    public static String partToString(Part part) {
        if(part == null) return null;
        try {
            InputStream stream = part.getInputStream();
            byte[] partBytes = new byte[stream.available()];
            stream.read(partBytes);
            return new String(partBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}