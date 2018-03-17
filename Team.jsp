`<!-- $Header: /u02/cvsroot/hockeystats/Team.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*" session="true" isThreadSafe="false" %>

<%!
   boolean isAdmin = false;
   boolean addingTeam = true;  // if false then we are modifying a team
   String modTeamName;         // if modifying a team, name of the team
   Team team = null;
   int seasonSessionID;

   String emptyString = "";
%>

<%
  if (session.isNew())
  {
%>
<jsp:forward page="sessionTimeout.jsp" />
<%
  }

  SeasonSession selectedSeasonSession =
    (SeasonSession)session.getAttribute("hockeystats.selectedSeasonSession");
  seasonSessionID = selectedSeasonSession.getSessionID();

  String isAdminAttr = (String)session.getAttribute("isAdmin");
  if (isAdminAttr != null && isAdminAttr.equals("Y"))
  {
    isAdmin = true;
  }

  String actionParam = request.getParameter("action");
  if (actionParam == null || !actionParam.equals("add"))
  {
    modTeamName = request.getParameter("teamName");
    if (modTeamName != null)
    {
      // We are modifying an existing team.  Get the team info.
      team = new Team(modTeamName, dbContext);
      addingTeam = false;
    }
  }
  else
    addingTeam = true;
%>

<HTML>
<HEAD>
<TITLE><%= addingTeam ? "New" : "Update" %> Team</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>
<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<FONT FACE="Modern" SIZE=5><B><CENTER><%= addingTeam ? "Add a" : "Update a" %> team</CENTER></B></FONT><BR>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="DataServlet" onSubmit="return checkrequired(this)">

<INPUT TYPE="hidden" NAME=sourcePage VALUE="team">

<TABLE WIDTH=70% BORDER=0 CELLSPACING=0 CELLPADDING=5>
<TR>
<TD>Season:</TD>
<TD><SELECT NAME="season">
<OPTION VALUE="Current">Current
<%!  hockeystats.Season[] seasons = null; %>
<%
	seasons = hockeystats.Season.getAllSeasons(dbContext);
	for (int i=0; i<seasons.length; i++)
	{
	   String theSeasonName = seasons[i].getSeasonName();
           out.println ("<OPTION VALUE=\"" + theSeasonName + "\" " + (addingTeam ? emptyString : team.getSeason().getSeasonName().equals(theSeasonName) ? "SELECTED" : emptyString) + ">" + theSeasonName);
	}
%>
</SELECT>
</TD></TR>
<TR>
<TD WIDTH=350>Team Name:</TD>
<TD><FONT COLOR="RED">*</FONT>
<INPUT TYPE="text" NAME="req_TeamName" VALUE="<%= addingTeam ? emptyString : team.getTeamName()%>" maxlength="25" size="25"></TD></TR>
<TR>
<TD>Color:</TD>
<TD><FONT COLOR="RED">*</FONT>
<INPUT TYPE="text" NAME="req_color" VALUE="<%= addingTeam ? emptyString : team.getTeamColor()%>" maxlength="20" size="20"></TD></TR>
<TR>
<TD>Division:</TD>
<TD><SELECT NAME="req_division">
<OPTION VALUE="" <%= addingTeam ? "SELECTED" : emptyString %>>Not placed
<%!  hockeystats.Division[] divisions = null; %>
<%
	divisions = hockeystats.Division.getAllDivisions(selectedSeasonSession, dbContext);
	for (int i=0; i<divisions.length; i++)
	{
	   String theDivisionName = divisions[i].getDivisionName();
           out.println ("<OPTION VALUE=\"" + theDivisionName + "\" " + (addingTeam ? emptyString : team.getDivision().getDivisionName().equals(theDivisionName) ? "SELECTED" : emptyString) + ">" + theDivisionName);
	}
%>
</SELECT>
</TD></TR>
<TR>
<TD>Wins in division:</TD>
<TD><INPUT TYPE="text" NAME="winsInDivision" VALUE="<%= addingTeam ? "0" : Integer.toString(team.getWinsInDivision(seasonSessionID))%>" maxlength="2" size="2" onBlur="valid_num(this)"></TD></TR>
<TR>
<TD>Wins outside division:</TD>
<TD><INPUT TYPE="text" NAME="winsOutDivision" VALUE="<%= addingTeam ? "0" : Integer.toString(team.getWinsOutDivision(seasonSessionID))%>" maxlength="2" size="2" onBlur="valid_num(this)"></TD></TR>
<TR>
<TD>Losses in division:</TD>
<TD><INPUT TYPE="text" NAME="lossesInDivision" VALUE="<%= addingTeam ? "0" : Integer.toString(team.getLossesInDivision(seasonSessionID))%>" maxlength="2" size="2" onBlur="valid_num(this)"></TD></TR>
<TR>
<TD>Losses outside division:</TD>
<TD><INPUT TYPE="text" NAME="lossesOutDivision" VALUE="<%= addingTeam ? "0" : Integer.toString(team.getLossesOutDivision(seasonSessionID))%>" maxlength="2" size="2" onBlur="valid_num(this)"></TD></TR>
<TR>
<TD>Ties in division:</TD>
<TD><INPUT TYPE="text" NAME="tiesInDivision" VALUE="<%= addingTeam ? "0" : Integer.toString(team.getTiesInDivision(seasonSessionID))%>" maxlength="2" size="2" onBlur="valid_num(this)"></TD></TR>
<TR>
<TD>Ties outside division:</TD>
<TD><INPUT TYPE="text" NAME="tiesOutDivision" VALUE="<%= addingTeam ? "0" : Integer.toString(team.getTiesOutDivision(seasonSessionID))%>" maxlength="2" size="2" onBlur="valid_num(this)"></TD></TR>
<TR>
<TD>Goals For:</TD>
<TD><INPUT TYPE="text" NAME="goalsFor" VALUE="<%= addingTeam ? "0" : Integer.toString(team.getGoalsFor(seasonSessionID))%>" maxlength="4" size="4" onBlur="valid_num(this)"></TD></TR>
<TR>
<TD>Goals Against:</TD>
<TD><INPUT TYPE="text" NAME="goalsAgainst" VALUE="<%= addingTeam ? "0" : Integer.toString(team.getGoalsAgainst(seasonSessionID))%>" maxlength="4" size="4" onBlur="valid_num(this)"></TD></TR>
</TD></TR>

</TABLE>
<BR>
Display in standings?<INPUT NAME="displayInStandings" TYPE="checkbox" VALUE="Y" <%= addingTeam || team.isDisplayedInStandings() ? "CHECKED" : "" %>><BR>
<br><br>
<CENTER><INPUT TYPE="SUBMIT" VALUE=" <%= addingTeam ? "Add " : "Update " %> Team "></CENTER>
</FORM>

</CENTER>

<!--End of FORM-->

<BR><BR><A HREF="admin.jsp">Return to main admin page</A>

<%@include file="template_end.html"%>
</BODY>
</HTML>
