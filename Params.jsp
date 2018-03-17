<!-- $Header: /u02/cvsroot/hockeystats/Params.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*" isThreadSafe="false" %>

<HTML>

<HEAD>
<TITLE>JSP Parameters</TITLE>
<META name="description" content="">
<META name="keywords" content="">

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%!
  java.util.Enumeration myParams = null;
%>

<%
  myParams = request.getParameterNames();
  while (myParams.hasMoreElements()) {
%>

param = <%=myParams.nextElement()%><BR>

<%
  }
%>

</BODY>
</HTML>
