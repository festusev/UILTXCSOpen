<%--
  Created by IntelliJ IDEA.
  User: trappist1
  Date: 3/8/20
  Time: 7:58 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="Outlet.Dynamic"  contentType="text/html;charset=UTF-8"%>
<%@ page import="Outlet.ContextListener" %>
<html lang="en" class="no-js">
<head>
  <title>TXCSOpen 2020 Competition</title>
  <%=
  Dynamic.loadHeaders()
  %>
  <link rel="stylesheet" href="./css/index.css">
</head>
<body>
<%=
Dynamic.loadNav(request)
%>
<div id="upperHalf">
    <div id="web-instructions">
        <div id="body-title"><img draggable="false" alt="TXCSOpen" src="res/logo_dark_texasless.svg"/></div>
        <div id="body-description">Seamlessly Run UIL Competitions Online.</div>
        <p>TXCSOpen helps you run UIL competitions with just a few clicks. Register below to create a competition or start competing:</p>
        <a id="register_cta" href="${pageContext.request.contextPath}/register">Register</a>
        <!--<p>Join the community <a href="https://discord.gg/ukT4QnZ" class="link">discord</a> to receive announcements and
            ask for help. You can always email us at <a href="mailto: contact@txcsopen.com" class="link">contact@txcsopen.com</a>.
            Feel free to send us suggestions for improvements.</p>-->
    </div>
</div>
<!--<div id="schedule">
    <table>
        <tr>
            <th>Schedule</th>
        </tr>
        <tr>
            <td>7/31/2020 - 8/2/2020</td>
            <td>Computer Science</td>
            <td>Java, C++, and Python Programming competition</td>
        </tr>
        <tr>
            <td>8/7/2020 - 8/9/2020</td>
            <td>Mathematics</td>
            <td>Comprehensive math competition testing speed and knowledge</td>
        </tr>
        <tr>
            <td>8/14/2020 - 8/16/2020</td>
            <td>Number Sense</td>
            <td>High-velocity 10-minute mental math test.</td>
        </tr>
        <tr>
            <td>8/21/2020 - 8/23/2020</td>
            <td>Calculator Applications</td>
            <td>A unique math competition testing speed with a calculator</td>
        </tr>
        <tr>
            <td>9/11/2020 - 9/16/2020</td>
            <td>TXCSOpen Challenge</td>
            <td>5-day one-problem heuristic programming challenge</td>
        </tr>
    </table>
</div>-->
<%=
Dynamic.loadBigCopyright()
%>
</body>
</html>