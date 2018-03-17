// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/SeasonSession.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;
import java.math.*;
import java.util.*;
import hockeystats.util.*;
import hockeystats.util.db.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Class to represent the seasonSession.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: SeasonSession.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.7  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.6  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.5  2002/04/11 02:18:56  tom
 * Don't keep currentSeasonSession as a static member.
 *
 * Revision 1.4  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.3  2002/04/08 03:50:59  tom
 * Explicitly create a DBEngine object when needed.
 *
 * Revision 1.2  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.1  2001/12/20 14:19:59  tom
 * Latest changes.
 *
 *
 */

public class SeasonSession extends DbStore {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/SeasonSession.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  private BigDecimal oid;
  private Season season;
  private int sessionID;
  private String sessionName;
  private boolean current;
  private boolean rollupStatsToTotals;

  public static int PreSeasonSessionID=0;
  public static int RegularSeasonSessionID=1;
  public static int PostSeasonSessionID=2;

  public static class InvalidSeasonSessionException extends Exception
  {
    public InvalidSeasonSessionException(String message)
    {
      super(message);
    }
  }

  /**
   * Constructor to create a brand new SeasonSession object
   */
  public SeasonSession(Season season, int sessionID, String sessionName, boolean rollUpStatsToTotals,
		       DbContext dbContext)
  {
    this.oid = OIDSequencer.next(dbContext);
    setSeason(season);
    setSessionName(sessionName);
    setSessionID(sessionID);
    setCurrent(false);
    setRollupStatsToTotals(rollupStatsToTotals);

    inserted = false;
  }


  /**
   * Constructor to create a SeasonSession from the data in the database.  Called by getAllSeasonSessions
   */
  public SeasonSession(BigDecimal oid, Season season, int sessionID, String sessionName,
		       boolean current, boolean rollupStatsToTotals)
  {
    this.oid = oid;
    setSeason(season);
    setSessionName(sessionName);
    setSessionID(sessionID);
    setCurrent(current);
    setRollupStatsToTotals(rollupStatsToTotals);

    inserted = true;
  }


