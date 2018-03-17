// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/SeasonStats.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;
import java.math.*;
import java.util.*;
import java.sql.SQLException;
import hockeystats.util.*;
import hockeystats.util.db.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Class to represent the stats for a player in a season-session
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: SeasonStats.java,v $
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

public class SeasonStats extends DbStore {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/SeasonStats.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  private BigDecimal oid;
  private Player player;
  private BigDecimal playerOID;
  private SeasonSession seasonSession;
  private BigDecimal seasonSessionOID;
  private int goals;
  private int assists;
  private int penalties;
  private int penaltyMinutes;
  private int gamesPlayed;
  private int goalieGamesWon;
  private int goalieGamesLost;
  private int goalieGamesTied;
  private int goalsAgainst;
  private DbContext myDbContext = null;

  /**
   * Constructor to create a brand new SeasonStats object
   */
  public SeasonStats(Player player, SeasonSession seasonSession, int goals,
		     int assists, int penalties, int penaltyMinutes, int gamesPlayed,
		     int goalieGamesWon, int goalieGamesLost, int goalieGamesTied,
		     int goalsAgainst, DbContext dbContext)
  {
    this.oid = OIDSequencer.next(dbContext);
    setPlayer(player);
    setSeasonSession(seasonSession);
    setGoals(goals);
    setAssists(assists);
    setPenalties(penalties);
    setPenaltyMinutes(penaltyMinutes);
    setGamesPlayed(gamesPlayed);
    setGoalieGamesWon(goalieGamesWon);
    setGoalieGamesLost(goalieGamesLost);
    setGoalieGamesTied(goalieGamesTied);
    setGoalsAgainst(goalsAgainst);

    this.myDbContext = dbContext;

    inserted = false;
  }


  /**
   * Constructor to create a SeasonStats from the data in the database.
   */
  public SeasonStats(BigDecimal playerOID, BigDecimal seasonSessionOID, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());

    String query = "SELECT oid, player_id, season_session_id, goals, assists, penalties, " +
      "penalty_minutes, games_played, goalie_games_won, goalie_games_lost, goalie_games_tied, " +
      "goals_against FROM season_stats WHERE player_id = ? AND season_session_id = ?";

    Object[] params = new Object[2];
    params[0] = playerOID;
    params[1] = seasonSessionOID;
    Hashtable record = null;

    log.debug ("Get season-stats, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    try {
      Vector results = db.executeQuery (query, params);
      if (results.size() > 0)
	record = (Hashtable) results.get(0);
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting seasonSession: " + sqle);
      return;
    }

    if (record != null) // Record found in database
    {
      this.oid = DBUtils.getBigDecimal ("OID", record);
      this.myDbContext = dbContext;

      setPlayer(playerOID);
      setSeasonSession(seasonSessionOID);
      setGoals(DBUtils.getBigDecimal("GOALS", record).intValue());
      setAssists(DBUtils.getBigDecimal("ASSISTS", record).intValue());
      setPenalties(DBUtils.getBigDecimal("PENALTIES", record).intValue());
      setPenaltyMinutes(DBUtils.getBigDecimal("PENALTY_MINUTES", record).intValue());
      setGamesPlayed(DBUtils.getBigDecimal("GAMES_PLAYED", record).intValue());
      setGoalieGamesWon(DBUtils.getBigDecimal("GOALIE_GAMES_WON", record).intValue());
      setGoalieGamesLost(DBUtils.getBigDecimal("GOALIE_GAMES_LOST", record).intValue());
      setGoalieGamesTied(DBUtils.getBigDecimal("GOALIE_GAMES_TIED", record).intValue());
      setGoalsAgainst(DBUtils.getBigDecimal("GOALS_AGAINST", record).intValue());
      this.inserted = true;
    } // End, if record found for the user/season-session
    else // No record yet for this user/season-session
    {
      this.oid = OIDSequencer.next(dbContext);
      this.myDbContext = dbContext;
      setPlayer(playerOID);
      setSeasonSession(seasonSessionOID);
      setGoals(0);
      setAssists(0);
      setPenalties(0);
      setPenaltyMinutes(0);
      setGamesPlayed(0);
      setGoalieGamesWon(-1);
      setGoalieGamesLost(-1);
      setGoalieGamesTied(-1);
      setGoalsAgainst(-1);
      inserted = false;
    } // End, else

