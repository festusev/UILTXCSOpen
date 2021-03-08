package Outlet;
import Outlet.uil.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/***
 * The first page the user sees when they log in. Contains user and team configurations.
 * Created by Evan Ellis.
 */
public class Help extends HttpServlet{
    protected static Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User u = UserMap.getUserByRequest(request);
        if(u==null || u.token == null){
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        Conn.setHTMLHeaders(response);

        String right = "<div id='Help'><div class='helpSection'><h1>Help</h1>Join the community <a href='https://discord.gg/ukT4QnZ'" +
                " class='link'>discord</a> to receive announcements and ask for help. You can always email us at " +
                "<a href='mailto: contact@txcsopen.com' class='link'>contact@txcsopen.com</a>.</div>";
        if(u.teacher) {
            right += "<div class='helpSection'><h2>Quick Help</h2>Right now you are in your teacher profile. " +
                    "On the left sidebar, you will see links to access your Profile, Class, Competitions, and the Help " +
                    "page (where you are now).</div>" +

                    "<div class='helpSection'><h2>Profile</h2>In the profile page, you can update your name, school, " +
                    "and password. Then click save changes to publish your changes.</div>" +

                    "<div class='helpSection'><h2>Class</h2>The class page lets you manage the students in your classroom, " +
                    "who are able to view nonpublic competitions. Give your class code out to your students so that " +
                    "they can sign up to join your class. In their profile page, they will have the option to use " +
                    "this class code to join your class. If you want to remove a student, simply click the ‘kick’ " +
                    "button to the right of their name. The ability to send messages to your class is coming soon.</div>" +

                    "<div class='helpSection'><h2>Competitions</h2>The competitions page allows you to quickly create " +
                    "UIL competitions. Click the orange new button to create a new competition. From there you initially " +
                    "have a few options. You can name and give a description for your competition, which your students " +
                    "will see on the competition’s page when competing. This is where we suggest adding links to resources " +
                    "other than the tests, like the student packets. You can select whether the competition is public " +
                    "(viewable to everyone) or nonpublic (viewable to the students in your class). You can choose whether " +
                    "you want a written test and/or hands on programming test by clicking the checkboxes on the right " +
                    "of the labels. Finally, at the top of each competition, you can choose to save, publish, delete, " +
                    "or, if your competition is already published, jump to the competition’s page, where you can score students.<br><br>" +

                    "The written test has a start date and length (usually 45 mins). Click the plus button to the right " +
                    "of answers to insert the test key. Click the add button to add questions to the key. The key supports " +
                    "two types of questions, multiple choice questions (MC) from A to E, and short answer questions (SAQ), " +
                    "which allow any text. Click the drop down box on the right of a question to change its type. All " +
                    "questions can be rescored in the competition’s page after and when students are competing. To quickly " +
                    "edit the key, press tab after clicking on the input to a given question, and you will automatically " +
                    "highlight text from question to question. To attach the written test file, just put a link to the " +
                    "file in the test link section. (which you can do with Google Drive). You can also manually set the " +
                    "number of points per correct and incorrect problem (default 8 and 2).<br><br>" +

                    "The hands-on programming section also contains a start date and length (usually 2 hours). Click " +
                    "change problems to add problems. Next to the problem number, you can name a problem, add the judge " +
                    "input file, and the judge output file. You can manually judge student outputs on the competition’s " +
                    "page. You can link to the hands on programming test in the student packet link section. You can " +
                    "also set the number of points per correct and incorrect problem (default 60 and 5).</div>";
        } else {
            right += "<div class='helpSection'><h2>Class</h2>To join a class, enter your class’ code, which your teacher " +
                    "will give out to you. Once you’ve joined a class, you will be able to see your teacher, your classmates, " +
                    "and any competitions your teacher has published.</div>";
        }
        right += "</div>";
        if(u.teacher) right += "</div>";


        // create HTML form
        PrintWriter writer = response.getWriter();
        writer.append("<html>\n" +
                "<head>\n" +
                "    <title>Help - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                "    <link rel=\"stylesheet\" href=\"/css/console/console.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"/css/console/help.css\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                "    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css\">\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/flatpickr\"></script>" +
                "</head>\n" +
                "<body>\n" +
                Dynamic.get_consoleHTML(3, right, u) +
                "</body>\n" +
                "</html>");
    }
}