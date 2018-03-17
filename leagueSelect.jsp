<!-- $Header: /u02/cvsroot/hockeystats/leagueSelect.jsp,v 1.1 2002/09/15 01:53:23 tom Exp $ -->

<!-- This page allows the user to select the league (Novice or Open) for the application -->

<%@ page import="hockeystats.*" %>

<jsp:useBean id="motd" class="hockeystats.Motd" scope="application" />

<HTML>

<HEAD>
<TITLE>EastSide Hockey - Select League</TITLE>
<META name="description" content="">
<META name="keywords" content="">

</HEAD>

<BODY TEXT="black" BGCOLOR="FFFFFF">
<BR><BR><CENTER><IMG SRC="images/goalieinnet.gif"></CENTER>

<CENTER>
<H1><BR><BR>East Side Hockey<BR>
<H4>Formerly Jon's AHL<BR><BR><I>Fun, Fair, and Competitive Hockey</I>

<H2><BR><BR>
<jsp:getProperty name="motd" property="messageText" />

<BR><BR><BR>Select the league you wish to visit.<BR>

<table cellspacing="50">
<tr>
<td valign="bottom" align="center"><a href="/hockeystats/SelectionServlet?selectItem=League&SelectedLeague=Novice"><img src="images/novice.jpg"><br><H4>Novice</H4></a></td>

<td valign="bottom" align="center"><a href="/hockeystats/SelectionServlet?selectItem=League&SelectedLeague=X"><img src="images/X.jpg"><br><h4>X League</h4></a></td>
</tr>
</table>
<BR>
<H4><CENTER>If you have any questions or comments about this site, e-mail them to <BR><BR>
<A HREF="mailto:jonsadulthockey@yahoo.com">jonsadulthockey@yahoo.com</A></CENTER></H4>
<BR>
<BR>
<center>
<img src=images/PoweredByLinux_aracnet.gif>
</center>


</BODY>
</HTML>
