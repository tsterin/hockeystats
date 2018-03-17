// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Season.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import java.math.*;
import java.util.*;
import hockeystats.util.*;
import hockeystats.util.db.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Class to represent the season.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: Season.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.5  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.4  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.3  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.2  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.1  2001/12/20 14:19:59  tom
 * Latest changes.
 *
 * Revision 1.2  2001/12/10 07:09:45  tom
 * Add cvs Log section.
 *
 */

public class Season extends DbStore {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Season.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  private BigDecimal oid;
  private String seasonName;

  private static Season currentSeason = null;

  public static class NoSuchSeasonException extends Exception
  {
    public NoSuchSeasonException(String message)
    {
      super(message);
    }
  }

  /**
   * Constructor to create a brand new Season object
   */
  public Season(DbContext dbContext)
  {
    oid = OIDSequencer.next(dbContext);
    inserted = false;
  } // End, constructor

  /**
   * This constructor is called by getAllSeasons() to create
   * a Season object from the data in the database.
   */

  private Season (BigDecimal oid, String name)
  {
    this.oid = oid;
    this.seasonName = name;
  } // End, Season(oid, name)


  /**
   * Constructor to create a Season object given an OID
   */
  public Season(BigDecimal oid, DbContext dbContext) throws NoSuchSeasonException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    String query = "SELECT oid, season_name FROM season WHERE oid = ?";

    //log.debug ("Get Season with oid = " + oid);

    Object[] params = new Object[1];
    params[0] = oid;

    try {
      //log.debug ("SQL: " + DBUtils.getSQLStatementFromPreparedStatement (query, params));
      Vector results = db.executeQuery (query, params);
      Hashtable record = (Hashtable) results.get(0);
      this.oid = DBUtils.getBigDecimal ("OID", record);
      this.seasonName = DBUtils.getString("SEASON_NAME", record);
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting season: " + sqle);
      throw new NoSuchSeasonException(sqle.toString());
    }

    this.inserted = true;

  } // End, constructor

  /**
   * Factory method to return an existing Season object given a season name
   */
  public static Season getSeason(String seasonName, DbContext dbContext) throws NoSuchSeasonException
  {
    Category log = Log4jUtils.initLog("Season");
    log.debug ("Getting object for season name: " + seasonName);

    DBEngine db = new DBEngine(dbContext.getContextString());
    Season theSeason = new Season(dbContext);

    String query = "SELECT oid, season_name FROM season WHERE season_name = ?";

    log.debug ("Get Season with name = " + seasonName);

    Object[] params = new Object[1];
    params[0] = seasonName;

    try {
      log.debug ("SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (query, params));
      Vector results = db.executeQuery (query, params);
      Hashtable record = (Hashtable) results.get(0);
      theSeason.oid = DBUtils.getBigDecimal ("OID", record);
      theSeason.setSeasonName(DBUtils.getString("SEASON_NAME", record));
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting season: " + sqle);
      return null;
    }

    theSeason.inserted = true;
    return theSeason;

  } // End, getSeason()


  /**
   * Factory method to create a brand new season, given a season name.
   */
  public static Season newSeason(String seasonName, DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Season");
    log.debug ("Creating object for season name: " + seasonName);

    Season theSeason = new Season(dbContext);
    theSeason.setSeasonName(seasonName);

    // Create the seasonSessions within this season.
    SeasonSession mySessions[] = SeasonSession.createSessionsForSeason(theSeason, dbContext);


    DBEngine db = new DBEngine(dbContext.getContextString());

    try {
      db.beginTransaction();
    }
    catch (java.sql.SQLException e) {
      log.error ("*** newSeason(), error on beginTransaction" + e.toString());
      return null;
    }


    // Persist season to the database

    try {
      theSeason.store(db);
    }
    catch (DbStore.DbStoreException e) {
      log.error ("*** newSeason(), error saving season: " + e.toString());
      try {
	db.rollback();
	return null;
      }
      catch (java.sql.SQLException e2) {}
    } // End, DbStoreException


    // Persist all the season sessions to the database

    int i=0;
    try {
      for (i=0; i<mySessions.length; i++)
	mySessions[i].store(db);
    }
    catch (DbStore.DbStoreException e) {
      log.error ("*** newSeason(), error saving session " + i + ": " + e.toString());
      try {
	db.rollback();
	return null;
      }
      catch (java.sql.SQLException e2) {}
    } // End, DbStoreException

    try {
      db.commit();
    }
    catch (java.sql.SQLException e) {
      log.error ("*** newSeason(), error committing transaction" + e.toString());
      return null;
    }


    return theSeason;

  } // End, newSeason()


  /**
   * Get the season-name (eg, Winter 2001-2002)
   */
  public String getSeasonName() { return seasonName; };

  /**
   * Set the season-year (eg, Winter 2001-2002)
   *
   * @param seasonName String containing the season name
   */
  public void setSeasonName(String seasonName) { this.seasonName = seasonName; };

  public BigDecimal getOID() { return oid; }


  /**
   * Get all the seasons
   *
   * @return seasons Array of Season objects
   */

  public static Season[] getAllSeasons(DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());

    Category log = Log4jUtils.initLog("Season");
    log.debug ("Getting all seasons.");

    String query = "SELECT oid, season_name FROM season";
    Season[] seasons = null;

    try {
      Vector results = db.executeQuery (query, null);
      int numSeasons = results.size();
      seasons = new Season[numSeasons];
      for (int index=0; index<results.size(); index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	BigDecimal seasonOID = DBUtils.getBigDecimal("OID", record);
	String seasonName = DBUtils.getString("SEASON_NAME", record);
	seasons[index] = new Season(seasonOID, seasonName);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting season name: " + sqle);
    }

    return seasons;
    
  } // End, getAllSeasons()


  public static Season getCurrentSeason(DbContext dbContext)
  {
    if (currentSeason == null)
    {
      currentSeason = SeasonSession.getCurrentSeasonSession(dbContext).getSeason();
    }

    return currentSeason;
  } // End, getCurrentSeason()


  public void store(DBEngine db) throws DbStoreException
  {
    int numRows;

    String storeSeason =
      "INSERT INTO season (season_name, oid) VALUES (?, ?)";

   String updateSeason =
     "UPDATE season SET season_name = ? WHERE oid = ?";

   Object[] params = new Object[2];
   int x=0;
   params[x++] = getSeasonName();
   params[x++] = getOID();

   String query;
   if (inserted)
     query = updateSeason;
   else
     query = storeSeason;

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
    return getSeasonName();
  } // End, toString();


  public boolean equals(Object target)
  {
    if (!(target instanceof Season))
      return false;

    if (((Season)target).getOID().equals(this.getOID()))
      return true;
    else
      return false;
  } // End, equals()

} // End, class Season
