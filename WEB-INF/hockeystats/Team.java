// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Team.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;
import java.math.*;
import hockeystats.util.db.*;
import java.util.*;
import java.sql.*;

 // Import log4j classes.
import hockeystats.util.Log4jUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Class to represent a team in the league.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: Team.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.8  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.7  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.6  2002/04/14 14:05:51  tom
 * Fixes for updating players.
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

public class Team extends DbStore implements GlobalVars {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Team.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  private static Hashtable teamLists = new Hashtable(MaxDbContexts);

  private BigDecimal oid;
  private String teamName;
  private String teamColor;
  private Division division;
  private BigDecimal divisionOID;
  private double pctInDivision;
  private Season season;
  private boolean displayedInStandings;
  private TeamRoster roster;
  private TeamStats teamStats;
  private boolean statsModified;

  private DbContext myDbContext;

  private static String teamFields =
  " t.oid oid, team_name, color, division, season, show_in_standings ";

  private static String selectStmt = "SELECT " + teamFields + " FROM team t ";


  public static class NoSuchTeamException extends Exception {
    public NoSuchTeamException(String teamName)
    {
      super("No team found with name: " + teamName);
    }

    private NoSuchTeamException() {}
  } // End, class NoSuchTeamException


  /**
   * Create a new, empty Team object.
   *
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   */

  public Team(DbContext dbContext)
  {
    oid = OIDSequencer.next(dbContext);

    division = null;
    divisionOID = null;
    displayedInStandings = true;
    roster = null;
    teamStats = null;

    myDbContext = dbContext;

    inserted = false;
    modified = false;
    statsModified = false;

    if (teamLists.containsKey(dbContext))
    {
      ArrayList theList = (ArrayList)teamLists.get(dbContext);
      theList.add(this);
    }
  }

  /**
   * Create a Team object for the team with the specified
   * name, from the data in the database.
   *
   * @param teamName String, name of a team in the database
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   * @throws NoSuchTeamException
   */

  public Team (String teamName, DbContext dbContext) throws NoSuchTeamException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    String query = selectStmt + "WHERE team_name = ?";
    String myTeamName = java.net.URLDecoder.decode(teamName);

    myDbContext = dbContext;

    try {
      Object[] params = new Object[1];
      params[0] = myTeamName;
      log.debug("get team, SQL: " + DBUtils.getSQLStatementFromPreparedStatement(query, params));
      Vector results = db.executeQuery(query, params);
      Hashtable record = (Hashtable) results.get(0);
      setTeamInfo(record);
    }
    catch (SQLException sqle) {
      log.error ("SQL Error getting team info: " + sqle);
      throw new NoSuchTeamException(myTeamName);
    }

    roster = null;

