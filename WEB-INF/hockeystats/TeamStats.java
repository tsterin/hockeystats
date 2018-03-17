// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/TeamStats.java,v 1.1 2002/09/15 01:53:23 tom Exp $

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
 * Class to represent season-session stats for a team in the league.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: TeamStats.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.2  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.1  2002/08/23 01:53:56  tom
 * Object to maintain statistics for an individual season.
 *
 *
 */

public class TeamStats extends DbStore {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/TeamStats.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  private BigDecimal oid;
  private Team team;
  private BigDecimal teamOID;
  private int sessionID;
  private int winsInDivision;
  private int winsOutDivision;
  private int lossesInDivision;
  private int lossesOutDivision;
  private int tiesInDivision;
  private int tiesOutDivision;
  private int goalsFor;
  private int goalsAgainst;
  private int points;
  private double pct;
  private double pctInDivision;
  private DbContext myDbContext;

  private static String selectStmt =
  "SELECT oid, team, session_id, nvl(wins_in_division, 0) wins_in_division," +
      "nvl(losses_in_division, 0) losses_in_division, nvl(ties_in_division, 0) ties_in_division, " +
      "nvl(wins_out_division, 0) wins_out_division, nvl(losses_out_division, 0) losses_out_division, " +
      "nvl(ties_out_division, 0) ties_out_division, nvl(goals_for, 0) goals_for, " +
      "nvl(goals_against, 0) goals_against, nvl(pts, 0) pts, nvl(pct, 0.0) pct, " +
      "nvl(pct_in_division, 0.0) pct_in_division FROM team_stats ";

  /**
   * Create a new, empty TeamStats object.
   *
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   */

//    public TeamStats(DbContext dbContext)
//    {
//      oid = OIDSequencer.next(dbContext);

//      team = null;
//      teamOID = new BigDecimal(0);
//      winsInDivision = 0;
//      winsOutDivision = 0;
//      lossesInDivision = 0;
//      lossesOutDivision = 0;
//      tiesInDivision = 0;
//      tiesOutDivision = 0;
//      goalsFor = 0;
//      goalsAgainst = 0;
//      pct = 0.0;
//      pctInDivision = 0.0;
//      points = 0;
//      myDbContext = dbContext;

//      inserted = false;
//      modified = false;
//    }

  /**
   * Create a TeamStats object for the specified team and session
   * from the data in the database.
   *
   * @param team Team object
   * @param int sessionID
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   * @throws NoSuchTeamException
   */

  public TeamStats (Team team, int sessionID, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Hashtable record = null;
    String query = selectStmt + "WHERE team = ? AND session_id = ?";

    myDbContext = dbContext;

    Object[] params = new Object[2];
    params[0] = team.getOID();
    params[1] = new BigDecimal(sessionID);
    log.debug("get team-stats, SQL: " + DBUtils.getSQLStatementFromPreparedStatement(query, params));

    try {
      Vector results = db.executeQuery(query, params);
      if (results.size() > 0)
	record = (Hashtable) results.get(0);
    }
    catch (SQLException sqle) {
      log.error ("SQL Error getting team stats: " + sqle);
      return;
    }

    if (record == null) // Stats not found
    {
      this.oid = OIDSequencer.next(dbContext);
      setTeam(team);
      setSessionID(sessionID);
      setWinsInDivision(0);
      setLossesInDivision(0);
      setTiesInDivision(0);
      setWinsOutDivision(0);
      setLossesOutDivision(0);
      setTiesOutDivision(0);
      setGoalsFor(0);
      setGoalsAgainst(0);
      setPoints(0);
      setPct(0.0);
      setPctInDivision(0.0);

      inserted = false;
    }
    else // Stats record found
    {
      setStats(record);
      inserted = true;
    }

    modified = false;

  } // End, constructor


  /**
   * Set all stats from the info retrieved from the DB.
   *
   */

  private void setStats(Hashtable record)
  {
    this.oid = DBUtils.getBigDecimal("OID", record);
    setTeam(DBUtils.getBigDecimal("TEAM", record));
    setSessionID(DBUtils.getBigDecimal("SESSION_ID", record).intValue());
    setWinsInDivision(DBUtils.getBigDecimal("WINS_IN_DIVISION", record).intValue());
    setLossesInDivision(DBUtils.getBigDecimal("LOSSES_IN_DIVISION", record).intValue());
    setTiesInDivision(DBUtils.getBigDecimal("TIES_IN_DIVISION", record).intValue());
    setWinsOutDivision(DBUtils.getBigDecimal("WINS_OUT_DIVISION", record).intValue());
    setLossesOutDivision(DBUtils.getBigDecimal("LOSSES_OUT_DIVISION", record).intValue());
    setTiesOutDivision(DBUtils.getBigDecimal("TIES_OUT_DIVISION", record).intValue());
    setGoalsFor(DBUtils.getBigDecimal("GOALS_FOR", record).intValue());
    setGoalsAgainst(DBUtils.getBigDecimal("GOALS_AGAINST", record).intValue());
    setPoints(DBUtils.getBigDecimal("PTS", record).intValue());
    setPct(DBUtils.getBigDecimal("PCT", record).doubleValue());
    setPctInDivision(DBUtils.getBigDecimal("PCT_IN_DIVISION", record).doubleValue());

    inserted = true;
    modified = false;

    return;

  } // End, setStats()


