<!-- $Header: /u02/cvsroot/hockeystats/PenaltiesGoalie.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="java.util.*,hockeystats.*" isThreadSafe="false" %>

<HTML>

<HEAD>
<TITLE>Penalties and Points</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="Black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<%!
  ArrayList players = null;
  Player aPlayer;
%>

<TABLE BORDER=1><CAPTION><H2>Penalty-minutes Leaders</CAPTION><H3>

<TH WIDTH=140>Name</TH>
<TH WIDTH=110>Team</TH>
<TH>Pen</TH>
<TH>Min</TH>
<TH>GP</TH>

<%
  SeasonSession selectedSeasonSession =
    (SeasonSession)session.getAttribute("hockeystats.selectedSeasonSession");
  players = Player.getPlayersByPenaltyMinutes(selectedSeasonSession, dbContext);
  for (Iterator i=players.iterator(); i.hasNext();)
  {
    aPlayer = (Player)i.next();
    Team team = aPlayer.getTeam();
    String teamName = team == null ? "NOT PLACED" : team.getTeamName();
    int penalties = aPlayer.getPenalties(selectedSeasonSession);
    int penaltyMinutes = aPlayer.getPenaltyMinutes(selectedSeasonSession);
%>

<TR>
<TD WIDTH=140><%=aPlayer.getFName()%> <%=aPlayer.getLName()%></TD>
<TD WIDTH=110><%=teamName%></TD>
<TD><%=penalties%></TD>
<TD><%=penaltyMinutes%></TD>
<TD><%=aPlayer.getGamesPlayed(selectedSeasonSession)%></TD>
</TR>


<%
    } // End, for all players
%>

</TABLE><BR><BR>

<TABLE BORDER=1><CAPTION><H2>Points Leaders</CAPTION><H3>

<TH WIDTH=140>Name</TH>
<TH WIDTH=100>Team</TH>
<TH>G</TH>
<TH>A</TH>
<TH>Pts</TH>
<TH>GP</TH>

<%
  players = Player.getPlayersByPoints(selectedSeasonSession, dbContext);
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

<TABLE BORDER=1><CAPTION><H2>Goalie Stats</CAPTION><H3>

<TH WIDTH=140>Name</TH>
<TH WIDTH=100>Team</TH>
<TH>GA</TH>
<TH>W</TH>
<TH>L</TH>
<TH>T</TH>
<TH>GAA</TH>

<%
  players = Player.getGoalies(selectedSeasonSession, dbContext);
  for (Iterator i=players.iterator(); i.hasNext();)
  {
    aPlayer = (Player)i.next();
    Team team = aPlayer.getTeam();
    String teamName = team == null ? "NOT PLACED" : team.getTeamName();
    int goalsAgainst = aPlayer.getGoalsAgainst(selectedSeasonSession);
    int gamesWon = aPlayer.getGoalieGamesWon(selectedSeasonSession);
    int gamesLost =aPlayer.getGoalieGamesLost(selectedSeasonSession);
    int gamesTied = aPlayer.getGoalieGamesTied(selectedSeasonSession);
    float totalGames = gamesWon + gamesLost + gamesTied;

    java.text.NumberFormat formatter = java.text.NumberFormat.getInstance();
    formatter.setMaximumFractionDigits(3);
    formatter.setMinimumFractionDigits(3);
    String gaa = formatter.format(goalsAgainst / totalGames);
%>

<TR>
<TD><%=aPlayer.getFName()%> <%=aPlayer.getLName()%></TD>
<TD><%=teamName%></TD>
<TD><%=goalsAgainst%></TD>
<TD><%=gamesWon%></TD>
<TD><%=gamesLost%></TD>
<TD><%=gamesTied%></TD>
<TD><%=gaa%></TD>
</TR>


<%
    } // End, for all players
%>

</TABLE><BR><BR>

<%@include file="template_end.html"%>
</BODY>
</HTML>
