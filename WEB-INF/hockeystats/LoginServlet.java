// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/LoginServlet.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import hockeystats.util.*;
import hockeystats.util.db.*;
import java.math.*;
import java.util.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Login servlet
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: LoginServlet.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.8  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.7  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.6  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.5  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.4  2001/12/20 14:19:59  tom
 * Latest changes.
 *
 * Revision 1.3  2001/12/10 07:05:48  tom
 * Put logging back in, add cvs Log section.
 *
 */
public class LoginServlet extends HttpServlet {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/LoginServlet.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  private boolean validateUser (String userId, String password, HttpSession userSession)
  {
    String getLoginSQL = 
      "SELECT oid, full_name, admin FROM users WHERE username = ? AND password = ?";

    DBEngine db = new DBEngine(((DbContext)userSession.getAttribute("hockeystats.dbContext")).getContextString());

    Object[] params = new Object[2];
    params[0] = userId.toUpperCase();
    params[1] = password.toUpperCase();
    Vector results;

    try {
      results = db.executeQuery (getLoginSQL, params);
    }
    catch (java.sql.SQLException sqle) {
      return false;
    }

    if (results.size() == 0)
      return false;

    Hashtable record = (Hashtable) results.get(0);
    
    // put the user name session variable.
    userSession.putValue("isAdmin", DBUtils.getString("ADMIN", record));
    userSession.putValue("userOID", DBUtils.getBigDecimal("OID", record));
    userSession.putValue("userID", userId);
    userSession.putValue("userName", DBUtils.getString("FULL_NAME", record));

    log.info ("User " + userId + " has logged in.");

    return true;

  } // End, validateUser()


  public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    String userId = req.getParameter("userId");
    String password = req.getParameter("password");

    HttpSession userSession = req.getSession();

    /**********************************************************************
     ** call a method to validate the password which will return
     ** true for authorized users and false for un-authorized.
     **********************************************************************/
    String userName;

    if (!validateUser(userId, password, userSession))
    {
      PrintWriter ot = res.getWriter();
      ot.println(" Incorrect userid and/or password");
      ot.close();
    }
    else
    {
      res.sendRedirect("/hockeystats/mainpage.jsp");
    }
  }// end of doPost

  public void init() throws ServletException
  {
    log.info ("Initializing servlet Login");

    // Set application-wide attributes here.

  }

}// end of servlet class
