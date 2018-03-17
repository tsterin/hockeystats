// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/GamePlayer.java,v 1.1 2002/09/15 01:53:23 tom Exp $

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
 * Class to represent the GAME_PLAYER associative table.  This is used
 * to store players who play in a game.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: GamePlayer.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.9  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.8  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.7  2002/05/30 19:28:14  tom
 * Changes to site to allow viewing and changing of goal data.
 *
 * Revision 1.6  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.5  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.4  2001/12/23 02:35:46  tom
 * Changes to fix updating game information.
 *
 * Revision 1.3  2001/12/20 14:19:59  tom
 * Latest changes.
 *
 * Revision 1.2  2001/12/10 07:09:45  tom
 * Add cvs Log section.
 *
 */

public class GamePlayer extends DbStore {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/GamePlayer.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  private Category log = Log4jUtils.initLog(this);

  private BigDecimal oid;
  private Game game;
  private BigDecimal gameOID;
  private Player player;
  private BigDecimal playerOID;
  private Team team;     // Which team the player played for IN THIS GAME
  private BigDecimal teamOID;
  private boolean substitute;  // True if the player was not on the team's roster
  private boolean goalie; // True if this player was goalie in the game
  private DbContext myDbContext;


  // Constructor to create a brand, new object

  public GamePlayer(Game game, Player player, Team team, boolean substitute,
		    boolean goalie, DbContext dbContext)
  {
    log.debug ("Creating a brand new GamePlayer object.");
    this.oid = OIDSequencer.next(dbContext);

    this.game = game;
    this.gameOID = game.getOID();
    this.player = player;
    this.playerOID = player.getOID();
    this.team = team;
    this.teamOID = team.getOID();
    this.substitute = substitute;
    this.goalie = goalie;

    myDbContext = dbContext;

  } // End, constructor


  // Constructor to create an object from the data in the database.  Called from the 
  // getGamePlayersInGame factory method.

  private GamePlayer(BigDecimal oid, BigDecimal gameOID, BigDecimal playerOID, 
		     BigDecimal teamOID, boolean substitute, boolean goalie, 
		     DbContext dbContext)
  {
    this.oid = oid;
    this.gameOID = gameOID;
    this.game = null;
    this.playerOID = playerOID;
    this.player = null;
    this.teamOID = teamOID;
    this.team = null;
    this.substitute = substitute;
    this.goalie = goalie;
    myDbContext = dbContext;
  } // End, constructor


  public Player getPlayer()
  {
    if (player == null)
      player = Player.getPlayer (playerOID, myDbContext);

    return player;
  } // End, getPlayer


  public BigDecimal getOID() { return oid; }

  public Game getGame()
  {
    if (game == null)
      game = new Game(gameOID, myDbContext);

    return game;
  } // End, getGame()


  public Team getTeam()
  {
    if (team == null)
      team = Team.getTeam(teamOID, myDbContext);

    return team;
  } // End, getTeam()

  public boolean isSubstitute() { return substitute; }
  public boolean isGoalie() { return goalie; }
  public void setGoalie() { goalie = true; }
  public void clearGoalie() { goalie = false; }

  public static String selectStmt = "SELECT game, player, team, goalie, substitute ";

