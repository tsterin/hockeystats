<!-- $Header: /u02/cvsroot/hockeystats/template.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*" %>

<%-- If the session has expired, send the user back to select the league --%>

<%  if (session.getAttribute("hockeystats.selectedLeague") == null) { %>
<jsp:forward page="/leagueSelect.jsp" />
<% } %>

<IMG SRC=images/img0.gif><BR>
<%!
  String adminFlag;
  SeasonSession selectedSeasonSession;
  DbContext dbContext;
%>
<%
  selectedSeasonSession = (SeasonSession)session.getAttribute("hockeystats.selectedSeasonSession");
  dbContext = (DbContext)session.getAttribute("hockeystats.dbContext");
  if (selectedSeasonSession == null)
  {
    selectedSeasonSession = SeasonSession.getCurrentSeasonSession(dbContext);
    session.setAttribute("hockeystats.selectedSeasonSession", selectedSeasonSession);
  }
%>
<H3>DEV System</H3>
<TABLE BORDER=0 CELLSPACING=20 CELLPADDING=0>
<TR><TD>
<% if (session.getAttribute("userName") != null) { %>
<H5>
User: <%=session.getAttribute("userName")%> <% } %>
</TD><TD>
<H5>
Season: <A HREF="seasonSelection.jsp">
<%=selectedSeasonSession.getSeason().getSeasonName() + " - " + selectedSeasonSession.getSessionName()%></A>
</TD><TD>
<H5>
League: <A HREF="leagueSelect.jsp"><%=session.getAttribute("hockeystats.selectedLeague")%></A>
</TD></TR></TABLE>


<TABLE BORDER=0 CELLSPACING=20 CELLPADDING=0>
<TR><TD VALIGN=TOP>

<H5>
<A HREF="mainpage.jsp">Standings</A><BR>
<BR><HR COLOR="3300FF" NOSHADE WIDTH=120 ALIGN=LEFT><BR>

<H5>
<A HREF="teamSelect.jsp?sel=schedule">Schedule</A><BR>
<BR><HR COLOR="3300FF" NOSHADE WIDTH=120 ALIGN=LEFT><BR>

<H5>
<A HREF="GoalsAssists.jsp">Goals & Assists</A><BR>
<BR><HR COLOR="3300FF" NOSHADE WIDTH=120 ALIGN=LEFT><BR>

<H5>
<A HREF="PenaltiesGoalie.jsp">Penalties, Points, <BR>& Goalie Stats</A><BR>
<BR><HR COLOR="3300FF" NOSHADE WIDTH=120 ALIGN=LEFT><BR>

<H5>
<A HREF="rules.jsp">League Rules</A><BR>
<BR><HR COLOR="3300FF" NOSHADE WIDTH=120 ALIGN=LEFT><BR>

<H5>
<A HREF="teamSelect.jsp?sel=roster">Team Rosters</A><BR>
<BR><HR COLOR="3300FF" NOSHADE WIDTH=120 ALIGN=LEFT><BR>

<H5>
<A HREF="Pictures.jsp">Pictures</A><BR>
<BR><HR COLOR="3300FF" NOSHADE WIDTH=120 ALIGN=LEFT><BR>

<H5>
<A HREF="Links.jsp">Links</A><BR>
<BR><HR COLOR="3300FF" NOSHADE WIDTH=120 ALIGN=LEFT><BR>

<%
  adminFlag = (String)session.getAttribute("isAdmin");
  if (adminFlag != null && adminFlag.equals("Y"))
  {
%>
<H5>
<A HREF="admin.jsp">Admin menu</A><BR>
<BR><HR COLOR="3300FF" NOSHADE WIDTH=120 ALIGN=LEFT><BR>
<%
  }
%>

<%
  if (session.getAttribute("userID") == null)
  {
%>
<H5>
<A HREF="login.jsp">Login</A><BR>
<%
  }
  else
  {
%>
<H5>
<A HREF="logout.jsp">Logout</A><BR>
<%
  } // End, else
%>
<BR><HR COLOR="3300FF" NOSHADE WIDTH=120 ALIGN=LEFT><BR>

</H5><BR>

</TD>
<TD VALIGN=TOP>
