package Outlet;
import Outlet.uil.Competition;
import Outlet.uil.FRQTest;
import Outlet.uil.MCTest;
import Outlet.uil.UIL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/***
 * The first page the user sees when they log in. Contains user and team configurations.
 * Created by Evan Ellis.
 */
public class Profile extends HttpServlet{
    protected static Gson gson = new Gson();
    private static final String PAGE_NAME = "console";

    public static String getClassHTML(User u, Teacher teacher) {
        String html = "<div class='profile_cmpnt full'>";

        if(teacher != null) {
            if (u.teacher) {
                html += "<script>loadCompetitions();</script><h2>Class Code: <b>" + ((Teacher) u).classCode + "</b></h2></div>";
            } else {
                html += "<h2>Teacher: <b>" + teacher.fname + " " + teacher.lname + "</b></h2><span onclick='leaveClass()' class='leaveClass'>Leave Class</span></div>";
            }
            html += "<div class='profile_cmpnt full'><h2>Students</h2>";

            Collection<Student> students = StudentMap.getByTeacher(teacher.uid).values();    // All of the students in the class
            if (students.size() <= 0) {
                html += "<div>No students.</div>";
            } else {
                html += "<ul id='studentList'>";
                for (Student s : students) {
                    html += "<li>" + s.fname + " " + s.lname;
                    if (u.teacher) html += "<span class='kick' onclick='kickStudent(this, "+s.uid+")'>Kick</span>";
                    html += "</li>";
                }
                html += "</ul>";
            }
        } else {    // They are not a teacher and do not belong to a class
            html += "<script>showJoinClass();</script>"; // I use an img tag here so that the script is executed when inserted using .innerHTML
        }
        return html + "</div>";
    }

    private void savePublished(HttpServletRequest request, PrintWriter writer, User u) throws IOException, ServletException {
        System.out.println("Saving published");
        String cidS = request.getParameter("cid");
        String description = request.getParameter("description");
        String name = request.getParameter("name");
        boolean isPublic = request.getParameter("isPublic").equals("true");
        boolean writtenExists = request.getParameter("writtenExists").equals("true");
        boolean handsOnExists = request.getParameter("handsOnExists").equals("true");

        if(name.isEmpty()) {
            writer.write("{\"error\":\"Competition name is empty.\"}");
            return;
        } else if (description.length() > 32000) {
            writer.write("{\"error\":\"Description cannot exceed 32000 characters.\"}");
            return;
        }

        long now = (new Date()).getTime();
        short cid;
        MCTest mcTest;
        FRQTest frqTest;
        if(!writtenExists) {   // No MC Test
            mcTest = new MCTest();
        } else {
            String mcOpensString;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(Countdown.DATETIME_FORMAT, Locale.ENGLISH);  // Lets us make dates easily
                sdf.setTimeZone(TimeZone.getTimeZone("CST"));
                mcOpensString = request.getParameter("mcOpens");
                Date opensDate = sdf.parse(mcOpensString);
                long difference = opensDate.getTime() - now;
                if(difference <= 0) {   // The given datetime is in the past
                    writer.write("{\"error\":\"Written Start cannot be in the past.\"}");
                    return;
                } else if(difference > (long)1000*60*60*24*365*10) {   // The given datetime is more than 10 years in the future
                    writer.write("{\"error\":\"Written Start cannot be more than 10 years in the future.\"}");
                    return;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Written Start is formatted incorrectly.\"}");
                return;
            }
            String[][] mcAnswers;
            try {
                mcAnswers = gson.fromJson(request.getParameter("mcAnswers"), String[][].class);
                if(mcAnswers.length <= 0) {
                    writer.write("{\"error\":\"Written Test is empty.\"}");
                    return;
                } else if(mcAnswers.length > 240) {
                    writer.write("{\"error\":\"Written Test cannot have more than 240 problems.\"}");
                    return;
                }

                for(String[] problem:mcAnswers) {
                    if(problem[0].isEmpty()) {
                        writer.write("{\"error\":\"Written answer cannot be empty.\"}");
                        return;
                    } else if(!problem[1].equals("0") && !problem[1].equals("1")) {
                        System.out.println("Issue with problems");
                        writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                        return;
                    }
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Written Answers is formatted incorrectly.\"}");
                return;
            }

            short mcCorrectPoints;
            try {
                mcCorrectPoints = Short.parseShort(request.getParameter("mcCorrectPoints"));
                if(mcCorrectPoints > 1000) {
                    writer.write("{\"error\":\"Written Test Correct Points cannot be greater than 1000.\"}");
                    return;
                } else if(mcCorrectPoints < -1000) {
                    writer.write("{\"error\":\"Written Test Correct Points cannot be less than -1000.\"}");
                    return;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Points Per Correct for the Written Test.\"}");
                return;
            }

            short mcIncorrectPoints;
            try {
                mcIncorrectPoints = Short.parseShort(request.getParameter("mcIncorrectPoints"));
                if(mcIncorrectPoints > 1000) {
                    writer.write("{\"error\":\"Written Test Incorrect Points cannot be greater than 1000.\"}");
                    return;
                } else if(mcIncorrectPoints < -1000) {
                    writer.write("{\"error\":\"Written Test Incorrect Points cannot be less than -1000.\"}");
                    return;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Points Per Incorrect for the Written Test.\"}");
                return;
            }
            long mcTime;
            try {
                mcTime = Long.parseLong(request.getParameter("mcTime"))*1000*60;
                if(mcTime <= 0) {
                    writer.write("{\"error\":\"Written Test Length must be greater than zero.\"}");
                    return;
                } else if(mcTime > 1000*60*60*24*7) {
                    writer.write("{\"error\":\"Written Test cannot last longer than 7 days.\"}");
                    return;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Length for the Written Test.\"}");
                return;
            }
            mcTest = new MCTest(true, mcOpensString, mcAnswers, mcCorrectPoints,
                    mcIncorrectPoints,request.getParameter("mcInstructions"),
                    request.getParameter("mcTestLink"), request.getParameter("mcAnswersLink"),
                    mcTime);
        }

