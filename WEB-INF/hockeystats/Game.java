// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Game.java,v 1.1 2002/09/15 01:53:23 tom Exp $

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
 * Class to represent a game in the league.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: Game.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.12  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.11  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.10  2002/05/30 19:28:14  tom
 * Changes to site to allow viewing and changing of goal data.
 *
 * Revision 1.9  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.8  2002/04/08 13:44:37  tom
 * Add season_session to all select statements, now that we are populating it.
 *
 * Revision 1.7  2002/04/08 04:18:12  tom
 * Changes to populate/use season_session column in game table.
 *
 * Revision 1.6  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.5  2001/12/23 02:40:16  tom
 * Qualify Date as java.util.Date
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

public class Game extends DbStore {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Game.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  private Category log = Log4jUtils.initLog(this);

  private BigDecimal oid;
  private java.util.Date gameDate;
  private int homeScore;
  private int visitorScore;
  private String rinkLocation;
  private String homeLockerRoom;
  private String visitorLockerRoom;
  private Team homeTeam;
  private BigDecimal homeTeamOID;
  private Team visitorTeam;
  private BigDecimal visitorTeamOID;
  private SeasonSession seasonSession;
  private BigDecimal seasonSessionOID;
  private DbContext myDbContext;

  /**
   *
   */

  private void setGameInfo(Hashtable record)
  {
    oid = DBUtils.getBigDecimal("OID", record);
    gameDate = DBUtils.getDate("GAME_DATE", record);
    rinkLocation = DBUtils.getString("RINK_LOCATION", record);
    homeScore = DBUtils.getBigDecimal("HOME_SCORE", record).intValue();
    visitorScore = DBUtils.getBigDecimal("VISITOR_SCORE", record).intValue();
    homeLockerRoom = DBUtils.getString("HOME_LOCKER_ROOM", record);
    visitorLockerRoom = DBUtils.getString("VISITOR_LOCKER_ROOM", record);
    homeTeamOID = DBUtils.getBigDecimal("HOME_TEAM", record);
    visitorTeamOID = DBUtils.getBigDecimal("VISITOR_TEAM", record);
    homeTeam = null;
    visitorTeam = null;
    seasonSession = null;
    seasonSessionOID = DBUtils.getBigDecimal("SEASON_SESSION", record);

    inserted = true;

    return;

  } // End, setGameInfo()

  private Game() {}

  /**
   * Constructor to create a brand new game.
   */
  public Game (java.util.Date gameDate, String rinkLocation, Team homeTeam, Team visitorTeam,
	       SeasonSession selectedSeasonSession, DbContext dbContext)
  {
    oid = OIDSequencer.next(dbContext);
    this.gameDate = gameDate;
    this.rinkLocation = rinkLocation;
    this.homeTeam = homeTeam;
    this.homeTeamOID = homeTeam.getOID();
    this.visitorTeam = visitorTeam;
    this.visitorTeamOID = visitorTeam.getOID();
    this.homeScore = -1;
    this.visitorScore = -1;
    this.homeLockerRoom = null;
    this.visitorLockerRoom = null;
    this.setSeasonSession(selectedSeasonSession);
    this.inserted = false;
    this.myDbContext = dbContext;
  } // End, constructor

  /**
   * Constructor that gets Game data from database given a Game OID.
   *
   * @param oid OID of the game to be retrieved
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   */
  public Game (BigDecimal oid, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    String query = "SELECT oid, home_team, visitor_team, game_date, home_score, visitor_score, " +
      "rink_location, season_session FROM game WHERE oid = ?";

    log.debug ("Get game with oid = " + oid);

    Object[] params = new Object[1];
    params[0] = oid;

    try {
      Vector results = db.executeQuery (query, params);
      Hashtable record = (Hashtable) results.get(0);
      setGameInfo(record);
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting game: " + sqle);
    }

    this.inserted = true;
    this.myDbContext = dbContext;

  } // End, constructor


  /**
   * Factory method to create an array of games which is a team's schedule. 
   *
   * @param teamName The name of the team whose schedule is to be returned
   * @param seasonSession SeasonSession object describing the season of interest
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   *
   */

  public static ArrayList getSchedule(String teamName, SeasonSession selectedSeasonSession,
				      DbContext dbContext)
    throws hockeystats.Team.NoSuchTeamException
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Category log = Log4jUtils.initLog("GameGetSchedule");
    ArrayList games = null;
    String query;
    Object[] params = null;