    this.modified = false;

  } // End, constructor


  /**
   * Constructor to create a SeasonSession object given its OID
   */
  public SeasonStats(BigDecimal oid, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());

    String query = "SELECT oid, player_id, season_session_id, goals, assists, penalties, " +
      "penalty_minutes, games_played, goalie_games_played, goals_against FROM season_stats " +
      "WHERE oid = ?";

    Object[] params = new Object[1];
    params[0] = oid;
    Hashtable record = null;

    try {
      Vector results = db.executeQuery (query, params);
      record = (Hashtable) results.get(0);
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting seasonSession: " + sqle);
    }
    this.oid = DBUtils.getBigDecimal ("OID", record);
    setPlayer(DBUtils.getBigDecimal("PLAYER_ID", record));
    try {
      setSeasonSession(new SeasonSession(DBUtils.getBigDecimal("SEASON_SESSION_ID", record), myDbContext));
    }
    catch (SeasonSession.InvalidSeasonSessionException e) {
      log.debug ("No season-session found for SeasonSession, oid = " + this.oid.toString());
    }
    setGoals(DBUtils.getBigDecimal("GOALS", record).intValue());
    setAssists(DBUtils.getBigDecimal("ASSISTS", record).intValue());
    setPenalties(DBUtils.getBigDecimal("PENALTIES", record).intValue());
    setPenaltyMinutes(DBUtils.getBigDecimal("PENALTY_MINUTES", record).intValue());
    setGamesPlayed(DBUtils.getBigDecimal("GAMES_PLAYED", record).intValue());
    setGoalieGamesWon(DBUtils.getBigDecimal("GOALIE_GAMES_WON", record).intValue());
    setGoalieGamesLost(DBUtils.getBigDecimal("GOALIE_GAMES_LOST", record).intValue());
    setGoalieGamesTied(DBUtils.getBigDecimal("GOALIE_GAMES_TIED", record).intValue());
    setGoalsAgainst(DBUtils.getBigDecimal("GOALS_AGAINST", record).intValue());

    this.myDbContext = dbContext;
    this.inserted = true;
    this.modified = false;

  } // End, constructor

  private void setPlayer(BigDecimal playerOID)
  {
    this.playerOID = playerOID;
    this.player = Player.getPlayer(playerOID, myDbContext);
  } // End, setPlayer()

  private void setPlayer(Player thePlayer)
  {
    this.playerOID = thePlayer.getOID();
    this.player = thePlayer;
  } // End, setPlayer()

  /**
   * Get the player's goals.
   */
  public int getGoals() { return goals; }


  /**
   * Set the player's goals.
   */
  private void setGoals(int goals)
  { this.goals = goals; modified = true; }


  /**
   * Increment the number of player goals by one.
   */
  public void incrementGoals() { incrementGoals(1); }


  /**
   * Increment the number of player goals by the specified amount.
   *
   * @param int numberOfGoals The number of goals to increment by.
   */
  public void incrementGoals(int numGoals)
  {
    log.debug ("** Incrementing goals by " + numGoals + ", current is " + this.goals);
    this.goals += numGoals;
    modified = true;
  }


  /**
   * Get the player's assists.
   */
  public int getAssists() { return assists; }


  /**
   * Set the player's assists.
   */
  private void setAssists(int assists)
  { this.assists = assists; modified = true; }


  /**
   * Increment the number of player assists by one.
   */
  public void incrementAssists() { incrementAssists(1); }


  /**
   * Increment the number of player assists by the specified amount.
   *
   * @param int numberOfAssists The number of assists to increment by.
   */
  public void incrementAssists(int numAssists)
  {
    this.assists += numAssists;
    modified = true;
  }


  /**
   * Get the player's number of penalties
   */
  public int getPenalties() { return penalties; }


  /**
   * Set the player's number of penalties.
   */
  private void setPenalties(int penalties)
  { this.penalties = penalties; modified = true; }


  /**
   * Increment the number of player penalties by one.
   */
  public void incrementPenalties() { incrementPenalties(1); }


  /**
   * Increment the number of player penalties by the specified amount.
   *
   * @param int numberOfPenalties The number of penalties to increment by.
   */
  public void incrementPenalties(int numPenalties)
  {
    this.penalties += numPenalties;
    modified = true;
  }


  /**
   * Get the player's penalty minutes.
   */
  public int getPenaltyMinutes() { return penaltyMinutes; }


  /**
   * Set the player's penalty minutes.
   */
  private void setPenaltyMinutes(int penaltyMinutes)
  { this.penaltyMinutes = penaltyMinutes; modified = true; }


  /**
   * Increment the number of player penaltyMinutes by the specified amount.
   *
   * @param int numberOfPenaltyMinutes The number of penaltyMinutes to increment by.
   */
  public void incrementPenaltyMinutes(int numPenaltyMinutes)
  {
    this.penaltyMinutes += numPenaltyMinutes;
    modified = true;
  }


  /**
   * Get the number of games played by the player (goalie or not)
   */
  public int getGamesPlayed() { return gamesPlayed; }


  /**
   * Set the number of games played by the player (goalie or not)
   */
  private void setGamesPlayed(int gamesPlayed)
  { this.gamesPlayed = gamesPlayed; modified = true; }


  /**
   * Increment the number of player games by one.
   */
  public void incrementGames() { incrementGames(1); }


  /**
   * Increment the number of player games by the specified amount.
   *
   * @param int numberOfGames The number of games to increment by.
   */
  public void incrementGames(int numGames)
  {
    this.gamesPlayed += numGames;
    modified = true;
  }


  /**
   * Get the number of games won by the played as a goalie
   */
  public int getGoalieGamesWon() { return goalieGamesWon; }


  /**
   * Get the number of games lost by the played as a goalie
   */
  public int getGoalieGamesLost() { return goalieGamesLost; }


  /**
   * Get the number of games tied by the played as a goalie
   */
  public int getGoalieGamesTied() { return goalieGamesTied; }


  /**
   * Set the number of games won by the player as a goalie
   */
  private void setGoalieGamesWon(int goalieGamesWon)
  { this.goalieGamesWon = goalieGamesWon; modified = true; }


  /**
   * Set the number of games lost by the player as a goalie
   */
  private void setGoalieGamesLost(int goalieGamesLost)
  { this.goalieGamesLost = goalieGamesLost; modified = true; }


  /**
   * Set the number of games tied by the player as a goalie
   */
  private void setGoalieGamesTied(int goalieGamesTied)
  { this.goalieGamesTied = goalieGamesTied; modified = true; }


  /**
   * Increment the number of games player won as goalie by 1.  If
   * the current count is -1, this is the first game played as a 
   * goalie, so set all counts to 0, then increment.
   */
  public void incrementGoalieGamesWon()
  {
    if (goalieGamesWon == -1)
      goalieGamesWon = goalieGamesLost = goalieGamesTied = 0;

    this.goalieGamesWon++;
    modified = true;
  } // End, incrementGoalieGamesWon()


  /**
   * Increment the number of games player lost as goalie by 1.  If
   * the current count is -1, this is the first game played as a 
   * goalie, so set all counts to 0, then increment.
   */
  public void incrementGoalieGamesLost()
  {
    if (goalieGamesWon == -1)
      goalieGamesWon = goalieGamesLost = goalieGamesTied = 0;

    this.goalieGamesLost++;
    modified = true;
  } // End, incrementGoalieGamesLost()


  /**
   * Increment the number of games player tied as goalie by 1.  If
   * the current count is -1, this is the first game played as a 
   * goalie, so set all counts to 0, then increment.
   */
  public void incrementGoalieGamesTied()
  {
    if (goalieGamesWon == -1)
      goalieGamesWon = goalieGamesLost = goalieGamesTied = 0;

    this.goalieGamesTied++;
    modified = true;
  } // End, incrementGoalieGamesTied()


  /**
   * Get the player's goals against.  Returns -1
   * if the player has never been a goalie.
   */
  public int getGoalsAgainst() { return goalsAgainst; }


  /**
   * Set the player's goals against.  -1 means
   * the player is not or has never been a goalie.
   */
  private void setGoalsAgainst(int goalsAgainst)
  { this.goalsAgainst = goalsAgainst; modified = true; }


  /**
   * Increment the number of player goalsAgainst by one.
   */
  public void incrementGoalsAgainst() { incrementGoalsAgainst(1); }


  /**
   * Increment the number of player goalsAgainst by the specified amount.
   *
   * If goalsAgainst is -1, the player has never been a goalie before so value is the
   * increment amount.
   *
   * @param int numberOfGoalsAgainst The number of goalsAgainst to increment by.
   */
  public void incrementGoalsAgainst(int numGoalsAgainst)
  {
    if (goalsAgainst == -1) goalsAgainst = 0;
    this.goalsAgainst += numGoalsAgainst;
    modified = true;
  }


  /**
   * Get the seasonSession object
   */
  public SeasonSession getSeasonSession()
  {
    if (seasonSession == null && 
	seasonSessionOID != null && (seasonSessionOID.compareTo(new BigDecimal(0)) == 1))
    {	
      try {
	setSeasonSession(new SeasonSession(seasonSessionOID, myDbContext));
      }
      catch (SeasonSession.InvalidSeasonSessionException e) {
	log.error ("Could not find season-session for oid: " + seasonSessionOID + ": " + e);
      }
    } // End, if

    return seasonSession;
  }

  /**
   * Set the seasonSession
   */
  private void setSeasonSession(SeasonSession seasonSession)
  {
    this.seasonSession = seasonSession;
    if (seasonSession == null)
      this.seasonSessionOID = new BigDecimal(0);
    else
      this.seasonSessionOID = seasonSession.getOID();
  } // End, setSeasonSession


  /**
   * Set the seasonSession
   */
  private void setSeasonSession(BigDecimal seasonSessionOID)
  {
    this.seasonSessionOID = seasonSessionOID;
    if (seasonSessionOID == new BigDecimal(0))
      this.seasonSession = null;
    else
    {
      try {
	this.seasonSession = new SeasonSession(seasonSessionOID, myDbContext);
      }
      catch (SeasonSession.InvalidSeasonSessionException e) {
	log.error ("No SeasonSession found with oid: " + seasonSessionOID + ": " + e);
	this.seasonSession = null;
      }
    }
  } // End, setSeasonSession


  public BigDecimal getOID() { return oid; }

  private void setOID(BigDecimal oid) { this.oid = oid; }


  /**
   * The routine is called to persist the object to the database.
   */

  public void store(DBEngine db) throws DbStoreException
  {
    int numRows;

    String insertSeasonStats =
      "INSERT INTO season_stats (player_id, season_session_id, goals, assists, penalties, " + 
      "penalty_minutes, games_played, goalie_games_won, goalie_games_lost, goalie_games_tied, " +
      "goals_against, oid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

   String updateSeasonStats = 
     "UPDATE season_stats SET player_id = ?, season_session_id = ?, goals = ?, assists = ?, " +
     "penalties = ?, penalty_minutes = ?, games_played = ?, goalie_games_won = ?, " +
     "goalie_games_lost = ?, goalie_games_tied = ?, goals_against = ? WHERE oid = ?";


   Object[] params = new Object[12];
   int x=0;
   params[x++] = playerOID;
   params[x++] = seasonSessionOID;
   params[x++] = new BigDecimal(getGoals());
   params[x++] = new BigDecimal(getAssists());
   params[x++] = new BigDecimal(getPenalties());
   params[x++] = new BigDecimal(getPenaltyMinutes());
   params[x++] = new BigDecimal(getGamesPlayed());
   params[x++] = new BigDecimal(getGoalieGamesWon());
   params[x++] = new BigDecimal(getGoalieGamesLost());
   params[x++] = new BigDecimal(getGoalieGamesTied());
   params[x++] = new BigDecimal(getGoalsAgainst());
   params[x++] = getOID();

   String query;
   if (inserted)
     query = updateSeasonStats;
   else
     query = insertSeasonStats;

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
    theString.append("Stats for Player " + player);
    theString.append("\n  oid: " + oid.toString());
    theString.append("\n  Season: " + getSeasonSession().toString());

    return theString.toString();
  } // End, toString()

  public boolean equals(Object obj)
  {
    return obj instanceof SeasonStats && ((SeasonStats)obj).getOID().equals(getOID());
  } // End, equals()

  /*************************************************************************************/
  /*************************************************************************************/
  /*              FACTORY METHODS                                                      */
  /*************************************************************************************/
  /*************************************************************************************/

  /**
   * A factory method returning ArrayList of Players ordered by the number
   * of goals scored in the specified season-session.
   *
   * @param seasonSession Season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   * @exception SQLException
   */

  public static ArrayList getPlayersByGoalCount(SeasonSession selectedSeasonSession,
						DbContext dbContext) throws SQLException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Category log = Log4jUtils.initLog("Player");
    log.debug("Getting all players by number of goals for " +
	      selectedSeasonSession.getSeason().getSeasonName() + " " +
	      selectedSeasonSession.getSessionName());

    String query = "SELECT player_id, goals FROM season_stats WHERE season_session_id = ? " +
      " AND goals > 0 ORDER BY goals DESC";

    Object[] params = new Object[1];
    params[0] = selectedSeasonSession.getOID();

    log.debug ("Get players, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    ArrayList players = null;

    try {
      Vector results = db.executeQuery (query, params);
      int numPlayers = results.size();
      players = new ArrayList(numPlayers);
      for (int index=0; index<numPlayers; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	Player player = Player.getPlayer(DBUtils.getBigDecimal("PLAYER_ID", record), dbContext);
	if (!player.getFName().equals("Unknown"))
	  players.add(player);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting player data by stat: " + sqle);
      throw sqle;
    }

    return players;

  } // End, getPlayersByGoalCount()


  /**
   * A factory method returning ArrayList of Players ordered by the number
   * of assists in the specified season-session.
   *
   * @param seasonSession Season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   * @exception SQLException
   */

  public static ArrayList getPlayersByAssistCount(SeasonSession selectedSeasonSession,
						  DbContext dbContext) throws SQLException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Category log = Log4jUtils.initLog("Player");
    log.debug("Getting all players by number of assists for " +
	      selectedSeasonSession.getSeason().getSeasonName() + " " +
	      selectedSeasonSession.getSessionName());

    String query = "SELECT player_id, assists FROM season_stats WHERE season_session_id = ? " +
      " AND assists > 0 ORDER BY assists DESC";

    Object[] params = new Object[1];
    params[0] = selectedSeasonSession.getOID();

    log.debug ("Get players, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    ArrayList players = null;

    try {
      Vector results = db.executeQuery (query, params);
      int numPlayers = results.size();
      players = new ArrayList(numPlayers);
      for (int index=0; index<numPlayers; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	Player player = Player.getPlayer(DBUtils.getBigDecimal("PLAYER_ID", record), dbContext);
	if (!player.getFName().equals("Unknown"))
	  players.add(player);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting player data by number of assists: " + sqle);
      throw sqle;
    }

    return players;

  } // End, getPlayersByAssistCount()


  /**
   * A factory method returning ArrayList of Players ordered by the number
   * of points in the specified season-session.
   *
   * @param seasonSession Season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   * @exception SQLException
   */

  public static ArrayList getPlayersByPoints(SeasonSession selectedSeasonSession,
					     DbContext dbContext) throws SQLException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Category log = Log4jUtils.initLog("Player");
    log.debug("Getting all players by number of points for " +
	      selectedSeasonSession.getSeason().getSeasonName() + " " +
	      selectedSeasonSession.getSessionName());

    String query = "SELECT player_id FROM season_stats WHERE season_session_id = ? " +
      " AND goals + assists > 0 ORDER BY goals + assists DESC";

    Object[] params = new Object[1];
    params[0] = selectedSeasonSession.getOID();

    log.debug ("Get players, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    ArrayList players = null;

    try {
      Vector results = db.executeQuery (query, params);
      int numPlayers = results.size();
      players = new ArrayList(numPlayers);
      for (int index=0; index<numPlayers; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	Player player = Player.getPlayer(DBUtils.getBigDecimal("PLAYER_ID", record), dbContext);
	if (!player.getFName().equals("Unknown"))
	  players.add(player);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting player data by number of points: " + sqle);
      throw sqle;
    }

    return players;

  } // End, getPlayersByPoints()


  /**
   * A factory method returning ArrayList of Players ordered by the number
   * of penalties in the specified season-session.
   *
   * @param seasonSession Season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   * @exception SQLException
   */

  public static ArrayList getPlayersByPenaltyCount(SeasonSession selectedSeasonSession,
						   DbContext dbContext) throws SQLException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Category log = Log4jUtils.initLog("Player");
    log.debug("Getting all players by number of penalties for " +
	      selectedSeasonSession.getSeason().getSeasonName() + " " +
	      selectedSeasonSession.getSessionName());

    String query = "SELECT player_id, penalties FROM season_stats WHERE season_session_id = ? " +
      " AND penalties > 0 ORDER BY penalties DESC";

    Object[] params = new Object[1];
    params[0] = selectedSeasonSession.getOID();

    log.debug ("Get players, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    ArrayList players = null;

    try {
      Vector results = db.executeQuery (query, params);
      int numPlayers = results.size();
      players = new ArrayList(numPlayers);
      for (int index=0; index<numPlayers; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	Player player = Player.getPlayer(DBUtils.getBigDecimal("PLAYER_ID", record), dbContext);
	if (!player.getFName().equals("Unknown"))
	  players.add(player);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting player data by number of penalties: " + sqle);
      throw sqle;
    }

    return players;

  } // End, getPlayersByPenaltyCount()


  /**
   * A factory method returning ArrayList of Players ordered by the number
   * of penalty minutes in the specified season-session.
   *
   * @param seasonSession Season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   * @exception SQLException
   */

  public static ArrayList getPlayersByPenaltyMinutes(SeasonSession selectedSeasonSession,
						     DbContext dbContext) throws SQLException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Category log = Log4jUtils.initLog("Player");

    String query = "SELECT player_id, penalty_minutes FROM season_stats " +
      "WHERE season_session_id = ? AND penalty_minutes > 0 ORDER BY penalty_minutes DESC";

    Object[] params = new Object[1];
    params[0] = selectedSeasonSession.getOID();

    log.debug ("Get players, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    ArrayList players = null;

    try {
      Vector results = db.executeQuery (query, params);
      int numPlayers = results.size();
      players = new ArrayList(numPlayers);
      for (int index=0; index<numPlayers; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	Player player = Player.getPlayer(DBUtils.getBigDecimal("PLAYER_ID", record), dbContext);
	if (!player.getFName().equals("Unknown"))
	  players.add(player);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting player data by number of penalty minutes: " + sqle);
      throw sqle;
    }

    return players;

  } // End, getPlayersByPenaltyMinutes()


  /**
   * A factory method returning ArrayList of goalies ordered by 
   * goals-against average
   *
   * @param seasonSession Season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   * @exception SQLException
   */

  public static ArrayList getGoalies(SeasonSession selectedSeasonSession,
				     DbContext dbContext) throws SQLException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Category log = Log4jUtils.initLog("Player");
    log.debug("Getting all players by number of penalties for " +
	      selectedSeasonSession.getSeason().getSeasonName() + " " +
	      selectedSeasonSession.getSessionName());

    String query = "SELECT player_id FROM season_stats WHERE season_session_id = ? " +
      "AND goals_against > -1 " +
      "ORDER BY goals_against / (goalie_games_won + goalie_games_lost + goalie_games_tied) ASC";

    Object[] params = new Object[1];
    params[0] = selectedSeasonSession.getOID();

    log.debug ("Get goalies, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    ArrayList players = null;

    try {
      Vector results = db.executeQuery (query, params);
      int numPlayers = results.size();
      players = new ArrayList(numPlayers);
      for (int index=0; index<numPlayers; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	Player player = Player.getPlayer(DBUtils.getBigDecimal("PLAYER_ID", record), dbContext);
	if (!player.getFName().equals("Unknown"))
	  players.add(player);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting goalies: " + sqle);
      throw sqle;
    }

    return players;

  } // End, getGoalies()

} // End, class SeasonStats
