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
        <div id="body-description">Student-run competition site.</div>
        <p>Create a team of 3 to compete in the competitions and rank up in the <a href="/scoreboard" class="link">aggregated scoreboard!</a></p>
        <p>Compete in 2-day Academic UILs that test skills in Mathematics and Computer Science, and take on the 5-day custom challenge. We're working to possibly offer prizes in the future, so stay tuned!</p>
        <p>Join the community <a href="https://discord.gg/ukT4QnZ" class="link">discord</a> to receive announcements and ask for help. You can always email us at <a href="mailto: contact@txcsopen.com" class="link">contact@txcsopen.com</a>, and we'll be releasing post-competition surveys to help us make improvements.</p>
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