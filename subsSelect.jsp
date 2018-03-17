!-- $Header: /u02/cvsroot/hockeystats/subsSelect.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<!--
This page is used to select the substitutes who played in a game.
-->

<%@ page isThreadSafe="false" import="hockeystats.*, java.util.*"session="true" %>

<HTML>
<HEAD>
<TITLE>Substitute Player Selection</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<%!
  ArrayList allPlayers = null;
  ArrayList playingPlayers = null;
  String homeTeamName, visitorTeamName;
  Player aPlayer;
  Iterator iter;
%>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<FONT FACE="Modern" SIZE=5><B><CENTER>Identify substitutes and the team they played for:</CENTER></B></FONT>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="GameEntryServlet" onSubmit="return checkrequired(this)">

<%
  homeTeamName = request.getParameter("home");
  Team homeTeam = new Team(homeTeamName, dbContext);
  visitorTeamName = request.getParameter("visitor");
  Team visitorTeam = new Team(visitorTeamName, dbContext);
  allPlayers =
    Player.getAllPlayers((SeasonSession)session.getAttribute("hockeystats.selectedSeasonSession"),
                          dbContext);
  int i;
  playingPlayers = (ArrayList)session.getAttribute("hockeystats.playingPlayers");
%>
<FONT SIZE=3 /><CENTER><B><INPUT TYPE="SUBMIT" VALUE=" Next "></B></CENTER></FONT><BR>
<TABLE BORDER=1><H2>
<tr nowrap>
<TH width=200>Substituted on:</TH>
<TH width=150>Player name</TH>
<TH width=100>Regular team</TH>
</tr>
<%
  for (iter=allPlayers.iterator(), i=0; iter.hasNext();)
  {
    aPlayer = (Player)iter.next();

    // If the player has already been selected as a playing player, he
    // can't be a sub so remove him from the list and get the next player
    if (playingPlayers.indexOf(aPlayer) != -1)
    {
      iter.remove();
      continue;
    }
%>

<TR nowrap>
<TD>
 <INPUT TYPE="radio" NAME="SubPlayer<%=i%>" VALUE="Home"> <%=homeTeamName%>
 <INPUT TYPE="radio" NAME="SubPlayer<%=i%>" VALUE="Visitor"> <%=visitorTeamName%>
 <INPUT TYPE="radio" NAME="SubPlayer<%=i%>" VALUE="NotPlay" CHECKED> Did not play
</TD>
<TD>
 <%=aPlayer.getLName()%>, <%=aPlayer.getFName().trim() %>
</TD>
<TD>
 <%=aPlayer.getTeam().getTeamName().trim()%>
</TD></TR>

<%
    i++;
  } // End, for

  // Save the list with possible subs to the session for use by the servlet
  session.setAttribute("hockeystats.allPlayers", allPlayers);
%>

</TABLE>

<BR><BR>

<INPUT TYPE="HIDDEN" NAME="phase" VALUE="subsSelect">

<FONT SIZE=3 /><CENTER><INPUT TYPE="SUBMIT" VALUE=" Next "></CENTER>
</FORM>

<!--End of FORM-->

<%@include file="template_end.html"%>
</BODY>
</HTML>
