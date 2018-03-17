<!-- $Header: /u02/cvsroot/hockeystats/Player.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="java.util.*,hockeystats.*, java.net.URLEncoder" session="true" isThreadSafe="false" %>

<%!
   boolean isAdmin = false;
   boolean addingPlayer = true;  // if false then we are modifying a player
   int playerIndex;              // if modifying a player, index into players[] arrary
   Player aPlayer = null;
   String playerSkillLevel;
   String playerTeamName;
   String playerPrefPosition;
   String emptyString = "";
   String teamName;
   ArrayList players = null;
%>

<%
  if (session.isNew())
  {
%>
<jsp:forward page="sessionTimeout.jsp" />
<%
  }
%>

<HTML>

<HEAD>
<TITLE>Player Maintenance</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<%
  String isAdminAttr = (String)session.getAttribute("isAdmin");
  if (isAdminAttr != null && isAdminAttr.equals("Y"))
  {
    isAdmin = true;
  }

  String index = request.getParameter("index");
  if (index != null)
  {
    // We are modifying an existing player.  Get the roster the user was looking at.
    teamName = request.getParameter("team");
    if (teamName.equals("ALL"))
      players = Player.getAllPlayers(
                     (SeasonSession)session.getAttribute("hockeystats.selectedSeasonSession"),
                     dbContext);
    else
    {
      Team theTeam = new Team(teamName, dbContext);

      TeamRoster roster = theTeam.getTeamRoster();
      players = roster.getPlayers();
    }

    // Now get the player info for the selected player.
    playerIndex = Integer.parseInt(index);
    addingPlayer = false;
    aPlayer = (Player)players.get(playerIndex);
    playerSkillLevel = aPlayer.getSkillLevel();
    playerTeamName = aPlayer.getTeam().getTeamName();
    playerPrefPosition = aPlayer.getPrefPosition();
    if (playerPrefPosition == null) playerPrefPosition = "ANY";
  }
  else
    addingPlayer = true;
%>

<FONT FACE="Modern" SIZE=5><B><CENTER><%= addingPlayer ? "Add a" : "Update a" %> player</CENTER></B></FONT><BR>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="DataServlet" onSubmit="return checkrequired(this)">

<INPUT TYPE="hidden" NAME=sourcePage VALUE="player">

<%
  if (!addingPlayer)
  {
%>
<INPUT TYPE="hidden" NAME=playerIndex VALUE=<%= playerIndex %>>
<INPUT TYPE="hidden" NAME=teamName VALUE=<%=URLEncoder.encode(teamName)%>>
<%
  }
%>

<TABLE WIDTH=500 BORDER=0 CELLSPACING=0 CELLPADDING=5>

<TR>
<TD WIDTH=100>First and Last Name:</TD>
<TD><FONT COLOR="RED">*</FONT>
<INPUT TYPE="text" NAME="req_firstName" VALUE="<%= addingPlayer ? emptyString : aPlayer.getFName()%>"
       MAXLENGTH="15" SIZE="15">
<FONT COLOR="RED">*</FONT>
<INPUT TYPE="text" NAME="req_lastName" VALUE="<%= addingPlayer ? emptyString : aPlayer.getLName()%>"
       MAXLENGTH="20" SIZE="20" ALIGN=left></TD></TR>

<%--
<TR>
<TD>Street Address:</TD>
<TD><INPUT TYPE="text" NAME="address" VALUE="<%= addingPlayer ? emptyString : aPlayer.getAddress()%>"
       MAXLENGTH="40" SIZE="40"></TD></TR>

<TR>
<TD>City:</TD>
<TD><INPUT TYPE="text" NAME="city" VALUE="<%= addingPlayer ? emptyString : aPlayer.getCity()%>"
       MAXLENGTH="30" SIZE="30"></TD></TR>

<TR>
<TD>State:</TD>
<TD><INPUT TYPE="text" NAME="state" VALUE="<%= addingPlayer ? emptyString : aPlayer.getState()%>"
       MAXLENGTH="2" SIZE="2"> Zip: 
<INPUT TYPE="text" NAME="zip" VALUE="<%= addingPlayer ? emptyString : Integer.toString(aPlayer.getZip())%>"
       MAXLENGTH="5" SIZE="5" onBlur="valid_num(this)"></TD></TR>

<TR>
<TD>Phone Number:</TD>
<TD>
<INPUT TYPE="text" NAME="areaCode" VALUE="<%= addingPlayer ? emptyString : aPlayer.getPhoneNumber().substring(0,3) %>"
       MAXLENGTH="3" SIZE="3" onBlur="valid_num(this)">) 
