package Outlet;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/***
 * The page that lets the user login to an existing account. Redirects to Console.java once they have logged in.
 * Created by Evan Ellis.
 */
public class Login extends HttpServlet{
    private static Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Dynamic.addPageview();
        if(Conn.isLoggedIn(request)){
            response.sendRedirect(request.getContextPath() + "/console");
        }
        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        String body = "    <div class=\"row\" id=\"upperHalf\">\n" +
                    "        <div class=\"center\">\n" +
                    "            <div id=\"body-header\">\n" +
                    "                Login\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    <div class=\"row\" id=\"lowerHalf\">\n" +
                    "        <form onsubmit=\"login(); return false;\" id=\"login-box\">\n" +
                    "            <label for=\"email\">Email</label>\n" +
                    "            <input type=\"text\" id=\"email\" name=\"email\" maxlength=\"255\">\n" +
                    "            <label for=\"pass\">Password</label>\n" +
                    "            <input type=\"password\" id=\"pass\" name=\"pass\">\n" +
                    "            <button id=\"login\">Login</button>\n" +
                    "            <p id=\"regWrapper\">Don't have an account? <a href=\"register\">Register.</a></p>\n" +
                    "        </form>\n" +
                    "    </div>\n";

        PrintWriter writer = response.getWriter();
        // create HTML form
        writer.write("<html>\n" +
                "<head>\n" +
                "    <title>Login - TXCSOpen</title>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "<link rel=\"icon\" type=\"image/png\" href=\"res/icon.png\">" +
                "    <link rel=\"stylesheet\" href=\"./css/bootstrap.min.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"./css/style.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"./css/login.css\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                "    <script src=\"js/login.js\"></script>" +
                "</head>\n" +
                "<body>\n" +
                Dynamic.loadLoggedOutNav() +
                body +
                Dynamic.loadLeftFlair() +
                Dynamic.loadCopyright() +
                "</body>\n" +
                "</html>\n");
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        if(Conn.isLoggedIn(request)){
            writer.write("{\"success\":\"" +request.getContextPath() + "/console\"}");
            return;
        }
        String email = request.getParameter("email");
        String pass = request.getParameter("pass");

        if(email == null || email.isEmpty()) {
            writer.write("{\"error\":\"Email is empty.\"}");
            return;
        } else if(pass == null || pass.isEmpty()) {
            writer.write("{\"error\":\"Password is empty.\"}");
            return;
        }

        try {
            BigInteger token = Conn.Login(email, pass);
            if(token.compareTo(BigInteger.valueOf(0)) >= 0) {   // The login was successful
                Cookie tokenCookie = new Cookie("token", token.toString(Character.MAX_RADIX));
                tokenCookie.setMaxAge(60*60*48);    // Set 2 Days before they must login again
                tokenCookie.setPath("/");   // Necessary for logging out fluidly
                response.addCookie(tokenCookie);
                writer.write("{\"success\":\"" +request.getContextPath() + "/console\"}");
                return;
            }
            if(token.compareTo(BigInteger.valueOf(-1)) == 0) {   // If a server error occurred
                writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
            } else if(token.compareTo(BigInteger.valueOf(-2)) == 0) {   // If the user doesn't exist
                writer.write("{\"error\":\"No user with that email exists.\"}");
            } else if(token.compareTo(BigInteger.valueOf(-3)) == 0) {   // If the password is incorrect
                writer.write("{\"error\":\"Incorrect password.\"}");
            }
        } catch (NoSuchAlgorithmException | SQLException e) {
            e.printStackTrace();
        }
    }

}