        if(!handsOnExists) {   // No FRQ Test
            frqTest = new FRQTest();
        } else {
            String frqOpensString;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(Countdown.DATETIME_FORMAT, Locale.ENGLISH);  // Lets us make dates easily
                sdf.setTimeZone(TimeZone.getTimeZone("CST"));
                frqOpensString = request.getParameter("frqOpens");
                Date opensDate = sdf.parse(frqOpensString);

                long difference = opensDate.getTime() - now;
                if(difference <= 0) {   // The given datetime is in the past
                    writer.write("{\"error\":\"Hands-On Start cannot be in the past.\"}");
                    return;
                } else if(difference > (long)1000*60*60*24*365*10) {   // The given datetime is more than 10 years in the future
                    writer.write("{\"error\":\"Hands-On Start cannot be more than 10 years in the future.\"}");
                    return;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Hands-On Start is formatted incorrectly.\"}");
                return;
            }

            String[] frqProblemMap;
            try {
                frqProblemMap = gson.fromJson(request.getParameter("frqProblemMap"), String[].class);

                if(frqProblemMap.length <= 0) {
                    writer.write("{\"error\":\"Hands-On Test is empty.\"}");
                    return;
                } else if(frqProblemMap.length > 48) {
                    writer.write("{\"error\":\"Hands-On Test cannot have more than 48 problems.\"}");
                    return;
                }

                Set<String> duplicateChecker = new HashSet<>();
                for(String s: frqProblemMap) {
                    if(duplicateChecker.contains(s)) {
                        writer.write("{\"error\":\"Duplicate Hands-On problem name '"+s+"'.\"}");
                        return;
                    } else if(s.isEmpty()) {
                        writer.write("{\"error\":\"Hands-On problem name is empty.\"}");
                        return;
                    }
                    duplicateChecker.add(s);
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Hands-On Problem Map is formatted incorrectly.\"}");
                return;
            }

