<%--
  Created by IntelliJ IDEA.
  User: trappist1
  Date: 3/8/20
  Time: 7:58 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page import="Outlet.Dynamic"  contentType="text/html;charset=UTF-8"%>
<html lang="en" class="no-js">
<head>
  <title>Example</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="./css/bootstrap.min.css">
  <link rel="stylesheet" href="./css/style.css">
  <link rel="stylesheet" href="./css/index.css">
  <link href="https://fonts.googleapis.com/css?family=Lato&display=swap" rel="stylesheet">
  <script src="./js/jquery.min.js"></script>
</head>
<body>
  <%=
  Dynamic.loadNav(request)
  %>
  <div class="row">
    <div class="center" id="first-row">
      <div id="body-title">TXCSOpen</div>
      <div id="body-description">TXCSOpen is a free computer science created to inspire middle and high school students to pursue careers in advanced STEM fields. Unlike other algorithmic competitions, teams receive a single np-hard problem and corresponding test case with a week to design and implement a heuristic. This encourages competitors to practice SDLCs, git, and communication software, simulating a professional workplace environment.</div>
    </div>
  </div>
  <div class="row info_row">
    <div class="info">
      <p class="info_title">Register and Create A Team</p>
      <p class="info_body">Once registration opens, create an account with your email and custom username--don’t worry, we won’t share your data. Create or join a team of up to five people to compete! Many teams are eligible to win prizes; read more about eligibility and prizes here.</p>
    </div>
    <img class="stock_pic" src="res/stock_photo.jpg"/>
  </div>
  <div class="row info_row">
    <div class="info">
      <p class="info_title">Compete</p>
      <p class="info_body">The competition lasts for a week from May 20th to May 27th, 2019. This is to give more people the opportunity to compete; you won’t be severely disadvantaged if you are unable to dedicate the week to TXCS.</p>
    </div>
    <img class="stock_pic" src="res/stock_photo.jpg"/>
  </div>
  <div class="row info_row">
    <div class="info">
      <p class="info_title">The Challenge</p>
      <p class="info_body">As a heuristic-based competition, TXCS_Open will challenge you to build the most successful solution to an impossible problem. Read more about np-hard problems here, and the many roles they play in our modern world.</p>
    </div>
    <img class="stock_pic" src="res/stock_photo.jpg"/>
  </div>
  <div class="row info_row">
    <div class="info">
      <p class="info_title">Reach Us</p>
      <p class="info_body">Join the competition discord to find teams, contact the moderators, and share your feedback. As usual, there are a few rules of discussion which you can read up more about here. We are always interested in what competitors are thinking, and we will be releasing a post-competition survey once scores are finalized.
      </p>
    </div>
    <img class="stock_pic" src="res/stock_photo.jpg"/>
  </div>
</body>
</html>