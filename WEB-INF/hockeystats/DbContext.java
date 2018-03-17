// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/DbContext.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

/**
 * This class implements a String which is used to define
 * the database context; ie, which database schema is to
 * be used for a connection.
 */

public class DbContext {
  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/DbContext.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  private String contextString;

  /**
   * Constructor.
   *
   * @param context String containing the context for a database connection
   */
  public DbContext (String aContextString) {
    contextString = aContextString;
  }

  // Can't create an empty object.
  private DbContext() {}

  public String getContextString() { return contextString; }

  public String toString() { return "context string: " + contextString; }

}// End, class
