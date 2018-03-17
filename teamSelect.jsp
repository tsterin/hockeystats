<!-- $Header: /u02/cvsroot/hockeystats/teamSelect.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page isThreadSafe="false" import="hockeystats.*, java.util.*" session="true" %>

<HTML>
<HEAD>
<TITLE>Select Team</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>
<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<FONT FACE="Modern" SIZE=5><B><CENTER>
<%
  String action = request.getParameter("sel");
  if (action != null && action.equals("gameInfo")) {
%>
Enter GameDay Information<BR>
Select one of the teams which played in the game:
<% } else { %>
Select the team:
<% } %>
</CENTER></B></FONT><BR>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="SelectionServlet" onSubmit="return checkrequired(this)">

<INPUT TYPE="hidden" VALUE="<%=request.getParameter("sel")%>" NAME="selection">
<INPUT TYPE="hidden" VALUE="Team" NAME="selectItem">

<CENTER><SELECT NAME="selectedTeam">
<OPTION SELECTED VALUE="ALL">All teams
<%!  ArrayList teams = null; %>
<%
	teams = hockeystats.Team.getAllTeams(selectedSeasonSession.getSeason(), dbContext);
	for (int i=0; i<teams.size(); i++)
	{
	   String theTeamName = ((Team)teams.get(i)).getTeamName();
           out.println ("<OPTION VALUE=\"" + theTeamName + "\">" + theTeamName);
	}
%>
</SELECT></CENTER>

<CENTER><INPUT TYPE="SUBMIT" VALUE=" Go! "></CENTER>
</FORM>

</CENTER>

<!--End of FORM-->

<%@include file="template_end.html"%>
</BODY>
</HTML>
