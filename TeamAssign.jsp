<!-- $Header: /u02/cvsroot/hockeystats/TeamAssign.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%--
This page is used to assign players to teams.
--%>

<%@ page import="hockeystats.*,java.util.*" isThreadSafe="false" %>

<%!
  String teamName;
  boolean isAdmin;
  ArrayList players = null;
  Team[] teams = null;
  Iterator p_iter;
  Iterator t_iter;
  Player aPlayer;
  Team aTeam;
  int i;
%>

<HTML>

<HEAD>
<TITLE>Jon's AHL - Team Assignment Tool</TITLE>
<META name="description" content="">
<META name="keywords" content="">
</HEAD>

<%@include file="template.jsp"%>
<%@include file="reqFields.js"%>

<%
  String isAdminAttr = (String)session.getAttribute("isAdmin");
  if (isAdminAttr != null && isAdminAttr.equals("Y"))
  {
    isAdmin = true;
  }

  if (!isAdmin) {
%>
<jsp:forward page="/notAdmin.jsp" />
<% }

  players = Player.getAllPlayers(dbContext);
  teams = Team.getAllTeams(selectedSeasonSession, dbContext);
%>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<TABLE ><CAPTION><H3>Assign players to teams</CAPTION><H2>
<TH>First Name</TH>
<TH>Last Name</TH>
<TH>Team</TH>

<%
  if (players == null)
  {
%>
<TD>No players found.</TD>
<%
  }
  else if (teams == null)
  {
%>
<TD>No teams found.</TD>
<%
  }
  else // players ArrayList and teams Array are not null
  {
    for (p_iter=players.iterator(), i=0; p_iter.hasNext();i++)
    {
      aPlayer=(Player)p_iter.next();
%>
<TR>
<TD><%=aPlayer.getFName()%></TD>
<TD><%=aPlayer.getLName()%></TD>
<TD>
<%
      for (i=0; i<teams.length; i++)
      {
        aTeam = teams[i];
%>
<INPUT TYPE="radio" NAME="Player<%=i%>" VALUE="<%=aTeam.getTeamName()%>"> <%=aTeam.getTeamName()%><BR>
<%
      } // End, for all teams
%>
<INPUT TYPE="radio" NAME="Player<%=i%> VALUE="Not placed"> Not Placed
</TD>
<%
    } // End, for all players
  } // End, else - players ArrayList is not null
%>
</TABLE>
<%@include file="template_end.html"%>
</BODY>
</HTML>
