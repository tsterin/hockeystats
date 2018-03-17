// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Motd.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;
import hockeystats.util.db.*;
import java.util.*;
import java.sql.*;

 // Import log4j classes.
import hockeystats.util.Log4jUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Class to represent the message of the day in the database
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: Motd.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.2  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.1  2002/08/23 01:52:57  tom
 * Object to maintain the message-of-the-day.
 *
 *
 */

public class Motd extends DbStore {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Motd.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  private String messageText;
  private DbContext myDbContext = new DbContext("properties.hockeystats.util_db_DBCacheProps_Novice");


  /**
   * Create a new, empty MOTD object.
   *
   */

  public Motd()
  {
    messageText = null;
  } // End, constructor


  /**
   * Get message text
   */
  public String getMessageText()
  {
    DBEngine db = new DBEngine(myDbContext.getContextString());
    if (messageText == null)
      {
	String query = "SELECT message_text FROM motd";
	Object params[] = new Object[0];
	try {
	  Vector results = db.executeQuery(query, params);
	  Hashtable record = (Hashtable) results.get(0);
	  messageText = DBUtils.getString("MESSAGE_TEXT", record, "");
	}
	catch (java.sql.SQLException sqle) {
	  log.error ("Error getting message of the day text: " + sqle);
	}
      }

    return messageText;

  } // End, getMessageText()


  /**
   * Set message text and store in database
   */
  public void setMessageText (String messageText)
  {
    this.messageText = messageText;
    modified = true;
    log.debug ("Setting messageText to: " + this.messageText + "<");
    try {
      store(new DBEngine(myDbContext.getContextString()));
    }
    catch (DbStoreException e) {
      log.error ("Error storing message of the day: " + e);
    }
  } // End, setMessageText()


  public void store(DBEngine db) throws DbStoreException
  {
    // If we don't need to do anything, bail out now.
    if (inserted && !modified) return;

    int numRows;

    String query =
      "UPDATE motd SET message_text = ?";

    Object[] params = new Object[1];
    params[0] = messageText;

    try {
      log.debug ("SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (query, params));
      if ((numRows = db.executeUpdate(query, params)) != 1)
	log.error ("Update only updated " + numRows + " rows.");
    }
    catch (java.sql.SQLException e) {
     throw new DbStoreException(e.toString());
   }

  } // End, store()


  public String toString()
  {
    return messageText;
  } // End, toString()

} // End, class Motd

