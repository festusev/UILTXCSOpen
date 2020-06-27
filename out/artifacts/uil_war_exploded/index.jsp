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
        <div id="body-description">Online competition website</div>
        <p>Create a team of 3 to compete in the competitions and rank up in the <a href="/scoreboard">aggregated scoreboard!</a></p>
        <p>Compete in 2-day Academic UILs that test skills in Mathematics and Computer Science, and take on the 5-day custom challenge. We're working to possibly offer prizes in the future, so stay tuned!</p>
        <p>Join the community <a>discord</a> to receive announcements and ask for help. You can always email us at <a href="mailto: contact@txcsopen.com">contact@txcsopen.com</a>, and we'll be releasing post-competition surveys to help us make improvements.</p>
    </div>
</div>
<div id="schedule">
    <table>
        <tr>
            <th>Schedule</th>
        </tr>
        <tr>
            <td>8/1/2020 - 8/2/2020</td>
            <td>UIL CS</td>
            <td>Java, C++, and Python Programming competitions.</td>
        </tr>
        <tr>
            <td>8/1/2020 - 8/2/2020</td>
            <td>UIL CS</td>
            <td>Java, C++, and Python Programming competitions.</td>
        </tr>
        <tr>
            <td>8/1/2020 - 8/2/2020</td>
            <td>UIL CS</td>
            <td>Java, C++, and Python Programming competitions.</td>
        </tr>
        <tr>
            <td>8/1/2020 - 8/2/2020</td>
            <td>UIL CS</td>
            <td>Java, C++, and Python Programming competitions.</td>
        </tr>
        <tr>
            <td>8/1/2020 - 8/2/2020</td>
            <td>UIL CS</td>
            <td>Java, C++, and Python Programming competitions.</td>
        </tr>
    </table>
</div>
<%=
Dynamic.loadBigCopyright()
%>
</body>
</html>