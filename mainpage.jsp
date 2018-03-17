<!-- $Header: /u02/cvsroot/hockeystats/mainpage.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*" session="true" isThreadSafe="false" %>

<HTML>

<HEAD>
<TITLE>Jon's AHL</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%><H2>

<!--img src="images/Eagle_Flag.jpg" hspace="100" vspace="50"-->

<%!
  Team[] teams = null;
  Division[] divisions = null;
%>

<%
  // For each division, get the standings for the team.

  SeasonSession selectedSeasonSession =
    (SeasonSession)session.getAttribute("hockeystats.selectedSeasonSession");
  int seasonSessionID = selectedSeasonSession.getSessionID();
  divisions = Division.getAllDivisions(selectedSeasonSession, dbContext);
  for (int division=0; division<divisions.length; division++)
  {
    teams = Team.getTeamsByStanding(divisions[division], seasonSessionID, dbContext);
%>

<TABLE BORDER=1><CAPTION><H2><%=divisions[division].getDivisionName() %> Division</CAPTION><H3>

<TH WIDTH=100>Team</TH>
<TH>W</TH>
<TH>L</TH>
<TH>T</TH>
<TH>W Div</TH>
<TH>L Div</TH>
<TH>T Div</TH>
<TH>Pts</TH>
<TH>GF</TH>
<TH>GA</TH>

<%
    for (int team=0; team<teams.length; team++)
    {
%>

<TR>
<TD><%=teams[team].getTeamName()%></TD>
<TD><%=teams[team].getWinsInDivision(seasonSessionID) + teams[team].getWinsOutDivision(seasonSessionID)%></TD>
<TD><%=teams[team].getLossesInDivision(seasonSessionID) + teams[team].getLossesOutDivision(seasonSessionID)%></TD>
<TD><%=teams[team].getTiesInDivision(seasonSessionID) + teams[team].getTiesOutDivision(seasonSessionID)%></TD>
<TD><%=teams[team].getWinsInDivision(seasonSessionID)%></TD>
<TD><%=teams[team].getLossesInDivision(seasonSessionID)%></TD>
<TD><%=teams[team].getTiesInDivision(seasonSessionID)%></TD>
<TD><%=teams[team].getPoints(seasonSessionID)%></TD>
<TD><%=teams[team].getGoalsFor(seasonSessionID)%></TD>
<TD><%=teams[team].getGoalsAgainst(seasonSessionID)%></TD>
</TR>


<%
    } // End, for all teams
%>

</TABLE><BR><BR>

<%
  } // End, for all divisions
%>

<%@include file="template_end.html"%>
</BODY>
</HTML>
