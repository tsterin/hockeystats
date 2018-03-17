<!-- $Header: /u02/cvsroot/hockeystats/enterPenalties.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<!--
This page is used to enter any penalties assessed during the game.
-->

<%@ page isThreadSafe="false" import="hockeystats.*, java.util.*" session="true" %>

<HTML>
<HEAD>
<TITLE>Enter Penalties</TITLE>
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
  int homePenalties = newGame.getHomePenalties();
  int visitorPenalties = newGame.getVisitorPenalties();
  int penalty=0; // Index
  int playerIndex; // Index, used to identify players in servlet
  Iterator iter;
  Map.Entry penaltyMap;
  String penaltyText;
  Integer penaltyID;
%>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="GameEntryServlet" onSubmit="return checkrequired(this)">

<H3>Penalties to <%=homeTeam.getTeamName()%><NORMAL>
<BR><BR>
<TABLE WIDTH=780 BORDER=0 CELLSPACING=0 CELLPADDING=0>
<TR><TH width=5%>Penalty</TH><TH width=30%>Penalty to</TH><TH width=25%>Penalty for</TH>
<TH width=5%>Minutes</TH><TH width=5%>Period</TH><TH width=30%>Time</TH></TR>

<%
  players = newGame.getHomeTeamPlayers();
%>
<%
  for (penalty=0; penalty<homePenalties; penalty++)
  {
%>
<TR>
<TD><%=penalty+1%></TD>
<TD>
<SELECT NAME="homePenalty<%=penalty%>">
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
<SELECT NAME="homeInfraction<%=penalty%>">
<%
    HashMap penaltyNames = Penalty.getPenaltyNames(dbContext);
    Set penaltyMapSet = penaltyNames.entrySet();

    for (iter=penaltyMapSet.iterator(); iter.hasNext(); ) {
      penaltyMap = (Map.Entry)iter.next();
      penaltyID = (Integer)penaltyMap.getKey();
      penaltyText = (String)penaltyMap.getValue();
%>
<OPTION VALUE="<%=penaltyID.intValue()%>"><%=penaltyText%>
<%
    } // End, for loop
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="homeMinutes<%=penalty%>">
<OPTION VALUE="2" SELECTED>2
<OPTION VALUE="5">5
<OPTION VALUE="10">10
</SELECT>
</TD>

<TD>
<SELECT NAME="home_penalty_period<%=penalty%>">
<OPTION VALUE="0" SELECTED> 0
<OPTION VALUE="1"> 1
<OPTION VALUE="2"> 2
<OPTION VALUE="3"> 3
<OPTION VALUE="4"> OT
</SELECT>
</TD>

<TD>
<INPUT TYPE="text" NAME="reqnum_home_penalty_minutes<%=penalty%>" MAXLENGTH=2 SIZE=2 onBlur="valid_min(this)" VALUE="">:
<INPUT TYPE="text" NAME="reqnum_home_penalty_seconds<%=penalty%>" MAXLENGTH=2 SIZE=2 onBlur="valid_sec(this)" VALUE="">
</TD>

</TR>
<%
  } // End, for all penalties
%>

</TABLE>

<BR><BR><BR>

<H3>Penalties to <%=visitorTeam.getTeamName()%><NORMAL>
<BR><BR>
<TABLE WIDTH=780 BORDER=0 CELLSPACING=0 CELLPADDING=0>
<TR><TH width=5%>Penalty</TH><TH width=30%>Penalty to</TH><TH width=25%>Penalty for</TH>
<TH width=5%>Minutes</TH><TH width=5%>Period</TH><TH width=30%>Time</TH></TR>
<%
  players = newGame.getVisitorTeamPlayers();
%>
<%
  for (penalty=0; penalty<visitorPenalties; penalty++)
  {
%>
<TR>
<TD><%=penalty+1%></TD>
<TD>
<SELECT NAME="visitorPenalty<%=penalty%>">
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
<SELECT NAME="visitorInfraction<%=penalty%>">
<%
    HashMap penaltyNames = Penalty.getPenaltyNames(dbContext);
    Set penaltyMapSet = penaltyNames.entrySet();

    for (iter=penaltyMapSet.iterator(); iter.hasNext(); ) {
      penaltyMap = (Map.Entry)iter.next();
      penaltyID = (Integer)penaltyMap.getKey();
      penaltyText = (String)penaltyMap.getValue();
%>
<OPTION VALUE="<%=penaltyID.intValue()%>"><%=penaltyText%>
<%
    } // End, for loop
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="visitorMinutes<%=penalty%>">
<OPTION VALUE="2" SELECTED>2
<OPTION VALUE="5">5
<OPTION VALUE="10">10
</SELECT>
</TD>

<TD>
<SELECT NAME="visitor_penalty_period<%=penalty%>">
<OPTION VALUE="0" "SELECTED"> 0
<OPTION VALUE="1"> 1
<OPTION VALUE="2"> 2
<OPTION VALUE="3"> 3
<OPTION VALUE="4"> OT
</SELECT>
</TD>

<TD>
<INPUT TYPE="text" NAME="reqnum_visitor_penalty_minutes<%=penalty%>" MAXLENGTH=2 SIZE=2 onBlur="valid_min(this)" VALUE="">:
<INPUT TYPE="text" NAME="reqnum_visitor_penalty_seconds<%=penalty%>" MAXLENGTH=2 SIZE=2 onBlur="valid_sec(this)" VALUE="">
</TD>

</TR>
<%
  } // End, for all penalties
%>

</TABLE>

<BR><BR>

<INPUT TYPE="HIDDEN" NAME="phase" VALUE="enterPenalties">

<FONT SIZE=3 /><CENTER><INPUT TYPE="SUBMIT" VALUE=" Complete "></CENTER>
</FORM>

<!--End of FORM-->

<%@include file="template_end.html"%>
</BODY>
</HTML>