    inserted = true;
    modified = false;
    statsModified = false;

  } // End, Team()

  /**
   * Create a Team object for the team with the specified
   * OID, from the data in the database.
   *
   * @param teamOID OID of a team in the database
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   * @throws NoSuchTeamException
   */

  public Team (BigDecimal teamOID, DbContext dbContext) throws NoSuchTeamException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    String query = selectStmt + "WHERE oid = ?";

    myDbContext = dbContext;

    try {
      Object[] params = new Object[1];
      params[0] = teamOID;
      log.debug ("Get team info, sql: " +
		 DBUtils.getSQLStatementFromPreparedStatement(query, params));

      Vector results = db.executeQuery(query, params);
      if (results.size() == 0) throw new NoSuchTeamException ("No team with OID = " + teamOID);
      Hashtable record = (Hashtable) results.get(0);
      //log.debug ("Results: " + record);
      setTeamInfo(record);
    }
    catch (SQLException sqle) {
      log.error ("SQL Error getting team info: " + sqle);
      throw new NoSuchTeamException("Team with oid: " + teamOID);
    }

    roster = null;

    inserted = true;
    modified = false;
    statsModified = false;

  } // End, Team(BigDecimal teamOid)


  /**
   * Get all the teams
   *
   * @return teams Array of Team objects
   */
  public static ArrayList getAllTeams(DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Team");
    DBEngine db = new DBEngine(dbContext.getContextString());
    ArrayList teams = null;

    //log.debug ("Getting all teams.");

    String query = selectStmt;

    // Is the list already cached?  Return the cached list if so.
    if (teamLists.containsKey(dbContext))
    {
      //log.debug ("Using cached list");
      teams = (ArrayList)teamLists.get(dbContext);
    }
    else
    {
      //log.debug("List is not cached - get it from the database.");

      Object[] params = null;

      try {
	Vector results = db.executeQuery (query, params);
	int numTeams = results.size();
	teams = new ArrayList(numTeams);
	for (int index=0; index<results.size(); index++)
	{
	  Hashtable record = (Hashtable) results.get(index);
	  Team theTeam = new Team(dbContext);
	  theTeam.setTeamInfo(record);
	  teams.add(theTeam);
	}
      }
      catch (java.sql.SQLException sqle) {
	log.error("Error getting team name: " + sqle);
      }
      teamLists.put(dbContext, teams);
    }

    
    return teams;
    
  } // End, getAllTeams()


  /**
   * Get all the teams for a given season
   *
   * @return teams Array of Team objects
   */
  public static ArrayList getAllTeams(Season season, DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Team");
    //log.debug("Getting all teams for a season.");

    class SeasonComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Team a = (Team)o1;
	Team b = (Team)o2;

	return a.getOID().compareTo(b.getOID());
      } // End, compare()
    } // End, class SeasonComparator

    ArrayList teams = getAllTeams(dbContext);
    ArrayList returnTeams = new ArrayList(teams.size());

    for (Iterator iter=teams.iterator(); iter.hasNext(); )
    {
      Team t = (Team)iter.next();
      if (t.getSeason().equals(season))
	returnTeams.add(t);
    } // End, for

    Collections.sort(returnTeams, new SeasonComparator());

    returnTeams.trimToSize();
    return returnTeams;

  } // End, getAllTeams()


  /**
   * Get all the teams for a division, ordered by standings
   *
   * @param division Division object representing the division whose teams are being requested
   * @param sessionID integer describing the season-session being requested
   * @param dbContext current db context
   * @return teams Array of Team objects
   *
   * @see SeasonSession
   */
  public static Team[] getTeamsByStanding(Division division, int sessionID, DbContext dbContext)
  {
    DBEngine db= new DBEngine(dbContext.getContextString());

    Category log = Log4jUtils.initLog("Team");
    //log.debug ("Getting all teams for division " + division.getDivisionName());

    String query = "SELECT " + teamFields + ", nvl(pts, 0) pts " +
      "FROM team t, team_stats ts, season_session ss " +
      "WHERE division = ? AND show_in_standings = 'Y' AND " +
      "t.oid = ts.team (+) AND t.season = ss.season_id AND ss.session_id = ? " +
      "ORDER BY pts DESC";

    Team[] teams = null;

    try {
      Object[] params = new Object[2];
      params[0] = division.getOID();
      params[1] = new BigDecimal(sessionID);
      log.debug ("Get teams for a division, SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (query, params));

      Vector results = db.executeQuery (query, params);
      int numTeams = results.size();
      teams = new Team[numTeams];
      for (int index=0; index<results.size(); index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	teams[index] = new Team(dbContext);
	teams[index].setTeamInfo(record);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting teams by division: " + sqle);
    }

    return teams;
    
  } // End, getTeamsByStanding()


  /**
   * Get a team object from its OID
   *
   * @param oid The oid of the team object we want
   * @param dbContext The dbContext for the query
   * @return Team The team object
   */
  public static Team getTeam(BigDecimal oid, DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Team");
    //log.debug("Getting a team from an oid");

    ArrayList teams = getAllTeams(dbContext);

    for (Iterator iter=teams.iterator(); iter.hasNext(); )
    {
      Team t = (Team)iter.next();
      if (t.getOID().equals(oid))
      {
	//log.debug("Team found is " + t);
	return t;
      }
    } // End, for

    log.error ("No team found in getTeam()");
    return null;

  } // End, getTeam()


  /**
   * Get a team object from its name and season
   *
   * @param oid The name of the team whose object we want
   * @param season The season of the team we want (because the same name may
   * be used in more than a single season)
   * @param dbContext The dbContext for the query
   * @return Team The team object
   */
  public static Team getTeam(String teamName, Season theSeason, DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Team");
    //log.debug("Getting team object for team " + teamName + " in season " + theSeason);

    ArrayList teams = getAllTeams(dbContext);

    for (Iterator iter=teams.iterator(); iter.hasNext(); )
    {
      Team t = (Team)iter.next();
      if (t.getTeamName().equals(teamName) && t.getSeason().equals(theSeason))
      {
	//log.debug("Team found is " + t);
	return t;
      }
    } // End, for

    log.error ("No team found in getTeam()");
    return null;

  } // End, getTeam()


  /**
   * Set all Team attributes from the info retrieved from the DB.
   *
   */

  private void setTeamInfo(Hashtable record)
  {
    this.oid = DBUtils.getBigDecimal("OID", record);
    teamName = DBUtils.getString("TEAM_NAME", record);
    teamColor = DBUtils.getString("COLOR", record);
    divisionOID = DBUtils.getBigDecimal("DIVISION", record);

    try {
      setSeason(new Season(DBUtils.getBigDecimal("SEASON", record), myDbContext));
    }
    catch (Season.NoSuchSeasonException e) {
      log.error ("Could not find Season object for team: " + e);
      setSeason(null);
    }
    displayedInStandings = DBUtils.getString("SHOW_IN_STANDINGS", record).equals("Y");

    inserted = true;
    modified = false;

    return;

  } // End, setTeamInfo()


  /**
   * Get the OID of the team.
   */

  public BigDecimal getOID() { return oid; }


  /**
   * Get the name of the team
   */
  public String getTeamName() { return teamName; }


  /**
   * Set the name of the team
   */
  public void setTeamName(String teamName) { this.teamName = teamName; modified = true; }


  /**
   * Get the team color
   */
  public String getTeamColor() { return teamColor; }


  /**
   * Set the team color
   *
   * @param teamColor The team's color
   */
  public void setTeamColor(String teamColor) { this.teamColor = teamColor; modified = true; }


  /**
   * Get the division this team is in.
   */
  public Division getDivision()
  {
    if (division == null && !divisionOID.equals(new BigDecimal(0)))
    {
      try {
	setDivision(new Division(divisionOID, myDbContext));
      }
      catch (Division.NoSuchDivisionException e) {
	log.error ("No division found for team " + teamName + ": " + e);
      }
    }

    return division;
  } // End, getDivision()


  /**
   * Set the division the team is in
   *
   * @param division An object of class Division giving the division the team is in
   */
  public void setDivision(Division division)
  {
    this.division = division;
    if (division != null)
      divisionOID = division.getOID();
    modified = true;
  } // End, setDivision()


  /**
   * Set the division the team is in
   *
   * @param divisionOID OID of the division the team is in.
   */
  public void setDivision(BigDecimal oid) { this.divisionOID = oid; modified = true; } // End, setDivision()


  /**
   * Get the number of wins in the division for the specified session of the season
   */
  public int getWinsInDivision (int sessionID)
  {
    setSessionForStats(sessionID);
    return teamStats.getWinsInDivision();
  } // End, getWinsInDivision()


  /**
   * Increment wins for the team in the division for the specified session of the season
   */
  public void incWinsInDivision(int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.incWinsInDivision();
    statsModified = true;
  } // End incWinsInDivision()


  /**
   * Get the number of losses in the division for the specified session of the season
   */
  public int getLossesInDivision (int sessionID)
  {
    setSessionForStats(sessionID);
    return teamStats.getLossesInDivision();
  } // End, getLossesInDivision()


  /**
   * Increment losses for the team in the division for the specified session of the season
   */
  public void incLossesInDivision(int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.incLossesInDivision();
    statsModified = true;
  } // End incLossesInDivision()


  /**
   * Get the number of ties in the division for the specified session of the season
   */
  public int getTiesInDivision (int sessionID)
  {
    setSessionForStats(sessionID);
    return teamStats.getTiesInDivision();
  } // End, getTiesInDivision()


  /**
   * Increment ties for the team in the division for the specified session of the season
   */
  public void incTiesInDivision(int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.incTiesInDivision();
    statsModified = true;
  } // End incTiesInDivision()


  /**
   * Get the number of wins outside the division for the specified session of the season
   */
  public int getWinsOutDivision (int sessionID)
  {
    setSessionForStats(sessionID);
    return teamStats.getWinsOutDivision();
  } // End, getWinsOutDivision()


  /**
   * Increment wins for the team outside the division for the specified session of the season
   */
  public void incWinsOutDivision(int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.incWinsOutDivision();
    statsModified = true;
  } // End incWinsOutDivision()


  /**
   * Get the number of losses outside the division for the specified session of the season
   */
  public int getLossesOutDivision (int sessionID)
  {
    setSessionForStats(sessionID);
    return teamStats.getLossesOutDivision();
  } // End, getLossesOutDivision()


  /**
   * Increment losses for the team outside the division for the specified session of the season
   */
  public void incLossesOutDivision(int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.incLossesOutDivision();
    statsModified = true;
  } // End incLossesOutDivision()


  /**
   * Get the number of ties outside the division for the specified session of the season
   */
  public int getTiesOutDivision (int sessionID)
  {
    setSessionForStats(sessionID);
    return teamStats.getTiesOutDivision();
  } // End, getTiesOutDivision()


  /**
   * Increment ties for the team outside the division
   */
  public void incTiesOutDivision(int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.incTiesOutDivision();
    statsModified = true;
  } // End incTiesOutDivision()


  /**
   * Increment wins for the team
   *
   * @param inDivision boolean, true if the game is a division game, else false
   */
  public void incrementWins(boolean inDivision, int sessionID)
  {
    setSessionForStats(sessionID);
    if (inDivision)
      teamStats.incWinsInDivision();
    else
      teamStats.incWinsOutDivision();
  } // End, incrementWins()


  /**
   * Increment losses for the team
   *
   * @param inDivision boolean, true if the game is a division game, else false
   */
  public void incrementLosses(boolean inDivision, int sessionID)
  {
    setSessionForStats(sessionID);
    if (inDivision)
      teamStats.incLossesInDivision();
    else
      teamStats.incLossesOutDivision();
  } // End, incrementLosses()


  /**
   * Increment ties for the team
   *
   * @param inDivision boolean, true if the game is a division game, else false
   */
  public void incrementTies(boolean inDivision, int sessionID)
  {
    setSessionForStats(sessionID);
    if (inDivision)
      teamStats.incTiesInDivision();
    else
      teamStats.incTiesOutDivision();
  } // End, incrementTies()


  /**
   * Get the number of goals the team has scored
   */
  public int getGoalsFor (int sessionID) { setSessionForStats(sessionID); return teamStats.getGoalsFor(); }


  /**
   * Set the number of goals the team has scored
   */
  public void setGoalsFor (int goalsFor, int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.setGoalsFor(goalsFor);
    statsModified = true;
  } // End, setGoalsFor()


  /**
   * Increment the goals-for for the team by the specified amount
   *
   * @param goalsForInc The number of goals-for to add
   */
  public void incrementGoalsFor(int goalsForInc, int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.incrementGoalsFor(goalsForInc);
    statsModified = true;
  } // End, incrementGoalsFor()


  /**
   * Get the number of goals scored against the team
   */
  public int getGoalsAgainst (int sessionID)
  {
    setSessionForStats(sessionID);
    return teamStats.getGoalsAgainst();
  } // End, getGoalsAgainst()


  /**
   * Set the number of goals scored against the team
   */
  public void setGoalsAgainst (int goalsAgainst, int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.setGoalsAgainst(goalsAgainst);
    statsModified = true;
  } // End, setGoalsAgainst()


  /**
   * Increment the goals-against for the team by the specified amount
   *
   * @param goalsAgainstInc The number of goals-against to add
   */
  public void incrementGoalsAgainst(int goalsAgainstInc, int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.incrementGoalsAgainst(goalsAgainstInc);
    statsModified = true;
  } // End, incrementGoalsAgainst()


  /**
   * Get the percentage (games won)
   */
  public double getPct (int sessionID) { setSessionForStats(sessionID); return teamStats.getPct(); }


  /**
   * Set the percentage (games won)
   */
  public void setPct (double pct, int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.setPct(pct);
    statsModified = true;
  } // End, setPct


  /**
   * Get the percentage in division (games won)
   */
  public double getPctInDivision (int sessionID)
  {
    setSessionForStats(sessionID);
    return teamStats.getPctInDivision();
  } // End, getPctInDivision()


  /**
   * Set the percentage in division (games won)
   */
  public void setPctInDivision (double pctInDivision, int sessionID)
  {
    setSessionForStats(sessionID);
    teamStats.setPctInDivision(pctInDivision);
    statsModified = true;
  } // End, setPctInDivision()


  /**
   * Get points
   */
  public int getPoints(int sessionID) { setSessionForStats(sessionID); return teamStats.getPoints(); }


  /**
   * Set points
   */
  //  public void setPoints (int points) { this.points = points; modified = true; }


  /**
   * Get Season
   */
  public Season getSeason() { return season; }


  /**
   * Set Season
   */
  public void setSeason (Season season) { this.season = season; modified = true; }

  /**
   * Get displayedInStandings
   */
  public boolean isDisplayedInStandings() { return displayedInStandings; }


  /**
   * Set displayedInStandings
   *
   * Sets whether or not this team will be displayed in the standings.  A 
   * team is not displayed in the standings if it is a "virtual team".  These
   * teams are place-holders, like "Division A 1st seed".  These teams are
   * used to set up the post-season schedule before the actual teams are known.
   */
  public void setDisplayedInStandings (boolean displayed)
  {
    this.displayedInStandings = displayed;
    modified = true;
  } // End, setDisplayedInStandings()


  /**
   * Get the roster for this team.
   */
  public TeamRoster getTeamRoster()
  {
    if (roster == null)
      roster = new TeamRoster(this, myDbContext);

    return roster;
  }


  /**
   * Team session-to-date stats have to be associated with a session.
   * Use this method to set the session that all stat modifications apply to.
   */
  private void setSessionForStats(int sessionID)
  {
    if (teamStats == null || teamStats.getSessionID() != sessionID)
    {
      if (statsModified)
      {
	try {
	  teamStats.store(new DBEngine(myDbContext.getContextString()));
	}
	catch (DbStoreException e) {
	  log.error ("**** Major error storing team stats for team: " + this + ": " + e);
	}
	statsModified = false;
      }
      teamStats = new TeamStats(this, sessionID, myDbContext);
    }
  } // End, setSeasonForStats()


  public void store(DBEngine db) throws DbStoreException
  {
    // If we don't need to do anything, bail out now.
    if (inserted && !modified) return;

    if (!db.isInTransaction())
      throw new DbStoreException("For Team object, store() method must be called within a transaction.");

    int numRows;

    String storeTeam =
      "INSERT INTO team (team_name, color, division, show_in_standings, season, oid) " +
      " VALUES (?, ?, ?, ?, ?, ?)";

    String updateTeam =
      "UPDATE team SET team_name = ?, color = ?, division = ?, show_in_standings = ?, " +
      "season = ? WHERE oid = ?";

    Division myDivision = getDivision();

    Object[] params = new Object[6];
    int x=0;
    params[x++] = getTeamName();
    params[x++] = getTeamColor();
    params[x++] = divisionOID;
    params[x++] = isDisplayedInStandings() ? "Y" : "N";
    params[x++] = getSeason().getOID();
    params[x++] = getOID();

    //log.debug ("division oid: " + getDivision());

    String query;
    if (inserted)
      query = updateTeam;
    else
      query = storeTeam;

    try {
      log.debug ("Store team, SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (query, params));
      if ((numRows = db.executeUpdate(query, params)) != 1)
	log.error ("Update only updated " + numRows + " rows.");
    }
    catch (java.sql.SQLException e) {
     throw new DbStoreException(e.toString());
   }

   // Now store the team's stats if they have changed.
   if (statsModified)
     teamStats.store(db);

  } // End, store()


  public String toString()
  {
    return getTeamName();
  }

  public boolean equals(Object obj)
  {
    return (obj instanceof Team) && (((Team)obj).oid.equals(this.oid));
  } // End, equals()

  //  public static void main(String[] args)
  //  {
  //    Team[] teams = Team.getAllTeams(SeasonSession.getCurrentSeasonSession());
  //    System.out.println("Found " + teams.length + " team(s).");
  //  } // End, main()

} // End, class Team

