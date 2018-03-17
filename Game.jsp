<!-- $Header: /u02/cvsroot/hockeystats/Game.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*, java.util.*" isThreadSafe="false" session="true" %>

<%!
   boolean isAdmin = false;
   boolean addingGame = true;  // if false then we are modifying a game
   int gameIndex;              // if modifying a game, index into games[] arrary
   ArrayList teams = null;
   Game game = null;
   String prevGameMonth = "";
   String prevGameDay = "";
   String prevGameYear = "";
   String prevGameHour = "";
   String prevGameMinute = "";
   String prevGameAMPM = "";
   GregorianCalendar gc = new GregorianCalendar();
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

  String index = request.getParameter("index");
  if (index != null)
  {
    // We are modifying an existing game.  Get the game info.
    gameIndex = Integer.parseInt(index);
    addingGame = false;
    ArrayList schedule = (ArrayList)session.getAttribute("hockeystats.schedule");
    game = (Game)schedule.get(gameIndex);
    gc.setTime(game.getGameDate());
  }
  else
  { // Adding a game.  See if we've added one previously, and remember the date
    addingGame = true;
    String prevGameDate = request.getParameter("dt");

    if (prevGameDate != null)
    {
      prevGameMonth = prevGameDate.substring(0, 2);
      prevGameDay = prevGameDate.substring(2, 4);
      prevGameYear = prevGameDate.substring(4, 8);
      prevGameHour = prevGameDate.substring(8, 10);
      prevGameMinute = prevGameDate.substring(10, 12);
      prevGameAMPM = prevGameDate.substring(12);
    }
  }
%>

<HTML>

<HEAD>
<TITLE><%= addingGame ? "New" : "Update" %> Game</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<FONT FACE="Modern" SIZE=5><B><CENTER><%= addingGame ? "Add" : "Update" %> a game</CENTER></B></FONT><BR>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="DataServlet" onSubmit="return checkrequired(this)">

<INPUT TYPE="hidden" NAME=sourcePage VALUE="game">

<%
  teams = Team.getAllTeams(selectedSeasonSession.getSeason(), dbContext);

  if (!addingGame)
  {
%>
<INPUT TYPE="hidden" NAME=gameIndex VALUE=<%= gameIndex %>>
<%
  }
%>

<TABLE WIDTH=450 BORDER=0 CELLSPACING=0 CELLPADDING=5>

<TR>
<TD WIDTH=30>Home Team:</TD>
<TD WIDTH=300><FONT COLOR="RED">*</FONT>
<SELECT NAME="req_homeTeam">
<OPTION VALUE="">
<%
	for (int i=0; i<teams.size(); i++)
	{
	   String theTeamName = ((Team)teams.get(i)).getTeamName();
           out.println ("<OPTION VALUE=\"" + theTeamName + "\" " + (addingGame ? "" : (game.getHomeTeam().getTeamName().equals(theTeamName) ? "SELECTED" : "")) + ">" + theTeamName);
	}
%>
</SELECT>
</TD></TR>

<TR>
<TD WIDTH=350>Visitor Team:</TD>
<TD><FONT COLOR="RED">*</FONT>
<SELECT NAME="req_visitorTeam">
<OPTION VALUE="">
<%
	for (int i=0; i<teams.size(); i++)
	{
	   String theTeamName = ((Team)teams.get(i)).getTeamName();
           out.println ("<OPTION VALUE=\"" + theTeamName + "\" " + (addingGame ? "" : game.getVisitorTeam().getTeamName().equals(theTeamName) ? "SELECTED" : "") + ">" + theTeamName);
	}
%>
</SELECT>
</TD></TR>

<TR>
<TD>Game Date:</TD>
<TD><FONT COLOR="RED">*</FONT>
<SELECT NAME="req_gameMonth">
<OPTION VALUE="" <%= addingGame ? (prevGameMonth.equals("") ? "SELECTED" : "") : "" %>>
<OPTION VALUE="0"<%= addingGame ? (prevGameMonth.equals("00") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 0 ? "SELECTED" : "")%>>Jan
<OPTION VALUE="1"<%= addingGame ? (prevGameMonth.equals("01") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 1 ? "SELECTED" : "")%>>Feb
<OPTION VALUE="2"<%= addingGame ? (prevGameMonth.equals("02") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 2 ? "SELECTED" : "")%>>Mar
<OPTION VALUE="3"<%= addingGame ? (prevGameMonth.equals("03") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 3 ? "SELECTED" : "")%>>Apr
<OPTION VALUE="4"<%= addingGame ? (prevGameMonth.equals("04") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 4 ? "SELECTED" : "")%>>May
<OPTION VALUE="5"<%= addingGame ? (prevGameMonth.equals("05") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 5 ? "SELECTED" : "")%>>Jun
<OPTION VALUE="6"<%= addingGame ? (prevGameMonth.equals("06") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 6 ? "SELECTED" : "")%>>Jul
<OPTION VALUE="7"<%= addingGame ? (prevGameMonth.equals("07") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 7 ? "SELECTED" : "")%>>Aug
<OPTION VALUE="8"<%= addingGame ? (prevGameMonth.equals("08") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 8 ? "SELECTED" : "")%>>Sep
<OPTION VALUE="9"<%= addingGame ? (prevGameMonth.equals("09") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 9 ? "SELECTED" : "")%>>Oct
<OPTION VALUE="10"<%= addingGame ? (prevGameMonth.equals("10") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 10? "SELECTED" : "")%>>Nov
<OPTION VALUE="11"<%= addingGame ? (prevGameMonth.equals("11") ? "SELECTED" : "") :
                        (gc.get(Calendar.MONTH) == 11? "SELECTED" : "")%>>Dec
