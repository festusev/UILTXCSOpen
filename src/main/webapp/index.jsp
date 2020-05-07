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
  <title>UIL - LASA CSClub</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="icon" type="image/png" href="res/icon.png">
  <link rel="stylesheet" href="./css/bootstrap.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="./css/style2.css">
  <link rel="stylesheet" href="./css/index.css">
  <script>
    !function(){var analytics=window.analytics=window.analytics||[];if(!analytics.initialize)if(analytics.invoked)window.console&&console.error&&console.error("Segment snippet included twice.");else{analytics.invoked=!0;analytics.methods=["trackSubmit","trackClick","trackLink","trackForm","pageview","identify","reset","group","track","ready","alias","debug","page","once","off","on","addSourceMiddleware","addDestinationMiddleware"];analytics.factory=function(e){return function(){var t=Array.prototype.slice.call(arguments);t.unshift(e);analytics.push(t);return analytics}};for(var e=0;e<analytics.methods.length;e++){var t=analytics.methods[e];analytics[t]=analytics.factory(t)}analytics.load=function(e,t){var n=document.createElement("script");n.type="text/javascript";n.async=!0;n.src="https://cdn.segment.com/analytics.js/v1/"+e+"/analytics.min.js";var a=document.getElementsByTagName("script")[0];a.parentNode.insertBefore(n,a);analytics._loadOptions=t};analytics.SNIPPET_VERSION="4.1.0";
      analytics.load("SB8NYycWISR5bAYZUbKMJcDwUARwF2Uf");
      analytics.page();
    }}();
  </script>
</head>
<body>
  <%=
  Dynamic.loadNav(request, "index")
  %>
  <div class="row" id="headRow">
    <div class="center" id="first-row">
      <div id="body-title"><div><img alt="LASACSClub" src="res/lasacslogo_dark.svg"></div></div>
      <%=
      Dynamic.loadCountdown()
      %>
      <div id="body-description">Official UIL may be canceled, but you can still compete! UIL by TXCSOpen is an online verison of Computer Science UIL created by LASA CS. The format is slightly altered to accommodate for competing remotely. The competition is open from May 7th at 12am to May 8th at 11:59pm. Unlike UIL, students sign up and manage their own team. Check the <a href="rules" class="link">rules</a> page for more information.</div>
      <button id="getStarted" onclick="window.location.href='register';">Get Started</button>
    </div>
  </div>
</body>
</html>