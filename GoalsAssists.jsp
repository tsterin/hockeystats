<!-- $Header: /u02/cvsroot/hockeystats/GoalsAssists.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<!-- This page displays Goals and Assists Leaders for the league. -->

<%@ page import="hockeystats.*, java.util.*" isThreadSafe="false" %>

<HTML>

<HEAD>
<TITLE>Goals/Assists Leaders</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<%!
  ArrayList players = null;
  Player aPlayer;
%>

<TABLE BORDER=1><CAPTION><H2>Goals Leaders</CAPTION><H3>

<TH WIDTH=140>Name</TH>
<TH WIDTH=110>Team</TH>
<TH>G</TH>
<TH>A</TH>
<TH>Pts</TH>
<TH>GP</TH>

<%
  SeasonSession selectedSeasonSession =
    (SeasonSession)session.getAttribute("hockeystats.selectedSeasonSession");
  players = Player.getPlayersByGoalCount(selectedSeasonSession, dbContext);
  for (Iterator i=players.iterator(); i.hasNext();)
  {
    aPlayer = (Player)i.next();
    Team team = aPlayer.getTeam();
    String teamName = team == null ? "NOT PLACED" : team.getTeamName();
    int ytdGoals = aPlayer.getGoals(selectedSeasonSession);
    int ytdAssists = aPlayer.getAssists(selectedSeasonSession);
    int points = ytdGoals + ytdAssists;
%>

<TR>
<TD WIDTH=140><%=aPlayer.getFName()%> <%=aPlayer.getLName()%></TD>
<TD WIDTH=110><%=teamName%></TD>
<TD><%=ytdGoals%></TD>
<TD><%=ytdAssists%></TD>
<TD><%=points%></TD>
<TD><%=aPlayer.getGamesPlayed(selectedSeasonSession)%></TD>
</TR>


<%
    } // End, for all players
%>

</TABLE><BR><BR>

<TABLE BORDER=1><CAPTION><H2>Assists Leaders</CAPTION><H3>

<TH WIDTH=140>Name</TH>
<TH WIDTH=100>Team</TH>
<TH>G</TH>
<TH>A</TH>
<TH>Pts</TH>
<TH>GP</TH>

<%
  players = Player.getPlayersByAssistCount(selectedSeasonSession, dbContext);
  for (Iterator i=players.iterator(); i.hasNext();)
  {
    aPlayer = (Player)i.next();
    Team team = aPlayer.getTeam();
    String teamName = team == null ? "NOT PLACED" : team.getTeamName();
    int ytdGoals = aPlayer.getGoals(selectedSeasonSession);
    int ytdAssists = aPlayer.getAssists(selectedSeasonSession);
    int points = ytdGoals + ytdAssists;
%>

<TR>
<TD><%=aPlayer.getFName()%> <%=aPlayer.getLName()%></TD>
<TD><%=teamName%></TD>
<TD><%=ytdGoals%></TD>
<TD><%=ytdAssists%></TD>
<TD><%=points%></TD>
<TD><%=aPlayer.getGamesPlayed(selectedSeasonSession)%></TD>
</TR>


<%
    } // End, for all players
%>

</TABLE><BR><BR>

<%@include file="template_end.html"%>
</BODY>
</HTML>
