package Outlet.uil;

import Outlet.*;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/***
 * Manages teacher-created competitions, has post and get methods. Manages interfacing and uses a Template to render.
 */
public class Competition {
    public Template template;
    private static Gson gson = new Gson();

    public MCTest mcTest;
    public FRQTest frqTest;

    public Teacher teacher;
    public boolean isPublic;

    public HashMap<Short, UILEntry> entries;    // Maps a teams tid to its UILEntry

    /***
     * Used if there is an frq and no mc.
     * @param teacher
     * @param cid
     * @param isPublic
     * @param name
     * @param whatItIs
     * @param rules
     * @param practice
     * @param frqMaxPoints
     * @param frqIncorrectPenalty
     * @param frqProblemMap
     * @param frqStudentPack
     * @param frqJudgePacket
     * @param frqOpens
     * @param frqTime
     * @param datMap
     */
    public Competition(Teacher teacher, short cid, boolean isPublic, String name, String whatItIs,String rules, String practice, short frqMaxPoints, short frqIncorrectPenalty,
                       String[] frqProblemMap, String frqStudentPack, String frqJudgePacket,
                       String frqOpens, long frqTime, String[] datMap) {
        MCTest mc = new MCTest();
        FRQTest frq = new FRQTest(frqOpens, cid+"_"+teacher.uid, cid+"_"+teacher.uid, (short)frqProblemMap.length, frqMaxPoints, frqIncorrectPenalty, frqProblemMap, "Hands-On Programming", (frqTime/(1000*60)) + " minutes", frqStudentPack, frqJudgePacket, frqTime, datMap);

        this.teacher = teacher;
        this.isPublic = isPublic;
        template = new Template(name, whatItIs, rules, practice, mc, frq, frq.opens, Countdown.add(frq.opens, frqTime, ""), cid, this);
    }

    /***
     * Used if there is no frq.
     * @param teacher
     * @param cid
     * @param isPublic
     * @param name
     * @param whatItIs
     * @param rules
     * @param practice
     * @param mcKey
     * @param mcProblemMap
     * @param mcCorrectPoints
     * @param mcIncorrectPoints
     * @param mcInstructions
     * @param mcTestLink
     * @param mcAnswers
     * @param mcOpens
     * @param mcTime
     */
    public Competition(Teacher teacher, short cid, boolean isPublic, String name, String whatItIs,String rules, String practice, String[] mcKey, short[] mcProblemMap, short mcCorrectPoints, short mcIncorrectPoints,
                       String mcInstructions, String mcTestLink, String mcAnswers, String mcOpens, long mcTime) {
        MCTest mc = new MCTest(mcOpens, mcKey, mcProblemMap, mcProblemMap.length, mcCorrectPoints, mcIncorrectPoints, 0, "Written Test", (mcTime/(1000*60)) + " minutes", mcInstructions, mcTestLink, mcAnswers, mcTime);
        FRQTest frq = new FRQTest();

        this.teacher = teacher;
        this.isPublic = isPublic;
        template = new Template(name, whatItIs, rules, practice, mc, frq, mc.opens, Countdown.add(mc.opens, mcTime, ""), cid, this);
    }

