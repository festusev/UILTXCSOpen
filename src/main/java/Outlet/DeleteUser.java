package Outlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

public class DeleteUser extends HttpServlet {
    //private static final Logger LOGGER = LogManager.getLogger(DeleteUser.class);
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        User uData = Conn.getUser(request);
        Team team = uData.team;
        if(uData != null && !Conn.isLoggedIn(uData.token)){
            writer.write("{\"reload\":\"" +request.getContextPath() + "\"}");
            return;
        }

        String pass = request.getParameter("delUserPass");

        if(pass==null || pass.isEmpty()) {  // If it is an empty password
            writer.write("{\"error\":\"Password is empty\"}");
            return;
        }

        // Their supplied password is incorrect
        if(!uData.verifyPassword(pass)) {
            writer.write("{\"error\":\"Password is incorrect.\"}");
            return;
        }
        int status = 0;
        try {
            status = Conn.delUser(uData);
        } catch (SQLException e) {
            e.printStackTrace();
            writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
            return;
        }
        if(status<0) {
            writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
            return;
        }
        Conn.delToken(request, response, uData.token);

        // Finally Redirect back to the Console
        writer.write("{\"reload\":\""+request.getContextPath()+"\"}");
    }
}
