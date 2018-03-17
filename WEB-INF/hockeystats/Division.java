// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Division.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import java.math.*;
import hockeystats.util.*;
import hockeystats.util.db.*;
import java.util.*;
import java.sql.*;

 // Import log4j classes.
import hockeystats.util.Log4jUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Class to represent a division in the league.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: Division.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.7  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.6  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.5  2002/04/08 03:49:11  tom
 * Include the season as a parameter to the default constructor.
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

public class Division extends DbStore {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Division.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  private BigDecimal oid;
  private String divisionName;
  private Season season;
  private BigDecimal seasonOID;
  private DbContext myDbContext = null;

  class NoSuchDivisionException extends Exception {
    public NoSuchDivisionException(String divisionName)
    {
      super("No division found with name: " + divisionName);
    }

    public NoSuchDivisionException(BigDecimal oid)
    {
      super("No division found with oid: " + oid);
    }

    private NoSuchDivisionException() {}
  } // End, class NoSuchDivisionException

  /**
   * Create a Division object for the division with the specified
   * name and season, from the data in the database.
   *
   * @param divisionName Division name of a division in the database
   * @param season Season object of the season the division will be in
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   * @throws NoSuchDivisionException
   */

  public Division (String divisionName, Season aSeason, DbContext dbContext)
    throws NoSuchDivisionException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    String query = "SELECT oid FROM division WHERE name = ? AND season = ?";

    try {
      Object[] params = new Object[2];
      params[0] = divisionName;
      params[1] = aSeason.getOID();

      log.debug("get division, SQL: " + DBUtils.getSQLStatementFromPreparedStatement(query, params));
      Vector results = db.executeQuery(query, params);
      Hashtable record = (Hashtable) results.get(0);
      this.divisionName = divisionName;
      oid = DBUtils.getBigDecimal("OID", record);
      seasonOID = aSeason.getOID();
      season = null;
    }
    catch (SQLException sqle) {
      log.error ("SQL Error getting division info: " + sqle);
      throw new NoSuchDivisionException (divisionName);
    }

    myDbContext = dbContext;

