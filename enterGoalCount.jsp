<!-- $Header: /u02/cvsroot/hockeystats/enterGoalCount.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<!--
This page is used to enter the number of goals and penalties in the game.  We do this so 
that on the next page we can have the right number of blanks for the user to fill in
who got the all goals and assists, rather than entering each goal on a separate page.
-->

<%@ page isThreadSafe="false" import="hockeystats.*" session="true" %>

<HTML>
<HEAD>
<TITLE>Enter Number of Goals and Penalties</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<%!
  Player[] players = null;
%>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="GameEntryServlet" onSubmit="return checkrequired(this)">

<%
  Team homeTeam = (Team)session.getAttribute("hockeystats.homeTeam");
  Team visitorTeam = (Team)session.getAttribute("hockeystats.visitorTeam");
%>

<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=5>
<TR><TD>
Number of goals scored by <%=homeTeam.getTeamName()%>
</TD><TD>
<INPUT TYPE="TEXT" NAME="req_homeGoals" VALUE="0" maxlength="2" size="2" onBlur="valid_num(this)">
</TD></TR>

<TR><TD>
Number of goals scored by <%=visitorTeam.getTeamName()%>
</TD><TD>
<INPUT TYPE="TEXT" NAME="req_visitorGoals" VALUE="0" maxlength="2" size="2" onBlur="valid_num(this)">
</TD></TR>

<TR><TD>
Number of penalties on <%=homeTeam.getTeamName()%>
</TD><TD>
<INPUT TYPE="TEXT" NAME="req_homePenalties" VALUE="0" maxlength="2" size="2" onBlur="valid_num(this)">
</TD><TD>

<TR><TD>
Number of penalties on <%=visitorTeam.getTeamName()%>
</TD><TD>
<INPUT TYPE="TEXT" NAME="req_visitorPenalties" VALUE="0" maxlength="2" size="2" onBlur="valid_num(this)">
</TD></TR>

</TABLE>

<BR><BR>

<INPUT TYPE="HIDDEN" NAME="phase" VALUE="enterGoalCounts">

<FONT SIZE=3 /><CENTER><INPUT TYPE="SUBMIT" VALUE=" Next "></CENTER>
</FORM>

<!--End of FORM-->

<%@include file="template_end.html"%>
</BODY>
</HTML>