<INPUT TYPE="text" NAME="prefix" VALUE="<%= addingPlayer ? emptyString : aPlayer.getPhoneNumber().substring(3,7) %>"
       MAXLENGTH="3" SIZE="3" onBlur="valid_num(this)"> - 
<INPUT TYPE="text" NAME="phoneExtn" VALUE="<%= addingPlayer ? emptyString : aPlayer.getPhoneNumber().substring(6) %>"
       MAXLENGTH="4" SIZE="4" onBlur="valid_num(this)"></TD></TR>

<TR>
<TD>E-mail Address:</TD>
<TD><INPUT TYPE="text" NAME="emailAddr" VALUE="<%= addingPlayer ? emptyString : aPlayer.getEmailAddr()%>"
           MAXLENGTH="50" SIZE="30"></TD></TR>
<TR>
<TD VALIGN="TOP">Skill level:</TD>
<TD>
<INPUT TYPE="radio" NAME="skillLevel" VALUE="A"
       <%= addingPlayer ? emptyString : playerSkillLevel.equals("A") ? "CHECKED" : emptyString %>> A<BR>
<INPUT TYPE="radio" NAME="skillLevel" VALUE="B"
       <%= addingPlayer ? "CHECKED" : playerSkillLevel.equals("B") ? "CHECKED" : emptyString %>> B<BR>
<INPUT TYPE="radio" NAME="skillLevel" VALUE="C"
       <%= addingPlayer ? emptyString : playerSkillLevel.equals("C") ? "CHECKED" : emptyString %>> C
</TD></TR>

--%>

<TR>
<TD>Jersey Number:</TD>
<TD><INPUT TYPE="text" NAME="jerseyNumber" VALUE="<%= addingPlayer ? emptyString : Integer.toString(aPlayer.getJerseyNumber())%>"
           MAXLENGTH="2" SIZE="2" onBlur="valid_num(this)"></TD></TR>

<TR>
<TD>Select Team:</TD>
<TD><SELECT NAME="team">
<OPTION VALUE="NOT PLACED" <%= addingPlayer ? "SELECTED" : playerTeamName.equals("NOT PLACED") ? "SELECTED" : emptyString %>>Not placed
<%!  hockeystats.Team[] teams = null; %>
<%
	teams = hockeystats.Team.getAllTeams(selectedSeasonSession, dbContext);
	for (int i=0; i<teams.length; i++)
	{
	   String theTeamName = teams[i].getTeamName();
           out.println ("<OPTION VALUE=\"" + theTeamName + "\" " + (addingPlayer ? emptyString : playerTeamName.equals(theTeamName) ? "SELECTED" : emptyString) + ">" + theTeamName);
	}
%>
</SELECT>
</TD></TR>

<TR>
<TD>Preferred Position:</TD>
<TD><SELECT NAME="prefPosition">
<OPTION VALUE="ANY" <%= addingPlayer ? "SELECTED" : playerPrefPosition.equals("ANY") ? "SELECTED" : emptyString %>>ANY
<OPTION VALUE="FORWARD" <%= addingPlayer ? "SELECTED" : playerPrefPosition.equals("FORWARD") ? "SELECTED" : emptyString %>>FORWARD
<OPTION VALUE="DEFENSE" <%= addingPlayer ? "SELECTED" : playerPrefPosition.equals("DEFENSE") ? "SELECTED" : emptyString %>>DEFENSE
<OPTION VALUE="GOALIE" <%= addingPlayer ? "SELECTED" : playerPrefPosition.equals("GOALIE") ? "SELECTED" : emptyString %>>GOALIE
</SELECT>
</TD></TR>

</TABLE>
<BR>
Can play goalie?<INPUT NAME="canPlayGoal" TYPE="checkbox" VALUE="Y" <%= addingPlayer ? "" : aPlayer.canPlayGoal() ? "CHECKED" : "" %>><BR>
Display personal information to other registered users?
<INPUT TYPE="checkbox" NAME="displayOnLine" VALUE="Y" <%= addingPlayer ? "" : aPlayer.displayOnLine() ? "CHECKED" : "" %>>
<br><br>

<% if (isAdmin) { %>
<CENTER><INPUT TYPE="SUBMIT" VALUE=" <%= addingPlayer ? "Add" : "Update" %> this player "></CENTER>
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