  /**
   * Constructor to create a SeasonSession object given its OID
   */
  public SeasonSession(BigDecimal oid, DbContext dbContext) throws InvalidSeasonSessionException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());

    String query = "SELECT oid, season_id, s.session_id session_id, session_name, is_current, " +
      "rollup_stats_to_totals FROM season_session ss, sessions s " +
      "WHERE oid = ? and s.session_id = ss.session_id";

    Object[] params = new Object[1];
    params[0] = oid;

    try {
      Vector results = db.executeQuery (query, params);
      Hashtable record = (Hashtable) results.get(0);
      this.oid = DBUtils.getBigDecimal ("OID", record);
      try {
	setSeason(new Season(DBUtils.getBigDecimal("SEASON_ID", record), dbContext));
      }
      catch (Season.NoSuchSeasonException e) {
	log.debug ("No season found for SeasonSession, oid = " + this.oid.toString());
	throw new InvalidSeasonSessionException("No such season found.");
      }
      setSessionName(DBUtils.getString("SESSION_NAME", record));
      setSessionID(DBUtils.getBigDecimal("SESSION_ID", record).intValue());
      setCurrent(DBUtils.getString("IS_CURRENT", record).equals("Y"));
      setRollupStatsToTotals(DBUtils.getString("ROLLUP_STATS_TO_TOTALS", record).equals("Y"));
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting seasonSession: " + sqle);
      throw new InvalidSeasonSessionException("Error retrieving data from database.");
    }

    this.inserted = true;

  } // End, constructor


  /**
   * Constructor to create an empty SeasonSession object.  This will be 
   * populated from data in the database, so inserted is set to true.
   */
  public SeasonSession()
  {
    inserted = true;
  }


  /**
   * A factory method to create a new set of seasonSession objects for a new Season
   */
  public static SeasonSession[] createSessionsForSeason(Season theSeason, DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Season");
    log.debug ("Creating sessions for season name: " + theSeason.getSeasonName());

    SeasonSession seasonSessions[] = null;
    DBEngine db = new DBEngine(dbContext.getContextString());
    String query = "SELECT session_id, session_name, rollup_stats_to_totals FROM sessions";

    Object[] params = new Object[0];

    try {
      Vector results = db.executeQuery (query, params);
      seasonSessions = new SeasonSession[results.size()];
      for (int i=0; i<results.size(); i++)
      {
	Hashtable record = (Hashtable) results.get(i);
	int sessionID = DBUtils.getBigDecimal ("SESSION_ID", record).intValue();
	String sessionName = DBUtils.getString("SESSION_NAME", record);
	boolean rollUpStatsToTotals = DBUtils.getString("ROLLUP_STATS_TO_TOTALS", record).equals("Y");
	seasonSessions[i] = new SeasonSession(theSeason, sessionID, sessionName, rollUpStatsToTotals, 
					       dbContext);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting sessions: " + sqle);
      return null;
    }

    return seasonSessions;

  } // End, createSessionsForSeason()


  /**
   * Get the season object
   */
  public Season getSeason() { return season; };


  /**
   * Set the season object
   *
   * @param season a Season object
   */
  public void setSeason(Season season) { this.season = season; };


  /**
   * Get the session ID
   */
  public int getSessionID() { return sessionID; };


  /**
   * Set the session ID
   *
   * @param sessionID Integer containing the session ID
   */
  private void setSessionID(int sessionID) { this.sessionID = sessionID; };


  /**
   * Get the session-name (eg, "Regular season")
   */
  public String getSessionName() { return sessionName; };


  /**
   * Set the session-name (eg, "Regular season")
   *
   * @param sessionName String containing the session name
   */
  public void setSessionName(String sessionName) { this.sessionName = sessionName; };


  /**
   * Is this session the current session?
   */
  public boolean isCurrent() { return current; };


  /**
   * Sets if stats from this session roll up to players' totals
   */
  public void setRollupStatsToTotals(boolean rollup) { this.rollupStatsToTotals = rollup; };

  /**
   * Do stats from this session roll up to players' totals?
   */
  public boolean getRollupStatsToTotals() { return rollupStatsToTotals; };


  /**
   * Sets if this is or is not the current session.
   */
  public void setCurrent(boolean current) { this.current = current; modified = true; };

  public BigDecimal getOID() { return oid; }

  private void setOID(BigDecimal oid) { this.oid = oid; modified = true; }


  public static SeasonSession getCurrentSeasonSession(DbContext dbContext)
  {
    SeasonSession currentSeasonSession = null;
    DBEngine db = new DBEngine(dbContext.getContextString());
    Category log = Log4jUtils.initLog("SeasonSelection");

    String query = "SELECT oid, season_id, ss.session_id, is_current, session_name, rollup_stats_to_totals " +
      " FROM season_session ss, sessions s WHERE is_current = 'Y' AND s.session_id = ss.session_id";

    log.debug ("Get current SeasonSession.");

    Object[] params = new Object[0];

    try {
      log.debug ("SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (query, params));
      Vector results = db.executeQuery (query, params);
      Hashtable record = (Hashtable) results.get(0);
      currentSeasonSession = new SeasonSession();
      currentSeasonSession.setOID(DBUtils.getBigDecimal ("OID", record));
      currentSeasonSession.setSeason(new Season(DBUtils.getBigDecimal("SEASON_ID", record), dbContext));
      currentSeasonSession.setSessionID(DBUtils.getBigDecimal("SESSION_ID", record).intValue());
      currentSeasonSession.setSessionName(DBUtils.getString("SESSION_NAME", record));
      currentSeasonSession.setCurrent(DBUtils.getString("IS_CURRENT", record).equals("Y"));
      currentSeasonSession.setRollupStatsToTotals(DBUtils.getString("ROLLUP_STATS_TO_TOTALS", record).equals("Y"));
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting seasonSession: " + sqle);
    }
    catch (Season.NoSuchSeasonException e) {
      log.error("Error getting a Season: " + e);
    }

    return currentSeasonSession;

  } // End, getCurrentSeasonSession()


  public static SeasonSession[] getAllSeasonSessions(DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());

    Category log = Log4jUtils.initLog("SeasonSession");

    String query = "SELECT oid, season_id, s.session_id session_id, session_name, is_current, " +
      "rollup_stats_to_totals FROM season_session ss, sessions s WHERE s.session_id = ss.session_id " +
      "ORDER BY season_id DESC, session_id ASC";
    Object[] params = new Object[0];
    SeasonSession[] seasonSessions = null;

    try {
      log.debug ("SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (query, params));
      Vector results = db.executeQuery (query, params);
      seasonSessions = new SeasonSession[results.size()];
      for (int index=0; index<results.size(); index++)
      {
	Hashtable record = (Hashtable) results.get(index);

	BigDecimal seasonSessionOID = DBUtils.getBigDecimal("OID", record);
	Season season = null;
	try {
	  season = new Season(DBUtils.getBigDecimal("SEASON_ID", record), dbContext);
	}
	catch (Season.NoSuchSeasonException e) {
	  log.error ("No season found for SeasonSession, oid = " + seasonSessionOID.toString());
	}
	int sessionID = DBUtils.getBigDecimal("SESSION_ID", record).intValue();
	String sessionName = DBUtils.getString("SESSION_NAME", record);
	boolean isCurrent = DBUtils.getString("IS_CURRENT", record).equals("Y");
	boolean rollupStatToTotals = DBUtils.getString("ROLLUP_STATS_TO_TOTALS", record).equals("Y");

	seasonSessions[index] = new SeasonSession(seasonSessionOID, season, sessionID, sessionName,
						  isCurrent, rollupStatToTotals);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting seasonSession name: " + sqle);
    }

    return seasonSessions;
    
  } // End, getAllSeasonSessions()


  /**
   * The routine called to persist the object to the database.
   */

  public void store(DBEngine db) throws DbStoreException
  {
    // If we don't need to do anything, bail out now.
    if (inserted && !modified) return;

    int numRows;

    String insertSeasonSession =
      "INSERT INTO season_session (season_id, session_id, is_current, oid) " +
      "VALUES (?, ?, ?, ?)";

   String updateSeasonSession =
     "UPDATE season_session SET season_id = ?, session_id = ?, is_current = ? WHERE oid = ?";

   Object[] params = new Object[4];
   int x=0;
   params[x++] = getSeason().getOID();
   params[x++] = new BigDecimal(getSessionID());
   params[x++] = isCurrent() ? "Y" : "N";
   params[x++] = getOID();

   String query;
   if (inserted)
     query = updateSeasonSession;
   else
     query = insertSeasonSession;

   try {
     log.debug ("SQL: " +
		DBUtils.getSQLStatementFromPreparedStatement (query, params));
     if ((numRows = db.executeUpdate(query, params)) != 1)
      log.error ("Update only updated " + numRows + " rows.");
   }
   catch (java.sql.SQLException sqlEx) {
     throw new DbStoreException(sqlEx.toString());
   }

  } // End, store()


  public String toString()
  {
    StringBuffer theString = new StringBuffer();
    theString.append("oid: " + oid.toString());
    theString.append("\nSeason: " + getSeason().toString());
    theString.append("\nSession: " + getSessionName());

    return theString.toString();
  } // End, toString()

  public boolean equals(Object target)
  {
    if (!(target instanceof SeasonSession))
      return false;

    if (((SeasonSession)target).getOID().equals(this.getOID()))
      return true;
    else
      return false;
  } // End, equals()

} // End, class SeasonSession
