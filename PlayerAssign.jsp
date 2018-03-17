<!-- $Header: /u02/cvsroot/hockeystats/PlayerAssign.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%--
This page is used to assign players to a specific team.
--%>

<%@ page import="hockeystats.*,java.util.*" isThreadSafe="false" %>

<%!
  String teamName;
  boolean isAdmin;
  ArrayList players = null;
  Iterator p_iter;
  Player aPlayer;
  Team theTeam;
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

  // Get the name of the team whose roster we're assigning, then display players.

  teamName = request.getParameter("selectedTeam");
  theTeam = new Team(teamName, dbContext);

  players = Player.getAllPlayers(dbContext);
%>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<!--Start of FORM-->

<FORM METHOD=POST ACTION="DataServlet" onSubmit="return checkrequired(this)">

<INPUT TYPE="hidden" NAME=sourcePage VALUE="playerSelect">
<INPUT TYPE="hidden" NAME=teamName VALUE="<%=teamName%>">

<TABLE ><CAPTION><H3>Assign players to team <%=theTeam.getTeamName()%></CAPTION><H2>
<TH>Select</TH>
<TH>First Name</TH>
<TH>Last Name</TH>
<TH>Current team</TH>
<%
  if (players == null)
  {
%>
<TD>No players found.</TD>
<%
  }
  else // players ArrayList is not null
  {
    for (p_iter=players.iterator(), i=0; p_iter.hasNext();i++)
    {
      aPlayer=(Player)p_iter.next();
%>
<TR>
<TD><INPUT TYPE=checkbox NAME="Player<%=i%>" VALUE="<%=i%>"></TD>
<TD><%=aPlayer.getFName()%></TD>
<TD><%=aPlayer.getLName()%></TD>
<TD><%=aPlayer.getTeam() == null ? "Not Placed" : aPlayer.getTeam().getTeamName()%></TD>
</TR>
<%
   } // End, for all players
  } // End, players arraylist is not empty
%>

</TABLE>
<CENTER><H4><INPUT TYPE="SUBMIT" NAME="SUBMIT" VALUE=" Save "> <INPUT TYPE="SUBMIT" VALUE=" Cancel "></CENTER>
</FORM>
<%@include file="template_end.html"%>
</BODY>
</HTML>
