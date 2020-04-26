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
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/png" href="res/icon.png">
  <link rel="stylesheet" href="./css/bootstrap.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="./css/style.css">
  <link rel="stylesheet" href="./css/index.css">
</head>
<body>
  <%
    Dynamic.addPageview();
  %>
  <%=
  Dynamic.loadNav(request)
  %>
  <div class="row" id="headRow">
    <div class="center" id="first-row">
      <div id="body-title"><div><img alt="TXCSOpen" src="res/logo_dark_texasless.svg"/></div><div><img alt="LASACSClub" src="res/lasacslogo_dark.svg"></div></div>
      <%=
      Dynamic.loadCountdown()
      %>
      <div id="body-description">Official UIL may be canceled, but that doesn’t mean you can’t compete! UIL by TXCSOpen is an online verison of Computer Science UIL created in partnership with LASACSClub to simulate the competition in a remote environment. The format is slightly altered to accommodate shifting schedules. The competition is open from May 13th at 12am to May 14th at 11:59pm. Check the <a href="rules" class="link">rules</a> page for more information.</div>
    </div>
  </div>
</body>
</html>