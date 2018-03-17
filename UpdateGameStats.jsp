<!-- $Header: /u02/cvsroot/hockeystats/UpdateGameStats.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*, java.util.*" session="true" isThreadSafe="false" %>

<%!
   boolean isAdmin = false;
   int gameIndex;              // index into games[] arrary
   int goalIndex;
   int playerIndex;
   int penaltyIndex;
   Team[] teams = null;
   Team homeTeam = null;
   Team visitorTeam = null;
   Game game = null;
   ArrayList homeGoals = null;
   ArrayList visitorGoals = null;
   ArrayList penalties = null;
   GameGoal aGameGoal = null;
   Penalty aPenalty = null;
   ArrayList games = null;
   ArrayList homePlayers = null;
   ArrayList visitorPlayers = null;
   Player scoredBy = null;
   Player assist1 = null;
   Player assist2 = null;
   Player penalizedPlayer = null;
   Player aPlayer = null;
   int period = 0;
   StringBuffer time;
   Iterator iter;
   Iterator playerIter;
%>

<%
  if (session.isNew())
  {
%>
<jsp:forward page="sessionTimeout.jsp" />
<%
  }

  String isAdminAttr = (String)session.getAttribute("isAdmin");
  if (isAdminAttr != null && isAdminAttr.equals("Y"))
  {
    isAdmin = true;
  }

  if (!isAdmin) {
%>
<jsp:forward page="/notAdmin.jsp" />
<% }
%>

<HTML>

<HEAD>
<TITLE>Update Game Stats</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<%
  String index = request.getParameter("index");

  // Get the game stats.
  gameIndex = Integer.parseInt(index);
  games = (ArrayList)session.getAttribute("hockeystats.gameArray");
  game = (Game)games.get(gameIndex);
  homeTeam = game.getHomeTeam();
  visitorTeam = game.getVisitorTeam();
  homeGoals = GameGoal.getGoalsInGame(game, homeTeam, dbContext);
  visitorGoals = GameGoal.getGoalsInGame(game, visitorTeam, dbContext);
  penalties = Penalty.getAllPenaltiesInGame(game, dbContext);
  homePlayers = game.getHomeTeamPlayers();
  visitorPlayers = game.getVisitorTeamPlayers();
%>

<FONT FACE="Modern" SIZE=5><B><CENTER>Game Stats</CENTER></B></FONT><BR>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="DataServlet" onSubmit="return checkrequired(this)">

<INPUT TYPE="hidden" NAME=sourcePage VALUE="gameStats">

<%
  teams = hockeystats.Team.getAllTeams(selectedSeasonSession, dbContext);
%>
<INPUT TYPE="hidden" NAME=gameIndex VALUE=<%= gameIndex %>>

<!-- Display the goals scored by the home team and allow info to be updated. -->

<%
  if (game.getHomeScore() > 0)
  {
%>

<H4>Goals scored by <%=homeTeam.getTeamName() %></H4>

<TABLE WIDTH=780 BORDER=0 CELLSPACING=0 CELLPADDING=5>

<TR>
<TR><TH width=5%>Goal</TH><TH width=22%>Scored by</TH><TH width=22%>Assist 1</TH>
<TH width=22%>Assist 2</TH><TH width=10%>Period</TH><TH width=19%>Time</TH></TR>

<%
    iter = homeGoals.iterator();
    scoredBy = null;
    assist1 = null;
    assist2 = null;
    period = 0;
    time = new StringBuffer("");
    for (goalIndex=0; goalIndex < game.getHomeScore(); goalIndex++)
    {
      if (iter.hasNext())
      {
        aGameGoal = (GameGoal)iter.next();
        scoredBy = aGameGoal.getScoredBy();
        assist1 = aGameGoal.getAssist1();
        assist2 = aGameGoal.getAssist2();
        period = aGameGoal.getPeriod();
        time = new StringBuffer(aGameGoal.getTime());
      }
%>

<TR>
<TD><%=goalIndex + 1%></TD>

<TD>
<SELECT NAME="homeScoredBy<%=goalIndex%>">
<OPTION VALUE="-1" <%=scoredBy == null ? "SELECTED" : ""%>> (Select)
<%
    for (playerIter=homePlayers.iterator(), playerIndex=0; playerIter.hasNext(); playerIndex++) {
      aPlayer = (Player)playerIter.next();
%>
<OPTION VALUE="<%=playerIndex%>" <%= aPlayer.equals(scoredBy) ? "SELECTED" : ""%>><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName() %>
<%
    } // End, for loop building player list
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="home1Assist<%=goalIndex%>">
<OPTION VALUE="-1" <%=assist1 == null ? "SELECTED" : ""%>>None
<%
    for (playerIter=homePlayers.iterator(), playerIndex=0; playerIter.hasNext(); playerIndex++) {
      aPlayer = (Player)playerIter.next();
%>
<OPTION VALUE="<%=playerIndex%>" <%= aPlayer.equals(assist1) ? "SELECTED" : ""%>><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName() %>
<%
    } // End, for loop building player list
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="home2Assist<%=goalIndex%>">
<OPTION VALUE="-1" <%=assist2 == null ? "SELECTED" : ""%>>None
<%
    for (playerIter=homePlayers.iterator(), playerIndex=0; playerIter.hasNext(); playerIndex++) {
      aPlayer = (Player)playerIter.next();
%>
<OPTION VALUE="<%=playerIndex%>" <%= aPlayer.equals(assist2) ? "SELECTED" : ""%>><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName()%>
<%
    } // End, for loop building player list
%>
<OPTION value="a"><%=time%>
</SELECT>
</TD>

<TD>
<SELECT NAME="home_goalperiod<%=goalIndex%>">
<OPTION VALUE="0" <%=period==0 ? "SELECTED" : ""%>> 0
<OPTION VALUE="1" <%=period==1 ? "SELECTED" : ""%>> 1
<OPTION VALUE="2" <%=period==2 ? "SELECTED" : ""%>> 2
<OPTION VALUE="3" <%=period==3 ? "SELECTED" : ""%>> 3
<OPTION VALUE="4" <%=period==4 ? "SELECTED" : ""%>> OT
</SELECT>
</TD>

<TD>
<INPUT TYPE="text" NAME="reqnum_homegoal_minutes<%=goalIndex%>" MAXLENGTH=2 SIZE=2 VALUE="<%=time.length() > 0 ? time.substring(0,2) : ""%>" onBlur="valid_min(this)">:
<INPUT TYPE="text" NAME="reqnum_homegoal_seconds<%=goalIndex%>" MAXLENGTH=2 SIZE=2 VALUE="<%=time.length() > 0 ? time.substring(3,5) : ""%>" onBlur="valid_sec(this)">
</TD>

</TR>

<%
    }  // End, for
%>
</TABLE>
<%
  } // End, if