    /***
     * Used if there is both an frq and a mc
     * @param teacher
     * @param cid
     * @param isPublic
     * @param name
     * @param whatItIs
     * @param rules
     * @param practice
     * @param mcKey
     * @param mcProblemMap
     * @param mcCorrectPoints
     * @param mcIncorrectPoints
     * @param mcInstructions
     * @param mcTestLink
     * @param mcAnswers
     * @param mcOpens
     * @param mcTime
     * @param frqMaxPoints
     * @param frqIncorrectPenalty
     * @param frqProblemMap
     * @param frqStudentPack
     * @param frqJudgePacket
     * @param frqOpens
     * @param frqTime
     * @param datMap
     */
    public Competition(Teacher teacher, short cid, boolean isPublic, String name, String whatItIs,String rules, String practice, String[] mcKey, short[] mcProblemMap, short mcCorrectPoints, short mcIncorrectPoints,
                       String mcInstructions, String mcTestLink, String mcAnswers, String mcOpens, long mcTime, short frqMaxPoints, short frqIncorrectPenalty,
                       String[] frqProblemMap, String frqStudentPack, String frqJudgePacket,
                       String frqOpens, long frqTime, String[] datMap) {
        MCTest mc = new MCTest(mcOpens, mcKey, mcProblemMap, mcProblemMap.length, mcCorrectPoints, mcIncorrectPoints, 0, "Written Test", (mcTime/(1000*60)) + " minutes", mcInstructions, mcTestLink, mcAnswers, mcTime);
        FRQTest frq = new FRQTest(frqOpens, cid+"_"+teacher.uid, cid+"_"+teacher.uid, (short)frqProblemMap.length, frqMaxPoints, frqIncorrectPenalty, frqProblemMap, "Hands-On Programming", (frqTime/(1000*60)) + " minutes", frqStudentPack, frqJudgePacket, frqTime, datMap);

        Countdown opens = Countdown.getEarliest(mc.opens, frq.opens);   // When the competition will read as open and closed
        Countdown ends = Countdown.getLatest(mc.opens, frq.opens);
        if(ends == frq.opens) ends = Countdown.add(ends, frqTime, "");
        else ends = Countdown.add(ends, mcTime, "");

        this.teacher = teacher;
        this.isPublic = isPublic;
        template = new Template(name, whatItIs, rules, practice, mc, frq, opens, ends, cid, this);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        template.render(request,response);
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User userBefore = Conn.getUser(request);
        if(userBefore == null || userBefore.token == null || !Conn.isLoggedIn(userBefore.token)){ // They are not logged in, return nothing
            return;
        }

        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();


        if (action.equals("updatePage")) {  // Reloads the columns portion of the page
            writer.write("{\"updatedHTML\":\""+template.getColumnsHTML(userBefore, template.getCompeteStatus(userBefore))+"\"}");
            return;
        }
        if(userBefore.teacher) {    // They are a teacher, so don't let them do anything
            return;
        }

        Student user = (Student) userBefore;
        if(user.cids.containsKey(template.cid)) { // If their team is already signed up for this competition
            if(template.getStatus() == 1) {
                UILEntry temp = user.cids.get(template.cid);
                if(action.equals("grabFRQProblems")) {
                    writer.write("{\"frqProblemsHTML\":\""+template.getFRQProblems(temp)+"\"}");
                } else if (action.equals("beginMC")) { // Even if they have already begun, will send them the MC test
                    if (!temp.finishedMC(user.uid)) {
                        writer.write("{\"status\":\"success\",\"mcHTML\":\""+template.getRunningMC(temp.beginMC(user.uid))+"\"}");
                        temp.update();
                        return;
                    }
                } else if (action.equals("submitMC")) {
                    String[] answers = gson.fromJson(request.getParameter("answers"), String[].class);
                    writer.write("{\"mcHTML\":\"" + template.getFinishedMC(temp.scoreMC(user.uid, answers)) + "\"}");
                    temp.update();
                    template.updateScoreboard();
                    return;
                } else if (action.equals("beginFRQ")) {
                    if (!temp.finishedFRQ()) {
                        temp.beginFRQ();
                        writer.write("{\"status\":\"success\",\"frqHTML\":\""+template.getRunningFRQ(temp)+"\"}");
                    }
                } else if(action.equals("submitFRQ")){
                    Part filePart = request.getPart("textfile");
                    InputStream fileContent = filePart.getInputStream();

                    byte[] bytes = new byte[fileContent.available()];
                    fileContent.read(bytes);

                    short probNum = Short.parseShort(request.getParameter("probNum"));

                    if(temp.frqResponses[probNum-1] > 0){
                        writer.write("{\"status\":\"error\",\"error\":\"You've already gotten this problem.\"}");
                        return;
                    }

                    int status = template.frqTest.score(probNum, bytes, filePart.getSubmittedFileName(), user.uid, temp.tid);
                    temp.addFRQRun(status, probNum);
                    switch(status){
                        case 0:
                            writer.write("{\"status\":\"success\",\"scored\":\"You gained points!\"}");
                            template.updateScoreboard();
                            return;
                        case 1:
                            writer.write("{\"status\":\"error\",\"error\":\"Compile-time error.\"}");
                            template.updateScoreboard();
                            return;
                        case 2:
                            writer.write("{\"status\":\"error\",\"error\":\"Runtime error\"}");
                            template.updateScoreboard();
                            return;
                        case 3:
                            writer.write("{\"status\":\"error\",\"error\":\"Time limit exceeded.\"}");
                            template.updateScoreboard();
                            return;
                        case 4:
                            writer.write("{\"status\":\"error\",\"error\":\"Wrong answer.\"}");
                            template.updateScoreboard();
                            return;
                        default:
                            writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                            return;
                    }
                } else if(action.equals("finishFRQ")) {
                    writer.write("{\"frqHTML\":\""+template.getFinishedFRQ(temp)+"\"}");
                    return;
                }
            } else {
                writer.write("{\"status\":\"error\",\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                return;
            }
        } else if(action.equals("signup")) {    // Their team is not signed up for this competition
            // TODO: Let people sign up in teams
            try {
                //user.team.comps.put(cid,template.signup());
                //user.team.updateComps();
                template.updateScoreboard();
                writer.write("{\"status\":\"success\",\"updatedHTML\":\""+template.getColumnsHTML(user, template.getCompeteStatus(user))+"\"}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                writer.write("{\"status\":\"error\",\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                return;
            }
        }
    }

    /***
     * Gets a UIL entry from the database or from the entries map if already loaded, using the specified team id.
     * @param tid
     * @return
     */
    public UILEntry getEntry(short tid) throws SQLException {
        if(this.entries.containsKey(tid)) return this.entries.get(tid);

        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `c"+template.cid+"` WHERE tid=?");
        stmt.setShort(1,tid);
        ResultSet rs = stmt.executeQuery();
        if(rs.next()) {
            UILEntry entry = new UILEntry(rs,this);
            this.entries.put(tid, entry);
            return entry;
        }
        return null;
    }

    /***
     * Loads in entirely NEW UILEntries, so even if one is already in the UILEntry hashmap it will be a different object.
     * @return
     */
    public ArrayList<UILEntry> getAllEntries() throws SQLException {
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM `c"+template.cid+"`");
        ResultSet rs = stmt.executeQuery();
        ArrayList<UILEntry> entries = new ArrayList<>();
        while(rs.next()) {
            entries.add(new UILEntry(rs, this));
        }
        return entries;
    }
}
