package Outlet.uil;
import Outlet.*;
import com.google.gson.Gson;
import sun.nio.cs.ext.COMPOUND_TEXT;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/***
 * Manages all of the public and private competitions. Serves up a competition based on a cid passed through the url.
 * Initializes the competitions as Competition objects from the 'competitions' database, and passes off GET and POST
 * requests to the respective competition's methods.
 * Created by Evan Ellis.
 */
public class UIL extends HttpServlet{
    protected static Gson gson = new Gson();

    // TODO: Be sure that competitions change which array they are in
    private static HashMap<Short, Competition> upcoming;    // Upcoming competitions
    private static HashMap<Short, Competition> running;     // Running competitions
    private static HashMap<Short, Competition> archived;    // Past competitions
    public static boolean initialized = false;
    private static void initialize() throws SQLException {
        upcoming = new HashMap<>();
        running = new HashMap<>();
        archived = new HashMap<>();
        System.out.println("Getting connection");
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM competitions");
        ResultSet rs = stmt.executeQuery();

        System.out.println("Got connection");

        while(rs.next()) {
            Competition comp = new Competition((Teacher) UserMap.getUserByUID(rs.getShort("uid")),rs.getShort("cid"),
                    rs.getBoolean("isPublic"),rs.getString("name"),rs.getString("whatItIs"),
                    rs.getString("rules"),rs.getString("practice"),gson.fromJson(rs.getString("mcKey"),String[].class),gson.fromJson(rs.getString("mcProblemMap"),short[].class),
                    rs.getShort("mcCorrectPoints"),rs.getShort("mcIncorrectPoints"),rs.getString("mcInstructions"),
                    rs.getString("mcTestLink"),rs.getString("mcAnswers"),rs.getString("mcOpens"),rs.getLong("mcTime"),rs.getShort("frqMaxPoints"),
                    rs.getShort("frqIncorrectPenalty"), gson.fromJson(rs.getString("frqProblemMap"),String[].class),rs.getString("frqStudentPack"),rs.getString("frqJudgePacket"),
                    rs.getString("frqOpens"),rs.getLong("frqTime"),gson.fromJson(rs.getString("datMap"),String[].class));
            if(!comp.template.opens.done()) {   // The competition is yet to open
                upcoming.put(comp.template.cid, comp);
            } else if(!comp.template.closes.done()) {   // The competition is yet to close
                running.put(comp.template.cid, comp);
            } else {    // The competition is over
                archived.put(comp.template.cid, comp);
            }
        }
        initialized = true;
    }

    public static Competition getCompetition(short cid) {
        if(!initialized) {
            try {
                initialize();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        Competition up = upcoming.get(cid);
        if(up != null) return up;
        Competition run = running.get(cid);
        if(run != null) return run;
        Competition done = running.get(cid);
        if(done != null) return done;

        return null;
    }

    public static HashMap<Short,Competition> getUpcoming() {
        if(!initialized) {
            try {
                initialize();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return upcoming;
    }

    public static HashMap<Short, Competition> getRunning() {
        if(!initialized) {
            try {
                initialize();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return running;
    }

    public static HashMap<Short, Competition> getArchived() {
        if(!initialized) {
            try {
                initialize();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return archived;
    }
    public static HashMap<Short, Competition> getAll() {
        HashMap<Short, Competition> competitions = getRunning();
        competitions.putAll(getArchived());
        competitions.putAll(getUpcoming());

        return competitions;
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cidS = request.getParameter("cid");
        System.out.println("Doing get for cid="+cidS);
        User user = UserMap.getUserByRequest(request);
        if(cidS == null || cidS.isEmpty() || getCompetition(Short.parseShort(cidS))==null) {    // In this case we are showing all of the available competitions
            Conn.setHTMLHeaders(response);
            PrintWriter writer = response.getWriter();
            String body = "<div id='center'>";
            String left = "<div id='left'><h1>Public UIL</h1><p class='menu' onclick='showPublic()'>Public</p>";
            String right = "<div id='right'><h1></h1><ul id='public_competitions' class='column'>";
            if(getUpcoming().size() <=0) {  // There are no upcoming competitions
                right+="<p class='emptyWarning'>There are no upcoming competitions.</p>";
            } else {    // There are upcoming competitions
                ArrayList<Competition> ordered = new ArrayList<>(getUpcoming().values());  // Sort them by date
                Collections.sort(ordered, new SortCompByDate());

                for(Competition comp: ordered) {
                    if(comp.isPublic)
                        right+="<li class='competitionCnt'>"+comp.template.getMiniHTML(user)+"</li>";
                }
            }
            right+="</ul>";

            if(user!=null && user.uid>=0) {  // They are signed in
                if(user.teacher || !user.teacher && ((Student)user).teacherId >= 0) {   // In this case, they belong to a class
                    left += "<p class='menu' onclick='showClassComps()'>Class</p>";
                    ArrayList<Competition> ordered;
                    String teacherName;
                    // TODO: Add in a "school" column to the database
                    String classmates = "";

                    if(!user.teacher) {
                        Teacher teacher = TeacherMap.getByUID(((Student)user).teacherId);
                        ordered = teacher.getCompetitions();
                        teacherName = teacher.uname;
                        HashMap<Short, Student> temp = StudentMap.getByTeacher(teacher.uid);
                    }
                    else {
                        ordered = ((Teacher) user).getCompetitions();
                        teacherName = ((Teacher)user).uname;
                    }

                    right+="<div id='class_competitions' style='display:none' class='column'><p class='teacher_name'><br>Teacher</br>" + teacherName + "</p><p id='classmates'><br>Classmates</br><div>"+classmates+"</div></p>";
                    for(Competition comp: ordered) {
                        right+="<li class='competitionCnt'>"+comp.template.getMiniHTML(user)+"</li>";
                    }
                    right+="</div>";
                }

                if (!user.teacher) {
                    left+="<p class='menu' onclick='showMyComps()'>My Competitions</p>";

                    Collection<UILEntry> myCompetitions = ((Student) user).cids.values();
                    right+="<ul id='my_competitions' style='display:none' class='column'>";
                    for(UILEntry comp: myCompetitions) {
                        right+="<li class='competitionCnt'>"+comp.competition.template.getMiniHTML(user)+"</li>";
                    }
                    right+="</ul>";
                }
            }

            writer.write("<html>\n" +
                    "<head>\n" +
                    "    <title>Login - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                    "    <link rel=\"stylesheet\" href=\"./css/uil.css\">\n" +
                    "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                    "    <script src=\"js/uil.js\"></script>" +
                    "</head>\n" +
                    "<body>\n" + Dynamic.loadNav(request) +
                    body + left + "</div>"+right+"</div></div>" +
                    "</body></html>");
        } else {    // Render a specific competition. Users will be able to switch which competition they are viewing by clicking on the competition's name
            System.out.println("CID="+cidS);
            Competition competition = getCompetition(Short.parseShort(cidS));
            competition.doGet(request, response);
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cidS = request.getParameter("cid");
        if(cidS == null || cidS.isEmpty() || getCompetition(Short.parseShort(cidS))!=null) {    // In this case we are showing all of the available competitions
            return;
        } else {
            Competition competition = getCompetition(Short.parseShort(cidS));
            competition.doPost(request, response);
        }
    }
}