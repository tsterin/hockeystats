// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/DbStore.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import hockeystats.util.*;
import hockeystats.util.db.*;
import java.util.Timer;
import java.util.TimerTask;

/*
 * $Log: DbStore.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.6  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.5  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.4  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.3  2001/12/20 14:19:59  tom
 * Latest changes.
 *
 * Revision 1.2  2001/12/10 07:09:45  tom
 * Add cvs Log section.
 *
 */

public abstract class DbStore extends TimerTask {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/DbStore.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  class DbStoreException extends Exception {
    public DbStoreException(String text)
    {
      super("Error storing to database: " + text);
    }

    private DbStoreException() {}
  }

  boolean inserted;  // Has this game object been persisted to the database?
  boolean modified;  // Has this game object modified since being retrieved from the database?

  private int secondsTillStore=60;  // How long to wait before storing changes.
  private boolean timerRunning = false;
  private Timer myTimer = null;

  protected abstract void store(DBEngine db) throws DbStoreException;

  /**
   * Notify the dbStore-er that the object has been changed and 
   * needs to be persisted to the database.
   */

  public void updateNotify ()
  {
    if (myTimer != null)
      myTimer.cancel();

    myTimer = new Timer();
    myTimer.schedule (this, secondsTillStore*1000);
  } // End, updateNotify()

  public void run()
  {
    try {
      store(new DBEngine("*** this needs to be fixed ***"));
    }
    catch (DbStoreException e) {
      System.out.println ("*** Error from background store() operation: " + e.toString());
    }
  } // End, run()
} // End, DbStore class
