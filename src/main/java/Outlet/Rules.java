package Outlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/***
 * The page that lets the user login to an existing account. Redirects to Console.java once they have logged in.
 * Created by Evan Ellis.
 */
public class Rules extends HttpServlet{
    private static final String PAGE_NAME = "rules";
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        // create HTML form
        PrintWriter writer = response.getWriter();
        writer.append("<html>\n" +
                "<head>\n" +
                "    <title>Rules - TXCSOpen</title>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "<link rel=\"icon\" type=\"image/png\" href=\"res/icon.png\">" +
                "    <link rel=\"stylesheet\" href=\"./css/bootstrap.min.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"./css/style2.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"./css/rules.css\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                "    <script src=\"./js/jquery.min.js\"></script>\n" +
                "    <script src=\"js/connect.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                Dynamic.loadNav(request, PAGE_NAME) +
                "    <div class=\"row\" id=\"upperHalf\">\n" +
                "        <div class=\"center\">\n" +
                "            <div id=\"body-header\">\n" +
                "                Rules\n" +
                "                <div class=\"subtitle\">For At-Home UIL</div>" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <div id=\"column\"><div class=\"row secRow\">\n" +
                "\n" +
                "<p class=\"secHead\">General</p><p class=\"secBody\">\n" +
                "<br><ul><li>Unlike UIL, students register and maintain their own team.</li><li>The multiple choice section is available on May 7th. Choose any 45 minute time period in which to take it. Team members do not have to take it at the same time.</li><li>The programming section is open on May 8th. Each team chooses a 2-hour period in which all members compete.</li><li>Teams are 3 people with no 4th designated tester for logistics reasons.</li><li>You can program in Java 11, Python 3, or C++ 17. The programming packet shows example program names in Java as well as an example input method for assistance.</li><li>If you have any questions, reach out to us at <span class=\"link\">contact@txcsopen.com</span>.</li></ul>" +
                "   </div>" +
                "<div class=\"row secRow\">\n" +
                "\n" +
                        "<p class=\"secHead\">Written Test</p><p class=\"secBody\">\n" +
                        "<br><span class=\"tab\"></span>The test will be taken individually during any 45 minute period on May 7th. Team members do not have to test at the same time. " +
                "There is no designated fourth tester as in UIL for logistics reasons. " +
                "Using any resources not provided during a typical UIL test is strictly prohibited. It is difficult to monitor this, so we ask that teams preserve the integrity of the test. They're no prizes, so don’t bother cheating. " +
                "The test will consist of 40 unmodified multiple choice questions, the same questions provided to us by UIL." +
                "Scoring for the multiple choice is slightly different to adjust for more hands-on questions. Each question correct is worth 9 points and each incorrect is -3 points. As usual, blank answers are +0.</p>" +
                "   </div>" +
                "    <div class=\"row secRow\">\n" +
                "       <p class=\"secHead\">Hands on Programming</p><p class=\"secBody\">" +
                "       <br><span class=\"tab\"></span>The hands on programming portion will be taken as a group of at most three people during any 2 hour long period on May 8th. " +
                "Competitors will be allowed to code simultaneously on their own machines, and are allowed to use the internet. They can collaborate with their two teammates and nobody else. " +
                "There will be 18 problems to account for every competitor having their own computer. " +
                "Each problem is still worth 60 points, and 5 points will be subtracted from the maximum for every unsuccessful attempt. Each submission has a time limit of 15 seconds before quitting.</p>" +
                "   </div>" +
                "    <div class=\"row secRow\">\n" +
                "       <p class=\"secHead\">Scoring</p><p class=\"secBody\">" +
                "       <br><span class=\"tab\"></span>Both halves of the competition will be equally weighted, the same as in normal UIL. However, to account for the increased number of questions on the hands on portion, each half is now out of 1080 points. " +
                "Teams can check their rank on our live scoreboard.</p>" +
                "   </div>" +
                Dynamic.loadLeftFlair() +
                "</body>\n" +
                "</html>\n");
    }
}