    String teamQuery = "SELECT oid, home_team, visitor_team, game_date, home_score, " +
      "visitor_score, nvl(home_locker_room, '') home_locker_room, " +
      "nvl(visitor_locker_room, '') visitor_locker_room, " +
      "rink_location, season_session " +
      "FROM game WHERE (home_team = ? OR visitor_team = ?) AND season_session = ? " +
      "ORDER BY game_date";

    String allQuery = "SELECT oid, home_team, visitor_team, game_date, home_score, " +
      "visitor_score, nvl(home_locker_room, '') home_locker_room, " +
      "nvl(visitor_locker_room, '') visitor_locker_room, " +
      "rink_location, season_session FROM game WHERE season_session = ? ORDER BY game_date";

    if (teamName.equals("ALL"))
    {
      log.debug ("Get schedule for whole league.");

      query = allQuery;
      params = new Object[1];
      params[0] = selectedSeasonSession.getOID();
    }
    else
    {
      Team team = Team.getTeam(teamName, selectedSeasonSession.getSeason(), dbContext);
      log.debug ("Get schedule for " + team.getTeamName());

      query = teamQuery;
      params = new Object[3];
      params[0] = team.getOID();
      params[1] = team.getOID();
      params[2] = selectedSeasonSession.getOID();
    }

