<%@ page import="Outlet.ContextListener" %>
<%@ page import="Outlet.Dynamic" %><%--
  Created by IntelliJ IDEA.
  User: trappist1
  Date: 4/24/20
  Time: 2:44 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>404 - TXCSOpen</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="res/icon.png">
    <link rel="stylesheet" href="./css/bootstrap.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="./css/style2.css">
</head>
<body style="background-image:url('res/stars.jpg')">
<%
    Dynamic.addPageView(request, "Error404");
%>
<style>
    #center{
        font-size:2em;
        text-align:center;
        margin-top:8em;
        color:white;
    }
    #comment{
        font-size:0.8em;
    }
    #errorCode{
        font-size:2em;
        font-weight:bold;
    }
    a{
        font-size:0.8em;
    }
</style>
<div id="center">
    <p id="errorCode">404</p>
    <p id="comment">Nothing but stars and hidden servlets out here.</p>
    <a href="index.jsp" class="link">Return home</a>
</div>
</body>
</html>
