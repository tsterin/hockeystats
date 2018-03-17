// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/DataServlet.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.math.*;
import java.util.*;
import hockeystats.util.*;
import hockeystats.util.db.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Servlet to handle database changes/inserts
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: DataServlet.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.15  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.14  2002/08/23 01:51:35  tom
 * Added message-of-the-day, current season changes, assigning players to teams.
 *
 * Revision 1.13  2002/05/30 19:28:14  tom
 * Changes to site to allow viewing and changing of goal data.
 *
 * Revision 1.12  2002/04/14 14:05:51  tom
 * Fixes for updating players.
 *
 * Revision 1.11  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.10  2002/04/08 04:18:12  tom
 * Changes to populate/use season_session column in game table.
 *
 * Revision 1.9  2002/04/08 03:48:18  tom
 * Fixed more calls to Division constructor - include the season.
 *
 * Revision 1.8  2002/04/03 15:05:00  tom
 * Fix call to constructor to Division - now takes a Season param.
 *
 * Revision 1.7  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.6  2001/12/23 02:35:46  tom
 * Changes to fix updating game information.
 *
 * Revision 1.5  2001/12/21 03:33:04  tom
 * Make address, state, phone not required fields.
 *
 * Revision 1.4  2001/12/20 14:19:59  tom
 * Latest changes.
 *
 * Revision 1.3  2001/12/10 07:09:45  tom
 * Add cvs Log section.
 *
 */

public class DataServlet extends HttpServlet {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/DataServlet.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    String sourcePage = req.getParameter("sourcePage");