    try {
      log.debug ("SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (query, params));
      Vector results = db.executeQuery (query, params);
      games = new ArrayList(results.size());
      for (int gameIndex=0; gameIndex < results.size(); gameIndex++)
      {
	Hashtable record = (Hashtable) results.get(gameIndex);
	Game aGame = new Game();
	aGame.setGameInfo(record);
	aGame.myDbContext = dbContext;
	games.add(aGame);
//	System.out.println("game: " + gameIndex + ", " +
//			   games[gameIndex].getHomeTeam().getTeamName() + " VS " +
//			   games[gameIndex].getVisitorTeam().getTeamName());
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting game: " + sqle);
    }

    return games;

  } // End, getSchedule(Team team)


  /**
   * Factory method to create an array of games which is the league schedule. 
   *
   */

//    public static Game[] getSchedule()
//    {
//      Game[] games = null;

//      return games;

//    } // End, getSchedule()


  /**
   * Get the game date
   */
  public java.util.Date getGameDate() { return gameDate; }


  /**
   * Get the game date in a readable format
   */
  public String getFormattedDate(String dateFormat)
  {
    SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

    return formatter.format(gameDate);
  } // End, getFormattedDate()


  /**
   * Set the game date
   *
   * @param gameDate Date to set/change the date to
   */
  public void setGameDate(java.util.Date gameDate) { this.gameDate = gameDate; }


  /**
   * Get the home team score
   */
  public int getHomeScore() { return homeScore; }


  /**
   * Set the home team score
   *
   * @param homeScore The home team's score in the game
   */
  public void setHomeScore(int homeScore) { this.homeScore = homeScore; }

  /**
   * Get the visitor team score
   */
  public int getVisitorScore() { return visitorScore; }

  /**
   * Set the visitor team score
   *
   * @param visitorScore The visitor team's score in the game
   */
  public void setVisitorScore(int visitorScore) { this.visitorScore = visitorScore; }


  /**
   * Get the home team using lazy initialization.  The home team object will not be
   * retrieved from the database until the first time it is accessed.
   */
  public Team getHomeTeam()
  {
    if (homeTeam == null)
    {
      homeTeam = Team.getTeam(homeTeamOID, myDbContext);
    }

  return homeTeam;

  } // End, getHomeTeam()


  /**
   * Set the home team
   */
  public void setHomeTeam(Team team) { this.homeTeam = team; }


  /**
   * Set the seasonSession and seasonSessionOID
   */
  public void setSeasonSession(SeasonSession selectedSeasonSession)
  {
    this.seasonSession = selectedSeasonSession;
    this.seasonSessionOID = selectedSeasonSession.getOID();
  } // End, setSeasonSession()

  /**
   * Get seasonSessionOID
   */
  public BigDecimal getSeasonSessionOID() { return seasonSessionOID; }


  /**
   * Get the visitor team using lazy initialization.  The visitor team object
   * will not be retrieved from the database until the first time it is accessed.
   */
  public Team getVisitorTeam()
  {
    if (visitorTeam == null)
      visitorTeam = Team.getTeam(visitorTeamOID, myDbContext);

  return visitorTeam;

  } // End, getVisitorTeam()


  /**
   * Set the visitor team
   */
  public void setVisitorTeam(Team team) { this.visitorTeam = team; }


  /**
   * Get the home team locker room
   */
  public String getHomeLockerRoom() { return homeLockerRoom == null ? "" : homeLockerRoom; }


  /**
   * Get the visitor team locker room
   */
  public String getVisitorLockerRoom() { return visitorLockerRoom == null ? "" : visitorLockerRoom; }


  /**
   * Get the rink where the game is to be played
   */
  public String getRinkLocation() { return rinkLocation; }


  /**
   * Get the oid for this game.
   */

  public BigDecimal getOID() { return oid; }


  /**
   * Get an ArrayList with the players who played for the home team.
   */

  public ArrayList getHomeTeamPlayers()
  {
    Player[] players = GamePlayer.getPlayersInGame(this, getHomeTeam(), myDbContext);
    ArrayList playersAL = new ArrayList(players.length);
    for (int i=0; i<players.length; i++)
      playersAL.add(players[i]);

    return playersAL;
  } // End, getHomeTeamPlayers()


  /**
   * Get an ArrayList with the players who played for the visitor team.
   */

  public ArrayList getVisitorTeamPlayers()
  {
    Player[] players = GamePlayer.getPlayersInGame(this, getVisitorTeam(), myDbContext);
    ArrayList playersAL = new ArrayList(players.length);
    for (int i=0; i<players.length; i++)
      playersAL.add(players[i]);

    return playersAL;
  } // End, getVisitorTeamPlayers()


  /**
   *
   * Get the home team goalie for the game.
   *
   */
  public Player getHomeTeamGoalie()
  {
    return GamePlayer.getGoalie(this, homeTeam, myDbContext);
  } // End, getHomeTeamGoalie()


  /**
   *
   * Get the visitor team goalie for the game.
   *
   */
  public Player getVisitorTeamGoalie()
  {
    return GamePlayer.getGoalie(this, visitorTeam, myDbContext);
  } // End, getVisitorTeamGoalie()


  /**
   *
   * Delete all the goal and penalty info for this game.
   *
   */

  public boolean clearGoalPenaltyInfo(DBEngine db)
  {
    String delete1 = "DELETE FROM goal_player WHERE game = ?";
    String delete2 = "DELETE FROM penalty WHERE game = ?";

    Object[] params = new Object[1];
    params[0] = getOID();

    try {
      log.debug ("Delete goals, SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (delete1, params));
      db.executeUpdate(delete1, params);
      log.debug ("Delete penalties, SQL: " +
		 DBUtils.getSQLStatementFromPreparedStatement (delete2, params));
      db.executeUpdate(delete2, params);
    }
    catch (java.sql.SQLException sqlEx) {
      log.error("Error deleting game data: " + sqlEx.toString());
      return false;
    }

    return true;
  } // End, clearGoalPenaltyInfo()


  public void store(DBEngine db) throws DbStoreException
  {
    int numRows;

    String insertGame =
      "INSERT INTO game (home_team, visitor_team, game_date, home_score, " +
      "visitor_score, home_locker_room, visitor_locker_room, rink_location, season_session, oid) " +
      "VALUES (?, ?, TO_DATE(?, 'MM-DD-YYYY HH24:MI'), ?, ?, ?, ?, ?, ?, ?)";

   String updateGame =
     "UPDATE game SET home_team = ?, visitor_team = ?, game_date = TO_DATE(?, 'MM-DD-YYYY HH24:MI'), " +
     "home_score = ?, visitor_score = ?, home_locker_room = ?, visitor_locker_room = ?, " +
     "rink_location = ?, season_session = ? WHERE oid = ?";

   Object[] params = new Object[10];
   int x=0;
   params[x++] = homeTeamOID;
   params[x++] = visitorTeamOID;
   params[x++] = getFormattedDate("MM-dd-yyyy HH:mm");
   params[x++] = new BigDecimal(getHomeScore());
   params[x++] = new BigDecimal(getVisitorScore());
   params[x++] = getHomeLockerRoom();
   params[x++] = getVisitorLockerRoom();
   params[x++] = getRinkLocation();
   params[x++] = getSeasonSessionOID();
   params[x++] = getOID();

   String query;
   if (inserted)
     query = updateGame;
   else
     query = insertGame;

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
    StringBuffer theString = new StringBuffer("Game Date: " + gameDate.toString());

    if (homeScore == -1) // Game has not been played yet
    {
      theString.append("\nGame has not been played yet.");
    }
    else
    {
      theString.append("\nScore   Home: " + homeScore + "  Visitor: " + visitorScore);
    }

    return theString.toString();
  } // End, toString()

  //  public static void main (String[] args)
  //  {
  //    Game game = new Game(new BigDecimal((double)135));
  //    System.out.println("Game info: " + game);
  //  } // End, main()

} // End, class Game
