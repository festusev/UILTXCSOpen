package Outlet.uil;
import Outlet.Conn;
import Outlet.Teacher;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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

    private static HashMap<Short, Competition> competitions;    // Maps a competition's cid to its competition object
    private static boolean initialized = false;
    private static void initialize() throws SQLException {
        competitions = new HashMap<>();
        Connection conn = Conn.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM competitions");
        ResultSet rs = stmt.executeQuery();

        while(rs.next()) {
            Competition comp = new Competition((Teacher) Conn.getUserByUID(rs.getShort("uid")),rs.getShort("cid"),
                    rs.getBoolean("isPublic"),rs.getString("name"),rs.getString("whatItIs"),
                    rs.getString("rules"),rs.getString("practice"),gson.fromJson(rs.getString("mcKey"),String[].class),gson.fromJson(rs.getString("mcProblemMap"),short[].class),
                    rs.getShort("mcCorrectPoints"),rs.getShort("mcIncorrectPoints"),rs.getString("mcInstructions"),
                    rs.getString("mcTestLink"),rs.getString("mcAnswers"),rs.getString("mcOpens"),rs.getLong("mcTime"),rs.getShort("frqMaxPoints"),
                    rs.getShort("frqIncorrectPenalty"), gson.fromJson(rs.getString("frqProblemMap"),String[].class),rs.getString("frqStudentPack"),rs.getString("frqJudgePacket"),
                    rs.getString("frqOpens"),rs.getLong("frqTime"),gson.fromJson(rs.getString("datMap"),String[].class));
            competitions.put(comp.template.cid, comp);
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
        return competitions.get(cid);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cidS = request.getParameter("cid");
        if(cidS == null || cidS.isEmpty() || competitions.containsKey(Short.parseShort(cidS))) {    // In this case we are showing all of the available competitions
            Conn.setHTMLHeaders(response);
            PrintWriter writer = response.getWriter();
            writer.write("");
        } else {    // Render a specific competition. Users will be able to switch which competition they are viewing by clicking on the competition's name
            Competition competition = competitions.get(Short.parseShort(cidS));
            competition.doGet(request, response);
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cidS = request.getParameter("cid");
        if(cidS == null || cidS.isEmpty() || competitions.containsKey(Short.parseShort(cidS))) {    // In this case we are showing all of the available competitions
            return;
        } else {
            Competition competition = competitions.get(Short.parseShort(cidS));
            competition.doPost(request, response);
        }
    }
}