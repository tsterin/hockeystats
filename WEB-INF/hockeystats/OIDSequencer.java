// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/OIDSequencer.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import hockeystats.util.db.*;
import org.apache.log4j.Category;
import java.math.*;
import java.util.*;
import hockeystats.util.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Class to represent a sequence of OIDs.  Implementation is an Oracle sequence.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: OIDSequencer.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.5  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.4  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.3  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.2  2001/12/10 07:09:45  tom
 * Add cvs Log section.
 *
 * Revision 1.1.1.1  2001/12/09 22:32:50  tom
 * Initial versions
 *
 */

public class OIDSequencer {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/OIDSequencer.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  private static String nextInSequence = "SELECT oid_sequence.nextval next_oid FROM dual";
  public static BigDecimal next(DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());

    Category log = Log4jUtils.initLog("OIDSequencer");
    Vector results = null;

    try {
      //      log.debug ("getting next value in sequence");
      results = db.executeQuery(nextInSequence, null);
    }
    catch (java.sql.SQLException sqlEx) {
      while (sqlEx != null)
      {
	System.out.println (sqlEx.getErrorCode() + ": " +sqlEx.getSQLState());
	System.out.println (sqlEx.toString());
	sqlEx = sqlEx.getNextException();
      }
    }

    Hashtable record = (Hashtable) results.get(0);
    return DBUtils.getBigDecimal("NEXT_OID", record);

  } // End, next()

} // End, class OIDSequencer