  /**
   * Get the OID of the teamStats record.
   */

  public BigDecimal getOID() { return oid; }


  private void setTeam(Team team)
  {
    this.teamOID = team.getOID();
    this.team = team;
  } // End, setTeam()

  private void setTeam(BigDecimal teamOID) { this.teamOID = teamOID; }

  public Team getTeam()
  {
    if (teamOID.equals(new BigDecimal(0)))
      this.team = null;
    else
      this.team = Team.getTeam(teamOID, myDbContext);

    return this.team;

  } // End, getTeam()


  /**
   * Get the number of wins in the division
   */
  public int getWinsInDivision () { return winsInDivision; }


  /**
   * Set the number of wins in the division
   */
  public void setWinsInDivision (int wins) { winsInDivision = wins; recalcStats(); modified = true; }


  /**
   * Increment wins for the team in the division
   */
  public void incWinsInDivision() { winsInDivision++; recalcStats(); modified = true; }


  /**
   * Get the number of losses in the division
   */
  public int getLossesInDivision () { return lossesInDivision; }


  /**
   * Set the number of losses in the division
   */
  public void setLossesInDivision (int losses) { lossesInDivision = losses; recalcStats(); modified = true; }


  /**
   * Increment losses for the team in the division
   */
  public void incLossesInDivision() { lossesInDivision++; recalcStats(); modified = true; }


  /**
   * Get the number of ties in the division
   */
  public int getTiesInDivision () { return tiesInDivision; }


  /**
   * Set the number of ties in the division
   */
  public void setTiesInDivision (int ties) { tiesInDivision = ties; recalcStats(); modified = true; }


  /**
   * Increment ties for the team inside of the division
   */
  public void incTiesInDivision() { tiesInDivision++; recalcStats(); modified = true; }


  /**
   * Get the number of wins outside the division
   */
  public int getWinsOutDivision () { return winsOutDivision; }


  /**
   * Set the number of wins outside the division
   */
  public void setWinsOutDivision (int wins) { winsOutDivision = wins; recalcStats(); modified = true; }


  /**
   * Increment wins for the team outside the division
   */
  public void incWinsOutDivision() { winsOutDivision++; recalcStats(); modified = true; }


  /**
   * Get the number of losses outside the division
   */
  public int getLossesOutDivision () { return lossesOutDivision; }


  /**
   * Set the number of losses outside the division
   */
  public void setLossesOutDivision (int losses) { lossesOutDivision = losses; recalcStats(); modified = true; }


  /**
   * Increment losses for the team outside the division
   */
  public void incLossesOutDivision() { lossesOutDivision++; recalcStats(); modified = true; }


  /**
   * Get the number of ties outside the division
   */
  public int getTiesOutDivision () { return tiesOutDivision; }


  /**
   * Set the number of ties outside the division
   */
  public void setTiesOutDivision (int ties) { tiesOutDivision = ties; recalcStats(); modified = true; }


  /**
   * Increment ties for the team outside the division
   */
  public void incTiesOutDivision() { tiesOutDivision++; recalcStats(); modified = true; }


  /**
   * Increment wins for the team 
   *
   * @param inDivision boolean, true if the game is a division game, else false
   */
  public void incrementWins(boolean inDivision)
  {
    if (inDivision)
      incWinsInDivision();
    else
      incWinsOutDivision();
  } // End, incrementWins()


  /**
   * Increment losses for the team
   *
   * @param inDivision boolean, true if the game is a division game, else false
   */
  public void incrementLosses(boolean inDivision)
  {
    if (inDivision)
      incLossesInDivision();
    else
      incLossesOutDivision();
  } // End, incrementLosses()


  /**
   * Increment ties for the team
   *
   * @param inDivision boolean, true if the game is a division game, else false
   */
  public void incrementTies(boolean inDivision)
  {
    if (inDivision)
      incTiesInDivision();
    else
      incTiesOutDivision();
  } // End, incrementTies()


  /**
   * Get the number of goals the team has scored
   */
  public int getGoalsFor () { return goalsFor; }


  /**
   * Set the number of goals the team has scored
   */
  public void setGoalsFor (int goalsFor) { this.goalsFor = goalsFor; modified = true; }


  /**
   * Increment the goals-for for the team by the specified amount
   *
   * @param goalsForInc The number of goals-for to add
   */
  public void incrementGoalsFor(int goalsForInc) { goalsFor += goalsForInc; modified = true; }


  /**
   * Get the number of goals scored against the team
   */
  public int getGoalsAgainst () { return goalsAgainst; }


