package Outlet;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import javax.servlet.ServletException;
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
        BigInteger token = Conn.getToken(request);
        if(!Conn.isLoggedIn(token)){
            response.sendRedirect(request.getContextPath());
        } else {
            Conn.delToken(request, response, token);    // Remove the token from the cookie and the user from the user list
            try {
                Conn.logout(token);      // Set the token to null in the database
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath());
    }
}