%>

<!-- Display the goals scored by the visitor team and allow info to be updated. -->

<%
  if (game.getVisitorScore() > 0)
  {
%>

<H4>Goals scored by <%=visitorTeam.getTeamName() %></H4>

<TABLE WIDTH=780 BORDER=0 CELLSPACING=0 CELLPADDING=5>

<TR>
<TR><TH width=5%>Goal</TH><TH width=22%>Scored by</TH><TH width=22%>Assist 1</TH>
<TH width=22%>Assist 2</TH><TH width=10%>Period</TH><TH width=19%>Time</TH></TR>

<%
    iter = visitorGoals.iterator();
    scoredBy = null;
    assist1 = null;
    assist2 = null;
    period = 0;
    time = new StringBuffer("");
    for (goalIndex=0; goalIndex < game.getVisitorScore(); goalIndex++)
    {
      if (iter.hasNext())
      {
        aGameGoal = (GameGoal)iter.next();
        scoredBy = aGameGoal.getScoredBy();
        assist1 = aGameGoal.getAssist1();
        assist2 = aGameGoal.getAssist2();
        period = aGameGoal.getPeriod();
        time = new StringBuffer(aGameGoal.getTime());
      }
%>

<TR>
<TD><%=goalIndex + 1%></TD>

<TD>
<SELECT NAME="visitorScoredBy<%=goalIndex%>">
<OPTION VALUE="-1" <%=scoredBy == null ? "SELECTED" : ""%>> (Select)
<%
    for (playerIter=visitorPlayers.iterator(), playerIndex=0; playerIter.hasNext(); playerIndex++) {
      aPlayer = (Player)playerIter.next();
%>
<OPTION VALUE="<%=playerIndex%>" <%= aPlayer.equals(scoredBy) ? "SELECTED" : ""%>><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName() %>
<%
    } // End, for loop building player list
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="visitor1Assist<%=goalIndex%>">
<OPTION VALUE="-1" <%=assist1 == null ? "SELECTED" : ""%>>None
<%
    for (playerIter=visitorPlayers.iterator(), playerIndex=0; playerIter.hasNext(); playerIndex++) {
      aPlayer = (Player)playerIter.next();
%>
<OPTION VALUE="<%=playerIndex%>" <%= aPlayer.equals(assist1) ? "SELECTED" : ""%>><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName() %>
<%
    } // End, for loop building player list
%>
</SELECT>
</TD>

<TD>
<SELECT NAME="visitor2Assist<%=goalIndex%>">
<OPTION VALUE="-1" <%=assist2 == null ? "SELECTED" : ""%>>None
<%
    for (playerIter=visitorPlayers.iterator(), playerIndex=0; playerIter.hasNext(); playerIndex++) {
      aPlayer = (Player)playerIter.next();
%>
<OPTION VALUE="<%=playerIndex%>" <%= aPlayer.equals(assist2) ? "SELECTED" : ""%>><%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName()%>
<%
    } // End, for loop building player list
%>
<OPTION value="a"><%=time%>
</SELECT>
</TD>

<TD>
<SELECT NAME="visitor_goalperiod<%=goalIndex%>">
<OPTION VALUE="0" <%=period==0 ? "SELECTED" : ""%>> 0
<OPTION VALUE="1" <%=period==1 ? "SELECTED" : ""%>> 1
<OPTION VALUE="2" <%=period==2 ? "SELECTED" : ""%>> 2
<OPTION VALUE="3" <%=period==3 ? "SELECTED" : ""%>> 3
<OPTION VALUE="4" <%=period==4 ? "SELECTED" : ""%>> OT
</SELECT>
</TD>

<TD>
<INPUT TYPE="text" NAME="reqnum_visitorgoal_minutes<%=goalIndex%>" MAXLENGTH=2 SIZE=2 VALUE="<%=time.length() > 0 ? time.substring(0,2) : ""%>" onBlur="valid_min(this)">:
<INPUT TYPE="text" NAME="reqnum_visitorgoal_seconds<%=goalIndex%>" MAXLENGTH=2 SIZE=2 VALUE="<%=time.length() > 0 ? time.substring(3,5) : ""%>" onBlur="valid_sec(this)">
</TD>

</TR>

<%
    }  // End, for
%>
</TABLE>
<%
  } // End, if
