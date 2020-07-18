package Outlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class LeaveTeam extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User uData = Conn.getUser(request); // We need to display the user's data
        if(uData == null || uData.token == null || !Conn.isLoggedIn(uData.token)){
            response.sendRedirect(request.getContextPath());
            return;
        }

        // Get the user's team, if they belong to one
        Team uTeam = Conn.getLoadedTeam(uData.tid);
        int status = uTeam.leaveTeam(uData);
        System.out.println("Status = " + status);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        if(status == -3) {  // This team is currently competing so you can't leave
            writer.write("{\"error\":\"You can't leave while your team is competing\"}");
        } else {
            writer.write("{\"reload\":\"" + request.getContextPath() + "/console\"}");
        }
    }
}