</SELECT>

 <SELECT NAME="req_gameDay">
<OPTION VALUE="">
<%
   for (int i=1; i<32; i++)
   {
      out.println ("<OPTION VALUE=\"" + i + "\"" +
	(addingGame ? (prevGameDay.equals("") ? "" : i == Integer.parseInt(prevGameDay) ? "SELECTED" : "") : 
                     (gc.get(Calendar.DATE) == i ? "SELECTED" : "")) +
	">" + i);
   }
%>
</SELECT>
 <SELECT NAME="req_gameYear">
<OPTION VALUE="">
<%
   for (int i=2001; i<2004; i++)
   {
      out.println ("<OPTION VALUE=\"" + i + "\"" +
	(addingGame ? (prevGameYear.equals("") ? "" : i == Integer.parseInt(prevGameYear) ? "SELECTED" : "") : 
                     (gc.get(Calendar.YEAR) == i ? "SELECTED" : "")) +
	">" + i);
   }
%>
</SELECT>

</TD></TR>

<TR>
<TD>Game Time:</TD>
<TD><FONT COLOR="RED">*</FONT>
<SELECT NAME="req_gameHour">
<OPTION VALUE="">
<%
   for (int i=1; i<13; i++)
   {
      out.println ("<OPTION VALUE=\"" + i + "\"" +
	(addingGame ? (prevGameHour.equals("") ? "" : i == Integer.parseInt(prevGameHour) ? "SELECTED" : "") :
                      (gc.get(Calendar.HOUR) == i ? "SELECTED" : "")) +
        ">" + i);
   }
%>
</SELECT>

 <SELECT NAME="req_gameMinute">
<OPTION VALUE="">
<OPTION VALUE="00" <%= addingGame ? (prevGameMinute.equals("00") ? "SELECTED" : "") : (gc.get(Calendar.MINUTE) == 0 ? "SELECTED" : "") %>>00
<OPTION VALUE="15" <%= addingGame ? (prevGameMinute.equals("15") ? "SELECTED" : "") : (gc.get(Calendar.MINUTE) == 15 ? "SELECTED" : "") %>>15
<OPTION VALUE="30" <%= addingGame ? (prevGameMinute.equals("30") ? "SELECTED" : "") : (gc.get(Calendar.MINUTE) == 30 ? "SELECTED" : "") %>>30
<OPTION VALUE="45" <%= addingGame ? (prevGameMinute.equals("45") ? "SELECTED" : "") : (gc.get(Calendar.MINUTE) == 45 ? "SELECTED" : "") %>>45
</SELECT>

 <SELECT NAME="gameAMPM">
<OPTION VALUE="AM" <%= addingGame ? "" : (gc.get(Calendar.AM_PM) == Calendar.AM ? "SELECTED" : "")%>>AM
<OPTION VALUE="PM" <%= addingGame ? "SELECTED" : (gc.get(Calendar.AM_PM) == Calendar.PM ? "SELECTED" : "")%>>PM
</SELECT>

</TD></TR>

<TR>
<TD>Home Locker Room:</TD>
<TD>
<INPUT TYPE="text" NAME="homeLockerRoom" VALUE="<%= addingGame ? "" : game.getHomeLockerRoom()%>"
       MAXLENGTH="2" SIZE="2">
</TD></TR>

<TR>
<TD>Visitor Locker Room:</TD>
<TD>
<INPUT TYPE="text" NAME="homeLockerRoom" VALUE="<%= addingGame ? "" : game.getVisitorLockerRoom()%>"
       MAXLENGTH="2" SIZE="2">
</TD></TR>
</TABLE>
<% if (isAdmin) { %>
<BR><CENTER><INPUT TYPE="SUBMIT" VALUE=" <%= addingGame ? "Add" : "Update" %> this game "></CENTER>
<% } %>

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
