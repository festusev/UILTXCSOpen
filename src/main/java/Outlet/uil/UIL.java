package Outlet.uil;
import Outlet.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

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
                        gson.fromJson(rs.getString("frqProblemMap"),FRQProblem[].class),
                        rs.getString("frqStudentPack"),rs.getString("frqJudgePacket"),
                        rs.getLong("frqTime"));
            }

            Competition comp = new Competition((Teacher) UserMap.getUserByUID(uid), cid, published,
                    rs.getBoolean("isPublic"),rs.getString("name"),rs.getString("description"),
                    mcTest, frqTest,Clarification.fromJsonToArray(rs.getString("clarifications")));

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
        Competition pub = published.get(cid);
        if(pub != null) return pub;
        return unpublished.get(cid);
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
    private boolean savePublished(HttpServletRequest request, PrintWriter writer, Teacher u) throws IOException, ServletException {
        System.out.println("Saving published");
        String cidS = request.getParameter("op_cid");
        String description = request.getParameter("description");
        String name = request.getParameter("name");
        boolean isPublic = request.getParameter("isPublic").equals("true");
        boolean writtenExists = request.getParameter("writtenExists").equals("true");
        boolean handsOnExists = request.getParameter("handsOnExists").equals("true");

        if(name.isEmpty()) {
            writer.write("{\"error\":\"Competition name is empty.\"}");
            return false;
        } else if (description.length() > 32000) {
            writer.write("{\"error\":\"Description cannot exceed 32000 characters.\"}");
            return false;
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
                /* if(difference <= 0) {   // The given datetime is in the past
                    writer.write("{\"error\":\"Written cannot end in the past.\"}");
                    return false;
                } else*/ if(difference > (long)1000*60*60*24*365*10) {   // The given datetime is more than 10 years in the future
                    writer.write("{\"error\":\"Written cannot start more than 10 years in the future.\"}");
                    return false;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Written start date is formatted incorrectly.\"}");
                return false;
            }
            String[][] mcAnswers;
            try {
                mcAnswers = gson.fromJson(request.getParameter("mcAnswers"), String[][].class);
                if(mcAnswers.length <= 0) {
                    writer.write("{\"error\":\"Written Test is empty.\"}");
                    return false;
                } else if(mcAnswers.length > 240) {
                    writer.write("{\"error\":\"Written Test cannot have more than 240 problems.\"}");
                    return false;
                }

                for(String[] problem:mcAnswers) {
                    if(problem[0].isEmpty()) {
                        writer.write("{\"error\":\"Written answer cannot be empty.\"}");
                        return false;
                    } else if(!problem[1].equals("0") && !problem[1].equals("1")) {
                        System.out.println("Issue with problems");
                        writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                        return false;
                    } else if(problem[0].length() > 40) {
                        writer.write("{\"error\":\"Written answer cannot be longer than 40 characters.\"}");
                        return false;
                    }
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Written Answers is formatted incorrectly.\"}");
                return false;
            }

            short mcCorrectPoints;
            try {
                mcCorrectPoints = Short.parseShort(request.getParameter("mcCorrectPoints"));
                if(mcCorrectPoints > 1000) {
                    writer.write("{\"error\":\"Written Test Correct Points cannot be greater than 1000.\"}");
                    return false;
                } else if(mcCorrectPoints < 0) {
                    writer.write("{\"error\":\"Written Test Correct Points cannot be less than 0.\"}");
                    return false;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Points Per Correct for the Written Test.\"}");
                return false;
            }

            short mcIncorrectPoints;
            try {
                mcIncorrectPoints = Short.parseShort(request.getParameter("mcIncorrectPoints"));
                if(mcIncorrectPoints > 0) {
                    writer.write("{\"error\":\"Written Test Incorrect Points cannot be greater than 0.\"}");
                    return false;
                } else if(mcIncorrectPoints < -1000) {
                    writer.write("{\"error\":\"Written Test Incorrect Points cannot be less than -1000.\"}");
                    return false;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Points Per Incorrect for the Written Test.\"}");
                return false;
            }
            long mcTime;
            try {
                mcTime = Long.parseLong(request.getParameter("mcTime"))*1000*60;
                if(mcTime <= 0) {
                    writer.write("{\"error\":\"Written Test Length must be greater than zero.\"}");
                    return false;
                } else if(mcTime > 1000*60*60*24*7) {
                    writer.write("{\"error\":\"Written Test cannot last longer than 7 days.\"}");
                    return false;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Length for the Written Test.\"}");
                return false;
            }
            mcTest = new MCTest(true, mcOpensString, mcAnswers, mcCorrectPoints,
                    mcIncorrectPoints,request.getParameter("mcInstructions"),
                    request.getParameter("mcTestLink"),
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
                /*if(difference <= 0) {   // The given datetime is in the past
                    writer.write("{\"error\":\"Hands-On cannot start in the past.\"}");
                    return false;
                } else */if(difference > (long)1000*60*60*24*365*10) {   // The given datetime is more than 10 years in the future
                    writer.write("{\"error\":\"Hands-On cannot start more than 10 years in the future.\"}");
                    return false;
                }
            } catch(Exception e) {
                e.printStackTrace();
                writer.write("{\"error\":\"Hands-On start date is formatted incorrectly.\"}");
                return false;
            }

            FRQProblem[] frqProblemMap;
            frqProblemMap = FRQProblem.fromJsonArray(request.getParameter("frqProblemMap"));

            if(frqProblemMap.length <= 0) {
                writer.write("{\"error\":\"Hands-On Test is empty.\"}");
                return false;
            } else if(frqProblemMap.length > 50) {
                writer.write("{\"error\":\"Hands-On Test cannot have more than 50 problems.\"}");
                return false;
            }

            Set<String> duplicateChecker = new HashSet<>();
            for(FRQProblem prob: frqProblemMap) {
                String s = prob.name;
                if(s.length() > 20) {
                    writer.write("{\"error\":\"Hands-On problem names cannot be longer than 20 characters.\"}");
                    return false;
                } else if(duplicateChecker.contains(s)) {
                    writer.write("{\"error\":\"Duplicate Hands-On problem name '"+s+"'.\"}");
                    return false;
                } else if(s.isEmpty()) {
                    writer.write("{\"error\":\"Hands-On problem name is empty.\"}");
                    return false;
                }
                duplicateChecker.add(s);
            }

            short frqMaxPoints;
            try {
                frqMaxPoints = Short.parseShort(request.getParameter("frqMaxPoints"));
                if(frqMaxPoints > 1000) {
                    writer.write("{\"error\":\"Hands-On Test Max Points cannot be greater than 1000.\"}");
                    return false;
                } else if(frqMaxPoints < -1000) {
                    writer.write("{\"error\":\"Hands-On Test Max Points cannot be less than -1000.\"}");
                    return false;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Max Points for the Hands-On Test.\"}");
                return false;
            }

            short frqIncorrectPenalty;
            try {
                frqIncorrectPenalty = Short.parseShort(request.getParameter("frqIncorrectPenalty"));
                if(frqIncorrectPenalty > 1000) {
                    writer.write("{\"error\":\"Hands-On Test Incorrect Points cannot be greater than 1000.\"}");
                    return false;
                } else if(frqIncorrectPenalty < -1000) {
                    writer.write("{\"error\":\"Hands-On Test Incorrect Points cannot be less than -1000.\"}");
                    return false;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Incorrect Penalty for the Hands-On Test.\"}");
                return false;
            }

            long frqTime;
            try {
                frqTime = Long.parseLong(request.getParameter("frqTime"))*1000*60;
                if(frqTime <= 0) {
                    writer.write("{\"error\":\"Hands-On Test Length must be greater than zero.\"}");
                    return false;
                } else if(frqTime > 1000*60*60*24*7) {
                    writer.write("{\"error\":\"Hands-On Test cannot last longer than 7 days.\"}");
                    return false;
                }
            } catch(Exception e) {
                writer.write("{\"error\":\"Specify Length for the Hands-On Test.\"}");
                return false;
            }

            frqTest = new FRQTest(true, frqOpensString, frqMaxPoints, frqIncorrectPenalty, frqProblemMap,
                    request.getParameter("frqStudentPacket"), request.getParameter("frqJudgePacket"), frqTime);
        }

        if(!mcTest.exists && !frqTest.exists) {
            writer.write("{\"error\":\"This competition has no tests.\"}");
            return false;
        }

        Competition competition = null;
        //boolean retCid = false; // If we should return the cid since we are creating the competition
        System.out.println("cidS="+cidS);
        if(cidS == null || cidS.isEmpty() || !u.cids.contains(cid = Short.parseShort(cidS))) {
            // We are creating a competition and returning the cid
            try {
                competition = Competition.createCompetition((Teacher)u, true, isPublic,
                        name, description, mcTest, frqTest);
                // retCid = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {    // We are modifying an existing competition
            competition = UIL.getCompetition(cid);
            if(competition == null) {
                System.out.println("Competition not found");
                writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                return false;
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
                return false;
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

        /*ArrayList<UILSocket> sockets = UILSocket.classes.get(u.uid);
        for(UILSocket socket: sockets) {
            if(socket.user.uid != u.uid) {
                JsonArray array = new JsonArray();
                array.add("addCompetition");
                array.add("")
                array.add(competition.template.getMiniHTML(socket.user));
                socket.send("[\"addCompetition\",\""+uid+"\",\""+submission.scoringReport[0]+"\"]");
            }
        }*/

        //if(!retCid) return true;
        //else {
        writer.write("{\"success\":\"Competition published.\",\"cid\":\""+competition.template.cid+"\"}");
        //    return false;
        //}
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = UserMap.getUserByRequest(request);
        if(user==null || user.token == null){
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        if(!initialized) {
            try {
                initialize();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
                    right += "<p class='emptyWarning'>There are no public competitions.</p>";
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
        boolean viewingSpecificCompetition = false; // If we are viewing a specific competition
        Competition competition = null;
        if(cidS!=null && !cidS.isEmpty()) {
            try {
                competition = getPublishedCompetition(Short.parseShort(cidS));
                if(competition != null) viewingSpecificCompetition = true;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(!viewingSpecificCompetition) {    // In this case we are showing all of the available competitions
            String action = request.getParameter("action");
            User u = UserMap.getUserByRequest(request);
            if(u==null || u.token == null){
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            if(!initialized) {
                try {
                    initialize();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            PrintWriter writer = response.getWriter();
            if(action.equals("saveCompetition")) {
                cidS = request.getParameter("op_cid");
                short cid;
                try {
                    cid = Short.parseShort(cidS);
                    competition = UIL.getCompetition(cid);

                    Teacher teacher = (Teacher) u;
                    if (competition != null && competition.teacher.uid == teacher.uid) {
                        savePublished(request, writer, (Teacher)u);
                    }
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
                        writer.write("{\"error\":\"Written start date is formatted incorrectly.\"}");
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
                            request.getParameter("mcAnswersLink"),
                            mcTime);
                }

                if(!handsOnExists) {   // No FRQ Test
                    frqTest = new FRQTest();
                } else {
                    String frqOpensString;
                    frqOpensString = request.getParameter("frqOpens");

                    FRQProblem[] frqProblemMap;
                    frqProblemMap = FRQProblem.fromJsonArray(request.getParameter("frqProblemMap"));


                    short frqMaxPoints;
                    frqMaxPoints = Short.parseShort(request.getParameter("frqMaxPoints"));

                    short frqIncorrectPenalty;
                    frqIncorrectPenalty = Short.parseShort(request.getParameter("frqIncorrectPenalty"));

                    long frqTime;
                    frqTime = Long.parseLong(request.getParameter("frqTime"))*1000*60;

                    frqTest = new FRQTest(false, frqOpensString, frqMaxPoints, frqIncorrectPenalty, frqProblemMap,
                            request.getParameter("frqStudentPacket"), request.getParameter("frqJudgePacket"), frqTime);
                }
                competition = null;
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
                savePublished(request, writer, (Teacher)u);
            } else if(action.equals("unPublishCompetition")) {
                short cid = Short.parseShort(request.getParameter("op_cid"));
                competition = UIL.getCompetition(cid);

                Teacher teacher = (Teacher)u;
                if(competition != null && competition.teacher.uid == teacher.uid) {
                    try {
                        competition.unPublish();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                writer.write("{\"success\":\"Competition unpublished.\"}");
            } else if(action.equals("deleteCompetition") && u.teacher) {
                short deleteCid = Short.parseShort(request.getParameter("op_cid"));
                competition = UIL.getCompetition(deleteCid);

                Teacher teacher = (Teacher)u;
                System.out.println("Pre-delete, cid="+deleteCid+", competition!=null="+(competition!=null));
                if(competition != null && competition.teacher.uid == teacher.uid) {
                    UIL.deleteCompetition(competition);
                    teacher.cids.remove(teacher.cids.indexOf(competition.template.cid));
                    teacher.updateUser(false);
                }
            }
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