  protected void store(DBEngine db) throws DbStoreException
  {
    int numRows;

    String query =
      "INSERT INTO game_player (oid, game, player, team, substitute, goalie) " +
      "VALUES (?, ?, ?, ?, ?, ?)";

    Object[] params = new Object[6];
    int x=0;
    params[x++] = getOID();
    params[x++] = gameOID;
    params[x++] = playerOID;
    params[x++] = teamOID;
    params[x++] = isSubstitute() ? "Y" : "N";
    params[x++] = isGoalie() ? "Y" : "N";

    try {
      log.debug ("store GamePlayer, SQL: " +
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
    return "Player " + getPlayer() + " played in game " + getGame() + " for team " + getTeam();
  } // End, toString()


  /*************************************************************************************/
  /*************************************************************************************/
  /*              FACTORY METHODS                                                      */
  /*************************************************************************************/
  /*************************************************************************************/

  public static Player[] getPlayersInGame(Game game, Team team, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    BigDecimal gameOID = game.getOID();
    BigDecimal teamOID = team.getOID();

    Category log = Log4jUtils.initLog("GamePlayer");
    log.debug ("Getting all players for the game on " + game.getGameDate() +
	       " on team " + team.getTeamName());

    String query = selectStmt + " FROM game_player " + " WHERE game = ? AND team = ?";
    Object[] params = new Object[2];
    params[0] = gameOID;
    params[1] = teamOID;

    Player[] players = null;

    log.debug ("get players, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    try {
      Vector results = db.executeQuery (query, params);
      int numRows = results.size();
      players = new Player[numRows];
      for (int index=0; index<numRows; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	BigDecimal playerOID = DBUtils.getBigDecimal("PLAYER", record);
	players[index] = Player.getPlayer(playerOID, dbContext);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting gamePlayer: " + sqle);
    }

    return players;
    
  } // End, getPlayersInGame()


  public static GamePlayer[] getGamePlayersInGame(Game game, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    BigDecimal gameOID = game.getOID();

    Category log = Log4jUtils.initLog("GamePlayer");
    log.debug ("Getting all players for the game on " + game.getGameDate());

    String query = selectStmt + " FROM game_player " + " WHERE game = ?";
    Object[] params = new Object[1];
    params[0] = gameOID;

    GamePlayer[] gamePlayers = null;

    log.debug ("get players, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    try {
      Vector results = db.executeQuery (query, params);
      int numRows = results.size();
      gamePlayers = new GamePlayer[numRows];
      for (int index=0; index<numRows; index++)
      {
	Hashtable record = (Hashtable) results.get(index);
	BigDecimal OID = DBUtils.getBigDecimal("OID", record);
	BigDecimal playerOID = DBUtils.getBigDecimal("PLAYER", record);
	BigDecimal teamOID = DBUtils.getBigDecimal("TEAM", record);
	boolean goalie = DBUtils.getString("GOALIE", record).equals("Y");
	boolean substitute = DBUtils.getString("SUBSTITUTE", record).equals("Y");

	gamePlayers[index] = new GamePlayer(OID, gameOID, playerOID, teamOID, substitute,
					    goalie, dbContext);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting gamePlayer: " + sqle);
    }

    return gamePlayers;
    
  } // End, getGamePlayersInGame()


  /**
   * Return the player object of the player who was the goalie in the game.
   *
   * @param game Game object of the game whose goalie is being retrieved
   * @param team Team object of the team whose goalie is being retrieved
   * @return Player object of the player who was the goalie in the specified game.
   *
   */
  public static Player getGoalie(Game game, Team team, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    BigDecimal gameOID = game.getOID();
    BigDecimal teamOID = team.getOID();

    Category log = Log4jUtils.initLog("GamePlayer");
    log.debug ("Getting goalie for the game on " + game.getGameDate() +
	       " on team " + team.getTeamName());

    String query = "SELECT oid, player, substitute, goalie FROM game_player " +
      " WHERE game = ? AND team = ? AND goalie = 'Y'";
    Object[] params = new Object[2];
    params[0] = gameOID;
    params[1] = teamOID;

    Player goalie = null;

    log.debug ("get players, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    try {
      Vector results = db.executeQuery (query, params);
      if (results.size() != 0)
      {
	Hashtable record = (Hashtable) results.get(0);
	BigDecimal playerOID = DBUtils.getBigDecimal("PLAYER", record);
	goalie = Player.getPlayer(playerOID, dbContext);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting gamePlayer: " + sqle);
    }

    return goalie;
    
  } // End, getGoalie()



} // End, class GamePlayer
