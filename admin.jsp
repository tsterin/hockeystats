<!-- $Header: /u02/cvsroot/hockeystats/admin.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%
  String isAdmin = (String) session.getAttribute("isAdmin");
  if (isAdmin == null || !isAdmin.equals("Y"))
  {
%>

<jsp:forward page="notAdmin.jsp" >
</jsp:forward>
<%
  }
%>


<HTML>
<HEAD>
<TITLE>Site Administration</TITLE>
<META name="description" content="">
<META name="keywords" content="">
</HEAD>
<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<FONT FACE="Modern" SIZE=5><B><CENTER>Site Administration Options</CENTER></B></FONT><BR>
<BR>
<BR>
<A HREF="teamSelect.jsp?sel=gameInfo">Enter game-day information</A><BR><BR>
<A HREF="EditMOTD.jsp">Change the message-of-the-day</A><BR><BR>
<A HREF="Game.jsp">Add a new game</A><BR><BR>
<A HREF="Team.jsp?action=add">Add a new team</A><BR><BR>
<A HREF="teamSelect.jsp?sel=modifyTeam">Change info for a team</A><BR><BR>
<A HREF="teamSelect.jsp?sel=pickPlayers">Select players for a team</A><BR><BR>
<A HREF="Player.jsp?action=add">Add a new player</A><BR><BR>
<A HREF="Division.jsp">Add a new division</A><BR><BR>
<A HREF="Season.jsp">Create a new season</A><BR><BR>
<A HREF="Division.jsp">Add a new division</A><BR><BR>
<A HREF="seasonSelection.jsp?sel=currentSeason">Change current season</A><BR><BR>
<A HREF="Schedule_upd.jsp">Update game-day information</A><BR><BR>
<%@include file="template_end.html"%>
</BODY>
</HTML>
