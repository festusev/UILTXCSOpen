package Outlet;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/***
 * Logs out the user on a get request. As always, the user is specified by the user token. Removes the user's token
 * from the database, from their cookie, and deletes the user variable from the Users ArrayList.
 * Created by Evan Ellis.
 */
public class Logout extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("Context Path="+request.getContextPath());
        User u = UserMap.getUserByRequest(request);
        if(u != null && u.token != null) {
            Conn.delToken(request, response, u);    // Remove the token from the cookie
            Conn.logout(u.token);      // Set the token to null in the database
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}