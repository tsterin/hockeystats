<!-- $Header: /u02/cvsroot/hockeystats/seasonSelection.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*" session="true"  isThreadSafe="false" %>

<HTML>
<HEAD>
<TITLE>Select Season</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>
<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<%
  String headerMsg;
  String servlet;
  String action = request.getParameter("sel");
  if (action != null && action.equals("currentSeason"))
  {
    headerMsg = "Select the new season<BR>(Note: this affects ALL users)";
    servlet = "DataServlet";
  }
  else
  {
    headerMsg = "Select the season:";
    servlet = "SelectionServlet";
  }
%>

<FONT FACE="Modern" SIZE=5><B><CENTER><%=headerMsg%></CENTER></B></FONT><BR>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="<%=servlet%>" onSubmit="return checkrequired(this)">

<INPUT TYPE="hidden" VALUE="Season" NAME="selectItem">
<INPUT TYPE="hidden" VALUE="seasonSelection" NAME="sourcePage">

<CENTER><SELECT NAME="selectedSeasonSession">
<%
  SeasonSessionsBean seasonSessionsBean = 
	(SeasonSessionsBean)session.getAttribute("hockeystats.seasonSessionsBean");
  SeasonSession[] seasonSessions = seasonSessionsBean.getSeasonSessions();

  for (int i=0; i<seasonSessions.length; i++)
  {
    String ss = seasonSessions[i].getSeason().getSeasonName() + " - " + seasonSessions[i].getSessionName();
    boolean isCurrent = seasonSessions[i].isCurrent();
    out.println ("<OPTION VALUE=\"" + i + "\" " + (isCurrent ? "SELECTED" : "") + ">" + ss);
  }
%>
</SELECT></CENTER>

<CENTER><INPUT TYPE="SUBMIT" VALUE=" Go! "></CENTER>
</FORM>

</CENTER>

<!--End of FORM-->

<%@include file="template_end.html"%>
</BODY>
</HTML>
