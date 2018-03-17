<!-- $Header: /u02/cvsroot/hockeystats/pickGoalies.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<!--
This page is used to identify who the goalies were for each team.
-->

<%@ page isThreadSafe="false" import="hockeystats.*, java.util.*" session="true" %>

<HTML>
<HEAD>
<TITLE>Pick Goalies</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<%
  GameEntry newGame = (GameEntry)session.getAttribute("hockeystats.newGame");
  Team homeTeam = (Team)session.getAttribute("hockeystats.homeTeam");
  Team visitorTeam = (Team)session.getAttribute("hockeystats.visitorTeam");
  ArrayList players;
  Player aPlayer;
  int homeGoals = newGame.getHomeGoalCount();
  int visitorGoals = newGame.getVisitorGoalCount();
  int goal=0; // Index
  int playerIndex; // Index, used to identify players in servlet
  Iterator iter;
%>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="GameEntryServlet" onSubmit="return checkrequired(this)">

<H3>Identify goalie for <%=homeTeam.getTeamName()%> in this game</H3><NORMAL>
<BR>
<%
  players = newGame.getHomeTeamPlayers();
%>
<SELECT NAME="homeGoalie">
<%
    for (iter=players.iterator(), playerIndex=0; iter.hasNext(); playerIndex++) {
      aPlayer = (Player)iter.next();
%>
<OPTION VALUE="<%=playerIndex%>"><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName()%>
<% } %>
</SELECT>


<H3><BR><BR>Identify goalie for <%=visitorTeam.getTeamName()%> in this game</H3><NORMAL>
<BR>
<%
  players = newGame.getVisitorTeamPlayers();
%>
<SELECT NAME="visitorGoalie">
<%
    for (iter=players.iterator(), playerIndex=0; iter.hasNext(); playerIndex++) {
      aPlayer = (Player)iter.next();
%>
<OPTION VALUE="<%=playerIndex%>"><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName()%>
<% } %>
</SELECT>

<BR><BR>

<INPUT TYPE="HIDDEN" NAME="phase" VALUE="pickGoalies">

<FONT SIZE=3 /><CENTER><INPUT TYPE="SUBMIT" VALUE=" Next "></CENTER>
</FORM>

<!--End of FORM-->

<%@include file="template_end.html"%>
</BODY>
</HTML>
