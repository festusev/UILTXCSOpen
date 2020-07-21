package Outlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;

public class UpdateTeamPassword extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
/*        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        User uData = Conn.getUser(request);
        Team team = uData.team;
        if(uData != null && !Conn.isLoggedIn(uData.token)){
            writer.write("{\"reload\":\"" +request.getContextPath() + "\"}");
            return;
        }

        String curPass = request.getParameter("curTeamPass");
        String newPass = request.getParameter("newTeamPass");
        String newPassConf = request.getParameter("confNewTeamPass");
        if(newPass==null || curPass == null || newPassConf == null || newPass.isEmpty() || curPass.isEmpty() || newPassConf.isEmpty()) {
            writer.write("{\"error\":\"Password is empty.\"}");
            return;
        } else if(!team.verifyPassword(curPass)) {
            writer.write("{\"error\":\"Password is incorrect\"}");
            return;
        } else if(!newPass.equals(newPassConf)) {     // If the passwords don't match
            writer.write("{\"error\":\"Passwords don't match.\"}");
            return;
        }

        try {
            team.changePassword(Conn.getHashedFull(newPass));
        } catch (Exception e) {
            writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"");
            e.printStackTrace();
        }

        // Finally Redirect back to the Console
        writer.write("{\"success\":\"Password changed successfully.\"}");*/
    }
}
