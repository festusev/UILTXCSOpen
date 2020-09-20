package Outlet.uil;
import Outlet.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

import static Outlet.StringEscapeUtils.*;

/***
 * The first page the user sees when they log in. Contains user and team configurations.
 * Created by Evan Ellis.
 */
public class Upcoming extends HttpServlet{
    protected static Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = UserMap.getUserByRequest(request);
        if(user==null || user.token == null){
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        Conn.setHTMLHeaders(response);
        PrintWriter writer = response.getWriter();
        String right = "<div id='upcoming'><h1 id='title'>Upcoming</h1><ul>";
        if(UIL.getUpcoming().size() <=0) {  // There are no upcoming competitions
            right+="<p class='emptyWarning'>There are no upcoming competitions.</p>";
        } else {    // There are upcoming competitions
            ArrayList<Competition> ordered = new ArrayList(UIL.getUpcoming().values());  // Sort them by date
            Collections.sort(ordered, new SortCompByDate());

            for(Competition comp: ordered) {
                if(comp.isPublic)
                    right+="<li class='competitionCnt'>"+comp.template.getMiniHTML(user)+"</li>";
            }
        }
        right+="</ul>";

        if (!user.teacher) {
            Collection<UILEntry> myCompetitions = ((Student) user).cids.values();
            right+="<ul id='my_competitions' style='display:none' class='column'>";
            if(myCompetitions.size() <= 0) right+="<p class='emptyWarning'>You haven't signed up for any competitions.</p>";
            for(UILEntry comp: myCompetitions) {
                if(comp.competition.published) right+="<li class='competitionCnt'>"+comp.competition.template.getMiniHTML(user)+"</li>";
            }
            right+="</ul>";
        }

        writer.write("<html>\n" +
                "<head>\n" +
                "    <title>UIL - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                "    <link rel=\"stylesheet\" href=\"/css/console/console.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"/css/console/upcoming.css\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                "    <script src=\"/js/console/uil.js\"></script>" +
                "</head>\n" +
                "<body>\n" + // Dynamic.loadNav(request) +
                Dynamic.get_consoleHTML(0, right + "</div>") +
//                    "<div id='content'>" + left + "</div></div>"+right+"</div></div>" +
                "</div></body></html>");
    }
}