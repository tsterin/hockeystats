// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/GameGoal.java,v 1.1 2002/09/15 01:53:23 tom Exp $

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
 * Class to represent the GAME_GOAL table.  This table is used
 * to store who scored goals and who got assists in each game.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: GameGoal.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.2  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.1  2002/05/30 19:26:56  tom
 * New object representing a goal in a game.
 *
 *
 */

public class GameGoal extends DbStore {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/GameGoal.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  private Category log = Log4jUtils.initLog(this);

  private BigDecimal oid;
  private Game game;
  private BigDecimal gameOID;
  private Team team;
  private BigDecimal teamOID;
  private int goal;
  private Player scoredBy;
  private BigDecimal scoredByOID;
  private Player assist1;
  private BigDecimal assist1OID;
  private Player assist2;
  private BigDecimal assist2OID;
  private int period;
  private String goalTime;
  private DbContext myDbContext = null;


  // Constructor to create a brand, new (empty) object

  public GameGoal(Game game, Team team, int goal, DbContext dbContext)
  {
    BigDecimal none = new BigDecimal(0);
    log.debug ("Creating a brand new empty GameGoal object.");
    this.oid = OIDSequencer.next(dbContext);

    this.game = game;
    this.gameOID = game.getOID();
    this.team = team;
    this.teamOID = team.getOID();
    this.goal = goal;
    this.scoredBy = null;
    this.scoredByOID = none;
    this.assist1 = null;
    this.assist1OID = none;
    this.assist2 = null;
    this.assist2OID = none;
    this.period = 0;
    this.goalTime = "";
    myDbContext = dbContext;
    inserted = false;
    modified = true;

  } // End, constructor


  // Constructor to create a brand, new object

  public GameGoal(Game game, Team team, int goal, Player scoredBy, Player assist1, Player assist2,
		  int period, String goalTime, DbContext dbContext)
  {
    log.debug ("Creating a brand new GameGoal object.");
    this.oid = OIDSequencer.next(dbContext);

    this.game = game;
    this.gameOID = game.getOID();
    this.team = team;
    this.teamOID = team.getOID();
    this.goal = goal;
    this.scoredBy = scoredBy;
    this.scoredByOID = scoredBy.getOID();
    this.assist1 = assist1;
    this.assist1OID = assist1 == null ? new BigDecimal(0) : assist1.getOID();
    this.assist2 = assist2;
    this.assist2OID = assist2 == null ? new BigDecimal(0) : assist2.getOID();
    this.period = period;
    this.goalTime = goalTime;
    myDbContext = dbContext;
    inserted = false;
    modified = true;

  } // End, constructor


  // Constructor to create a gameGoal object from the data in the database.

  private GameGoal(Hashtable record, DbContext dbContext)
  {
    log.debug ("Creating a GameGoal object.");

    this.oid = DBUtils.getBigDecimal("OID", record);
    this.game = null;
    this.gameOID = DBUtils.getBigDecimal("GAME", record);
    this.team = null;
    this.teamOID = DBUtils.getBigDecimal("TEAM", record);
    this.goal = DBUtils.getBigDecimal("GOAL_NUMBER", record).intValue();
    this.scoredBy = null;
    this.scoredByOID = DBUtils.getBigDecimal("SCORED_BY", record);
    this.assist1 = null;
    this.assist1OID = DBUtils.getBigDecimal("ASSIST1", record);
    this.assist2 = null;
    this.assist2OID = DBUtils.getBigDecimal("ASSIST2", record);
    this.period = DBUtils.getBigDecimal("PERIOD", record).intValue();
    this.goalTime = DBUtils.getString("GOAL_TIME", record);
    this.myDbContext = dbContext;
    inserted = true;
    modified = false;

  } // End, constructor


  public BigDecimal getOID() { return oid; }

  public Game getGame()
  {
    if (game == null)
      game = new Game(gameOID, myDbContext);

    return game;

  } // End, getGame()

  public Team getTeam() { return team; }