            short frqMaxPoints;
            try {
                frqMaxPoints = Short.parseShort(request.getParameter("frqMaxPoints"));
                if(frqMaxPoints > 1000) {
                    writer.write("{\"error\":\"Hands-On Test Max Points cannot be greater than 1000.\"}");
                    return;
                } else if(frqMaxPoints < -1000) {
                    writer.write("{\"error\":\"Hands-On Test Max Points cannot be less than -1000.\"}");
                    return;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Max Points for the Hands-On Test.\"}");
                return;
            }

            short frqIncorrectPenalty;
            try {
                frqIncorrectPenalty = Short.parseShort(request.getParameter("frqIncorrectPenalty"));
                if(frqIncorrectPenalty > 1000) {
                    writer.write("{\"error\":\"Hands-On Test Incorrect Points cannot be greater than 1000.\"}");
                    return;
                } else if(frqIncorrectPenalty < -1000) {
                    writer.write("{\"error\":\"Hands-On Test Incorrect  Points cannot be less than -1000.\"}");
                    return;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Incorrect Penalty for the Hands-On Test.\"}");
                return;
            }

            long frqTime;
            try {
                frqTime = Long.parseLong(request.getParameter("frqTime"))*1000*60;
                if(frqTime <= 0) {
                    writer.write("{\"error\":\"Hands-On Test Length must be greater than zero.\"}");
                    return;
                } else if(frqTime > 1000*60*60*24*7) {
                    writer.write("{\"error\":\"Hands-On Test cannot last longer than 7 days.\"}");
                    return;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Length for the Hands-On Test.\"}");
                return;
            }

