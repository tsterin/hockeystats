<!-- $Header: /u02/cvsroot/hockeystats/GameStats.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*, java.util.*" session="true" isThreadSafe="false" %>

<%!
   boolean isAdmin = false;
   int gameIndex;              // index into games[] arrary
   int gamePlayerIndex;
   Team homeTeam = null;
   Team visitorTeam = null;
   Game game = null;
   ArrayList homeGoals = null;
   ArrayList visitorGoals = null;
   ArrayList penalties = null;
   GameGoal aGameGoal = null;
   Penalty aPenalty = null;
   String teamName;
   GamePlayer[] gamePlayers = null;
   GamePlayer aGamePlayer;
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
%>

<HTML>

<HEAD>
<TITLE>Game Stats</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<%
  String index = request.getParameter("index");
//  if (index != null)
  {
    // Get the game stats.
    gameIndex = Integer.parseInt(index);
    ArrayList schedule = (ArrayList)session.getAttribute("hockeystats.schedule");
    game = (Game) schedule.get(gameIndex);
    homeTeam = game.getHomeTeam();
    visitorTeam = game.getVisitorTeam();
    homeGoals = GameGoal.getGoalsInGame(game, homeTeam, dbContext);
    visitorGoals = GameGoal.getGoalsInGame(game, visitorTeam, dbContext);
    penalties = Penalty.getAllPenaltiesInGame(game, dbContext);
    gamePlayers = GamePlayer.getGamePlayersInGame(game, dbContext);
  }
%>

<FONT FACE="Modern" SIZE=5><B><CENTER>Game Stats</CENTER></B></FONT><BR>
<BR><H4>Game date: <%=game.getFormattedDate("MMMM dd, yyyy KK:mm a")%></H4>

<%
  if (homeGoals.size() > 0)
  {
%>

<H4>Goals scored by <%=homeTeam.getTeamName() %></H4>

<TABLE WIDTH=600 BORDER=0 CELLSPACING=0 CELLPADDING=1>

<TR><TH width=8%>Goal</TH><TH ALIGN=LEFT width=24%>Scored by</TH>
<TH ALIGN=LEFT width=24%>Assist 1</TH><TH ALIGN=LEFT width=24%>Assist 2</TH>
<TH width=5%>Period</TH><TH width=15%>Time</TH></TR>

<%
    for (int goalIndex=0; goalIndex<homeGoals.size(); goalIndex++)
    {
      aGameGoal = (GameGoal)homeGoals.get(goalIndex);
%>

<TR>
<TD ALIGN=CENTER><%=goalIndex + 1%></TD>
<TD><%=aGameGoal.getScoredBy()%></TD>
<TD><%=aGameGoal.getAssist1() == null ? "" : aGameGoal.getAssist1().toString()%></TD>
<TD><%=aGameGoal.getAssist2() == null ? "" : aGameGoal.getAssist2().toString()%></TD>
<TD ALIGN=CENTER><%=aGameGoal.getPeriod()%></TD>
<TD ALIGN=CENTER><%=aGameGoal.getTime()%></TD>
</TR>

<%
    }  // End, for
%>

</TABLE>

<%
  } // End, if
  else
  {
%>
<H4>No goals scored by <%=homeTeam.getTeamName()%></H4>
<%
  }
%>

<%
  if (visitorGoals.size() > 0)
  {
%>

<BR><BR><H4>Goals scored by <%=visitorTeam.getTeamName() %></H4>

<TABLE WIDTH=600 BORDER=0 CELLSPACING=0 CELLPADDING=1>

<TR><TH width=8%>Goal</TH><TH ALIGN=LEFT width=24%>Scored by</TH>
<TH ALIGN=LEFT width=24%>Assist 1</TH><TH ALIGN=LEFT width=24%>Assist 2</TH>
<TH width=5%>Period</TH><TH width=15%>Time</TH></TR>

<%
    for (int goalIndex=0; goalIndex<visitorGoals.size(); goalIndex++)
    {
      aGameGoal = (GameGoal)visitorGoals.get(goalIndex);
%>

<TR>
<TD ALIGN=CENTER><%=goalIndex + 1%></TD>
<TD><%=aGameGoal.getScoredBy()%></TD>
<TD><%=aGameGoal.getAssist1() == null ? "" : aGameGoal.getAssist1().toString()%></TD>
<TD><%=aGameGoal.getAssist2() == null ? "" : aGameGoal.getAssist2().toString()%></TD>
<TD ALIGN=CENTER><%=aGameGoal.getPeriod()%></TD>
<TD ALIGN=CENTER><%=aGameGoal.getTime()%></TD>
</TR>

<%
    }  // End, for
%>

</TABLE>

<%
  } // End, if
  else
  {
%>
<H4>No goals scored by <%=visitorTeam.getTeamName()%></H4>
<%
  }
