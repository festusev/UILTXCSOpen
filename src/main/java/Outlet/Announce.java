package Outlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.logging.Logger;

/***
 * The page that lets me create server-wide announcements.
 * Created by Evan Ellis.
 */
public class Announce extends HttpServlet{
    private static final String PASSWORD = "7NNGztFcKv%&M9nt";
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Conn.setHTMLHeaders(response);

        // create HTML form
        PrintWriter writer = response.getWriter();
        writer.append("<html>\n" +
                "<head>\n" +
                "    <title>Register - TXCSOpen</title>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "<link rel=\"icon\" type=\"image/png\" href=\"res/icon.png\">" +
                "    <link rel=\"stylesheet\" href=\"./css/bootstrap.min.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"./css/style2.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"./css/register.css\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                "    <script src=\"./js/register.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "<form method=\"POST\" action=\"announce-qm30b0cwerev8cf3k22d\">" +
                "<input type=\"text\" value=\"Announcement\" name=\"announcement\">" +
                "<input type=\"password\" name=\"password\">" +
                "<input type=\"submit2\">Submit Announcement</input>" +
                "</form>"+
                "</body>\n" +
                "</html>");
    }

    // If they are posting, they are sending an 8 character code
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String announcement = request.getParameter("announcement");
        String pass = request.getParameter("password");

        if(PASSWORD.equals(pass)) {
            Dynamic.setAnnouncement(announcement);
            response.sendRedirect("index.jsp");
        } else {
            Conn.setHTMLHeaders(response);
            response.getWriter().write("ERROR: Incorrect Password");
        }
    }

}