            frqTest = new FRQTest(true, frqOpensString, frqMaxPoints, frqIncorrectPenalty, frqProblemMap,
                    request.getParameter("frqStudentPacket"), request.getParameter("frqJudgePacket"), frqTime);
        }

        if(!mcTest.exists && !frqTest.exists) {
            writer.write("{\"error\":\"This competition has no tests.\"}");
            return;
        }

        Competition competition = null;
        if(cidS == null || cidS.isEmpty() || !((Teacher)u).cids.contains(cid = Short.parseShort(cidS))) {
            // We are creating a competition and returning the cid
            try {
                competition = Competition.createCompetition((Teacher)u, true, isPublic,
                        name, description, mcTest, frqTest);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {    // We are modifying an existing competition
            competition = UIL.getCompetition(cid);
            if(competition == null) {
                System.out.println("Competition not found");
                writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                return;
            }

            competition.published = true;
            frqTest.setDirectories(cid, u.uid);

            if(frqTest.exists) {
                frqTest.updateProblemDirectories(gson.fromJson(request.getParameter("frqIndices"), short[].class),
                        competition.template.frqTest.PROBLEM_MAP.length);
            } else if(competition.template.frqTest.exists) {    // They have deleted the frq test, so remove the directory
                frqTest.deleteTestcaseDir();
            }
            try {
                competition.update((Teacher)u, true, isPublic, name, description, mcTest, frqTest);
            } catch (SQLException e) {
                e.printStackTrace();
                writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                return;
            }
            UIL.publish(competition);
        }

        /* Now, write all of the files they updated to the disk */
        System.out.println("Writing files to disk, handsOnExists="+handsOnExists);
        if(handsOnExists) {
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                String partName = part.getName();
                System.out.println("Looping, partName=" + partName);
                String prefix = partName.substring(0, 3);  // Either 'fi:' or 'fo:'
                if (prefix.equals("fi:")) {  // File in
                    int probNum = Integer.parseInt(partName.substring(3));
                    InputStream fileContent = part.getInputStream();

                    byte[] bytes = new byte[fileContent.available()];
                    fileContent.read(bytes);

                    System.out.println("probNum=" + probNum);
                    frqTest.setTestcaseFile(probNum, bytes, true);
                } else if (prefix.equals("fo:")) {   // File out
                    int probNum = Integer.parseInt(partName.substring(3));

                    InputStream fileContent = part.getInputStream();

                    byte[] bytes = new byte[fileContent.available()];
                    fileContent.read(bytes);

                    System.out.println("probNum=" + probNum);
                    frqTest.setTestcaseFile(probNum, bytes, false);
                }
            }
            frqTest.initializeFiles();
        }
        writer.write("{\"success\":\"Competition saved.\",\"cid\":\""+competition.template.cid+"\"}");
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
        if(action != null && action.equals("getCompetitions") && u.teacher) {
            JsonArray listJ = new JsonArray();
            // String json = "[";
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");

            int c = 0;
            int length = ((Teacher)u).cids.size();
            for(short cid: ((Teacher)u).cids) {
                Competition competition = UIL.getCompetition(cid);
                JsonObject compJ = new JsonObject();
                compJ.addProperty("cid", cid);
                compJ.addProperty("name", competition.template.name);
                compJ.addProperty("published", competition.published);
                compJ.addProperty("isPublic", competition.isPublic);
                compJ.addProperty("description", StringEscapeUtils.escapeHtml4(competition.template.description));

                if(competition.template.mcTest.exists) {
                    JsonObject writtenJ = new JsonObject();
                    writtenJ.addProperty("opens", competition.template.mcTest.opens.DATE_STRING);
                    writtenJ.addProperty("time", (competition.template.mcTest.TIME / (1000 * 60)));

                    JsonArray writtenAnswersJ = new JsonArray();
                    for (int i = 0, j = competition.template.mcTest.KEY.length; i < j; i++) {
                        JsonArray answerJ = new JsonArray();
                        answerJ.add(StringEscapeUtils.escapeHtml4(competition.template.mcTest.KEY[i][0]));
                        answerJ.add(competition.template.mcTest.KEY[i][1]);
                        writtenAnswersJ.add(answerJ);
                    }
                    writtenJ.add("answers", writtenAnswersJ);
                    writtenJ.addProperty("instructions", StringEscapeUtils.escapeHtml4(competition.template.mcTest.INSTRUCTIONS));
                    writtenJ.addProperty("testLink", StringEscapeUtils.escapeHtml4(competition.template.mcTest.TEST_LINK));
                    writtenJ.addProperty("correctPoints", competition.template.mcTest.CORRECT_PTS);
                    writtenJ.addProperty("incorrectPoints", competition.template.mcTest.INCORRECT_PTS);
                    compJ.add("written", writtenJ);
                }
                if(competition.template.frqTest.exists) {
                    JsonObject handsOnJ = new JsonObject();
                    handsOnJ.addProperty("opens", competition.template.frqTest.opens.DATE_STRING);
                    handsOnJ.addProperty("time", competition.template.frqTest.TIME / (1000 * 60));

                    JsonArray problemsJ = new JsonArray();
                    for (int i = 0, j = competition.template.frqTest.PROBLEM_MAP.length; i < j; i++) {
                        problemsJ.add(competition.template.frqTest.PROBLEM_MAP[i]);
                    }
                    handsOnJ.add("problems", problemsJ);
                    handsOnJ.addProperty("studentPacketLink", StringEscapeUtils.escapeHtml4(competition.template.frqTest.STUDENT_PACKET));
                    handsOnJ.addProperty("maxPoints",competition.template.frqTest.MAX_POINTS);
                    handsOnJ.addProperty("incorrectPenalty", competition.template.frqTest.INCORRECT_PENALTY);
                    compJ.add("handsOn", handsOnJ);
                }
                listJ.add(compJ);
                c++;
            }
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");

            PrintWriter writer = response.getWriter();
            writer.write(new Gson().toJson(listJ));
            return;
        } else {
            Teacher teacher;
            if (u.teacher) teacher = (Teacher) u;
            else teacher = TeacherMap.getByUID(((Student) u).teacherId);

            Conn.setHTMLHeaders(response);

            String nav = "<div id='profile_nav_cnt'><ul id='profile_nav'><li onclick='navTo(this)' class='selected' id='profile_link'>Profile</li><li onclick='navTo(this)'>Class</li>";
            String right = "<div id='profile_cnt'>" +
                    "<div class='profile_div' id='Profile'>" +
                    "<div class='profile_cmpnt half'><h2>First Name</h2><input type='text' name='fname' id='fname' value='" + StringEscapeUtils.escapeHtml4(u.fname) + "'/></div>" +
                    "<div class='profile_cmpnt half'><h2>Last Name</h2><input type='text' name='lname' id='lname' value='" + StringEscapeUtils.escapeHtml4(u.lname) + "'></div>";
            String delUserPassText = "Your information cannot be recovered.";    // The warning text that we display when the user tries to delete their account.
            if (u.teacher) {
                nav += "<li onclick='navTo(this)'>Competitions</li>";
                right += "<div class='profile_cmpnt full'><h2>School</h2><input type='text' name='school' id='school' value='" + StringEscapeUtils.escapeHtml4(u.school) + "'></div>";
                delUserPassText = "Your class and competitions will be permanently deleted.";
            }
            nav += "<li onclick='navTo(this)'>Help</li><li id='delAccount' onclick='document.getElementById(\"delUserPasswordCnt\").style.display=\"block\";'>Delete Account</li></ul></div>";
            right += "<div class='profile_cmpnt full'><h2><b>Change Password</b></h2><h2>Old Password</h2>" +
                    "<input type='password' name='oldPassword' id='oldPassword'/><h2>New Password</h2>" +
                    "<input type='password' name='newPassword' id='newPassword'/></div>" +
                    "<div class='profile_cmpnt full'><span onclick='saveChanges()' id='saveChanges'>Save Changes</span></div>" +
                    "</div><div class='profile_div' id='Class' style='display:none'>" + getClassHTML(u, teacher) + "</div>" +
                    "<div id='delUserPasswordCnt' style='display:none'><div class='center'><h1>Are you sure?</h1><p>" + delUserPassText +
                    "</p><label for='delPass'>Retype your password:</label><input name='delPass' type='password' id='delUserPassword'/><button onclick='delUser()'>Yes, delete my account.</button><a onclick='hideDelUser()'>Cancel</a></div></div>";

            if (u.teacher) {    // List the competitions that the teacher has created and include a "New" button
                right += "<div class='profile_div' id='Competitions'  style='display:none'><p id='newCompetition' data-hasCompetitions='false''>New</p></div>";
            }
            right += "<div class='profile_div' id='Help' style='display:none'><div class='helpSection'>Join the community <a href='https://discord.gg/ukT4QnZ'" +
                    "class='link'>discord</a> to receive announcements and ask for help. You can always email us at " +
                    "<a href='mailto: contact@txcsopen.com' class='link'>contact@txcsopen.com</a>.</div>";
            if(u.teacher) {
                right += "<div class='helpSection'><h2>Quick Help</h2>Right now you are in your teacher profile. " +
                        "On the left sidebar, you will see links to access your Profile, Class, Competitions, and the Help " +
                        "page (where you are now).</div>" +

                        "<div class='helpSection'><h2>Profile</h2>In the profile page, you can update your name, school, " +
                        "and password. Then click save changes to publish your changes.</div>" +

                        "<div class='helpSection'><h2>Class</h2>The class page lets you manage the students in your classroom, " +
                        "who are able to view nonpublic competitions. Give your class code out to your students so that " +
                        "they can sign up to join your class. In their profile page, they will have the option to use " +
                        "this class code to join your class. If you want to remove a student, simply click the ‘kick’ " +
                        "button to the right of their name. The ability to send messages to your class is coming soon.</div>" +

                        "<div class='helpSection'><h2>Competitions</h2>The competitions page allows you to quickly create " +
                        "UIL competitions. Click the orange new button to create a new competition. From there you initially " +
                        "have a few options. You can name and give a description for your competition, which your students " +
                        "will see on the competition’s page when competing. This is where we suggest adding links to resources " +
                        "other than the tests, like the student packets. You can select whether the competition is public " +
                        "(viewable to everyone) or nonpublic (viewable to the students in your class). You can choose whether " +
                        "you want a written test and/or hands on programming test by clicking the checkboxes on the right " +
                        "of the labels. Finally, at the top of each competition, you can choose to save, publish, delete, " +
                        "or, if your competition is already published, jump to the competition’s page, where you can score students.<br><br>" +

                        "The written test has a start date and length (usually 45 mins). Click the plus button to the right " +
                        "of answers to insert the test key. Click the add button to add questions to the key. The key supports " +
                        "two types of questions, multiple choice questions (MC) from A to E, and short answer questions (SAQ), " +
                        "which allow any text. Click the drop down box on the right of a question to change its type. All " +
                        "questions can be rescored in the competition’s page after and when students are competing. To quickly " +
                        "edit the key, press tab after clicking on the input to a given question, and you will automatically " +
                        "highlight text from question to question. To attach the written test file, just put a link to the " +
                        "file in the test link section. (which you can do with Google Drive). You can also manually set the " +
                        "number of points per correct and incorrect problem (default 8 and 2).<br><br>" +

                        "The hands-on programming section also contains a start date and length (usually 2 hours). Click " +
                        "change problems to add problems. Next to the problem number, you can name a problem, add the judge " +
                        "input file, and the judge output file. You can manually judge student outputs on the competition’s " +
                        "page. You can link to the hands on programming test in the student packet link section. You can " +
                        "also set the number of points per correct and incorrect problem (default 60 and 5).</div>";
            } else {
                right += "<div class='helpSection'><h2>Class</h2>To join a class, enter your class’ code, which your teacher " +
                        "will give out to you. Once you’ve joined a class, you will be able to see your teacher, your classmates, " +
                        "and any competitions your teacher has published.</div>";
            }
            right += "</div></div>";


            // create HTML form
            PrintWriter writer = response.getWriter();
            writer.append("<html>\n" +
                    "<head>\n" +
                    "    <title>Profile - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                    "    <link rel=\"stylesheet\" href=\"./css/console.css\">\n" +
                    "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                    "    <script src=\"./js/console.js\"></script>\n" +
                    "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css\">\n" +
                    "    <script src=\"https://cdn.jsdelivr.net/npm/flatpickr\"></script>" +
                    "</head>\n" +
                    "<body>\n" +
                    Dynamic.loadNav(request) +
                    //"<div id='changeInstructions' style='display:none;'></div>" +
                    "<div id='content'>" + nav + right + "</div>" +
                    // Dynamic.loadCopyright() +
                    "</body>\n" +
                    "</html>");
        }
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
            writer.write(new Gson().toJson(obj));
            return;
        } else if(action.equals("saveChanges")) {   // They are updating their user's information
            String fname = request.getParameter("fname");
            String lname = request.getParameter("lname");
            String school = request.getParameter("school");
            String oldPassword = request.getParameter("oldPassword");
            String newPassword = request.getParameter("newPassword");

            System.out.println("fname="+fname+", lname="+lname+", school="+school+", oldPassword="+oldPassword+", newPassword="+newPassword);

            boolean oldPassValid = oldPassword!=null && !oldPassword.isEmpty();
            boolean newPassValid = newPassword!=null && !newPassword.isEmpty();
            if(oldPassValid || newPassValid) {
                if(oldPassValid && !newPassValid) {
                    writer.write("{\"error\":\"New password is not specified\"}");
                    return;
                } else if(!oldPassValid && newPassValid) {
                    writer.write("{\"error\":\"Old password is not specified\"}");
                    return;
                } else if(u.verifyPassword(oldPassword)) {
                    try {
                        u.changePassword(Conn.getHashedFull(newPassword));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                        return;
                    }
                } else {
                    writer.write("{\"error\":\"Password is incorrect.\"}");
                    return;
                }
            }
            if(!fname.isEmpty()) {
                u.fname = fname;
            } else {
                writer.write("{\"error\":\"First name cannot be empty.\"}");
                return;
            }
            if(!lname.isEmpty()) {
                u.lname = lname;
            } else {
                writer.write("{\"error\":\"Last name cannot be empty.\"}");
                return;
            }
            u.school = school;  // School can be empty

            if(u.updateUser(false) == 0) {
                writer.write("{\"success\":\"Changes saved.\"}");
                return;
            } else {
                writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                return;
            }
        } else if(action.equals("saveCompetition")) {
            String cidS = request.getParameter("cid");
            short cid;
            try {
                cid = Short.parseShort(cidS);
                Competition competition = UIL.getCompetition(cid);

                Teacher teacher = (Teacher) u;
                if (competition != null && competition.teacher.uid == teacher.uid) savePublished(request, writer, u);
                return;
            } catch (Exception e) {}

            // In this case, we are not saving a public competition, so many things will be truncated
            String description = request.getParameter("description");
            String name = request.getParameter("name");
            boolean isPublic = request.getParameter("isPublic").equals("true");
            boolean writtenExists = request.getParameter("writtenExists").equals("true");
            boolean handsOnExists = request.getParameter("handsOnExists").equals("true");

            if(name.isEmpty()) {
                writer.write("{\"error\":\"Competition name is empty.\"}");
                return;
            } else if (description.length() > 32000) {
                writer.write("{\"error\":\"Description cannot exceed 32000 characters.\"}");
                return;
            }

            long now = (new Date()).getTime();
            MCTest mcTest;
            FRQTest frqTest;
            if(!writtenExists) {   // No MC Test
                mcTest = new MCTest();
            } else {
                String mcOpensString;
                try {
                    mcOpensString = request.getParameter("mcOpens");
                } catch(Exception e) {
                    writer.write("{\"error\":\"Written Start is formatted incorrectly.\"}");
                    return;
                }
                String[][] mcAnswers;
                try {
                    mcAnswers = gson.fromJson(request.getParameter("mcAnswers"), String[][].class);
                    if(mcAnswers.length > 240) {
                        writer.write("{\"error\":\"Written Test cannot have more than 240 problems.\"}");
                        return;
                    }
                } catch(Exception e) {
                    writer.write("{\"error\":\"Written Answers is formatted incorrectly.\"}");
                    return;
                }

                short mcCorrectPoints;
                mcCorrectPoints = Short.parseShort(request.getParameter("mcCorrectPoints"));

                short mcIncorrectPoints;
                mcIncorrectPoints = Short.parseShort(request.getParameter("mcIncorrectPoints"));

                long mcTime;
                mcTime = Long.parseLong(request.getParameter("mcTime"))*1000*60;

                mcTest = new MCTest(false, mcOpensString, mcAnswers, mcCorrectPoints,
                        mcIncorrectPoints,request.getParameter("mcInstructions"),
                        request.getParameter("mcTestLink"), request.getParameter("mcAnswersLink"),
                        mcTime);
            }

            if(!handsOnExists) {   // No FRQ Test
                frqTest = new FRQTest();
            } else {
                String frqOpensString;
                frqOpensString = request.getParameter("frqOpens");

                String[] frqProblemMap;
                try {
                    frqProblemMap = gson.fromJson(request.getParameter("frqProblemMap"), String[].class);
                } catch(Exception e) {
                    writer.write("{\"error\":\"Hands-On Problem Map is formatted incorrectly.\"}");
                    return;
                }

                short frqMaxPoints;
                frqMaxPoints = Short.parseShort(request.getParameter("frqMaxPoints"));

                short frqIncorrectPenalty;
                frqIncorrectPenalty = Short.parseShort(request.getParameter("frqIncorrectPenalty"));

                long frqTime;
                frqTime = Long.parseLong(request.getParameter("frqTime"))*1000*60;

                frqTest = new FRQTest(false, frqOpensString, frqMaxPoints, frqIncorrectPenalty, frqProblemMap,
                        request.getParameter("frqStudentPacket"), request.getParameter("frqJudgePacket"), frqTime);
            }
            Competition competition = null;
            if(cidS == null || cidS.isEmpty() || !((Teacher)u).cids.contains(cid = Short.parseShort(cidS))) {
                // We are creating a competition and returning the cid
                try {
                    competition = Competition.createCompetition((Teacher)u, false, isPublic,
                            name, description, mcTest, frqTest);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {    // We are modifying an existing competition
                competition = UIL.getCompetition(cid);
                if(competition == null) {
                    writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                    return;
                }

                competition.published = false;
                frqTest.setDirectories(cid, u.uid);

                if(frqTest.exists) {
                    frqTest.updateProblemDirectories(gson.fromJson(request.getParameter("frqIndices"), short[].class),
                            competition.template.frqTest.PROBLEM_MAP.length);
                } else if(competition.template.frqTest.exists) {    // They have deleted the frq test, so remove the directory
                    frqTest.deleteTestcaseDir();
                }
                try {
                    competition.update((Teacher)u, competition.published, isPublic, name, description, mcTest, frqTest);
                } catch (SQLException e) {
                    e.printStackTrace();
                    writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                    return;
                }
            }

            /* Now, write all of the files they updated to the disk */
            System.out.println("Writing files to disk, handsOnExists="+handsOnExists);
            if(frqTest.exists) {
                Collection<Part> parts = request.getParts();
                for (Part part : parts) {
                    String partName = part.getName();
                    System.out.println("Looping, partName=" + partName);
                    String prefix = partName.substring(0, 3);  // Either 'fi:' or 'fo:'
                    if (prefix.equals("fi:")) {  // File in
                        int probNum = Integer.parseInt(partName.substring(3));
                        InputStream fileContent = part.getInputStream();

                        byte[] bytes = new byte[fileContent.available()];
                        fileContent.read(bytes);

                        System.out.println("probNum=" + probNum);
                        frqTest.setTestcaseFile(probNum, bytes, true);
                    } else if (prefix.equals("fo:")) {   // File out
                        int probNum = Integer.parseInt(partName.substring(3));

                        InputStream fileContent = part.getInputStream();

                        byte[] bytes = new byte[fileContent.available()];
                        fileContent.read(bytes);

                        System.out.println("probNum=" + probNum);
                        frqTest.setTestcaseFile(probNum, bytes, false);
                    }
                }
                frqTest.initializeFiles();
            }
            writer.write("{\"success\":\"Competition saved.\",\"cid\":\""+competition.template.cid+"\"}");
        } else if(action.equals("publishCompetition") && u.teacher) {
            savePublished(request, writer, u);
        } else if(action.equals("unPublishCompetition")) {
            short cid = Short.parseShort(request.getParameter("cid"));
            Competition competition = UIL.getCompetition(cid);

            Teacher teacher = (Teacher)u;
            if(competition != null && competition.teacher.uid == teacher.uid) {
                try {
                    competition.unPublish();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else if(action.equals("deleteCompetition") && u.teacher) {
            short cid = Short.parseShort(request.getParameter("cid"));
            Competition competition = UIL.getCompetition(cid);

            Teacher teacher = (Teacher)u;
            if(competition != null && competition.teacher.uid == teacher.uid) {
                UIL.deleteCompetition(competition);
                teacher.cids.remove(teacher.cids.indexOf(competition.template.cid));
                teacher.updateUser(false);
            }
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