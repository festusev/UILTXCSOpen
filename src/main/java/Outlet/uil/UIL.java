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
    public static HashMap<Short, Competition> published;
    public static HashMap<Short, Competition> unpublished; // Unpublished competitions
    public static boolean initialized = false;

    public static final int MAX_FILE_SIZE = 1000000; // Maximum file size in bytes
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

            boolean alternateExists = rs.getBoolean("alternateExists");
            short numNonAlts = rs.getShort("numNonAlts");

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
                        FRQProblem.fromJsonArray(rs.getString("frqProblemMap")),
                        rs.getString("frqStudentPack"),rs.getString("frqJudgePacket"),
                        rs.getLong("frqTime"), rs.getBoolean("frqAutoGrade"));
            }

            Competition comp = new Competition(cid, published,
                    rs.getBoolean("isPublic"),rs.getString("name"),rs.getString("description"),
                    alternateExists, numNonAlts,mcTest, frqTest,
                    Clarification.fromJsonToArray(rs.getString("clarifications")),gson.fromJson(rs.getString("judges"),short[].class),
                    rs.getBoolean("showScoreboard"));

            if(!comp.published) {
                unpublished.put(comp.template.cid, comp);
            } else {
                UIL.published.put(comp.template.cid, comp);
                comp.loadAllEntries();
            }
            try {
                Teacher teacher = TeacherMap.getByUID(uid);
                teacher.competitions.add(comp);
                comp.setTeacher(teacher);
            } catch(Exception e) {
                e.printStackTrace();
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

    // Adds the competition objects to the teachers who are judging them.
    // Must be called after all of the teachers and competitions are loaded.
    public static void initializeJudges() {
        Collection<Competition> publishedValues = published.values();
        for(Competition competition: publishedValues) {
            short[] judges = competition.getJudges();
            for(short uid: judges) {
                Teacher teacher = TeacherMap.getByUID(uid);
                teacher.judging.add(competition);
            }
        }

        Collection<Competition> unpublishedValues = unpublished.values();
        for(Competition competition: unpublishedValues) {
            short[] judges = competition.getJudges();
            for(short uid: judges) {
                Teacher teacher = TeacherMap.getByUID(uid);
                teacher.judging.add(competition);
            }
        }
    }

    public static void sortFRQResponses() {
        try {
            initialize();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Collection<Competition> list = published.values();
        for(Competition comp: list) {
            Collections.sort(comp.frqSubmissions, new FRQSubmissionComparator());
        }
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

    private void writeFilesToDisk(HttpServletRequest request, FRQTest frqTest) throws IOException, ServletException {
        /* Now, write all of the files they updated to the disk */
        System.out.println("Writing files to disk");

        Collection<Part> parts = request.getParts();
        int numFiles = 0;   // We can only have a certain number of handsOn problems
        for (Part part : parts) {
            if(numFiles > 48) { // They can only have at most 24 hands-on problems, so they can upload at most 48
                break;
            }

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
                numFiles++;
            } else if (prefix.equals("fo:")) {   // File out
                int probNum = Integer.parseInt(partName.substring(3));

                InputStream fileContent = part.getInputStream();
                byte[] bytes = new byte[fileContent.available()];
                fileContent.read(bytes);

                System.out.println("probNum=" + probNum);
                frqTest.setTestcaseFile(probNum, bytes, false);
                numFiles++;
            }
        }
        frqTest.initializeFiles();
    }

    /*
    Check that all of the files sent are of the right size.
     */
    private String checkFileSize(HttpServletRequest request) throws IOException, ServletException {
        /* Now, write all of the files they updated to the disk */
        System.out.println("Checking file size");

        Collection<Part> parts = request.getParts();
        int numFiles = 0;   // We can only have a certain number of handsOn problems
        for (Part part : parts) {
            if(numFiles > 48) { // They can only have at most 24 hands-on problems, so they can upload at most 48
                break;
            }

            String partName = part.getName();
            System.out.println("Looping, partName=" + partName);
            String prefix = partName.substring(0, 3);  // Either 'fi:' or 'fo:'
            if (prefix.equals("fi:")) {  // File in
                int probNum = Integer.parseInt(partName.substring(3));

                InputStream fileContent = part.getInputStream();
                if(fileContent.available() > MAX_FILE_SIZE) return "Problem " + probNum + "'s input file is over 1 MB.";
                numFiles++;
            } else if (prefix.equals("fo:")) {   // File out
                int probNum = Integer.parseInt(partName.substring(3));

                InputStream fileContent = part.getInputStream();

                if(fileContent.available() > MAX_FILE_SIZE) return "Problem " + probNum + "'s output file is over 1 MB.";
                numFiles++;
            }
        }

        return null;
    }

    private boolean savePublished(HttpServletRequest request, PrintWriter writer, Teacher u) throws IOException, ServletException {
        System.out.println("Saving published");
        String cidS = request.getParameter("op_cid");
        String description = request.getParameter("description");
        String name = request.getParameter("name");
        boolean isPublic = request.getParameter("isPublic").equals("true");
        short numNonAlts = Short.parseShort(request.getParameter("numNonAlts"));
        boolean writtenExists = request.getParameter("writtenExists").equals("true");
        boolean handsOnExists = request.getParameter("handsOnExists").equals("true");
        boolean showScoreboard = request.getParameter("showScoreboard").equals("true");
        short[] judges = gson.fromJson(request.getParameter("judges"), short[].class);

        if(name.isEmpty()) {
            writer.write("{\"error\":\"Competition name is empty.\"}");
            return false;
        } else if (description.length() > 65535) {
            writer.write("{\"error\":\"Description cannot exceed 65535 characters.\"}");
            return false;
        } else if(numNonAlts < 1 || numNonAlts > 127) {
            writer.write("{\"error\":\"Number of non-alternates must be between 1 and 127.\"}");
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
                } else*/
                if(difference > (long)1000*60*60*24*365*10) {   // The given datetime is more than 10 years in the future
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
                mcTime = Long.parseLong(request.getParameter("mcTime"))*60*1000;
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

        boolean alternateExists = false;
        if(!handsOnExists) {   // No FRQ Test
            frqTest = new FRQTest();
        } else {
             alternateExists = request.getParameter("alternateExists").equals("true");

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
                } else */
                if(difference > (long)1000*60*60*24*365*10) {   // The given datetime is more than 10 years in the future
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
            } else if(frqProblemMap.length > 24) {
                writer.write("{\"error\":\"Hands-On Test cannot have more than 24 problems.\"}");
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
                    request.getParameter("frqStudentPacket"), request.getParameter("frqJudgePacket"), frqTime,
                    request.getParameter("frqAutoGrade").equals("true"));
        }

        if(!mcTest.exists && !frqTest.exists) {
            writer.write("{\"error\":\"This competition has no tests.\"}");
            return false;
        }

        if(frqTest.exists) {
            String error = checkFileSize(request);
            if(error != null) { // An error occurred
                writer.write("{\"error\":\""+error+"\"}");
                return false;
            }
        }

        Competition competition = null;
        //boolean retCid = false; // If we should return the cid since we are creating the competition
        System.out.println("cidS="+cidS);
        boolean creatingComp = cidS==null || cidS.isEmpty();
        if(creatingComp) {
            // We are creating a competition and returning the cid
            try {
                competition = Competition.createCompetition(u, true, isPublic,
                        name, description, alternateExists,numNonAlts, mcTest, frqTest, judges, showScoreboard);
                // retCid = true;
            } catch (SQLException e) {
                System.out.println("Error creating the competition");
                e.printStackTrace();
                writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                return false;
            }
        } else {    // We are modifying an existing competition
            cid = Short.parseShort(cidS);
            competition = UIL.getCompetition(cid);
            if(competition == null) {
                System.out.println("Competition not found");
                writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                return false;
            } else if (u.uid != competition.teacher.uid) {
                writer.write("{\"error\":\"You cannot edit this competition.\"}");
                return false;
            } else if(competition.alternateExists && !alternateExists && competition.entries.allEntries.size() > 0) {
                writer.write("{\"error\":\"You can't delete alternates while teams are signed up.\"}");
                return false;
            } else if(competition.numNonAlts > numNonAlts && competition.entries.allEntries.size() > 0) {
                writer.write("{\"error\":\"You can't reduce the team size while teams are signed up.\"}");
                return false;
            }

            competition.published = true;
            competition.setJudges(judges);
            frqTest.setDirectories(cid, u.uid);

            if(frqTest.exists) {
                frqTest.updateProblemDirectories(gson.fromJson(request.getParameter("frqIndices"), short[].class),
                        competition.template.frqTest.PROBLEM_MAP.length, competition);
            } else if(competition.template.frqTest.exists) {    // They have deleted the frq test, so remove the directory
                frqTest.delete(competition);
            }

            if(mcTest.exists) {
                mcTest.updateSubmissions(gson.fromJson(request.getParameter("mcIndices"),short[].class),
                        competition.template.mcTest.NUM_PROBLEMS,competition);
            }
            /*else if(competition.template.mcTest.exists) {    // They have deleted the mc test, so remove all mc submissions

            }*/

            try {
                competition.update(u, true, isPublic, alternateExists, numNonAlts, name, description, mcTest, frqTest,judges,showScoreboard);
                competition.template.updateScoreboard();
            } catch (SQLException e) {
                e.printStackTrace();
                writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                return false;
            }
            UIL.publish(competition);
        }

        if(handsOnExists) writeFilesToDisk(request, frqTest);

        writer.write("{\"success\":\"Competition published.\",\"cid\":\""+competition.template.cid+"\"}");

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
            if(user.temp) { // They are a temporary user, so redirect them to their specific competition
                Student student = (Student) user;
                UILEntry entry = (UILEntry) student.cids.values().toArray()[0];
                response.sendRedirect(request.getContextPath() + "/console/competitions?cid="+entry.competition.template.cid);
                return;
            }
            Conn.setHTMLHeaders(response);
            PrintWriter writer = response.getWriter();
            StringBuilder right = new StringBuilder("<div id='competitions'><div id='nav'><p onclick='showPublic(this)' class='selected'>Public</p>");

            boolean showClassHTML = user.teacher || TeacherMap.getByUID(((Student)user).teacherId) != null;
            if(user.teacher) right.append("<p onclick='showClassComps(this)' id='showClassComps'>My Competitions</p>" +
                    "<p onclick='showUpcomingComps(this)' id='showUpcomingComps'>Judging</p>" +
                    "<p id='createNewCompetition' onclick='createNewCompetition()'>New</p>");
            else {
                if(TeacherMap.getByUID(((Student)user).teacherId) != null) right.append("<p onclick='showClassComps(this)' id='showClassComps'>Class</p>");

                right.append("<p id='showUpcomingComps' onclick='showUpcomingComps(this)'>Upcoming</p>");
            }

            right.append("</div><div id='comp-list'><h1 id='title'>Competitions</h1><div id='public_competitions' class='column'>");

            if(published.size() <=0) {  // There are no published competitions
                right.append("<p class='emptyWarning'>There are no public competitions.</p>");
            } else {    // There are published competitions
                ArrayList<Competition> ordered = new ArrayList<>(published.values());  // Sort them by date
                ordered.sort(new SortCompByDate());

                boolean foundPublic = false;
                StringBuilder upcoming = new StringBuilder();
                StringBuilder running = new StringBuilder();
                StringBuilder archived = new StringBuilder();
                for(Competition comp: ordered) {
                    if(comp.isPublic) {
                        String html = comp.template.getMiniHTML(user);
                        if(!comp.template.opens.done()) upcoming.append(html);
                        else if(!comp.template.closes.done()) running.append(html);
                        else archived.append(html);
                        foundPublic = true;
                    }
                }
                if(!foundPublic) {
                    right.append("<p class='emptyWarning'>There are no public competitions.</p>");
                } else {
                    right.append("<h3>Upcoming</h3>");
                    if(upcoming.length() == 0) {
                        right.append("<p class='emptyWarning'>There are no upcoming public competitions</p>");
                    } else {
                        right.append(upcoming);
                    }
                    right.append("<h3>Running</h3>");
                    if(running.length() == 0) {
                        right.append("<p class='emptyWarning'>There are no running competitions.</p>");
                    } else {
                        right.append(running);
                    }
                    right.append("<h3>Archived</h3>");
                    if(archived.length() == 0) {
                        right.append("<p class='emptyWarning'>There are no archived competitions.</p>");
                    } else {
                        right.append(archived);
                    }
                }
            }
            right.append("</div>");

            if(showClassHTML) {
                right.append("<div id='class_competitions' style='display:none' class='column'>");
                ArrayList<Competition> ordered;
                if(user.teacher) {
                    ordered = ((Teacher) user).getCompetitions();
                } else {
                    Teacher teacher = TeacherMap.getByUID(((Student)user).teacherId);
                    ordered = teacher.getCompetitions();
                }

                if (ordered.size() <= 0) {   // There are no class competitions
                    if (user.teacher) {
                        right.append("You have not created any competitions.");
                    } else {
                        right.append("Your teacher has not created any competitions.");
                    }
                } else {
                    if (user.teacher) {
                        right.append("<script>loadCompetitions()</script>");
                    } else {
                        for (Competition comp : ordered) {
                            if (comp != null && comp.published) right.append(comp.template.getMiniHTML(user));
                        }
                    }
                }
                right.append("</div>");
            }

            if(user.teacher) {  // They are a teacher, so add in the template for the list of other teachers
                right.append("<script>requestLoadJudges()</script><div id='selectJudgeCnt'><div class='center'><h1>Select Judge</h1>" +
                             "<img src='/res/close.svg' class='close' onclick='closeSelectJudge()'/>" +
                             "<ul id='selectJudgeList'></ul></div></div>" +
                             "<div id='upcoming_competitions' style='display:none' class='column'>");
                Teacher teacher = (Teacher) user;
                boolean empty = true;
                for(Competition comp: teacher.judging) {
                    if(comp.published) {
                        right.append(comp.template.getMiniHTML(teacher));
                        empty = false;
                    }
                }
                if(empty) right.append("<p class='emptyWarning'>No teachers have added you as a judge to their competition.</p>");
                right.append("</div>");
            } else {
                Collection<UILEntry> myCompetitions = ((Student) user).cids.values();
                right.append("<div id='upcoming_competitions' style='display:none' class='column'>");
                if(myCompetitions.size() <= 0) right.append("<p class='emptyWarning'>You haven't signed up for any competitions.</p>");
                for(UILEntry comp: myCompetitions) {
                    if(comp.competition.published) right.append(comp.competition.template.getMiniHTML(user));
                }
                right.append("</div>");
            }
            right.append("</div></div>");

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
                    Dynamic.get_consoleHTML(1, right.toString(), user) +
//                    "<div id='content'>" + left + "</div></div>"+right+"</div></div>" +
                    "</div></body></html>");
        } else {    // Render a specific competition. Users will be able to switch which competition they are viewing by clicking on the competition's name
            System.out.println("CID="+cidS);
            Competition competition = getPublishedCompetition(Short.parseShort(cidS));
            if(competition != null) {
                if(user.temp) { // They are a temporary user, so check that this competition is the one they are signed up for
                    Student student = (Student) user;
                    UILEntry entry = (UILEntry) student.cids.values().toArray()[0];
                    if(entry.competition.template.cid != competition.template.cid) {
                        response.sendRedirect(request.getContextPath() + "/console/competitions?cid="+entry.competition.template.cid);
                    }
                }
                competition.doGet(request, response);
            } else System.out.println("Competition hasn't been published");
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
            if(action.equals("saveCompetition") && u.teacher) {
                cidS = request.getParameter("op_cid");
                Teacher teacher = (Teacher) u;

                short cid;
                try {
                    cid = Short.parseShort(cidS);   // If this is not a saved competition, cidS will be empty so this will error
                    competition = UIL.getCompetition(cid);

                    if (competition != null && competition.teacher.uid == teacher.uid && competition.published) {
                        savePublished(request, writer, teacher);
                        return;
                    }
                } catch (Exception e) {}

                // In this case, we are not saving a published competition, so many things will be truncated
                String description = request.getParameter("description");
                String name = request.getParameter("name");
                boolean isPublic = request.getParameter("isPublic").equals("true");
                boolean writtenExists = request.getParameter("writtenExists").equals("true");
                boolean handsOnExists = request.getParameter("handsOnExists").equals("true");
                short[] judges = gson.fromJson(request.getParameter("judges"), short[].class);
                boolean showScoreboard = request.getParameter("showScoreboard").equals("true");
                boolean alternateExists = false;
                short numNonAlts = 1;

                if(handsOnExists) {
                    alternateExists = request.getParameter("alternateExists").equals("true");
                    numNonAlts = Short.parseShort(request.getParameter("numNonAlts"));
                }
                if(name.isEmpty()) {
                    writer.write("{\"error\":\"Competition name is empty.\"}");
                    return;
                } else if (description.length() > 65535) {
                    writer.write("{\"error\":\"Description cannot exceed 65535 characters.\"}");
                    return;
                } else if(handsOnExists && (numNonAlts < 1 || numNonAlts > 127)) {
                    writer.write("{\"error\":\"Number of non-alternates must be between 1 and 127.\"}");
                    return;
                }

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
                    if(frqProblemMap.length > 24) {
                        writer.write("{\"error\":\"Hands-On Test cannot have more than 24 problems.\"}");
                        return;
                    }

                    short frqMaxPoints;
                    frqMaxPoints = Short.parseShort(request.getParameter("frqMaxPoints"));

                    short frqIncorrectPenalty;
                    frqIncorrectPenalty = Short.parseShort(request.getParameter("frqIncorrectPenalty"));

                    long frqTime;
                    frqTime = Long.parseLong(request.getParameter("frqTime"))*1000*60;

                    frqTest = new FRQTest(false, frqOpensString, frqMaxPoints, frqIncorrectPenalty, frqProblemMap,
                            request.getParameter("frqStudentPacket"), request.getParameter("frqJudgePacket"), frqTime,
                            request.getParameter("frqAutoGrade").equals("true"));
                }

                boolean creatingComp = cidS==null || cidS.isEmpty();
                cid = 0;
                if(!creatingComp) { // Check if this competition is one of this teacher's competitions
                    cid = Short.parseShort(cidS);
                    boolean temp = false;
                    for(Competition teacherComp: ((Teacher)u).competitions) {
                        if(teacherComp.template.cid == cid) {
                            temp = true;
                            break;
                        }
                    }
                    if(!temp) creatingComp = true;
                }

                if(frqTest.exists) {
                    String error = checkFileSize(request);
                    if(error != null) { // An error occurred
                        writer.write("{\"error\":\""+error+"\"}");
                        return;
                    }
                }

                if(creatingComp) {
                    // We are creating a competition and returning the cid
                    try {
                        competition = Competition.createCompetition((Teacher)u, false, isPublic,
                                name, description,alternateExists,numNonAlts, mcTest, frqTest, judges,showScoreboard);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                        return;
                    }
                } else {    // We are modifying an existing competition
                    competition = UIL.getCompetition(cid);
                    if(competition == null) {
                        writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                        return;
                    } else if(competition.teacher.uid != teacher.uid) {
                        writer.write("{\"error\":\"You cannot edit this competition.\"}");
                        return;
                    }


                    competition.published = false;
                    frqTest.setDirectories(cid, u.uid);

                    if(frqTest.exists) {
                        frqTest.updateProblemDirectories(gson.fromJson(request.getParameter("frqIndices"), short[].class),
                                competition.template.frqTest.PROBLEM_MAP.length, competition);
                    } else if(competition.template.frqTest.exists) {    // They have deleted the frq test, so remove the directory
                        frqTest.delete(competition);
                    }
                    try {
                        competition.update(teacher, competition.published, isPublic, alternateExists, numNonAlts, name,
                                description, mcTest, frqTest, judges, showScoreboard);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                        return;
                    }
                }

                /* Now, write all of the files they updated to the disk */
                if(frqTest.exists) writeFilesToDisk(request, frqTest);

                writer.write("{\"success\":\"Competition saved.\",\"cid\":\""+competition.template.cid+"\"}");
            } else if(action.equals("publishCompetition") && u.teacher) {
                savePublished(request, writer, (Teacher)u);
            } /*else if(action.equals("unPublishCompetition")) {
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
            } */else if(action.equals("deleteCompetition") && u.teacher) {
                short deleteCid = Short.parseShort(request.getParameter("op_cid"));
                competition = UIL.getCompetition(deleteCid);

                Teacher teacher = (Teacher)u;
                System.out.println("Pre-delete, cid="+deleteCid+", competition!=null="+(competition!=null));
                if(competition != null && competition.teacher.uid == teacher.uid) {
                    UIL.deleteCompetition(competition);
                    teacher.competitions.remove(competition);
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

class FRQSubmissionComparator implements Comparator<FRQSubmission> {
    @Override
    public int compare(FRQSubmission s1, FRQSubmission s2) {
        return (s1.submittedTime - s2.submittedTime) > 0?1:-1;
    }
}