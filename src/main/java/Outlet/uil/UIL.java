package Outlet.uil;

import Outlet.Dynamic;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/** For the UIL page */
public class UIL extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        // create HTML form
        PrintWriter writer = response.getWriter();
        writer.write("<html>" +
                "<head>" +
                "<title>UIL - TXCSOpen</title>" + Dynamic.loadHeaders() +
                "<link rel=\"stylesheet\" href=\"/css/uil.css\">" +
                "</head>" +
                "<body>" + Dynamic.loadNav(request) +
                "<div id=\"column\">" +
                "<div id=\"header\">" +
                "<h1>UIL</h1>" +
                "<p>Seriously respected academic competitions.</p>" +
                "</div>" +
                "<table id=\"tiles\">" +
                "<tr>" +
                "<td id=\"CS\"><a href=\"/uil/cs\"><h2>CS</h2><p>8/1/2020 to 8/2/2020</p></a></td>" +
                "<td id=\"CS\"><a href=\"/uil/cs\"><h2>CS</h2><p>8/1/2020 to 8/2/2020</p></a></td>" +
                "</tr>" +
                "<tr>" +
                "<td id=\"CS\"><a href=\"/uil/cs\"><h2>CS</h2><p>8/1/2020 to 8/2/2020</p></a></td>" +
                "<td id=\"CS\"><a href=\"/uil/cs\"><h2>CS</h2><p>8/1/2020 to 8/2/2020</p></a></td>" +
                "</tr>" +
                "</table>");
    }
}