%>

Number of penalties: <%=penalties.size()%><BR>

<%
  if (penalties.size() > 0)
  {
%>

<BR><H4>Penalties</H4>

<TABLE WIDTH=750 BORDER=0 CELLSPACING=0 CELLPADDING=5>

<TR>
<TR><TH width=5%>Penalty</TH><TH width=30%>Penalty to</TH><TH width=25%>Penalty for</TH>
<TH width=5%>Minutes</TH><TH width=5%>Period</TH><TH width=30%>Time</TH></TR>

<%
    for (iter=penalties.iterator(), penaltyIndex=0; iter.hasNext(); penaltyIndex++)
    {
      aPenalty = (Penalty)iter.next();
      penalizedPlayer = aPenalty.getPlayer();
      period = aPenalty.getPeriod();
      time = new StringBuffer(aPenalty.getTime());
%>

<TR>
<TD><%=penaltyIndex + 1%></TD>

<TD>
<SELECT NAME="penalty<%=penaltyIndex%>">
<%
      for (playerIter=homePlayers.iterator(), playerIndex=0; playerIter.hasNext(); playerIndex++) {
        aPlayer = (Player)playerIter.next();
%>
<OPTION VALUE="H<%=playerIndex%>" <%= aPlayer.equals(penalizedPlayer) ? "SELECTED" : ""%>><%=homeTeam.getTeamName()%>-<%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName() %>
<%
    } // End, for loop building home player list
%>
<%
      for (playerIter=visitorPlayers.iterator(), playerIndex=0; playerIter.hasNext(); playerIndex++) {
        aPlayer = (Player)playerIter.next();
%>
<OPTION VALUE="V<%=playerIndex%>" <%= aPlayer.equals(penalizedPlayer) ? "SELECTED" : ""%>><%=visitorTeam.getTeamName()%>-<%=aPlayer.getJerseyNumber() + ": " + aPlayer.getFName() + " " + aPlayer.getLName() %>
<%
    } // End, for loop building visitor player list
%>
</SELECT>
</TD>

<TD><%=aPenalty.getPenaltyText()%></TD>
<TD><%=aPenalty.getMinutes()%></TD>

<TD>
<SELECT NAME="penalty_period<%=penaltyIndex%>">
<OPTION VALUE="0" <%=period == 0 ? "SELECTED" : ""%>> 0
<OPTION VALUE="1" <%=period == 1 ? "SELECTED" : ""%>> 1
<OPTION VALUE="2" <%=period == 2 ? "SELECTED" : ""%>> 2
<OPTION VALUE="3" <%=period == 3 ? "SELECTED" : ""%>> 3
<OPTION VALUE="4" <%=period == 4 ? "SELECTED" : ""%>> OT
</SELECT>
</TD>

<TD>
<INPUT TYPE="text" NAME="reqnum_penalty_minutes<%=penaltyIndex%>" MAXLENGTH=2 SIZE=2 onBlur="valid_min(this)" VALUE=<%=time.length() > 0 ? time.substring(0,2) : ""%>>:
<INPUT TYPE="text" NAME="reqnum_penalty_seconds<%=penaltyIndex%>" MAXLENGTH=2 SIZE=2 onBlur="valid_sec(this)" VALUE=<%=time.length() > 0 ? time.substring(3,5) : ""%>>
</TD>

</TR>

<%
    }  // End, for
%>

</TABLE>

<%
  } // End, if
%>

<FONT SIZE=3 /><CENTER><INPUT TYPE="SUBMIT" VALUE=" Submit "></CENTER>
</FORM>

</CENTER>

<!--End of FORM-->

<BR>
<% if (isAdmin) { %>
<A HREF="admin.jsp">Return to main admin page</A>
<% } %>

<%@include file="template_end.html"%>
</BODY>
</HTML>