%>

<%
  if (penalties.size() > 0)
  {
%>

<BR><BR><H4>Penalties</H4>

<TABLE WIDTH=600 BORDER=0 CELLSPACING=0 CELLPADDING=1>

<TR>
<TR><TH width=10%>Penalty</TH><TH ALIGN=LEFT width=19%>Team</TH><TH ALIGN=LEFT width=23%>Penalty to</TH><TH ALIGN=LEFT width=23%>Penalty for</TH>
<TH width=5%>Minutes</TH><TH width=5%>Period</TH><TH width=15%>Time</TH></TR>

<%
    for (int penaltyIndex=0; penaltyIndex<penalties.size(); penaltyIndex++)
    {
      aPenalty = (Penalty)penalties.get(penaltyIndex);
      if (aPenalty.getTeam() == null)
        teamName = "";
      else
        teamName = aPenalty.getTeam().getTeamName();
%>

<TR>
<TD ALIGN=CENTER><%=penaltyIndex + 1%></TD>
<TD><%=teamName%></TD>
<TD><%=aPenalty.getPlayer()%></TD>
<TD><%=aPenalty.getPenaltyText()%></TD>
<TD ALIGN=CENTER><%=aPenalty.getMinutes()%></TD>
<TD ALIGN=CENTER><%=aPenalty.getPeriod()%></TD>
<TD ALIGN=CENTER><%=aPenalty.getTime()%></TD>
</TR>

<%
    }  // End, for
%>
</TABLE>
<%
  } // End, if
  else
  {
%>
<H4>No penalties</H4>
<%
  }
%>

<BR><BR><H4>Players for <%=homeTeam.getTeamName()%></H4>

<TABLE WIDTH=250 BORDER=0 CELLSPACING=0 CELLPADDING=1>

<TR>
<TR><TH width=5%>Sub</TH><TH width=20%>Num</TH><TH ALIGN=LEFT width=75%>Player</TH></TR>

<%
    for (int gamePlayerIndex=0; gamePlayerIndex<gamePlayers.length; gamePlayerIndex++)
    {
      aGamePlayer = (GamePlayer)gamePlayers[gamePlayerIndex];
      if (aGamePlayer.getTeam().equals(homeTeam))
      {
%>

<TR>
<TD ALIGN=CENTER><%=aGamePlayer.isSubstitute() ? "*" : ""%></TD>
<TD ALIGN=CENTER><%=aGamePlayer.getPlayer().getJerseyNumber()%></TD>
<TD><%=aGamePlayer.getPlayer()%></TD>
</TR>

<%
      } // End, if
    }  // End, for
%>
</TABLE>

<BR><BR><H4>Players for <%=visitorTeam.getTeamName()%></H4>

<TABLE WIDTH=250 BORDER=0 CELLSPACING=0 CELLPADDING=1>

<TR>
<TR><TH width=5%>Sub</TH><TH width=20%>Num</TH><TH ALIGN=LEFT width=75%>Player</TH></TR>

<%
    for (int gamePlayerIndex=0; gamePlayerIndex<gamePlayers.length; gamePlayerIndex++)
    {
      aGamePlayer = (GamePlayer)gamePlayers[gamePlayerIndex];
      if (aGamePlayer.getTeam().equals(visitorTeam))
      {
%>

<TR>
<TD ALIGN=CENTER><%=aGamePlayer.isSubstitute() ? "*" : " "%></TD>
<TD ALIGN=CENTER><%=aGamePlayer.getPlayer().getJerseyNumber()%></TD>
<TD><%=aGamePlayer.getPlayer()%></TD>
</TR>

<%
      } // End, if
    }  // End, for
%>
</TABLE>

<BR><BR><H4>Goalies</H4>
<%=homeTeam.getTeamName()%>: <%=game.getHomeTeamGoalie() == null ? "Not specified" : game.getHomeTeamGoalie().toString()%><BR>
<%=visitorTeam.getTeamName()%>: <%=game.getVisitorTeamGoalie() == null ? "Not specified" : game.getVisitorTeamGoalie().toString()%><BR>

<BR>

<%@include file="template_end.html"%>
</BODY>
</HTML>
