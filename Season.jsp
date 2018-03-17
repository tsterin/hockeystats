<!-- $Header: /u02/cvsroot/hockeystats/Season.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page isThreadSafe="false" import="hockeystats.*" session="true" %>

<%
  String isAdmin = (String) session.getAttribute("isAdmin");
  if (isAdmin == null || !isAdmin.equals("Y"))
  {
%>
<jsp:forward page="notAdmin.jsp" >
</jsp:forward>
<%
  }
%>


<HTML>
<HEAD>
<TITLE>Add a new season</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>
<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<FONT FACE="Modern" SIZE=5><B><CENTER>Add a new season</CENTER></B></FONT><BR>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="DataServlet" onSubmit="return checkrequired(this)">

<INPUT TYPE="hidden" NAME=sourcePage VALUE="season">

<TABLE WIDTH=70% BORDER=0 CELLSPACING=0 CELLPADDING=5>
<TR>
<TD WIDTH=350>Season Name:</TD>
<TD><FONT COLOR="RED">*</FONT><INPUT TYPE="text" NAME="req_SeasonName" VALUE="" maxlength="25" size="25"></TD></TR>
</TD></TR>

</TABLE>
<br><br>
<CENTER><INPUT TYPE="SUBMIT" VALUE=" Add this season "></CENTER>
</FORM>

</CENTER>

<!--End of FORM-->

<BR><BR><A HREF="admin.jsp">Return to main admin page</A>

<%@include file="template_end.html"%>
</BODY>
</HTML>
