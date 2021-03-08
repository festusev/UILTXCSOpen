package Outlet;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/***
 * The page that lets the user login to an existing account. Redirects to Console.java once they have logged in.
 * Created by Evan Ellis.
 */
public class Login extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if(Conn.isLoggedIn(request)){
            response.sendRedirect(request.getContextPath() + "/console/competitions");
        }
        // set response headers
        Conn.setHTMLHeaders(response);
        String body =
                    "    <div class=\"column\"><div id=\"center\" class=\"head-row row\">" +
                            "<h1>Login</h1>" +
                    "        <form onsubmit=\"login(); return false;\" id=\"login-box\">" +
                    "            <label for=\"email\">Email/Username</label>\n" +
                    "            <input type=\"text\" id=\"email\" name=\"email\" maxlength=\"255\">" +
                    "            <label for=\"pass\">Password</label>" +
                    "            <input type=\"password\" id=\"pass\" name=\"pass\">" +
                    "            <button id=\"login\">Login</button>" +
                    "            <p id=\"regWrapper\">Don't have an account? <a class='link' href=\"register\">Register.</a><br><a class='link' onclick=\"resetPassword()\">Reset password.</a></p>" +
                    "        </form>\n" +
                    "    </div></div>\n";

        PrintWriter writer = response.getWriter();
        // create HTML form
        writer.write("<html>\n" +
                "<head>\n" +
                "    <title>Login - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                "    <link rel=\"stylesheet\" href=\"./css/login.css\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                "    <script src=\"js/login.js\"></script>" +
                "</head>\n" +
                "<body>\n" +
                Dynamic.loadLoggedOutNav() +
                body +
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
            writer.write("{\"success\":\"" +request.getContextPath() + "/console/profile\"}");
            return;
        }

        String email = request.getParameter("email");
        String resendS = request.getParameter("resend");    // If the change password email should be resent
        boolean resend = resendS!=null && resendS.equals("true");

        if(email == null || email.isEmpty()) {
            writer.write("{\"error\":\"Email is empty.\"}");
            return;
        }

        User user = UserMap.getUserByEmail(email);
        if(user == null) {
            writer.write("{\"error\":\"No user exists with that email.\"}");
            return;
        }

        if(resend) {
            if(user.temp) {
                writer.write("{\"error\":\"Temporary users cannot reset their password.\"}");
                return;
            }
            int status = Conn.resendResetVerification(email);
            if(status != 0) writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
            else writer.write("{\"success\":\"Resent verification email.\"}");
            return;
        }
        String resetS = request.getParameter("reset");   // If this person is changing their password
        boolean reset = resetS!=null && resetS.equals("true");
        if(reset) { // Put their hashed code into the 'reset_pass' database along with their email
            if(user.temp) {
                writer.write("{\"error\":\"Temporary users cannot reset their password.\"}");
                return;
            }

            int status = Conn.ResetPassword(user); // Put the data into the 'reset_pass' database

            // Perform the reset update. If the user doesn't already exist and no errors occurred, then we tell the page to show the "input code" box for verification
            if (status >= 0) {    // The page will wait for a code to be entered. The user can also follow the link
                writer.write("{\"success\":\"<style>#codeTitle span span{ font-size:1em; display:inline-block; font-weight:bold; }#code{text-transform:uppercase;font-family:var(--mono);}.link2{ color:var(--sec-col); font-weight:bold; } .link2:hover{ color:var(--sec-dark); }#lowerHalf{display:none;}#codeCnt{ box-sizing:border-box;display:block; width:100%; background-color:white; box-shadow:0 0px 6px 1px rgba(0,0,0,.12); margin:auto; padding:2em; margin-top:10vh; } #codeTitle{ font-size:2em; font-weight:bold; color:var(--prim-middle); } #codeTitle span{ font-size:0.45em; display:block; font-weight:normal; color:var(--head-col); } #code{ width:100%; font-size:5em; height:1em; padding:0; text-align:center; color:var(--prim-light); } #bottomText{ margin-top:1em; } #upperHalf{ box-shadow:none; } #copyright_notice{ display:none; }</style><div id='codeCnt'><p id='codeTitle'>Verify your email<span>A verification code was sent to <span>EMAIL_REPLACE</span>. It will expire in 15 minutes.</p><div id='codeErrorBox'></div><input id='code' type='text' maxlength='6' oninput='codeEntered()'><p id='bottomText'>Didn't get the email? <a class='link2' style='color:var(--sec-col);cursor:pointer;font-weight:bold;' onclick='resend();'>Resend</a> the verification code.</div>\"}");
                return;
            } else if (status == -2) { // The email doesn't exist
                writer.write("{\"error\":\"No account with that email exists.\"}");
                return;
            } else {   // A server error occurred. token should be = -1, but who knows
                writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
                return;
            }
        }

        String pass = request.getParameter("pass");

        if(pass == null || pass.isEmpty()) {
            writer.write("{\"error\":\"Password is empty.\"}");
            return;
        }

        try {
            BigInteger token = Conn.Login(user, pass);
            if(token.compareTo(BigInteger.valueOf(0)) >= 0) {   // The login was successful
                Cookie tokenCookie = new Cookie("token", token.toString(Character.MAX_RADIX));
                tokenCookie.setMaxAge(60*60*48);    // Set 2 Days before they must login again
                tokenCookie.setPath("/");   // Necessary for logging out fluidly
                response.addCookie(tokenCookie);
                writer.write("{\"success\":\"" +request.getContextPath() + "/console/profile\"}");
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