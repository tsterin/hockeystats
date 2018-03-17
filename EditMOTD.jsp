<!-- $Header: /u02/cvsroot/hockeystats/EditMOTD.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<%@ page import="hockeystats.*, java.util.*" isThreadSafe="false" session="true" %>

<jsp:useBean id="motd" class="hockeystats.Motd" scope="application" />

<%
  String isAdminVal = (String)session.getAttribute("isAdmin");
  boolean isAdmin = isAdminVal != null && isAdminVal.equals("Y");

  if (!isAdmin) {
%>
<jsp:forward page="/notAdmin.jsp" />
<%
  }
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
<TITLE>Edit Message of the Day</TITLE>
<META name="description" content="">
<META name="keywords" content="">

<%@include file="reqFields.js"%>

</HEAD>

<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<FONT FACE="Modern" SIZE=5><B><CENTER>Edit Message of the Day</CENTER></B></FONT><BR>

<!--Start of FORM-->

<FORM METHOD=POST ACTION="DataServlet" onSubmit="return checkrequired(this)">

<INPUT TYPE="hidden" NAME=sourcePage VALUE="motd">

<TEXTAREA NAME="messageText" COLS=60 ROWS=15 WRAP=SOFT>
<jsp:getProperty name="motd" property="messageText"/></TEXTAREA>

<CENTER><INPUT TYPE="SUBMIT" NAME="SUBMIT" VALUE=" Save "> &nbsp &nbsp
<INPUT TYPE="SUBMIT" NAME="SUBMIT" VALUE=" Abort "></CENTER>

</FORM>

<!--End of FORM-->

      <h4>You can use HTML codes in the message.  Here are some examples:</h4>
      <dl compact> 
        <dt><code>&lt;font color="red"&gt;</code> . . . <code>&lt;/font&gt;</code>
        <dd>Set text color to <font color="red">red</font>
        <dt><code>&lt;b&gt;</code> . . . <code>&lt;/b&gt;</code>
        <dd> <b>Boldface</b> 
        <dt><code>&lt;i&gt;</code> . . . <code>&lt;/i&gt;</code>
        <dd> <i>Italics</i> 
        <dt><code>&lt;u&gt;</code> . . . <code>&lt;/u&gt;</code>
        <dd> <u>Underline</u> 
        <dt><code>&lt;tt&gt;</code> . . . <code>&lt;/tt&gt;</code>
        <dd> <tt>Typewriter font</tt> 
        <dt><code>&lt;br&gt;</code><BR>
        <dd> Force a line break
      </dl>

<BR>
<% if (isAdmin) { %>
<A HREF="admin.jsp">Return to main admin page</A>
<% } %>

<%@include file="template_end.html"%>
</BODY>
</HTML>
