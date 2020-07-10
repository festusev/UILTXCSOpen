package Outlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

/***
 * The page that lets the user register a new account. Redirects to Console.java once done.
 * Created by Evan Ellis.
 */
public class Verify extends HttpServlet{
    //private static final Logger LOGGER = Logger.getLogger(Register.class.getName());

    // If they are getting, they are sending a 50 character string
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if(Conn.isLoggedIn(request)){
            response.sendRedirect(request.getContextPath() + "/console");
        }
        /*Conn.setHTMLHeaders(response);
        String vtoken = request.getParameter("vtoken");   // The verification token sent through email
        if(vtoken==null) {
            response.sendRedirect(request.getContextPath());
        }

        BigInteger bToken = Conn.verifyToken(vtoken);

        String error = "";
        if(bToken.compareTo(BigInteger.valueOf(0)) >= 0) {
            Cookie tokenCookie = new Cookie("token", bToken.toString(Character.MAX_RADIX));
            tokenCookie.setMaxAge(60*60*48);    // Set 2 Days before they must login again
            tokenCookie.setPath("/");   // This path must stay the same so that logging out is fluid.
            response.addCookie(tokenCookie);
            response.sendRedirect(request.getContextPath() + "/console");
        }
        else if(bToken.compareTo(BigInteger.valueOf(-2)) == 0){ // The token has expired
            error = "This link has expired.";
        }
        else{   // A server error occurred. token should be = -1, but who knows
            error = Dynamic.SERVER_ERROR;
        }
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
                "<div class=\"forbidden\">" + error + "<p class=\"forbiddenRedirect\"><a class=\"link\" href=\""+
                request.getContextPath()+"\">Click Here to Go back.</a></p></div>" +
                "</body>\n" +
                "</html>");*/
    }

    // If they are posting, they are sending an 8 character code
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        if(Conn.isLoggedIn(request)){
            writer.write("{\"reload\":\"" +request.getContextPath() + "/console\"}");
            return;
        }

        String action = request.getParameter("action"); // Either 'reset' or 'register'
        String code = request.getParameter("code"); // The verification code
        if(action!=null && action.equals("register")) {
            BigInteger bToken = Conn.verifyCode(code);

            if(bToken.compareTo(BigInteger.valueOf(0)) >= 0) {
                Cookie tokenCookie = new Cookie("token", bToken.toString(Character.MAX_RADIX));
                tokenCookie.setMaxAge(60*60*48);    // Set 2 Days before they must login again
                tokenCookie.setPath("/");   // This path must stay the same so that logging out is fluid.
                response.addCookie(tokenCookie);
                writer.write("{\"reload\":\""+request.getContextPath() + "/console\"}");
            } else if(bToken.compareTo(BigInteger.valueOf(-2)) == 0){ // The token has expired
                writer.write("{\"error\":\"This code has expired.\"}");
            }
            else{   // A server error occurred. token should be = -1, but who knows
                writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
            }
        } else if(action!=null && action.equals("reset")) { // Reset a forgotten password
            String email = request.getParameter("email");
            System.out.println("Resetting password for user " + email);
            if(!Conn.verifyResetCode(code, email)) {   // The code is incorrect, so return a message saying that
                writer.write("{\"error\":\"This code has expired.\"}");
                return;
            }

            String pass= request.getParameter("pass");
            String confPass = request.getParameter("passAgain");

            // In this case with both passwords null, we are merely checking if the code is correct, and will return instructions to display password input fields
            if(pass==null&&confPass==null) {
                writer.write("{\"takePassword\":\"true\"}"); // Indicate that their code is correct so they should now enter new passwords
                return;
            } else if(pass==null || confPass == null || pass.isEmpty() || confPass.isEmpty()) {
                writer.write("{\"error\":\"Password is empty.\"}");
                return;
            } else if(!confPass.equals(pass)){ // If the passwords do not match
                writer.write("{\"error\":\"Passwords do not match.\"}");
                return;
            } else {    // In this case, we complete the password change by first verifying that the code is correct and then by changing the password
                User u = Conn.getUserByEmail(email);
                try {
                    u.changePassword(Conn.getHashedFull(pass));
                    Cookie tokenCookie = new Cookie("token", u.token.toString(Character.MAX_RADIX));
                    tokenCookie.setMaxAge(60*60*48);    // Set 2 Days before they must login again
                    tokenCookie.setPath("/");   // This path must stay the same so that logging out is fluid.
                    response.addCookie(tokenCookie);
                    writer.write("{\"reload\":\""+request.getContextPath() + "/console\"}");

                    // Finally, delete the entry from the 'reset_password' database
                    Connection conn = Conn.getConnection();
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM reset_password WHERE code = ? AND email = ?");
                    stmt.setString(1,code);
                    stmt.setString(2,email);
                    stmt.executeUpdate();
                } catch (NoSuchAlgorithmException | SQLException e) {
                    e.printStackTrace();
                    writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                }
            }
        }
    }

}