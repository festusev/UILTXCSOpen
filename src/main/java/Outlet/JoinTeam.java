package Outlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class JoinTeam extends HttpServlet {
    //private static final Logger LOGGER = LogManager.getLogger(JoinTeam.class);
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        User uData = Conn.getUser(request);
        if(uData != null && !Conn.isLoggedIn(uData.token)){
            writer.write("{\"reload\":\"" +request.getContextPath() + "/\"}");
            return;
        }

        String tname = request.getParameter("joinTeamName");
        String pass = request.getParameter("joinTeamPass");

        if(tname == null || tname.isEmpty()) {
            writer.write("{\"error\":\"Team name is empty.\"}");
            return;
        } else if(pass==null || pass.isEmpty()) {  // If it is an empty password
            writer.write("{\"error\":\"Password is empty\"}");
            return;
        }
        if(tname.length() > 25) {
            writer.write("{\"error\":\"Team doesn't exist.\"}");
            return;
        }
        try {
            int status = Conn.joinTeam(tname, pass, uData);
            if(status == -5) {     // Incorrect password
                writer.write("{\"error\":\"Incorrect Password\"}");
            } else if(status == -3) {   // The team can't be found
                writer.write("{\"error\":\"Team doesn't exist.\"}");
            } else if(status ==-2) {    // The team is full
                writer.write("{\"error\":\"This team is full.\"}");
            } else if(status ==1) { // They already belong to this team
                writer.write("{\"error\":\"You already belong to this team!\"}");
                return;
            } else if(status !=0) {
                writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
            }
            if(status !=0) return;
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("{\"error\":\"\"}");
            return;
        }

        // Finally Redirect back to the Console
        writer.write("{\"reload\":\"" + request.getContextPath() + "/console\"}");   // Reload the page
    }
}
