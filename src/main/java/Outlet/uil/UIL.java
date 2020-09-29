package Outlet.uil;
import Outlet.*;
import com.google.gson.Gson;

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
    //private static HashMap<Short, Competition> upcoming;    // Upcoming competitions
    //private static HashMap<Short, Competition> running;     // Running competitions
    //private static HashMap<Short, Competition> archived;    // Past competitions
    private static HashMap<Short, Competition> published;
    private static HashMap<Short, Competition> unpublished; // Unpublished competitions
    public static boolean initialized = false;
    public static void initialize() throws SQLException {
        if(initialized) return;
        unpublished = new HashMap<>();
        // upcoming = new HashMap<>();
        // running = new HashMap<>();
        published = new HashMap<>();
        System.out.println("Getting connection");
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM competitions");
        ResultSet rs = stmt.executeQuery();

        System.out.println("Got connection");

        while(rs.next()) {
            boolean published = rs.getBoolean("published");
            short type = rs.getShort("type");   // If 0, there is only a mc test, if 1, only a frq test, if 2, both

            MCTest mcTest;
            FRQTest frqTest;
            if(type==1) {   // No MC Test
                mcTest = new MCTest();
            } else {
                mcTest = new MCTest(published,rs.getString("mcOpens"), gson.fromJson(rs.getString("mcKey"), String[][].class),
                        rs.getShort("mcCorrectPoints"),
                        rs.getShort("mcIncorrectPoints"),rs.getString("mcInstructions"),
                        rs.getString("mcTestLink"), rs.getLong("mcTime"));
            }
            short cid = rs.getShort("cid");
            short uid = rs.getShort("uid");
            if(type==0) {   // No FRQ Test
                frqTest = new FRQTest();
            } else {
                frqTest = new FRQTest(published,rs.getString("frqOpens"),
                        rs.getShort("frqMaxPoints"), rs.getShort("frqIncorrectPenalty"),
                        gson.fromJson(rs.getString("frqProblemMap"),String[].class),
                        rs.getString("frqStudentPack"),rs.getString("frqJudgePacket"),
                        rs.getLong("frqTime"));
            }

            Competition comp = new Competition((Teacher) UserMap.getUserByUID(uid), cid,
                    published, rs.getBoolean("isPublic"),rs.getString("name"),rs.getString("description"),
                    mcTest, frqTest);

            if(!comp.published) {
                unpublished.put(comp.template.cid, comp);
            } else {
                UIL.published.put(comp.template.cid, comp);
            }
            /*else if(!comp.template.opens.done()) {   // The competition is yet to open
                upcoming.put(comp.template.cid, comp);
            } else if(!comp.template.closes.done()) {   // The competition is yet to close
                running.put(comp.template.cid, comp);
            } else {    // The competition is over
                archived.put(comp.template.cid, comp);
            }
            }*/
        }
        initialized = true;
    }

    public static Competition getPublishedCompetition(short cid) {
        if(!initialized) {
            try {
                initialize();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        /*Competition up = upcoming.get(cid);
        if(up != null) return up;
        Competition run = running.get(cid);
        if(run != null) return run;
        Competition done = archived.get(cid);*/
        return published.get(cid);
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
        /*Competition up = upcoming.get(cid);
        if(up != null) return up;
        Competition run = running.get(cid);
        if(run != null) return run;
        Competition done = archived.get(cid);
        if(done != null) return done;*/
        return published.get(cid);
    }

    /*public static HashMap<Short,Competition> getUpcoming() {
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
    }*/
    /*public static HashMap<Short, Competition> getAllPublished() {
        HashMap<Short, Competition> competitions = getRunning();
        competitions.putAll(getArchived());
        competitions.putAll(getUpcoming());

        return competitions;
    }*/
    public static void addCompetition(Competition comp) {
        if(!initialized) {
            try {
                initialize();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
        unpublished.remove(comp.template.cid);
        published.remove(comp.template.cid);
        //upcoming.remove(comp.template.cid);
        //running.remove(comp.template.cid);
        //archived.remove(comp.template.cid);
        if(!comp.published) {
            unpublished.put(comp.template.cid, comp);
        } else {
            published.put(comp.template.cid, comp);
        }
        /*else if(!comp.template.opens.done()) {   // The competition is yet to open
            upcoming.put(comp.template.cid, comp);
        } else if(!comp.template.closes.done()) {   // The competition is yet to close
            running.put(comp.template.cid, comp);
        } else {    // The competition is over
            archived.put(comp.template.cid, comp);
        }*/
    }
    public static void deleteCompetition(Competition comp){
        comp.delete();
        if(!initialized) {
            try {
                initialize();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
        /*running.remove(comp.template.cid);
        archived.remove(comp.template.cid);
        upcoming.remove(comp.template.cid);*/
        published.remove(comp.template.cid);
        unpublished.remove(comp.template.cid);
    }
    public static void unPublish(Competition comp) {
        /*running.remove(comp.template.cid);
        archived.remove(comp.template.cid);
        upcoming.remove(comp.template.cid);*/
        published.remove(comp.template.cid);
        unpublished.put(comp.template.cid, comp);
    }
    public static void publish(Competition comp) {
        unpublished.remove(comp.template.cid);
        addCompetition(comp);
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = UserMap.getUserByRequest(request);
        if(user==null || user.token == null){
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String cidS = request.getParameter("cid");
        System.out.println("Doing get for cid="+cidS);

        if(cidS == null || cidS.isEmpty() || getPublishedCompetition(Short.parseShort(cidS))==null) {    // In this case we are showing all of the available competitions
            Conn.setHTMLHeaders(response);
            PrintWriter writer = response.getWriter();
            String right = "<div id='competitions'><div id='nav'><p onclick='showPublic(this)' class='selected'>Public</p>";

            boolean showClassHTML = user.teacher || TeacherMap.getByUID(((Student)user).teacherId) != null;
            if(showClassHTML) right += "<p onclick='showClassComps(this)' id='showClassComps'>Class</p>";
            if(user.teacher) right+="<p id='createNewCompetition' onclick='createNewCompetition()'>New</p>";
            else right += "<p id='showUpcomingComps' onclick='showUpcomingComps(this)'>Upcoming</p>";

            right += "</div><div id='comp-list'><h1 id='title'>Competitions</h1><div id='public_competitions' class='column'>";

            if(published.size() <=0) {  // There are no published competitions
                right+="<p class='emptyWarning'>There are no public competitions.</p>";
            } else {    // There are published competitions
                ArrayList<Competition> ordered = new ArrayList<>(published.values());  // Sort them by date
                Collections.sort(ordered, new SortCompByDate());

                boolean foundPublic = false;
                String upcoming = "";
                String running = "";
                String archived = "";
                for(Competition comp: ordered) {
                    if(comp.isPublic) {
                        String html = comp.template.getMiniHTML(user);
                        if(!comp.template.opens.done()) upcoming += html;
                        else if(!comp.template.closes.done()) running += html;
                        else archived += html;
                        foundPublic = true;
                    }
                }
                if(!foundPublic) {
                    right+="<p class='emptyWarning'>There are no public competitions.</p>";
                } else {
                    right += "<h3>Upcoming</h3>";
                    if(upcoming.isEmpty()) {
                        right += "<p class='emptyWarning'>There are no upcoming public competitions</p>";
                    } else {
                        right += upcoming;
                    }
                    right += "<h3>Running</h3>";
                    if(running.isEmpty()) {
                        right += "<p class='emptyWarning'>There are no running competitions.</p>";
                    } else {
                        right += running;
                    }
                    right += "<h3>Archived</h3>";
                    if(archived.isEmpty()) {
                        right += "<p class='emptyWarning'>There are no archived competitions.</p>";
                    } else {
                        right += archived;
                    }
                }
            }
            right+="</div>";

            if(showClassHTML) {
                right += "<div id='class_competitions' style='display:none' class='column'>";
                ArrayList<Competition> ordered;
                if(!user.teacher) {
                    Teacher teacher = TeacherMap.getByUID(((Student)user).teacherId);
                    ordered = teacher.getCompetitions();
                } else {
                    ordered = ((Teacher) user).getCompetitions();
                }

                if (ordered.size() <= 0) {   // There are no class competitions
                    if (user.teacher) {
                        right += "You have not created any competitions.";
                    } else {
                        right += "Your teacher has not created any competitions.";
                    }
                } else {
                    if (user.teacher) {
                        right += "<script>loadCompetitions()</script>";
                    } else {
                        for (Competition comp : ordered) {
                            if (comp.published) right += comp.template.getMiniHTML(user);
                        }
                    }
                }
                right += "</div>";
            }

            if (!user.teacher) {
                Collection<UILEntry> myCompetitions = ((Student) user).cids.values();
                right+="<div id='upcoming_competitions' style='display:none' class='column'>";
                if(myCompetitions.size() <= 0) right+="<p class='emptyWarning'>You haven't signed up for any competitions.</p>";
                for(UILEntry comp: myCompetitions) {
                    if(comp.competition.published) right += comp.competition.template.getMiniHTML(user);
                }
                right+="</div>";
            }
            right+="</div></div>";

            /*=if (!user.teacher) {
                Collection<UILEntry> myCompetitions = ((Student) user).cids.values();
                right+="<ul id='my_competitions' style='display:none' class='column'>";
                if(myCompetitions.size() <= 0) right+="<p class='emptyWarning'>You haven't signed up for any competitions.</p>";
                for(UILEntry comp: myCompetitions) {
                    if(comp.competition.published) right+="<li class='competitionCnt'>"+comp.competition.template.getMiniHTML(user)+"</li>";
                }
                right+="</ul>";
            }*/

            writer.write("<html>\n" +
                    "<head>\n" +
                    "    <title>Competitions - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                    "    <link rel=\"stylesheet\" href=\"/css/console/console.css\">\n" +
                    "    <link rel=\"stylesheet\" href=\"/css/console/uil.css\">\n" +
                    "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                    "    <script src=\"/js/console/uil_list.js\"></script>" +
                    "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css\">\n" +
                    "    <script src=\"https://cdn.jsdelivr.net/npm/flatpickr\"></script>" +
                    "</head>\n" +
                    "<body>\n" + // Dynamic.loadNav(request) +
                    Dynamic.get_consoleHTML(1, right) +
//                    "<div id='content'>" + left + "</div></div>"+right+"</div></div>" +
                    "</div></body></html>");
        } else {    // Render a specific competition. Users will be able to switch which competition they are viewing by clicking on the competition's name
            System.out.println("CID="+cidS);
            Competition competition = getPublishedCompetition(Short.parseShort(cidS));
            if(competition != null) competition.doGet(request, response);
            else System.out.println("Competition hasn't been published");
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cidS = request.getParameter("cid");
        System.out.println("Doing post, cid = " + cidS);
        Competition competition = getPublishedCompetition(Short.parseShort(cidS));
        if(cidS.isEmpty() || competition==null) {    // In this case we are showing all of the available competitions
            return;
        } else {
            competition.doPost(request, response);
        }
    }
}

/***
 * Moves a competition between maps (upcoming, running, archived)
 */
class UpdateCompetitionMap extends TimerTask {
    private Competition competition;
    private HashMap<Short, Competition> oldMap;
    private HashMap<Short, Competition> newMap;

    public UpdateCompetitionMap(Competition competition, HashMap<Short, Competition> oldMap, HashMap<Short, Competition> newMap) {
        this.competition = competition;
        this.oldMap = oldMap;
        this.newMap = newMap;
    }
    @Override
    public void run() {

    }
}