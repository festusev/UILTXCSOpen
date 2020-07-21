package Outlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class CreateTeam extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
/*        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        User uData = Conn.getUser(request);
        if(uData != null && !Conn.isLoggedIn(uData.token)){
            writer.write("{\"reload\":\"" +request.getContextPath() + "\"}");
            return;
        }

        String tname = request.getParameter("newTeamName");
        String affiliation = request.getParameter("affiliation");
        String pass = request.getParameter("newTeamPass");
        String confPass = request.getParameter("confNewTeamPass");

        if(tname == null || tname.isEmpty()) {
            writer.write("{\"error\":\"Team name is empty.\"}");
            return;
        } else if(affiliation == null || affiliation.isEmpty()) {
            writer.write("{\"error\":\"Affiliation is empty.\"}");
            return;
        } else if(pass == null || pass.isEmpty() || confPass == null || confPass.isEmpty()) {
            writer.write("{\"error\":\"Password is empty.\"}");
            return;
        } else if(!pass.equals(confPass)) {
            writer.write("{\"error\":\"Passwords don't match.\"}");
            return;
        } else if(pass==null || pass.isEmpty()) {  // If it is an empty password
            writer.write("{\"error\":\"Password is empty\"}");
            return;
        }

        if(tname.length() > 25) {
            writer.write("{\"error\":\"Team name must not exceed 25 characters.\"}");
        } else if (affiliation.length() > 25) {
            writer.write("{\"error\":\"Affiliation must not exceed 25 characters.\"}");
        }

        int status = Conn.createTeam(tname, affiliation, pass, uData);
        if(status == -2) {  // Team already exists
            writer.write("{\"error\":\"Team name is taken.\"}");
        } else if(status == -3) {   // User belongs to a team already
            writer.write("{\"error\":\"You already belong to a team.\"}");
        } else if(status != 0) writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");    // A server error
        if(status != 0) return;

        // Finally Redirect back to the Console
        writer.write("{\"reload\":\"" + request.getContextPath() + "/console" + "\"}");*/
    }
}