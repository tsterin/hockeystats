<!-- $Header: /u02/cvsroot/hockeystats/playerSelect.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page isThreadSafe="false" import="hockeystats.*, java.util.*" session="true" %>

<HTML>
<HEAD>
<TITLE>Player Selection</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<%!
  ArrayList players = null;
  Team homeTeam, visitorTeam;
  String homeTeamName, visitorTeamName;
  Iterator iter;
  Player aPlayer;
  int gameIndex;
%>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<FONT FACE="Modern" SIZE=5><B><CENTER>Identify the players who played for each team:</CENTER></B></FONT>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="GameEntryServlet" onSubmit="return checkrequired(this)">

<INPUT TYPE="hidden" NAME=sourcePage VALUE="playerSelect">

<%
  homeTeamName = request.getParameter("home");
  homeTeam = new Team(homeTeamName, dbContext);
  visitorTeamName = request.getParameter("visitor");
  visitorTeam = new Team(visitorTeamName, dbContext);
  gameIndex = Integer.parseInt(request.getParameter("index"));
  players = Player.getPlayersByTeam(homeTeam, dbContext);
  session.setAttribute("hockeystats.homeTeam", homeTeam);
  session.setAttribute("hockeystats.visitorTeam", visitorTeam);
  int i;
%>

<TABLE BORDER=1><CAPTION><H3>Roster for <%= homeTeamName %></CAPTION></H3>
<H2>
<TH>Played?</TH>
<TH WIDTH=30>Num</TH>
<TH WIDTH=200>Player name</TH>
</H2>

<%
  for (iter=players.iterator(), i=0; iter.hasNext(); i++)
  {
    aPlayer = (Player)iter.next();
%>

<TR>
<TD>
 <INPUT TYPE="radio" NAME="HomePlayer<%=i%>" VALUE="Home"> <%=homeTeamName%>
 <INPUT TYPE="radio" NAME="HomePlayer<%=i%>" VALUE="NotPlay" CHECKED> Did not play
</TD>
<TD>
 <%=aPlayer.getJerseyNumber()%>
</TD>
<TD>
 <%=aPlayer.getFName()%> <%=aPlayer.getLName() %>
</TD></TR>
<%
  }

  players = Player.getPlayersByTeam(visitorTeam, dbContext);
%>

</TABLE>

<TABLE BORDER=1><CAPTION><H3>Roster for <%= visitorTeamName %></CAPTION><H2>
<TH>Played?</TH>
<TH WIDTH=30>Num</TH>
<TH WIDTH=200>Player name</TH>

<%
  for (iter=players.iterator(), i=0; iter.hasNext(); i++)
  {
    aPlayer = (Player)iter.next();
%>

<TR>
<TD>
 <INPUT TYPE="radio" NAME="VisitorPlayer<%=i%>" VALUE="Visitor"> <%=visitorTeamName%>
 <INPUT TYPE="radio" NAME="VisitorPlayer<%=i%>" VALUE="NotPlay" CHECKED> Did not play
</TD>
<TD>
 <%=aPlayer.getJerseyNumber()%>
</TD>
<TD>
 <%=aPlayer.getFName()%> <%=aPlayer.getLName() %>
</TD></TR>
<%
  }
%>

</TABLE>

<BR><BR>

<INPUT TYPE="HIDDEN" NAME="phase" VALUE="playerSelect">
<INPUT TYPE="HIDDEN" NAME="gameIndex" VALUE="<%=gameIndex%>">

<FONT SIZE=3 /><CENTER><INPUT TYPE="SUBMIT" NAME="nextStep" VALUE=" Select Substitutes "></CENTER>
</FORM>

<!--End of FORM-->

<%@include file="template_end.html"%>
</BODY>
</HTML>
