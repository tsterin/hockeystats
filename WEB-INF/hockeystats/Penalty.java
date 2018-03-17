// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Penalty.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;
import java.math.*;
import java.text.*;
import hockeystats.util.db.*;
import java.util.*;
import java.sql.*;

 // Import log4j classes.
import hockeystats.util.Log4jUtils;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;


/** 
 * Class to represent a penalty
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: Penalty.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.4  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.3  2002/05/30 19:28:14  tom
 * Changes to site to allow viewing and changing of goal data.
 *
 * Revision 1.2  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.1  2002/04/03 14:04:41  tom
 * First production version.
 *
 *
 */

public class Penalty extends DbStore {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Penalty.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  private Category log = Log4jUtils.initLog(this);

  private BigDecimal oid;
  private Player player;
  private BigDecimal playerOID;
  private Game game;
  private BigDecimal gameOID;
  private Team team;
  private BigDecimal teamOID;
  private int penaltyID;
  private int period; // 4 = overtime
  private String time; // game time at time of penalty
  private int minutes; // length of penalty assigned (not served)
  private boolean inserted;
  private DbContext myDbContext = null;

  private static HashMap penaltyNames = null;

  /**
   *
   */

  private void setPenaltyInfo(Hashtable record)
  {
    oid = DBUtils.getBigDecimal("OID", record);
    playerOID = DBUtils.getBigDecimal("PLAYER", record);
    player = null;
    gameOID = DBUtils.getBigDecimal("GAME", record);
    game = null;
    teamOID = DBUtils.getBigDecimal("TEAM", record, new BigDecimal(0));
    team = null;
    penaltyID = DBUtils.getBigDecimal("PENALTY_ID", record).intValue();
    period = DBUtils.getBigDecimal("PERIOD", record).intValue();
    time = DBUtils.getString("TIME", record);
    minutes = DBUtils.getBigDecimal("MINUTES", record).intValue();

    inserted = true;

    return;

  } // End, setPenaltyInfo()

  private Penalty() {}

  /**
   * Constructor to create a brand new penalty.
   */
  public Penalty(Player player, Game game, int penaltyID, int minutes, Team team, String time, int period,
		 DbContext dbContext)
  {
    oid = OIDSequencer.next(dbContext);
    this.player = player;
    this.playerOID = player.getOID();
    this.game = game;
    this.gameOID = game.getOID();
    this.penaltyID = penaltyID;
    this.minutes = minutes;
    this.team = team;
    this.teamOID = team.getOID();
    this.time = time;
    this.period = period;
    this.myDbContext = dbContext;
  } // End, constructor

  /**
   * Only this class can create an empty Penalty object.
   */
  private Penalty(DbContext dbContext)
  {
    this.inserted = true;
    this.modified = false;
    this.myDbContext = dbContext;
  }

  /**
   * Constructor that gets Penalty data from database given a penalty OID.
   */
  public Penalty (BigDecimal oid, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());

    String query = "SELECT oid, game, player, penalty_name, nvl(period,0) period, nvl(time, '') time, " +
      "nvl(team, 0) team, nvl(minutes,2) minutes, p.penalty_id" +
      " FROM penalty p, penalties ps" +
      " WHERE oid = ? AND p.penalty_id = ps.penalty_id";

    log.debug ("Get penalty with oid = " + oid);

    Object[] params = new Object[1];
    params[0] = oid;

    try {
      Vector results = db.executeQuery (query, params);
      Hashtable record = (Hashtable) results.get(0);
      setPenaltyInfo(record);
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting penalty: " + sqle);
    }

