<!-- $Header: /u02/cvsroot/hockeystats/login.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<HTML>
<TITLE>Jon's AHL - Login</TITLE>
<BODY TEXT="black" LINK="blue" BGCOLOR="FFFFFF">

<%@include file="template.jsp"%>

<HEAD><CENTER><H1>Administrative User Login</H1></CENTER></HEAD>
<FORM METHOD="POST"  ACTION="servlet/LoginServlet">
<BR><TABLE ALIGN=CENTER BORDER="0" >
<TR>
<TD VALIGN=CENTER ALIGN=RIGHT>
<B>User ID:</B>
</TD> <TD VALIGN=TOP>
<B><INPUT NAME= "userId"
TYPE= "TEXT"
MAXLENGTH= "10"
SIZE= "10"></B>
</TD>
</TR> <TR>
<TD VALIGN=CENTER ALIGN=RIGHT>
<B>Password:</B>
</TD> <TD VALIGN=TOP>
<B><INPUT NAME= "password"
TYPE= "Password"
MAXLENGTH= "15"
SIZE= "15"></B>
</TD>
</TR>
</TABLE>
<BR><CENTER><B><INPUT VALUE= "  Log In  " TYPE= "SUBMIT"></B></CENTER>
</FORM>
<%@include file="template_end.html"%>
</BODY>
</HTML>
