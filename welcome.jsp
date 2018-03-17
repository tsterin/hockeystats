<!-- $Header: /u02/cvsroot/hockeystats/welcome.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<HTML>
<HEAD> <B> Welcome  <%= session.getValue("userID")%> </B>  </HEAD>
<BODY>
<BR><BR>
<% if (session.getValue("isAdmin").equals("Y")) { %>
You are logged in as an admin.
<% } else { %>
You are logged in as a regular user.
<% } %>
<BR>Your OID is <%= session.getValue("userOID") %>
</BODY>
</ HTML>
