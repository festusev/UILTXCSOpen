<%@ page import="Outlet.ContextListener" %>
<%@ page import="Outlet.Dynamic" %><%--
  Created by IntelliJ IDEA.
  User: trappist1
  Date: 4/24/20
  Time: 3:14 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>500 - TXCSOpen</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="res/icon.png">
    <link rel="stylesheet" href="./css/bootstrap.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="./css/style.css">
</head>
<body>
<%
    Dynamic.addPageView(request, "Error500");
%>
<style>
    #center{
        font-size:2em;
        text-align:center;
        margin-top:8em;
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
    <p id="errorCode">500</p>
    <p id="comment">Whoops, this one's on us. Fill out this error report <a href="https://forms.gle/eKJSTFn7BTp6Gu538" class="link">form</a> to help us fix the issue.</p>
    <a href="index.jsp" class="link">Return home</a>
</div>
</body>
</html>
