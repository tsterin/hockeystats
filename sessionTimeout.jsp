<!-- $Header: /u02/cvsroot/hockeystats/sessionTimeout.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*" %>

<HTML>

<HEAD>
<TITLE>Not Admin</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="Black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<BR><BR><BR><H1><FONT color="red" FACE="courier">
Your session has timed out.  The session timeout interval is 
<%= session.getMaxInactiveInterval()/60 %> minutes.<BR>
If you are inactive for this long, you will receive this message and will need to begin again.
<BR>
<BR>
Sorry for the inconvenience.
</FONT></H1>

<%@include file="template_end.html"%>
</BODY>
</HTML>