    this.inserted = true;
    this.myDbContext = dbContext;

  } // End, constructor


  /**
   * Factory method to create the ArrayList of penalty types
   *
   */

  public static HashMap getPenaltyNames(DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Category log = Log4jUtils.initLog("PenaltyGetNames");
    Object[] params = null;

    String query = "SELECT penalty_id, penalty_name FROM penalties ORDER BY penalty_name";

    if (penaltyNames == null)  // If we haven't cached this yet
    {
      try {
	log.debug ("SQL: " + query);
	Vector results = db.executeQuery (query, params);
	penaltyNames = new HashMap((int)(results.size()/.75)); // .75 is default load factor
	for (int index=0; index < results.size(); index++)
	{
	  Hashtable record = (Hashtable) results.get(index);
	  String penaltyName = DBUtils.getString("PENALTY_NAME", record);
	  int penaltyID = DBUtils.getBigDecimal("PENALTY_ID", record).intValue();
	  penaltyNames.put(new Integer(penaltyID), penaltyName);
	}
      }
      catch (java.sql.SQLException sqle) {
	log.error("Error getting penalty name: " + sqle);
      }
    } // End, if not cached yet

    return penaltyNames;

  } // End, getPenaltyNames()


  /**
   * Get the Player
   */
  public Player getPlayer()
  {
    if (player == null)
      player = Player.getPlayer(playerOID, myDbContext);

    return player;
  } // End, getPlayer()


  /**
   * Set the Player
   *
   * @param player player object representing player who was penalized
   */
  public void setPlayer(Player player)
  {
    this.player = player;
    this.playerOID = player.getOID();
    modified = true;
  }


  /**
   * Get the Team
   */
  public Team getTeam()
  {
    if (team == null && !teamOID.equals(new BigDecimal(0)))
    {
      team = Team.getTeam(teamOID, myDbContext);
    } // End, if (team == null)

    return team;

  } // End, getTeam


  /**
   * Set the team
   *
   * @param team team object representing team of player who was penalized
   */
  public void setTeam(Team team)
  {
    this.team = team;
    this.teamOID = team.getOID();
    modified = true;
  }


  /**
   * Get the Game
   */
  public Game getGame()
  {
    if (game == null)
      game = new Game(gameOID, myDbContext);

    return game;
  } // End, getGame()


  /**
   * Set the Game
   *
   * @param game game object representing game when the penalty was given
   */
  public void setGame (Game game)
  {
    this.game = game;
    this.gameOID = game.getOID();
    modified = true;
  }


  /**
   * Get the penalty ID
   */
  public int getPenaltyID() { return penaltyID; }

  /**
   * Get the penalty text
   */
  public String getPenaltyText() { return Penalty.getPenaltyTextFromID(penaltyID, myDbContext); }


  /**
   * Get the number of minutes penalized.  This is not necessarily the number
   * of minutes served.
   */
  public int getMinutes() { return minutes; }

  public void setMinutes(int minutes)
  {
    this.minutes = minutes;
    modified = true;
  }

  /**
   * Get the period the penalty was given
   */
  public int getPeriod() { return period; }

  public void setPeriod(int period)
  {
    this.period = period;
    modified = true;
  } // End, setPeriod()

  /**
   * Get the time of the penalty
   */
  public String getTime() { return time; }

  public void setTime(String time)
  {
    this.time = time;
    modified = true;
  }

  /**
   * Get the oid for this penalty.
   */

  public BigDecimal getOID() { return oid; }


  /**
   * Get a penalty text from the penalty ID.
   *
   * @param penaltyID
   */

  private static String getPenaltyTextFromID(int penaltyID, DbContext dbContext)
  {
    HashMap penaltyNames = getPenaltyNames(dbContext);
    return (String)penaltyNames.get(new Integer(penaltyID));
  } // End, getPenaltyTextFromID()

  public void store(DBEngine db) throws DbStoreException
  {
    int numRows;

    String insertPenalty =
      "INSERT INTO penalty (player, game, penalty_id, period, time, minutes, team, oid) " + 
      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

   String updatePenalty =
     "UPDATE penalty SET player = ?, game = ?, penalty_id = ?, period = ?, time = ?, minutes = ?, " +
     "team = ? WHERE oid = ?";

   Object[] params = new Object[8];
   int x=0;
   params[x++] = playerOID;
   params[x++] = gameOID;
   params[x++] = new BigDecimal(getPenaltyID());
   params[x++] = new BigDecimal(getPeriod());
   params[x++] = getTime();
   params[x++] = new BigDecimal(getMinutes());
   params[x++] = teamOID;
   params[x++] = getOID();

   String query;
   if (inserted)
     query = updatePenalty;
   else
     query = insertPenalty;

   try {
     log.debug ("Storing penalty: " + toString());
     log.debug ("SQL: " +
		DBUtils.getSQLStatementFromPreparedStatement (query, params));
     if ((numRows = db.executeUpdate(query, params)) != 1)
      log.error ("Update only updated " + numRows + " rows.");
   }
   catch (java.sql.SQLException sqlEx) {
     throw new DbStoreException(sqlEx.toString());
   }

  } // End, store()

  public static ArrayList getPenalties(Game game, Team team, DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    DBEngine db = new DBEngine(dbContext.getContextString());
    ArrayList penalties = null;

    String query = "SELECT oid, game, player, penalty_name, nvl(period,0) period, nvl(time, '') time, " +
      "nvl(team, 0) team, nvl(minutes,2) minutes, p.penalty_id" +
      " FROM penalty p, penalties ps" +
      " WHERE game = ? AND team = ? AND p.penalty_id = ps.penalty_id";

    Object[] params = new Object[2];
    params[0] = game.getOID();
    params[1] = team.getOID();

    log.debug("Get penalties, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    try {
      Vector results = db.executeQuery(query, params);
      int numPenalties = results.size();
      penalties = new ArrayList(numPenalties);
      for (int index=0; index< numPenalties; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	Penalty aPenalty = new Penalty(dbContext);
	aPenalty.setPenaltyInfo(record);
	penalties.add(aPenalty);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting penalty info: " + sqle);
    }

    return penalties;

  } // end, getPenalties()


  public static ArrayList getAllPenaltiesInGame(Game game, DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    DBEngine db = new DBEngine(dbContext.getContextString());
    ArrayList penalties = null;

    String query = "SELECT oid, game, player, penalty_name, nvl(period,0) period, nvl(time, '') time, " +
      "team, nvl(minutes,2) minutes, p.penalty_id" +
      " FROM penalty p, penalties ps" +
      " WHERE game = ? AND p.penalty_id = ps.penalty_id";

    Object[] params = new Object[1];
    params[0] = game.getOID();

    log.debug("Get penalties, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    try {
      Vector results = db.executeQuery(query, params);
      int numPenalties = results.size();
      penalties = new ArrayList(numPenalties);
      for (int index=0; index< numPenalties; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	Penalty aPenalty = new Penalty(dbContext);
	aPenalty.setPenaltyInfo(record);
	penalties.add(aPenalty);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting penalty info: " + sqle);
    }

    return penalties;

  } // end, getAllPenaltiesInGame()


  public String toString()
  {
    StringBuffer theString = new StringBuffer("Penalty to " + getPlayer().getFName() +
					      " " + getPlayer().getLName());
    theString.append(", penalized " + getMinutes() + " minutes for " + getPenaltyText());

    return theString.toString();
  } // End, toString()

  //public static void main (String[] args)
  //{
  //} // End, main()


} // End, class Penalty;
