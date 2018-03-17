// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/SeasonSessionsBean.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import hockeystats.util.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * SeasonSessions bean
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: SeasonSessionsBean.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.4  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.3  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.2  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.1  2001/12/20 14:19:59  tom
 * Latest changes.
 *
 *
 */

public class SeasonSessionsBean {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/SeasonSessionsBean.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);
  private SeasonSession[] seasonSessions;

  SeasonSessionsBean(DbContext dbContext)
  {
    //log.info ("Getting all SeasonSessions.");
    seasonSessions = SeasonSession.getAllSeasonSessions(dbContext);
  }

  public SeasonSession[] getSeasonSessions() { return seasonSessions; }

  public String toString()
  {
    StringBuffer retString = new StringBuffer("Size: " + seasonSessions.length + "\n");
    for (int i=0; i<seasonSessions.length; i++)
      retString.append(seasonSessions[i] + "\n");
    return retString.toString();
  } // End, toString()

}// end of SeasonSessions bean
