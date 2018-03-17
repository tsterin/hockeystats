<!-- $Header: /u02/cvsroot/hockeystats/enterGoals.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<!--
This page is used to enter goals and assists in the game.
-->

<%@ page isThreadSafe="false" import="hockeystats.*, java.util.*" session="true" %>

<HTML>
<HEAD>
<TITLE>Enter Goals and Assists</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<%
  GameEntry newGame = (GameEntry)session.getAttribute("hockeystats.newGame");
  Team homeTeam = (Team)session.getAttribute("hockeystats.homeTeam");
  Team visitorTeam = (Team)session.getAttribute("hockeystats.visitorTeam");
  ArrayList players;
  Player aPlayer;
  int homeGoals = newGame.getHomeGoalCount();
  int visitorGoals = newGame.getVisitorGoalCount();
  int goal=0; // Index
  int playerIndex; // Index, used to identify players in servlet
  Iterator iter;
%>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="GameEntryServlet" onSubmit="return checkrequired(this)">

<H3>Goals by <%=homeTeam.getTeamName()%><NORMAL>
<BR><BR>
<TABLE WIDTH=750 BORDER=0 CELLSPACING=0 CELLPADDING=0>
<TR><TH width=5%>Goal</TH><TH width=22%>Scored by</TH><TH width=22%>Assist 1</TH>
<TH width=22%>Assist 2</TH><TH width=12%>Period</TH><TH width=17%>Time</TH></TR>
<%
  players = newGame.getHomeTeamPlayers();
%>
<%
  for (goal=0; goal<homeGoals; goal++)
  {
%>
<TR>
<TD><%=goal+1%></TD>
<TD>
<SELECT NAME="homeScoredBy<%=goal%>">
<%
    for (iter=players.iterator(), playerIndex=0; iter.hasNext(); playerIndex++) {
      aPlayer = (Player)iter.next();
%>
<OPTION VALUE="<%=playerIndex%>"><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName()%>
<%
    }
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="home1Assist<%=goal%>">
<OPTION VALUE="-1" SELECTED>None
<%
    for (iter=players.iterator(), playerIndex=0; iter.hasNext(); playerIndex++) {
      aPlayer = (Player)iter.next();
%>
<OPTION VALUE="<%=playerIndex%>"><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName()%>
<%
    }
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="home2Assist<%=goal%>">
<OPTION VALUE="-1" SELECTED>None
<%
    for (iter=players.iterator(), playerIndex=0; iter.hasNext(); playerIndex++) {
      aPlayer = (Player)iter.next();
%>
<OPTION VALUE="<%=playerIndex%>"><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName()%>
<%
    }
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="home_period<%=goal%>">
<OPTION VALUE="1" SELECTED> 1
<OPTION VALUE="2"> 2
<OPTION VALUE="3"> 3
<OPTION VALUE="4"> OT
</SELECT>
</TD>

<TD>
<INPUT TYPE="text" NAME="reqnum_home_minutes<%=goal%>" MAXLENGTH=2 SIZE=2 onBlur="valid_min(this)">:
<INPUT TYPE="text" NAME="reqnum_home_seconds<%=goal%>" MAXLENGTH=2 SIZE=2 onBlur="valid_sec(this)">
</TD>

</TR>
<%
  } // End, for all goals
%>

</TABLE>

<BR><BR><BR>

<H3>Goals by <%=visitorTeam.getTeamName()%><NORMAL>
<BR><BR>
<TABLE WIDTH=750 BORDER=0 CELLSPACING=0 CELLPADDING=0>
<TR><TH width=5%>Goal</TH><TH width=22%>Scored by</TH><TH width=22%>Assist 1</TH>
<TH width=22%>Assist 2</TH><TH width=12%>Period</TH><TH width=17%>Time</TH></TR>
<%
  players = newGame.getVisitorTeamPlayers();
%>
<%
  for (goal=0; goal<visitorGoals; goal++)
  {
%>
<TR>
<TD><%=goal+1%></TD>
<TD>
<SELECT NAME="visitorScoredBy<%=goal%>">
<%
    for (iter=players.iterator(), playerIndex=0; iter.hasNext(); playerIndex++) {
      aPlayer = (Player)iter.next();
%>
<OPTION VALUE="<%=playerIndex%>"><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName()%>
<%
    }
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="visitor1Assist<%=goal%>">
<OPTION VALUE="-1" SELECTED>None
<%
    for (iter=players.iterator(), playerIndex=0; iter.hasNext(); playerIndex++) {
      aPlayer = (Player)iter.next();
%>
<OPTION VALUE="<%=playerIndex%>"><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName()%>
<%
    }
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="visitor2Assist<%=goal%>">
<OPTION VALUE="-1" SELECTED>None
<%
    for (iter=players.iterator(), playerIndex=0; iter.hasNext(); playerIndex++) {
      aPlayer = (Player)iter.next();
%>
<OPTION VALUE="<%=playerIndex%>"> <%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName()%>
<%
    }
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="visitor_period<%=goal%>">
<OPTION VALUE="1" SELECTED> 1
<OPTION VALUE="2"> 2
<OPTION VALUE="3"> 3
<OPTION VALUE="4"> OT
</SELECT>
</TD>

<TD>
<INPUT TYPE="text" NAME="reqnum_visitor_minutes<%=goal%>" MAXLENGTH=2 SIZE=2 onBlur="valid_min(this)">:
<INPUT TYPE="text" NAME="reqnum_visitor_seconds<%=goal%>" MAXLENGTH=2 SIZE=2 onBlur="valid_sec(this)">
</TD>

</TR>
<%
  } // End, for all goals
%>

</TABLE>

<BR><BR>

<INPUT TYPE="HIDDEN" NAME="phase" VALUE="enterGoals">

<FONT SIZE=3 /><CENTER><INPUT TYPE="SUBMIT" VALUE=" Next "></CENTER>
</FORM>

<!--End of FORM-->

<%@include file="template_end.html"%>
</BODY>
</HTML>