  public void setScoredBy(Player player)
  {
    scoredByOID = player.getOID();
    scoredBy = player;
    modified = true;
  } // End, getScoredBy()

  public Player getScoredBy()
  {
    if (scoredBy == null)
      scoredBy = Player.getPlayer(scoredByOID, myDbContext);

    return scoredBy;
  }

  public void setAssist1(Player player)
  {
    assist1OID = player.getOID();
    assist1 = player;
    modified = true;
  } // End, getAssist1()

  public Player getAssist1() {
    if (assist1 == null && assist1OID.compareTo(new BigDecimal(0)) != 0)
      assist1 = Player.getPlayer(assist1OID, myDbContext);

    return assist1;
  }

  public void setAssist2(Player player)
  {
    assist2OID = player.getOID();
    assist2 = player;
    modified = true;
  } // End, getAssist2()

  public Player getAssist2()
  {
    if (assist2 == null && assist2OID.compareTo(new BigDecimal(0)) != 0)
	assist2 = Player.getPlayer(assist2OID, myDbContext);

    return assist2;
  }

  public int getPeriod() { return period; }

  public void setPeriod(int period) { this.period = period; }

  public String getTime() { return goalTime; }

  public void setGoalTime(String goalTime) { this.goalTime = goalTime; }

  protected void store(DBEngine db) throws DbStoreException
  {
    // If we don't need to do anything, bail out now.
    if (inserted && !modified) return;

    int numRows;

    String insertQuery = "INSERT INTO game_goal (game, team, goal_number, scored_by, assist1, " +
      "assist2, period, goal_time, oid) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String updateQuery = "UPDATE game_goal SET game = ?, team = ?, goal_number = ?, " +
      "scored_by = ?, assist1 = ?, assist2 = ?, period = ?, goal_time = ? WHERE oid = ?";

    Object[] params = new Object[9];
    int x=0;
    params[x++] = gameOID;
    params[x++] = teamOID;
    params[x++] = new BigDecimal(goal);
    params[x++] = scoredByOID;
    if (assist1OID.compareTo(new BigDecimal(0)) == 0)
      params[x++] = new Null(java.sql.Types.INTEGER);
    else
      params[x++] = assist1OID;
    if (assist2OID.compareTo(new BigDecimal(0)) == 0)
      params[x++] = new Null(java.sql.Types.INTEGER);
    else
      params[x++] = assist2OID;
    params[x++] = new BigDecimal(period);
    params[x++] = goalTime;
    params[x++] = getOID();

    String query;
    if (inserted)
      query = updateQuery;
    else
      query = insertQuery;

    try {
      log.debug ("update game_goal, SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (query, params));
      if ((numRows = db.executeUpdate(query, params)) != 1)
	log.error ("Update only updated " + numRows + " rows.");
    }
    catch (java.sql.SQLException sqlEx) {
      throw new DbStoreException(sqlEx.toString());
    }

  } // End, store()


  public static ArrayList getGoalsInGame(Game game, Team team, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    ArrayList goals = null;

    Category log = Log4jUtils.initLog("GameGoal");
    log.debug ("Getting all goals for the game on " + game.getGameDate() +
	       " on team " + team.getTeamName());

    String query = "SELECT oid, game, team, goal_number, scored_by, nvl(assist1,0) assist1, " +
      "nvl(assist2,0) assist2, period, goal_time FROM game_goal " +
      " WHERE game = ? AND team = ?";
    Object[] params = new Object[2];
    params[0] = game.getOID();
    params[1] = team.getOID();

    log.debug ("get goals, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    try {
      Vector results = db.executeQuery (query, params);
      int numRows = results.size();
      goals = new ArrayList(numRows);
      for (int index=0; index<numRows; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	GameGoal aGoal = new GameGoal(record, dbContext);
	goals.add(aGoal);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting goals: " + sqle);
    }

    return goals;
    
  } // End, getGoalsInGame()

  public String toString()
  {
    return "Game " + getGame() + " goal # " + goal + " for team " + getTeam() + " scored by " +
      scoredBy;
  } // End, toString()

} // End, class GameGoal
