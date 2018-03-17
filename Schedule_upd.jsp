<!-- $Header: /u02/cvsroot/hockeystats/Schedule_upd.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*, java.text.*, java.util.*" isThreadSafe="false" %>

<HTML>

<HEAD>
<TITLE>Jon's AHL - Update - Schedule</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<%!
  String teamName = "All";
  boolean isAdmin;
  ArrayList games = null;
  int i;
  String homeTeamWin = "";
  String visitorTeamWin = "";
  String homeTeamName;
  String visitorTeamName;
  String linkText;
  String context;
%>

<%
  games = Game.getSchedule("ALL", selectedSeasonSession, dbContext);
  session.setAttribute ("hockeystats.gameArray", games);

  String isAdminVal = (String)session.getAttribute("isAdmin");
  isAdmin = isAdminVal != null && isAdminVal.equals("Y");

  if (!isAdmin) {
%>
<jsp:forward page="/notAdmin.jsp" />
<% }

  // Get the date format
  SimpleDateFormat formatter = new SimpleDateFormat ("EEE MMM dd yyyy hh:mm a");
%>

<TABLE BORDER=0 WIDTH=500><CAPTION><H3>Select the game to be updated</CAPTION><H2>
<TH>Date</TH>

<TH>Home</TH>
<TH>Visitor</TH>
<TH>Score</TH>

<%
  if (games == null)
  {
    out.println ("<TD>No games found.</TD></TR>");
  }
  else // games array is not null
  {
    for (i=0; i<games.size(); i++)
    {
      Game thisGame = (Game)games.get(i);
      homeTeamName = thisGame.getHomeTeam().getTeamName();
      visitorTeamName = thisGame.getVisitorTeam().getTeamName();
%>
<TR><TD>

<%
     if (thisGame.getHomeScore() > -1)
     {
%>
<A HREF=UpdateGameStats.jsp?index=<%=i%>>
<%
     }
%>

<%=formatter.format(thisGame.getGameDate())%></A></TD>

<%
      if (thisGame.getHomeScore() > thisGame.getVisitorScore())
      {
        homeTeamWin = "<B>";
        visitorTeamWin = "";
      }
      else if (thisGame.getHomeScore() < thisGame.getVisitorScore())
      {
        homeTeamWin = "";
        visitorTeamWin = "<B>";
      }
      else
      {
        homeTeamWin = "";
        visitorTeamWin = "";
      }
%>

<TD><%=homeTeamWin%><%=homeTeamName%></B></TD>
<TD><%=visitorTeamWin%><%=visitorTeamName%></B></TD>
<%
      if (thisGame.getHomeScore() > -1)
      {
%>
<TD><%=thisGame.getHomeScore()%> - <%=thisGame.getVisitorScore()%></TD>

<%
      } // End, if home score > -1 (ie, the game has been played)
    } // End, for all games
  } // End, else games array is not null
%>

</TABLE>
<%@include file="template_end.html"%>
</BODY>
</HTML>