    log.debug ("Created object for division name: " + divisionName);

  } // End, Division(String divisionName)


  /**
   * Create a Division object for the division with the specified
   * oid, from the data in the database.
   *
   * @param divisionOID OID of a division in the database
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   * @throws NoSuchDivisionException if the specified OID is not the OID of a division
   */

  public Division (BigDecimal divisionOID, DbContext dbContext) throws NoSuchDivisionException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    String query = "SELECT name, season FROM division WHERE oid = ?";

    try {
      Object[] params = new Object[1];
      params[0] = divisionOID;

      log.debug("get division, SQL: " + DBUtils.getSQLStatementFromPreparedStatement(query, params));
      Vector results = db.executeQuery(query, params);
      Hashtable record = (Hashtable) results.get(0);
      oid = divisionOID;
      setDivisionName(DBUtils.getString("NAME", record));
      seasonOID = DBUtils.getBigDecimal("SEASON", record);
      season = null;
    }
    catch (SQLException sqle) {
      log.error ("SQL Error getting division info: " + sqle);
      throw new NoSuchDivisionException (divisionOID.toString());
    }

    myDbContext = dbContext;

    //log.debug ("Created object for division OID: " + divisionOID);

  } // End, Division(String divisionOID)


  /**
   * Create an empty Division object for a specific season.
   *
   * @param season Season object for the season the division applies to
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   */

  public Division (Season season, DbContext dbContext)
  {
    oid = OIDSequencer.next(dbContext);
    this.divisionName = null;
    this.seasonOID = season.getOID();
    this.season = null;
    myDbContext = dbContext;
  } // End, Division(Season)


  private Division() {}

  /**
   * This constructor is called by getAllDivisions() to create
   * a Division object from the data in the database.
   */

  private Division (BigDecimal oid, String name, BigDecimal seasonOID, DbContext dbContext)
  {
    this.oid = oid;
    this.divisionName = name;
    this.seasonOID = seasonOID;
    this.season = null;
    myDbContext = dbContext;
  } // End, Division(oid, name, season)

  /**
   * Get all the divisions
   *
   * @return divisions Array of Division objects
   */
  public static Division[] getAllDivisions(SeasonSession seasonSession, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Category log = Log4jUtils.initLog("Division");
    Season selectedSeason = 
      (seasonSession == null ? Season.getCurrentSeason(dbContext) : seasonSession.getSeason());
    //    log.debug ("Getting all divisions for season " +
    //	       seasonSession == null? "Current season" : seasonSession.getSeason().getSeasonName());

    if (selectedSeason == null)
      log.debug ("selectedSeason is null");
    else if (selectedSeason.getSeasonName() == null)
      log.debug ("selectedSeason.getSeasonName() is null");
    else
      log.debug ("season name: " + selectedSeason.getSeasonName());

    String query = "SELECT oid, name, season FROM division WHERE season = ?";
    Division[] divisions = null;

    Object[] params = new Object[1];
    params[0] = selectedSeason.getOID();

    try {
     log.debug ("SQL: " +
		DBUtils.getSQLStatementFromPreparedStatement (query, params));
      Vector results = db.executeQuery (query, params);
      int numDivisions = results.size();
      divisions = new Division[numDivisions];
      for (int index=0; index<numDivisions; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	BigDecimal newDivisionOID = DBUtils.getBigDecimal("OID", record);
	BigDecimal newSeasonOID = DBUtils.getBigDecimal("SEASON", record);
	String newName = DBUtils.getString("NAME", record);
	divisions[index] = new Division(newDivisionOID, newName, newSeasonOID, dbContext);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting division name: " + sqle);
    }

    return divisions;
    
  } // End, getAllDivisions()


  /**
   * Get the OID of the division.
   */

  public BigDecimal getOID() { return oid; }

  /**
   * Set the name of the division
   */
  public void setDivisionName(String divisionName) { this.divisionName = divisionName; }


  /**
   * Get the name of the division
   */
  public String getDivisionName() { return divisionName; }


  /**
   * Set the Season
   */
  public void setSeason(Season season)
  {
    this.season = season;
    if (seasonOID == null) this.seasonOID = season.getOID();
  }


  /**
   * Get the Season using lazy initialization.  The object will not be
   * retrieved from the database until it is accessed for the first time.
   */
  public Season getSeason()
  {
    if (season == null)
    {
      try {
	season = new Season(seasonOID, myDbContext);
      }
      catch (Season.NoSuchSeasonException e) {
	log.error ("Could not find Season object for division: " + e);
        setSeason(null);
      }
    } // End, if season not initialized

    return season;

  } // End, getSeason()


  public void store(DBEngine db) throws DbStoreException
  {
    int numRows;

    String storeDivision =
      "INSERT INTO division (name, season, oid) " +
      "VALUES (?, ?, ?)";

   String updateDivision =
      "UPDATE division SET name = ?, season = ? WHERE oid = ?";

   Object[] params = new Object[3];
   int x=0;
   params[x++] = getDivisionName();
   params[x++] = seasonOID;
   params[x++] = getOID();

   try {
     log.debug ("SQL: " +
		DBUtils.getSQLStatementFromPreparedStatement (storeDivision, params));
     if ((numRows = db.executeUpdate(storeDivision, params)) != 1)
      log.error ("Update only updated " + numRows + " rows.");
   }
   catch (java.sql.SQLException sqlEx) {
     throw new DbStoreException(sqlEx.toString());
   }
  } // End, store()

  /*
  public static void main(String[] args)
  {
    Division[] divisions = Division.getAllDivisions();
    System.out.println("Found " + divisions.length + " division(s).");
  } // End, main()
  */
} // End, class Division

