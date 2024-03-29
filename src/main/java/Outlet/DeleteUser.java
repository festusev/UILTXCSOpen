package Outlet;

import Outlet.uil.Competition;
import Outlet.uil.UIL;
import Outlet.uil.UILEntry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static Outlet.Conn.getConnection;

public class DeleteUser extends HttpServlet {
    //private static final Logger LOGGER = LogManager.getLogger(DeleteUser.class);
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        User uData = UserMap.getUserByRequest(request);
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


        int status;
        try {
            Connection conn = getConnection();

            // First, logout the user from the database
            Conn.logout(uData.token);
            Conn.delToken(request, response, uData);

            if(uData.teacher) { // They are a teacher, so delete their class and all of their competitions
                for(Competition competition: ((Teacher)uData).competitions) {
                    UIL.deleteCompetition(competition);
                }

                Team.teams.remove(uData.uid);
            } else {
                Collection<UILEntry> entries = ((Student)uData).cids.values();
                for(UILEntry entry: entries) {
                    entry.leaveTeam((Student) uData);
                }
            }

            // Next, remove the user's row from the database
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE uid=?");
            stmt.setShort(1, uData.uid);
            status = stmt.executeUpdate();
            UserMap.delUser(uData);
        } catch (SQLException e) {
            e.printStackTrace();
            writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
            return;
        }
        if(status<0) {
            writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
            return;
        }

        // Next, redirect home
        writer.write("{\"reload\":\""+request.getContextPath()+"\"}");

        // Finally, update all of the class html for any users connected to the profile socket.
        if(uData.teacher) {
            ((Teacher)uData).updateDeletedClassHTML();
        } else if(((Student)uData).teacherId > 0) {
            Teacher teacher = TeacherMap.getByUID(((Student)uData).teacherId);
            teacher.updateClassHTML();
        }
    }
}
