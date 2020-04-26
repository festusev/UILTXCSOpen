package Outlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;

public class UpdateUserPassword extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        User uData = Conn.getUser(request);
        if(uData != null && !Conn.isLoggedIn(uData.token)){
            writer.write("{\"reload\":\"" +request.getContextPath() + "\"}");
            return;
        }

        String curPass = request.getParameter("curUserPass");
        String newPass = request.getParameter("newUserPass");
        String newPassConf = request.getParameter("confNewUserPass");
        if(newPass==null || curPass == null || newPassConf == null || newPass.isEmpty() || curPass.isEmpty() || newPassConf.isEmpty()) {
            writer.write("{\"error\":\"Password is empty.\"}");
            return;
        } else if(!uData.verifyPassword(curPass)) {
            writer.write("{\"error\":\"Password is incorrect\"}");
            return;
        } else if(!newPass.equals(newPassConf)) {     // If the passwords don't match
            writer.write("{\"error\":\"Passwords don't match\"}");
            return;
        }

        try {
            uData.changePassword(Conn.getHashedFull(newPass));
        } catch (Exception e) {
            writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
            e.printStackTrace();
        }

        // Finally send a success message
        writer.write("{\"success\":\"Password changed successfully.\"}");
    }
}
