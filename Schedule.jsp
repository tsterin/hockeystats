<!-- $Header: /u02/cvsroot/hockeystats/Schedule.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*, java.text.*, java.util.*, java.net.*" isThreadSafe="false" %>

<HTML>

<HEAD>
<TITLE>Jon's AHL - Schedule</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<%!
  String teamName;
  boolean isAdmin;
  Game[] games = null;
  int i;
  String homeTeamWin = "";
  String visitorTeamWin = "";
  String homeTeamName;
  String visitorTeamName;
  String linkText;
  String context;
%>

<%
  // Get the name of the team whose schedule we'll display.  If the name does not come
  // in as a parameter then we are being re-called after changing some game information.
  // In this case, we'll get the team name from the session.
  teamName = request.getParameter("selectedTeam");
  if (teamName == null)
    teamName = (String) session.getAttribute("hockeystats.selectedTeamName");
  ArrayList schedule = (ArrayList)session.getAttribute ("hockeystats.schedule");

  context = request.getParameter("sel");

  String isAdminVal = (String)session.getAttribute("isAdmin");

  isAdmin = isAdminVal != null && isAdminVal.equals("Y");

  // Get the date format
  SimpleDateFormat formatter = new SimpleDateFormat ("EEE MMM dd yyyy hh:mm a");
%>

<TABLE BORDER=0 WIDTH=500><CAPTION><H3>Schedule for <%= teamName %></CAPTION><H2>
<TH>Date</TH>

<%
  if (teamName.equals("ALL"))
  {
%>
<TH>Home</TH>
<TH>Visitor</TH>
<TH>Score</TH>
<%
  }
  else
  {
%>
<TH>Opponent</TH>
<TH>Score</TH>
<%
  }

  if (schedule == null)
  {
    out.println ("<TD>No games found.</TD></TR>");
  }
  else // schedule array is not null
  {
    for (i=0; i<schedule.size(); i++)
    {
      Game thisGame = (Game)schedule.get(i);
      homeTeamName = thisGame.getHomeTeam().getTeamName();
      visitorTeamName = thisGame.getVisitorTeam().getTeamName();

      if (context != null && context.equals("gameInfo") && thisGame.getHomeScore() == -1)
        linkText = "playerSelect.jsp?home=" + URLEncoder.encode(homeTeamName) + "&visitor=" +
	 URLEncoder.encode(visitorTeamName) + "&";
      else if (thisGame.getHomeScore() == -1)
        linkText = "Game.jsp?";
      else
        linkText = "GameStats.jsp?";    
%>
<TR><TD>

<%
      if (isAdmin || thisGame.getHomeScore() > -1)
      {
%>
<A HREF=<%=linkText%>index=<%=i%>>
<%
      }
%>
<%=formatter.format(thisGame.getGameDate())%></A></TD>

<%
      if (teamName.equals("ALL"))
      {
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
      }
      else
      {
%>
<TD><%=homeTeamName.equals(teamName) ? "vs " + visitorTeamName : "at " + homeTeamName %></TD>
<TD>

<%
        if ((thisGame.getHomeScore() > thisGame.getVisitorScore() && homeTeamName.equals(teamName)) ||
            (thisGame.getHomeScore() < thisGame.getVisitorScore() && visitorTeamName.equals(teamName)))
        {
%>

W <%=thisGame.getHomeScore()%> - <%=thisGame.getVisitorScore()%>

<%
        }
        else if ((thisGame.getHomeScore() > thisGame.getVisitorScore() && visitorTeamName.equals(teamName)) ||
                 (thisGame.getHomeScore() < thisGame.getVisitorScore() && homeTeamName.equals(teamName)))
        {
%>

L <%=thisGame.getHomeScore()%> - <%=thisGame.getVisitorScore()%>

<%
        }
        else if (thisGame.getHomeScore() == thisGame.getVisitorScore() &&
                 thisGame.getHomeScore() > -1)
        {
%>

T <%=thisGame.getHomeScore()%> - <%=thisGame.getVisitorScore()%>

<%
        } // End, if (checking for winner)
      } // End, else teamName is not "ALL"
    } // End, for all games
  } // End, else games array is not null
%>

</TABLE>
<%@include file="template_end.html"%>
</BODY>
</HTML>
