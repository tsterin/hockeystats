<!-- $Header: /u02/cvsroot/hockeystats/TeamRoster.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*,java.util.*, java.net.URLEncoder" isThreadSafe="false" %>

<HTML>

<HEAD>
<TITLE>Jon's AHL - Team Roster</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<%!
  String teamName;
  boolean isAdmin;
  ArrayList players = null;
  Team theTeam = null;
  Iterator iter;
  Player aPlayer;
  int i;
%>

<%
  // Get the name of the team whose roster we'll display, then get the roster.

  teamName = request.getParameter("selectedTeam");
  if (teamName.equals("ALL"))
    players = Player.getAllPlayers(dbContext);
  else
  {
    theTeam = new Team(teamName, dbContext);

    TeamRoster roster = theTeam.getTeamRoster();
    players = roster.getPlayers();
  }

  String isAdminVal = (String)session.getAttribute("isAdmin");
  isAdmin = isAdminVal != null && isAdminVal.equals("Y");
%>

<TABLE ><CAPTION><H3>Roster for <%= teamName %></CAPTION><H2>
<TH>First Name</TH>
<TH>Last Name</TH>

<%
  if (teamName.equals("ALL"))
  {
%>
<TH>Team</TH>
<%
  }

  if (session.getAttribute("userID") != null)
  {
%>

<TH>Address</TH>
<TH>Phone</TH>
<TH>Email</TH>

<%
  }

  if (players == null)
  {
%>
<TD>No players found.</TD>
<%
  }
  else // players ArrayList is not null
  {
    for (iter=players.iterator(), i=0; iter.hasNext();i++)
    {
      aPlayer=(Player)iter.next();
%>
<TR>
<TD>
<% if (isAdmin)
   {
%>
<A HREF=Player.jsp?index=<%=i%>&team=<%=URLEncoder.encode(teamName)%>>
<%
   }
%>

<%=aPlayer.getFName()%></A></TD>

<TD><%=aPlayer.getLName()%></TD>

<%
      if (teamName.equals("ALL"))
      {
%>
<TD><%=aPlayer.getTeam() == null ? "Not placed" : aPlayer.getTeam().getTeamName()%></TD>
<%
      } // End, if teamName.equals("ALL");

      if (session.getAttribute("userID") != null) // Is the user logged on?
      {
        if (aPlayer.displayOnLine() || session.getAttribute("isAdmin").equals("Y"))
        {
%>
<TD><%=aPlayer.getAddress()%></TD>
<TD>(<%=aPlayer.getPhoneNumber().substring(0,3)%>) <%=aPlayer.getPhoneNumber().substring(3,6)%>-<%=aPlayer.getPhoneNumber().substring(6,10)%></TD>
<TD><%=aPlayer.getEmailAddr()%></TD>
<%
        } // End, if displayOnLine
        else // This player does not want details displayed
        {
%>
<TD>Info with-held</TD>
<TD>Info with-held</TD>
<TD>Info with-held</TD>
<%
        } // End, else - player does not want details displayed
      } // End, if logged on
    } // End, for
  } // End, else - players ArrayList is not null
%>
</TABLE>
<%@include file="template_end.html"%>
</BODY>
</HTML>