    if (sourcePage.equalsIgnoreCase("game")) handleGame(req, res);
    else if (sourcePage.equalsIgnoreCase("player")) handlePlayer(req, res);
    else if (sourcePage.equalsIgnoreCase("playerSelect")) handlePlayerSelect(req, res);
    else if (sourcePage.equalsIgnoreCase("division")) handleDivision(req, res);
    else if (sourcePage.equalsIgnoreCase("team")) handleTeam(req, res);
    else if (sourcePage.equalsIgnoreCase("gameStats")) handleUpdateGameStats(req,res);
    else if (sourcePage.equalsIgnoreCase("motd")) handleMotd(req, res);
    else if (sourcePage.equalsIgnoreCase("season")) handleSeason(req, res);
    else if (sourcePage.equalsIgnoreCase("seasonSelection")) handleSetCurrentSeason(req, res);
    else log.error ("No match for sourcePage, value: " + sourcePage);
  } // End, doPost()


  private void handleUpdateGameStats(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    String forwardURL;
    ArrayList goals = null;
    ArrayList penalties = null;
    ArrayList homePlayers = null;
    ArrayList visitorPlayers = null;
    GameGoal aGameGoal = null;
    Penalty aPenalty = null;
    String paramName;
    int playerIndex;
    int period;
    String goalMin;
    String goalSec;
    Team homeTeam;
    Team visitorTeam;

    SeasonSession selectedSeasonSession =
      (SeasonSession)req.getSession().getAttribute("hockeystats.selectedSeasonSession");


    // Get the values from the form

    int gameIndex = Integer.parseInt(req.getParameter("gameIndex"));
    Game myGame = (Game)(((ArrayList)req.getSession().getAttribute("hockeystats.gameArray")).get(gameIndex));

    homeTeam = myGame.getHomeTeam();
    visitorTeam = myGame.getVisitorTeam();

    // Create a DBEngine for the transaction
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    DBEngine db = new DBEngine(dbContext.getContextString());

    try {
      db.beginTransaction();
    }
    catch (java.sql.SQLException e) {
      log.error ("*** completeGameEntry(), error on beginTransaction" + e.toString());
 
     RequestDispatcher rd =
        getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
        log.error ("***** dispatcher is null *****");
      else
        rd.forward(req, res);
     }


    // Update the home goals

    goals = GameGoal.getGoalsInGame(myGame, homeTeam, dbContext);

    if (goals.size() == 0 && myGame.getHomeScore() > 0) // Goals haven't been entered for the game.
      for (int i=0; i<myGame.getHomeScore(); i++)
	goals.add(new GameGoal(myGame, homeTeam, i, dbContext));

    homePlayers = myGame.getHomeTeamPlayers();
    for (int i=0; i<myGame.getHomeScore(); i++)
    {
      aGameGoal = (GameGoal)goals.get(i);

      paramName = "homeScoredBy" + i;
      playerIndex = (Integer.parseInt(req.getParameter(paramName)));
      aGameGoal.setScoredBy((Player)homePlayers.get(playerIndex));

      paramName = "home1Assist" + i;
      playerIndex = (Integer.parseInt(req.getParameter(paramName)));
      if (playerIndex > -1)
	aGameGoal.setAssist1((Player)homePlayers.get(playerIndex));

      paramName = "home2Assist" + i;
      playerIndex = (Integer.parseInt(req.getParameter(paramName)));
      if (playerIndex > -1)
	aGameGoal.setAssist2((Player)homePlayers.get(playerIndex));

      paramName = "home_goalperiod" + i;
      period = (Integer.parseInt(req.getParameter(paramName)));
      aGameGoal.setPeriod(period);

      paramName = "reqnum_homegoal_minutes" + i;
      goalMin = Util.padString(req.getParameter(paramName), 2, '0');
      paramName = "reqnum_homegoal_seconds" + i;
      goalSec = Util.padString(req.getParameter(paramName), 2, '0');
      aGameGoal.setGoalTime(goalMin + ":" + goalSec);

      try {
	aGameGoal.store(db);
      }
      catch (DbStore.DbStoreException e) {
	log.error ("*** completeGameEntry(), error storing home goal: " + e.toString());
 
	try {
	  db.rollback();
	}
	catch (java.sql.SQLException e2) {}

	RequestDispatcher rd =
	  getServletContext().getRequestDispatcher("/ErrorPage.jsp");
	if (rd == null)
	  log.error ("***** dispatcher is null *****");
	else
	  rd.forward(req, res);
      }

    } // End, for all home goals



    // Update the visitor goals

    goals = GameGoal.getGoalsInGame(myGame, visitorTeam, dbContext);

    if (goals.size() == 0 && myGame.getVisitorScore() > 0) // Goals haven't been entered for the game.
      for (int i=0; i<myGame.getVisitorScore(); i++)
	goals.add(new GameGoal(myGame, visitorTeam, i, dbContext));

    visitorPlayers = myGame.getVisitorTeamPlayers();
    for (int i=0; i<myGame.getVisitorScore(); i++)
    {
      aGameGoal = (GameGoal)goals.get(i);

      paramName = "visitorScoredBy" + i;
      playerIndex = (Integer.parseInt(req.getParameter(paramName)));
      aGameGoal.setScoredBy((Player)visitorPlayers.get(playerIndex));

      paramName = "visitor1Assist" + i;
      playerIndex = (Integer.parseInt(req.getParameter(paramName)));
      if (playerIndex > -1)
	aGameGoal.setAssist1((Player)visitorPlayers.get(playerIndex));

      paramName = "visitor2Assist" + i;
      playerIndex = (Integer.parseInt(req.getParameter(paramName)));
      if (playerIndex > -1)
	aGameGoal.setAssist2((Player)visitorPlayers.get(playerIndex));

      paramName = "visitor_goalperiod" + i;
      period = (Integer.parseInt(req.getParameter(paramName)));
      aGameGoal.setPeriod(period);

      paramName = "reqnum_visitorgoal_minutes" + i;
      goalMin = Util.padString(req.getParameter(paramName), 2, '0');
      paramName = "reqnum_visitorgoal_seconds" + i;
      goalSec = Util.padString(req.getParameter(paramName), 2, '0');
      aGameGoal.setGoalTime(goalMin + ":" + goalSec);

      try {
	aGameGoal.store(db);
      }
      catch (DbStore.DbStoreException e) {
	log.error ("*** completeGameEntry(), error storing visitor goal: " + e.toString());
 
	try {
	  db.rollback();
	}
	catch (java.sql.SQLException e2) {}

	RequestDispatcher rd =
	  getServletContext().getRequestDispatcher("/ErrorPage.jsp");
	if (rd == null)
	  log.error ("***** dispatcher is null *****");
	else
	  rd.forward(req, res);
      }

    } // End, for all visitor goals


    // Update all penalties

    penalties = Penalty.getAllPenaltiesInGame(myGame, dbContext);
    for (int i=0; i<penalties.size(); i++)
    {
      aPenalty = (Penalty)penalties.get(i);

      paramName = "penalty" + i;
      String playerStringIndex = req.getParameter(paramName);
      playerIndex = Integer.parseInt(playerStringIndex.substring(1));
      if (playerStringIndex.charAt(0) == 'H')
      {
	aPenalty.setPlayer((Player)homePlayers.get(playerIndex));
	aPenalty.setTeam(homeTeam);
      }
      else
      {
	aPenalty.setPlayer((Player)visitorPlayers.get(playerIndex));
	aPenalty.setTeam(visitorTeam);
      }

      paramName = "penalty_period" + i;
      aPenalty.setPeriod(Integer.parseInt(req.getParameter(paramName)));

      paramName = "reqnum_penalty_minutes" + i;
      StringBuffer time = new StringBuffer(Util.padString(req.getParameter(paramName), 2, '0'));

      paramName = "reqnum_penalty_seconds" + i;
      time.append(":");
      time.append(Util.padString(req.getParameter(paramName), 2, '0'));
      aPenalty.setTime(time.toString());

      try {
	aPenalty.store(db);
      }
      catch (DbStore.DbStoreException e) {
	log.error ("*** completeGameEntry(), error storing a penalty: " + e.toString());
 
	try {
	  db.rollback();
	}
	catch (java.sql.SQLException e2) {}

	RequestDispatcher rd =
	  getServletContext().getRequestDispatcher("/ErrorPage.jsp");
	if (rd == null)
	  log.error ("***** dispatcher is null *****");
	else
	  rd.forward(req, res);
      }

    } // End, for all penalties

    try {
      db.commit();
    }
    catch (java.sql.SQLException e) {
      log.error ("*** completeGameEntry(), error on commit" + e.toString());
 
     RequestDispatcher rd =
        getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
        log.error ("***** dispatcher is null *****");
      else
        rd.forward(req, res);
     }


    // Return control to Schedule_upd.jsp

    RequestDispatcher rd = getServletContext().getRequestDispatcher("/Schedule_upd.jsp");
    if (rd == null)
      log.error ("***** dispatcher is null *****");
    else
      rd.forward(req, res);


  } // End, handleGameStats


  private void handleGame(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    String forwardURL;
    boolean newGame;
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    DBEngine db = new DBEngine(dbContext.getContextString());

    SeasonSession selectedSeasonSession =
      (SeasonSession)req.getSession().getAttribute("hockeystats.selectedSeasonSession");


    // Get the values from the form

    String homeTeam = req.getParameter("req_homeTeam");
    String visitorTeam = req.getParameter("req_visitorTeam");
    int gameMonth = Integer.parseInt(req.getParameter("req_gameMonth"));
    int gameDay = Integer.parseInt(req.getParameter("req_gameDay"));
    int gameYear = Integer.parseInt(req.getParameter("req_gameYear"));
    int gameHour = Integer.parseInt(req.getParameter("req_gameHour"));
    int gameMinute = Integer.parseInt(req.getParameter("req_gameMinute"));
    if (req.getParameter("gameAMPM").equals("PM")) gameHour += 12;
    String homeLocker = req.getParameter("req_homeLockerRoom");
    String visitorLocker = req.getParameter("req_visitorLockerRoom");
    String prevGameDate = null;

    GregorianCalendar gcal = new GregorianCalendar(gameYear, gameMonth, gameDay, gameHour, gameMinute);

    /*
     * See if we are adding a new game or updating an existing game.
     * If a new game, create the game object.  Otherwise, get the game from the bean.
     */

    Game theGame = null;

    String index = req.getParameter("gameIndex");

    if (index == null)
    {
      log.debug ("Adding a new game.");
      newGame = true;
      forwardURL = "/Game.jsp";
      theGame = new Game(gcal.getTime(), "Thornton", 
			 Team.getTeam(homeTeam, selectedSeasonSession.getSeason(), dbContext),
			 Team.getTeam(visitorTeam, selectedSeasonSession.getSeason(), dbContext),
			 selectedSeasonSession, dbContext);

      // Remember the game time.
      prevGameDate = (gameMonth > 9 ? Integer.toString(gameMonth) : "0" + gameMonth) +
	             (gameDay > 9 ? Integer.toString(gameDay) : "0" + gameDay) +
	             gameYear +
                     (gameHour < 12 ? (gameHour > 9 ? Integer.toString(gameHour) : "0" + gameHour) :
		       (gameHour - 12 > 9 ? Integer.toString(gameHour - 12) : "0" + (gameHour - 12))) +
	             (gameMinute > 9 ? Integer.toString(gameMinute) : "0" + gameMinute) +
	             (gameHour < 12 ? "AM" : "PM");
    }
    else
    { // Updating an existing game.
      log.debug ("Updating info for a game.");
      newGame = false;
      forwardURL = "/Schedule.jsp";
      int gameIndex = Integer.parseInt(index);
      ArrayList games = (ArrayList)req.getSession().getAttribute("hockeystats.schedule");
      theGame = (Game)games.get(gameIndex);
      theGame.setGameDate(gcal.getTime());
      theGame.setHomeTeam(Team.getTeam(homeTeam, selectedSeasonSession.getSeason(), dbContext));
      theGame.setVisitorTeam(Team.getTeam(visitorTeam, selectedSeasonSession.getSeason(), dbContext));
    }

    try {
      theGame.store(db);
    }
    catch (DbStore.DbStoreException e) {
      log.error ("Error storing game info: " + e.toString());
    }

    // Return control to the jsp

    if (prevGameDate != null) forwardURL += ("?dt=" + prevGameDate);
    RequestDispatcher rd = getServletContext().getRequestDispatcher(forwardURL);
    if (rd == null)
      log.error ("***** dispatcher is null *****");
    else
      rd.forward(req, res);

  } // End, handleGame()


  public void handleDivision(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    DBEngine db = new DBEngine(dbContext.getContextString());
    SeasonSession selectedSeasonSession =
      (SeasonSession)req.getSession().getAttribute("hockeystats.selectedSeasonSession");
    if (selectedSeasonSession == null)
      selectedSeasonSession = SeasonSession.getCurrentSeasonSession(dbContext);

    // Get the values from the form
    String name = req.getParameter("req_DivisionName");

    /*
     * Create the Division object.
     */

    Division newDivision = new Division(selectedSeasonSession.getSeason(), dbContext);
    newDivision.setDivisionName(name);
    log.debug ("The new division name is " + name + "<");

    try {
      newDivision.store(db);
    }
    catch (DbStore.DbStoreException e) {
      log.error ("Error storing division info: " + e.toString());
    }

    // Return control to Division.jsp

    RequestDispatcher rd = getServletContext().getRequestDispatcher("/Division.jsp");
    if (rd == null)
      log.error ("***** dispatcher is null *****");
    else
      rd.forward(req, res);

  } // End, handleDivision()


  public void handleTeam(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    boolean err=false;
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    DBEngine db = new DBEngine(dbContext.getContextString());
    SeasonSession selectedSeasonSession =
      (SeasonSession)req.getSession().getAttribute("hockeystats.selectedSeasonSession");
    if (selectedSeasonSession == null)
      selectedSeasonSession = SeasonSession.getCurrentSeasonSession(dbContext);


    /*
     * Create the Team object.
     */

    Team newTeam = new Team(dbContext);

    try {
      newTeam.setSeason(selectedSeasonSession.getSeason());
      newTeam.setTeamName(req.getParameter("req_TeamName"));
      newTeam.setTeamColor(req.getParameter("req_color"));
      Division myDivision = new Division(req.getParameter("req_division"),
					 selectedSeasonSession.getSeason(),
					 dbContext);
      newTeam.setDivision(myDivision);
      newTeam.setDisplayedInStandings(req.getParameter("displayInStandings").equals("Y"));

      try {
	db.beginTransaction();
      }
      catch (java.sql.SQLException e) {
	log.error ("*** handleTeam(), error on beginTransaction" + e.toString());
      }

      try {
	newTeam.store(db);
	db.commit();
      }
      catch (java.sql.SQLException e3) {
	log.error("Error on commit: " + e3.toString());
	err=true;
      }
      catch (DbStore.DbStoreException e) {
	log.error ("Error storing team info: " + e.toString());
	err=true;
	try {
	  db.rollback();
	}
	catch (java.sql.SQLException e2) {
	  log.error("Error on rollback: " + e2.toString());
	}
      }
    }
    catch (hockeystats.Division.NoSuchDivisionException e) {
      log.error ("No division found: " + e.toString());
      err=true;
    }


    // Return control to jsp/NewTeam.jsp
    RequestDispatcher dispatcher;
    if (err)
      dispatcher = getServletContext().getRequestDispatcher("/ErrorPage.jsp");
    else
      dispatcher = getServletContext().getRequestDispatcher("/Team.jsp");
    if (dispatcher == null)
      log.error ("***** dispatcher is null *****");
    else
      dispatcher.forward(req, res);

  } // End, handleTeam()


  private void handlePlayer(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    DBEngine db = new DBEngine(dbContext.getContextString());

    SeasonSession selectedSeasonSession =
      (SeasonSession)req.getSession().getAttribute("hockeystats.selectedSeasonSession");

    String forwardURL;
    boolean newPlayer;
    boolean err=false;

    // Get the values from the form
    String fName = req.getParameter("req_firstName");
    String lName = req.getParameter("req_lastName");
    String address = req.getParameter("req_address");
    String city = req.getParameter("req_city");
    String state = req.getParameter("req_state");
    String zip = req.getParameter("req_zip");
    String NPA = req.getParameter("req_areaCode");
    String NXX = req.getParameter("req_prefix");
    String extn = req.getParameter("req_phoneExtn");
    String emailAddr = " "; //req.getParameter("emailAddr");
    String jerseyNumber = req.getParameter("jerseyNumber");
    String skillLevel = " "; //req.getParameter("skillLevel");
    String team = req.getParameter("team");
    String prefPosition = req.getParameter("prefPosition");
    String canPlayGoal = req.getParameter("canPlayGoal");
    String displayOnLine = req.getParameter("displayOnLine");

    if (canPlayGoal == null) canPlayGoal = "N";
    if (displayOnLine == null) displayOnLine = "N";


    /*
     * See if we are adding a new player or updating an existing player.
     * If a new player, create the player object.  Otherwise, get the player from the bean.
     */

    Player player = null;
    String playerIndex = req.getParameter("playerIndex");

    if (playerIndex == null)
    {
      log.debug ("Adding a new player.");
      newPlayer = true;
      forwardURL = "/Player.jsp";
      player = new Player(fName, lName, address == null ? "" : address, city == null ? "" : city,
			  state == null ? "" : state, zip == null ? 0 : Integer.parseInt(zip),
			  (NPA == null ? "   " : NPA) + (NXX == null ? "   " : NXX) +
			  (extn == null ? "    " : extn),
			  displayOnLine.equals("Y"), dbContext);
    }
    else
    {
      log.debug ("Updating info for a player.");
      newPlayer = false;
      ArrayList players = null;
      String teamName = req.getParameter("teamName");
      forwardURL = "/TeamRoster.jsp?selectedTeam=" + teamName;

      // We are modifying an existing player.  Get the roster the user was looking at.
      if (teamName.equals("ALL"))
	players = Player.getAllPlayers(selectedSeasonSession, dbContext);
      else
      {
	Team theTeam = Team.getTeam(teamName, selectedSeasonSession.getSeason(), dbContext);

	TeamRoster roster = theTeam.getTeamRoster();
	players = roster.getPlayers();
      }

      player = (Player)players.get(Integer.parseInt(playerIndex));

      player.setFName(fName);
      player.setLName(lName);

      //log.debug ("Updating player with oid = " + player.getOID());
    }

    if (jerseyNumber.equals(""))
      player.setJerseyNumber(-1);
    else
      player.setJerseyNumber(Integer.parseInt(jerseyNumber));

    player.setEmailAddr(emailAddr);

    if (team.equals("NOT PLACED"))
      player.setTeam(null);
    else
    {
      log.debug ("Get Team object for team " + team);
      player.setTeam(Team.getTeam(team, selectedSeasonSession.getSeason(), dbContext));
    }  

    player.setPrefPosition(prefPosition);
    player.setSkillLevel(skillLevel);
    player.setCanPlayGoal(canPlayGoal != null);

    try {
      db.beginTransaction();
    }
    catch (java.sql.SQLException e) {
      log.error ("*** handlePlayer(), error on beginTransaction" + e.toString());
      err=true;
    }

    if (!err)
    {
      try {
	player.store(db);
	db.commit();
      }
      catch (DbStore.DbStoreException e) {
	log.error ("Error storing player info: " + e.toString());
	err=true;
	try {
	  db.rollback();
	}
	catch (java.sql.SQLException e2) {
	  log.error("Error on rollback: " + e2.toString());
	}
      }
      catch (java.sql.SQLException e3) {
	log.error("Error on commit: " + e3.toString());
	err=true;
      }
    } // End, if !err

    // Return control to jsp/NewPlayer.jsp
    RequestDispatcher dispatcher;
    if (err)
      dispatcher = getServletContext().getRequestDispatcher("/ErrorPage.jsp");
    else
      dispatcher = getServletContext().getRequestDispatcher(forwardURL);

    if (dispatcher == null)
      log.error ("***** dispatcher is null *****");
    else
      dispatcher.forward(req, res);

  } // End, handlePlayer()

  public void handleMotd(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    if (req.getParameter("SUBMIT").equals(" Save "))
    {
      Motd motd = (Motd)getServletContext().getAttribute("motd");
      motd.setMessageText(req.getParameter("messageText"));
    }

    // Return control to the main Admin menu
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/admin.jsp");
    if (dispatcher == null)
      log.error ("***** dispatcher is null *****");
    else
      dispatcher.forward(req, res);

  } // End, handleMotd()


  public void handlePlayerSelect(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    SeasonSession selectedSeasonSession =
      (SeasonSession)req.getSession().getAttribute("hockeystats.selectedSeasonSession");
    ArrayList players = null;
    Iterator iter;
    Team theTeam = null;
    Player aPlayer = null;
    int i;

    if (req.getParameter("SUBMIT").equals(" Save "))
    {
      DBEngine db = new DBEngine(dbContext.getContextString());
      theTeam = Team.getTeam (req.getParameter("teamName"), selectedSeasonSession.getSeason(),
			      dbContext);
      if (theTeam == null)
      {
	RequestDispatcher rd =
	  getServletContext().getRequestDispatcher("/ErrorPage.jsp");
	if (rd == null)
	  log.error ("***** dispatcher is null *****");
	else
	  rd.forward(req, res);
      }

      try {
	db.beginTransaction();
      }
      catch (java.sql.SQLException e) {
	log.error ("*** handlePlayerSelect(), error on beginTransaction" + e.toString());
 
	RequestDispatcher rd =
	  getServletContext().getRequestDispatcher("/ErrorPage.jsp");
	if (rd == null)
	  log.error ("***** dispatcher is null *****");
	else
	  rd.forward(req, res);
      }

      players = Player.getAllPlayers(dbContext);
      for (iter = players.iterator(), i=0; iter.hasNext(); i++)
      {
	aPlayer = (Player) iter.next();
	//log.debug ("Check player " + i + " Name: " + aPlayer.getFName() + " " + aPlayer.getLName());
	if (req.getParameter("Player" + i) != null)
	{
	  aPlayer.setTeam(theTeam);
	  try {
	    aPlayer.store(db);
	  }
	  catch (DbStore.DbStoreException e) {
	    log.error ("*** handlePlayerSelect(), error on store" + e.toString());
 
	    RequestDispatcher rd =
	      getServletContext().getRequestDispatcher("/ErrorPage.jsp");
	    if (rd == null)
	      log.error ("***** dispatcher is null *****");
	    else
	      rd.forward(req, res);
	  }
	} // End, if player selected for team
      } // End, for all players

      try {
	db.commit();
      }
      catch (java.sql.SQLException e) {
	log.error ("*** handlePlayerSelect, error on commit" + e.toString());
 
	RequestDispatcher rd =
	  getServletContext().getRequestDispatcher("/ErrorPage.jsp");
	if (rd == null)
	  log.error ("***** dispatcher is null *****");
	else
	  rd.forward(req, res);
      }

    } // End, if Save button pressed.

    // Return control to the main Admin menu
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/admin.jsp");
    if (dispatcher == null)
      log.error ("***** dispatcher is null *****");
    else
      dispatcher.forward(req, res);

  } // End, handlePlayerSelect()


  public void handleSeason(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    DBEngine db = new DBEngine(dbContext.getContextString());


    /*
     * Create a Season object.  This also creates the seasonSessions.
     */

    Season newSeason = Season.newSeason(req.getParameter("req_SeasonName"), dbContext);


    // Update the seasonSessionsBean, which caches the seasonSessions.

    SeasonSessionsBean seasonSessionsBean = new SeasonSessionsBean(dbContext);
    req.getSession().setAttribute("hockeystats.seasonSessionsBean", seasonSessionsBean);


    // Return control to admin.jsp

    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/admin.jsp");
    if (dispatcher == null)
      log.error ("***** dispatcher is null *****");
    else
      dispatcher.forward(req, res);

  } // End, handleSeason()


  public void handleSetCurrentSeason(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    DBEngine db = new DBEngine(dbContext.getContextString());

    // Get the current season-session
    SeasonSession curSeasonSession = SeasonSession.getCurrentSeasonSession(dbContext);


    // Get the index of the selected season in the SeasonSessionsBean from the request
    int seasonIndex = Integer.parseInt(req.getParameter("selectedSeasonSession"));
    log.debug ("season_Index: " + seasonIndex);


    HttpSession userSession = req.getSession(false);
    SeasonSessionsBean seasonSessionsBean =
      (SeasonSessionsBean) userSession.getAttribute("hockeystats.seasonSessionsBean");

    SeasonSession[] seasonSessions = seasonSessionsBean.getSeasonSessions();
    SeasonSession newCurSeasonSession = seasonSessions[seasonIndex];

    curSeasonSession.setCurrent(false);
    newCurSeasonSession.setCurrent(true);
    
    try {
      db.beginTransaction();
    }
    catch (java.sql.SQLException e) {
      log.error ("*** handleSetCurrentSeason(), error on beginTransaction" + e.toString());
 
     RequestDispatcher rd =
        getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
        log.error ("***** dispatcher is null *****");
      else
        rd.forward(req, res);
     }

    try {
      curSeasonSession.store(db);
      newCurSeasonSession.store(db);
    }
    catch (DbStore.DbStoreException e) {
      log.error ("*** handleSetCurrentSeason(), error storing: " + e.toString());
 
      try {
	db.rollback();
      }
      catch (java.sql.SQLException e2) {}

      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }

    try {
      db.commit();
    }
    catch (java.sql.SQLException e) {
      log.error ("*** handleSetCurrentSeason(), error on commit" + e.toString());
 
     RequestDispatcher rd =
        getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
        log.error ("***** dispatcher is null *****");
      else
        rd.forward(req, res);
     }


    // Return control to admin.jsp

    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/admin.jsp");
    if (dispatcher == null)
      log.error ("***** dispatcher is null *****");
    else
      dispatcher.forward(req, res);

  } // End, handleSetCurrentSeason()


  public void init() throws ServletException
  {
    log.info ("Initializing servlet DataServlet");
    //System.out.println ("CLASSPATH: " + System.getProperty("java.class.path","."));
    //System.out.println ("Current working dir: " + System.getProperty("user.dir","."));
  }

}// end of servlet class
