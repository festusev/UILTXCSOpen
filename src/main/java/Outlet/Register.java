package Outlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.logging.Logger;
import java.util.regex.Pattern;

/***
 * The page that lets the user register a new account. Redirects to Console.java once done.
 * Created by Evan Ellis.
 */
public class Register extends HttpServlet{
    private static final String PAGE_NAME = "register";
    private static final Logger LOGGER = Logger.getLogger(Register.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if(Conn.isLoggedIn(request)){
            response.sendRedirect(request.getContextPath() + "/console");
        }
        Conn.setHTMLHeaders(response);

        String body =
                    "    <div class=\"column\"><div id=\"center\" class=\"row head-row\">\n" +
                            "<h1>Register</h1>" +
                    "        <form onsubmit=\"register(); return false;\" id=\"reg-box\">\n" +
                    "            <label for=\"email\">Email</label>\n" +
                    "            <input type=\"text\" id=\"email\" name=\"email\" class=\"form-input\" maxlength=\"255\">\n" +
                    "            <label for=\"uname\">Username</label>\n" +
                    "            <input type=\"text\" id=\"uname\" name=\"uname\" class=\"form-input\" maxlength=\"15\">\n" +
                    "            <label for=\"pass\">Password</label>\n" +
                    "            <input type=\"password\" id=\"pass\" name=\"pass\" class=\"form-input\">\n" +
                    "            <label for=\"passAgain\">Re-Type Your Password</label>\n" +
                    "            <input type=\"password\" id=\"passAgain\" name=\"passAgain\" class=\"form-input\">\n" +
                    "            <button id=\"reg\">Register</button>\n" +
                    "            <p id=\"logWrapper\">Already have an account? <a href=\"login\" class='link'>Login.</a></p>\n" +
                    "        </form>\n" +
                    "    </div></div>\n" +
                    "</div>";

        // create HTML form
        PrintWriter writer = response.getWriter();
        writer.append("<html>\n" +
                "<head>\n" +
                "    <title>Register - TXCSOpen</title>\n" + Dynamic.loadHeaders() +
                "    <link rel=\"stylesheet\" href=\"./css/register.css\">\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                "    <script src=\"./js/register.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                Dynamic.loadNav(request) +
                body +
                Dynamic.loadCopyright() +
                "</body>\n" +
                "</html>");
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
        String resendS = request.getParameter("resend");    // If the verification email should be resent
        boolean resend = resendS!=null && resendS.equals("true");

        if(email == null || email.isEmpty()) {
            writer.write("{\"error\":\"Email is empty.\"}");
            return;
        }
        if(resend) {
            int status = Conn.resendVerification(email);
            if(status != 0) writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
            else writer.write("{\"success\":\"Resent verification email.\"}");
            return;
        }

        String uname = request.getParameter("uname");
        String pass = request.getParameter("pass");
        String confPass = request.getParameter("passAgain");
        String teacherString = request.getParameter("teacher");
        if(uname == null || uname.isEmpty()) {
            writer.write("{\"error\":\"Username is empty.\"}");
            return;
        } else if(pass==null || confPass == null || pass.isEmpty() || confPass.isEmpty()) {
            writer.write("{\"error\":\"Password is empty.\"}");
            return;
        } else if(!confPass.equals(pass)){ // If the passwords do not match
            writer.write("{\"error\":\"Passwords do not match.\"}");
            return;
        }

        if(uname.length() > 15) {
            writer.write("{\"error\":\"Username must not exceed 15 characters\"}");
        } else if(!isValid(email)) {
            writer.write("{\"error\":\"Invalid email.\"}");
            return;
        }

        // TODO: Alan make it so that they can check whether they are a teacher or not and then it sends that data in the POST
        boolean isTeacher = teacherString!=null&&!teacherString.isEmpty()&&teacherString.equals("true");    // True if they are a teacher
        try {
            int status = Conn.Register(uname , email, pass, isTeacher); // Put the data into the verification database

            // Perform the register update. If the user doesn't already exist and no errors occurred, then we tell the page to show the "input code" box for verification
            if(status >= 0){    // The page will wait for a code to be entered. The user can also follow the link
                writer.write("{\"success\":\"<style>#codeTitle span span{ font-size:1em; display:inline-block; font-weight:bold; }#code{text-transform:uppercase;font-family:var(--mono);}.link2{ color:var(--sec-col); font-weight:bold; } .link2:hover{ color:var(--sec-dark); }#lowerHalf{display:none;}#codeCnt{ display:block; width:100%; background-color:white; box-shadow:0 0px 6px 1px rgba(0,0,0,.12); margin:auto; padding:2em; margin-top:6em; } #codeTitle{ font-size:2em; font-weight:bold; color:var(--prim-middle); } #codeTitle span{ font-size:0.45em; display:block; font-weight:normal; color:var(--head-col); } #code{ width:100%; font-size:5em; height:1em; padding:0; text-align:center; color:var(--prim-light); } #bottomText{ margin-top:1em; } #upperHalf{ box-shadow:none; } #copyright_notice{ display:none; }</style><div id='codeCnt'><p id='codeTitle'>Verify your email<span>A verification code was sent to <span>EMAIL_REPLACE</span>. It will expire in 15 minutes.</p><div id='codeErrorBox'></div><input id='code' type='text' maxlength='6' oninput='codeEntered()'><p id='bottomText'>Didn't get the email? <a class='link2' style='color:var(--sec-col);cursor:pointer;font-weight:bold;' onclick='resend();'>Resend</a> the verification code.</div>\"}");
            }
            else if(status == -2){ // The email is already taken
                writer.write("{\"error\":\"Email is already taken by another user.\"}");
            }
            else if(status == -3) { // The username is already taken
                writer.write("{\"error\":\"Username is already taken by another user\"}");
            }
            else{   // A server error occurred. token should be = -1, but who knows
                writer.write("{\"error\":\"" + Dynamic.SERVER_ERROR + "\"}");
            }
        } catch (NoSuchAlgorithmException | SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if(email.length() > 255) return false;
        return pat.matcher(email).matches();
    }
}