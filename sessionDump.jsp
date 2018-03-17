<!-- $Header: /u02/cvsroot/hockeystats/sessionDump.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*,java.util.*" isThreadSafe="false" %>

<HTML>

<HEAD>
<TITLE>Session Dump</TITLE>
<META name="description" content="">
<META name="keywords" content="">

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<FONT SIZE=3><B>Session Attributes</B></FONT><BR>
Session ID: <%=session.getId()%><br>
Session created: <%=new Date(session.getCreationTime())%><BR>
Session timeout interval: <%=session.getMaxInactiveInterval()%> seconds<BR>
Session last accessed: <%=new Date(session.getLastAccessedTime())%><BR><BR>
<%
  Enumeration sessionAttribs = session.getAttributeNames();
  while (sessionAttribs.hasMoreElements()) {
    String attribName = (String)sessionAttribs.nextElement();
    out.println ("param = " + attribName + ": " + session.getAttribute(attribName) + "<BR>");
  }
%>
<BR><BR><BR>
<FONT SIZE=3><B>Application Attributes</B></FONT><BR>
<%
  Enumeration appAttribs = application.getAttributeNames();
  while (appAttribs.hasMoreElements()) {
    String attribName = (String)appAttribs.nextElement();
    out.println ("param = " + attribName + ": " + application.getAttribute(attribName) + "<BR>");
  }
%>
<BR>

</BODY>
</HTML>