  /**
   * Set the number of goals scored against the team
   */
  public void setGoalsAgainst (int goalsAgainst) { this.goalsAgainst = goalsAgainst; modified = true; }


  /**
   * Increment the goals-agains for the team by the specified amount
   *
   * @param goalsAgainsInc The number of goals-against to add
   */
  public void incrementGoalsAgainst(int goalsAgainstInc) { goalsAgainst += goalsAgainstInc; modified = true; }

  /**
   * Get the percentage (games won)
   */
  public double getPct () { return pct; }


  /**
   * Set the percentage (games won)
   */
  public void setPct (double pct) { this.pct = pct; modified = true; }


  /**
   * Get the percentage in division (games won)
   */
  public double getPctInDivision () { return pctInDivision; }


  /**
   * Set the percentage in division (games won)
   */
  public void setPctInDivision (double pctInDivision) { this.pctInDivision = pctInDivision; modified = true; }


  /**
   * Get points
   */
  public int getPoints() { return points; }


  /**
   * Set points
   */
  public void setPoints (int points) { this.points = points; modified = true; }


  /**
   * Get Session ID
   */
  public int getSessionID() { return sessionID; }


  /**
   * Set Session ID
   */
  public void setSessionID (int sessionID) { this.sessionID = sessionID; modified = true; }


  private void recalcStats()
  {
    if (winsInDivision == 0 && lossesInDivision == 0 && tiesInDivision == 0)
      pctInDivision = 0.0;
    else
      pctInDivision = winsInDivision / (winsInDivision + lossesInDivision + tiesInDivision);

    if (winsInDivision == 0 && lossesInDivision == 0 && tiesInDivision == 0 &&
	winsOutDivision == 0 && lossesOutDivision == 0 && tiesOutDivision == 0)
      pct = 0.0;
    else
      pct = (winsInDivision + winsOutDivision) / 
	(winsInDivision + lossesInDivision + tiesInDivision +
	 winsOutDivision + lossesOutDivision + tiesOutDivision);

    points = ((winsInDivision + winsOutDivision) * 2) + tiesInDivision + tiesOutDivision;

  } // End, reCalcStats()


  public void store(DBEngine db) throws DbStoreException
  {
    // If we don't need to do anything, bail out now.
    if (inserted && !modified) return;

    int numRows;

    String storeStats =
      "INSERT INTO team_stats (team, session_id, goals_for, goals_against, wins_in_division, " +
      "wins_out_division, losses_in_division, losses_out_division, ties_in_division, ties_out_division, " +
      "pct, pct_in_division, pts, oid) " +
      " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String updateStats =
      "UPDATE team_stats SET team = ?, session_id = ?, goals_for = ?, goals_against = ?, " +
      "wins_in_division = ?, wins_out_division = ?, losses_in_division = ?, losses_out_division = ?, " +
      "ties_in_division = ?, ties_out_division = ?, pct = ?, pct_in_division = ?, pts = ? WHERE oid = ?";

    Object[] params = new Object[14];
    int x=0;
    params[x++] = teamOID;
    params[x++] = new BigDecimal(getSessionID());
    params[x++] = new BigDecimal(getGoalsFor());
    params[x++] = new BigDecimal(getGoalsAgainst());
    params[x++] = new BigDecimal(getWinsInDivision());
    params[x++] = new BigDecimal(getWinsOutDivision());
    params[x++] = new BigDecimal(getLossesInDivision());
    params[x++] = new BigDecimal(getLossesOutDivision());
    params[x++] = new BigDecimal(getTiesInDivision());
    params[x++] = new BigDecimal(getTiesOutDivision());
    params[x++] = (new BigDecimal(getPct())).setScale(3);
    params[x++] = (new BigDecimal(getPctInDivision())).setScale(3);
    params[x++] = new BigDecimal(getPoints());
    params[x++] = getOID();


    String query;
    if (inserted)
      query = updateStats;
    else
      query = storeStats;

    try {
      log.debug ("store teamStats, SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (query, params));
      if ((numRows = db.executeUpdate(query, params)) != 1)
	log.error ("Update only updated " + numRows + " rows.");
    }
    catch (java.sql.SQLException e) {
     throw new DbStoreException(e.toString());
   }

  } // End, store()


//  public String toString()
//  {
//    return getTeamName();
//  }

//    public boolean equals(Object obj)
//    {
//      return obj instanceof Team && ((Team)obj).oid == this.oid;
//    } // End, equals()

  //  public static void main(String[] args)
  //  {
  //    Team[] teams = Team.getAllTeams(SeasonSession.getCurrentSeasonSession());
  //    System.out.println("Found " + teams.length + " team(s).");
  //  } // End, main()

  /*************************************************************************************/
  /*************************************************************************************/
  /*              FACTORY METHODS                                                      */
  /*************************************************************************************/
  /*************************************************************************************/

} // End, class TeamStats

