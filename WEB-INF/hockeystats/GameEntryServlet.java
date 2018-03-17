// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/GameEntryServlet.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import hockeystats.util.*;
import hockeystats.util.db.*;
import java.math.*;
import java.util.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * GameEntry servlet
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: GameEntryServlet.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.6  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.5  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.4  2002/05/30 19:28:14  tom
 * Changes to site to allow viewing and changing of goal data.
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
 *
 */
public class GameEntryServlet extends HttpServlet {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/GameEntryServlet.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  private Team homeTeam = null;
  private Team visitorTeam = null;

  public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {

    String phase = req.getParameter("phase");

//      Enumeration myParams = null;
//      myParams = req.getParameterNames();
//      while (myParams.hasMoreElements())
//      {
//        String param = (String)myParams.nextElement();
//        String[] values = req.getParameterValues(param);
//        log.debug ("Parameter: " + param + ", Value: " + values[0]);
//      }

    if (phase.equals("playerSelect"))
      handlePlayerSelect(req, res);
    else if (phase.equals("subsSelect"))
      handleSubsSelect(req, res);
    else if (phase.equals("pickGoalies"))
      handlePickGoalies(req, res);
    else if (phase.equals("enterGoalCounts"))
      handleGoalCounts(req, res);
    else if (phase.equals("enterGoals"))
      handleGoals(req, res);
    else if (phase.equals("enterPenalties"))
      handlePenalties(req, res);
    else
      completeGameEntry(req, res);

  } // End, doPost()


  // User has selected players for the game.

  private void handlePlayerSelect(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    HttpSession userSession = req.getSession();
    GameEntry thisGame;
    String teamPlayedFor;
    SeasonSession selectedSeasonSession =
      (SeasonSession)userSession.getAttribute("hockeystats.selectedSeasonSession");

    // If there is an uncompleted game in the session already then use that game.  There
    // will already be a game if the user used the BACK button to go back to the player
    // select screen.  If there is not already a game, create a new one.

    if ((thisGame = (GameEntry)userSession.getAttribute("hockeystats.newGame")) == null)
    {
      thisGame = new GameEntry();
      homeTeam = (Team)userSession.getAttribute("hockeystats.homeTeam");
      visitorTeam = (Team)userSession.getAttribute("hockeystats.visitorTeam");

      thisGame.setHomeTeam(homeTeam);
      thisGame.setVisitorTeam(visitorTeam);

      // Get the game object for the game being entered.
      ArrayList schedule = (ArrayList)userSession.getAttribute("hockeystats.schedule");
      thisGame.setGame((Game)schedule.get(Integer.parseInt(req.getParameter("gameIndex"))));
    }

    thisGame.setGameSeasonSession(selectedSeasonSession);

    thisGame.clearHomeTeamPlayers();
    thisGame.clearVisitorTeamPlayers();


    // First we'll do the players on the home team roster

    ArrayList homePlayers = Player.getPlayersByTeam(homeTeam, dbContext);
    for (int i=0; (teamPlayedFor = req.getParameter("HomePlayer" + i)) != null; i++)
      if (teamPlayedFor.equals("Home"))
      {
	Player aPlayer = (Player)homePlayers.get(i);
	aPlayer.incrementGamesPlayed(selectedSeasonSession);
	thisGame.addHomeTeamPlayer(aPlayer);
      } // End, if


    // Now do the same thing for the players on the visiting team roster

    ArrayList visitorPlayers = Player.getPlayersByTeam(visitorTeam, dbContext);
    for (int i=0; (teamPlayedFor = req.getParameter("VisitorPlayer" + i)) != null; i++)
    if (teamPlayedFor.equals("Visitor"))
    {
      Player aPlayer = (Player)visitorPlayers.get(i);
      aPlayer.incrementGamesPlayed(selectedSeasonSession);
      thisGame.addVisitorTeamPlayer(aPlayer);
    } // End, if


    // Create a session variable with a list of the players.  When we go to select substitutes,
    // these players will not be shown on the substitutes list.

    ArrayList playingPlayers = thisGame.getHomeTeamPlayersOnly();
    playingPlayers.addAll(thisGame.getVisitorTeamPlayersOnly());
    userSession.setAttribute("hockeystats.playingPlayers", playingPlayers);


    // Remember this gameEntry object
    userSession.setAttribute("hockeystats.newGame", thisGame);

    log.debug("after adding players");
    log.debug("Number of home team players: " + thisGame.getHomeTeamPlayersCount());
    log.debug("Number of visitor team players: " + thisGame.getVisitorTeamPlayersCount());


    // Go to subs select screen

    String nextStep = req.getParameter("nextStep");
    if (nextStep.equals(" Select Substitutes "))
    {
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/subsSelect.jsp?home=" +
						 homeTeam.getTeamName() + "&visitor=" +
						 visitorTeam.getTeamName());
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }
    else
      log.error ("*** Nowhere to go ...  ***");

  } // end of handlePlayerSelect


  // User has selected substitute players for the game.

  private void handleSubsSelect(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    HttpSession userSession = req.getSession();
    GameEntry thisGame;
    String teamPlayedFor;
    SeasonSession selectedSeasonSession =
      (SeasonSession)userSession.getAttribute("hockeystats.selectedSeasonSession");

    // Get the GameEntry object from the session.  Major uh-oh if it doesn't exist.

    if ((thisGame = (GameEntry)userSession.getAttribute("hockeystats.newGame")) == null)
    {
      log.error("***** In handleSubsSelect, no GameEntry object in Session. *****");
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }

    log.debug ("In handleSubsSelect()");
    thisGame.clearHomeTeamSubs();
    thisGame.clearVisitorTeamSubs();

    ArrayList allPlayers = (ArrayList)userSession.getAttribute("hockeystats.allPlayers");
    userSession.removeAttribute("hockeystats.allPlayers");

    for (int i=0; (teamPlayedFor = req.getParameter("SubPlayer" + i)) != null; i++)
    {
      Player aPlayer = (Player)allPlayers.get(i);
      log.debug("Check if " + aPlayer + " was a sub.");

      if (teamPlayedFor.equals("Home"))
      {
	thisGame.addHomeTeamSub(aPlayer);
	log.debug("Sub for home team");
      }
      else if (teamPlayedFor.equals("Visitor"))
      {
	thisGame.addVisitorTeamSub(aPlayer);
	log.debug ("Sub for visitor team");
      }
      else
	log.debug ("Did not play");

      aPlayer.incrementGamesPlayed(selectedSeasonSession);
    }

    log.debug("after adding all subs");
    log.debug("Number of home team subs: " + thisGame.getHomeTeamSubsCount());
    log.debug("Number of visitor team subs: " + thisGame.getVisitorTeamSubsCount());

    RequestDispatcher rd =
      getServletContext().getRequestDispatcher("/pickGoalies.jsp");
    if (rd == null)
      log.error ("***** dispatcher is null *****");
    else
      rd.forward(req, res);

  } // End, handleSubsSelect


  // User has identified the goalies on each team.

  private void handlePickGoalies(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    HttpSession userSession = req.getSession();
    GameEntry thisGame;
    SeasonSession selectedSeasonSession =
      (SeasonSession)userSession.getAttribute("hockeystats.selectedSeasonSession");

    // Get the GameEntry object from the session.  Major uh-oh if it doesn't exist.

    if ((thisGame = (GameEntry)userSession.getAttribute("hockeystats.newGame")) == null)
    {
      log.error("***** In handlePickGoalies(), no GameEntry object in Session. *****");
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }

    // Get index of home and away goalies
    int homeGoalieIdx = Integer.parseInt(req.getParameter("homeGoalie"));
    int visitorGoalieIdx = Integer.parseInt(req.getParameter("visitorGoalie"));

    ArrayList homeTeamPlayers = thisGame.getHomeTeamPlayers();
    Player homeGoalie = (Player)homeTeamPlayers.get(homeGoalieIdx);
    thisGame.setHomeTeamGoalie(homeGoalie);

    ArrayList visitorTeamPlayers = thisGame.getVisitorTeamPlayers();
    Player visitorGoalie = (Player)visitorTeamPlayers.get(visitorGoalieIdx);
    thisGame.setVisitorTeamGoalie(visitorGoalie);

    RequestDispatcher rd =
      getServletContext().getRequestDispatcher("/enterGoalCount.jsp");
    if (rd == null)
      log.error ("***** dispatcher is null *****");
    else
      rd.forward(req, res);

  } // End, handlePickGoalies()


  // User has entered the number of goals and penalties.

  private void handleGoalCounts(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
  {
    HttpSession userSession = req.getSession();
    GameEntry thisGame;
    SeasonSession selectedSeasonSession =
      (SeasonSession)userSession.getAttribute("hockeystats.selectedSeasonSession");

    // Get the GameEntry object from the session.  Major uh-oh if it doesn't exist.

    if ((thisGame = (GameEntry)userSession.getAttribute("hockeystats.newGame")) == null)
    {
      log.error("***** In handleGoalCounts, no GameEntry object in Session. *****");
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }

    int homeScore = Integer.parseInt(req.getParameter("req_homeGoals"));
    int visitorScore = Integer.parseInt(req.getParameter("req_visitorGoals"));

    thisGame.setHomeGoalCount(homeScore);
    thisGame.setVisitorGoalCount(visitorScore);

    thisGame.setHomePenalties(Integer.parseInt(req.getParameter("req_homePenalties")));
    thisGame.setVisitorPenalties(Integer.parseInt(req.getParameter("req_visitorPenalties")));


    // Now update the game stats for goalies.

    Player homeGoalie = thisGame.getHomeTeamGoalie();
    Player visitorGoalie = thisGame.getVisitorTeamGoalie();

    homeGoalie.addGoalAgainst(visitorScore, selectedSeasonSession);
    visitorGoalie.addGoalAgainst(homeScore, selectedSeasonSession);

    if (homeScore > visitorScore)
    {
      homeGoalie.incrementGoalieGamesWon(selectedSeasonSession);
      visitorGoalie.incrementGoalieGamesLost(selectedSeasonSession);
    }
    else if (visitorScore > homeScore)
    {
      homeGoalie.incrementGoalieGamesLost(selectedSeasonSession);
      visitorGoalie.incrementGoalieGamesWon(selectedSeasonSession);
    }
    else
    {
      homeGoalie.incrementGoalieGamesTied(selectedSeasonSession);
      visitorGoalie.incrementGoalieGamesTied(selectedSeasonSession);
    }

    RequestDispatcher rd =
      getServletContext().getRequestDispatcher("/enterGoals.jsp");
    if (rd == null)
      log.error ("***** dispatcher is null *****");
    else
      rd.forward(req, res);

  } // end of handleGoalCounts


  // User has entered the information for all the goals

  private void handleGoals(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    HttpSession userSession = req.getSession();
    GameEntry thisGame;
    SeasonSession selectedSeasonSession =
      (SeasonSession)userSession.getAttribute("hockeystats.selectedSeasonSession");

    // Get the GameEntry object from the session.  Major uh-oh if it doesn't exist.

    if ((thisGame = (GameEntry)userSession.getAttribute("hockeystats.newGame")) == null)
    {
      log.error("***** In handleGoals, no GameEntry object in Session. *****");
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }


    // Update statistics for the players who have scored goals.  Do home team first.

    int homeScore = thisGame.getHomeGoalCount();
    if (homeScore > 0)
    {
      ArrayList homeTeamPlayers = thisGame.getHomeTeamPlayers();
      Player sb = null;
      Player a1 = null;
      Player a2 = null;

      for (int i=0; i<homeScore; i++)
      {
	sb = null;
	a1 = null;
	a2 = null;

	// Get index of player who scored
	int scoredBy = Integer.parseInt(req.getParameter("homeScoredBy" + i));

	// Index of player who assisted
	int assist1 = Integer.parseInt(req.getParameter("home1Assist" + i));

	// Index of player who assisted
	int assist2 = Integer.parseInt(req.getParameter("home2Assist" + i));

	// Increment number of goals for player who scored
	sb = (Player)homeTeamPlayers.get(scoredBy);
	sb.addGoal(selectedSeasonSession);

	if (assist1 > -1)  // If a player assisted, increment his number of assists
	{
	  a1 = (Player)homeTeamPlayers.get(assist1);
	  a1.addAssist(selectedSeasonSession);
	}

	if (assist2 > -1) // If another player assisted, increment his number of assists
	{
	  a2 = (Player)homeTeamPlayers.get(assist2);
	  a2.addAssist(selectedSeasonSession);
	}

	int period = Integer.parseInt(req.getParameter("home_period" + i));
	String minutes = Util.padString(req.getParameter("reqnum_home_minutes" + i), 2, '0');
	String seconds = Util.padString(req.getParameter("reqnum_home_seconds" + i), 2, '0');
	
	String goalTime = minutes + ":" + seconds;

	log.debug ("Home goal, period: " + period + ", time: " + goalTime + " by " + sb);

	thisGame.addGoal (new GameGoal(thisGame.getGame(), thisGame.getHomeTeam(), i+1,
					   sb, a1, a2, period, goalTime, dbContext));
      } // End, for all home goals
    } // End, if home team score > 0

    // Now do visitor team

    int visitorScore = thisGame.getVisitorGoalCount();
    if (visitorScore > 0)
    {
      ArrayList visitorTeamPlayers = thisGame.getVisitorTeamPlayers();
      Player sb = null;
      Player a1 = null;
      Player a2 = null;

      for (int i=0; i<visitorScore; i++)
      {
	sb = null;
	a1 = null;
	a2 = null;

	// Get index of player who scored
	int scoredBy = Integer.parseInt(req.getParameter("visitorScoredBy" + i));

	// Get index of player who assisted
	int assist1 = Integer.parseInt(req.getParameter("visitor1Assist" + i));

	// Get index of player who assisted
	int assist2 = Integer.parseInt(req.getParameter("visitor2Assist" + i));

	// Increment number of goals for player who scored
	sb = (Player)visitorTeamPlayers.get(scoredBy);
	sb.addGoal(selectedSeasonSession);

	if (assist1 > -1)  // If a player assisted, increment his number of assists
	{
	  a1 = (Player)visitorTeamPlayers.get(assist1);
	  a1.addAssist(selectedSeasonSession);
	}

	if (assist2 > -1) // If another player assisted, increment his number of assists
	{
	  a2 = (Player)visitorTeamPlayers.get(assist2);
	  a2.addAssist(selectedSeasonSession);
	}
	int period = Integer.parseInt(req.getParameter("visitor_period" + i));
	String minutes = Util.padString(req.getParameter("reqnum_visitor_minutes" + i), 2, '0');
	String seconds = Util.padString(req.getParameter("reqnum_visitor_seconds" + i), 2, '0');
	
	String goalTime = minutes + ":" + seconds;

	log.debug ("Visitor goal, period: " + period + ", time: " + goalTime + " by " + sb);

	thisGame.addGoal (new GameGoal(thisGame.getGame(), thisGame.getVisitorTeam(), i+1, 
					   sb, a1, a2, period, goalTime, dbContext));

      } // End, for all visitor goals
    } // End, if visitor team score > 0


    // In the Team object for each team, set the goals_for, goals_against, and win/loss/tie

    int seasonSessionID = selectedSeasonSession.getSessionID();
    homeTeam.incrementGoalsFor(homeScore, seasonSessionID);
    homeTeam.incrementGoalsAgainst(visitorScore, seasonSessionID);
    visitorTeam.incrementGoalsFor(visitorScore, seasonSessionID);
    visitorTeam.incrementGoalsAgainst(homeScore, seasonSessionID);

    boolean sameDivision = homeTeam.getDivision().getOID().equals(visitorTeam.getDivision().getOID());
    if (homeScore > visitorScore)
    {
      homeTeam.incrementWins(sameDivision, seasonSessionID);
      visitorTeam.incrementLosses(sameDivision, seasonSessionID);
    }
    else if (homeScore < visitorScore)
    {
      homeTeam.incrementLosses(sameDivision, seasonSessionID);
      visitorTeam.incrementWins(sameDivision, seasonSessionID);
    }
    else
    {
      homeTeam.incrementTies(sameDivision, seasonSessionID);
      visitorTeam.incrementTies(sameDivision, seasonSessionID);
    }

    // Now go to the page where the user will enter any penalties in the game.  If there
    // were no penalties, we're done.

    if (thisGame.getHomePenalties() > 0 || thisGame.getVisitorPenalties() > 0)
    {
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/enterPenalties.jsp");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }
    else
      completeGameEntry(req, res);

  } // end of handleGoals


  // User has entered the information for all the penalties in the game

  private void handlePenalties(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    HttpSession userSession = req.getSession();
    GameEntry thisGame;
    Player aPlayer;
    SeasonSession selectedSeasonSession =
      (SeasonSession)userSession.getAttribute("hockeystats.selectedSeasonSession");

    // Get the GameEntry object from the session.  Major uh-oh if it doesn't exist.

    if ((thisGame = (GameEntry)userSession.getAttribute("hockeystats.newGame")) == null)
    {
      log.error("***** In handleGoals, no GameEntry object in Session. *****");
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }


    // Update statistics for the players who received penalties.  Do home team first.

    int homePenalties = thisGame.getHomePenalties();
    if (homePenalties > 0)
    {
      ArrayList homeTeamPlayers = thisGame.getHomeTeamPlayers();
      for (int i=0; i<homePenalties; i++)
      {
	// Get index of penalized player
	int pindex = Integer.parseInt(req.getParameter("homePenalty" + Integer.toString(i)));

	// Get the number penalty minutes for this penalty
	int minutes = Integer.parseInt(req.getParameter("homeMinutes" + Integer.toString(i)));

	// Get the penaltyID of the infraction
	int penaltyID = Integer.parseInt(req.getParameter("homeInfraction" + Integer.toString(i)));

	int period = Integer.parseInt(req.getParameter("home_penalty_period" + Integer.toString(i)));
	String paramName = "reqnum_home_penalty_minutes" + i;
	StringBuffer time = new StringBuffer(Util.padString(req.getParameter(paramName), 2, '0'));
	time.append(":");
	paramName = "reqnum_home_penalty_seconds" + i;
	time.append(Util.padString(req.getParameter(paramName), 2, '0'));

	aPlayer = (Player)homeTeamPlayers.get(pindex);
	aPlayer.addPenaltyMinutes(minutes, selectedSeasonSession);
	Penalty thisPenalty = new Penalty (aPlayer, thisGame.getGame(), penaltyID, minutes,
					   thisGame.getHomeTeam(), time.toString(), period, dbContext);
	thisGame.addHomePenalty(thisPenalty);
      } // End, for all home penalties
    } // End, if home team had penalties


    // Now do the visitor team

    int visitorPenalties = thisGame.getVisitorPenalties();
    if (visitorPenalties > 0)
    {
      ArrayList visitorTeamPlayers = thisGame.getVisitorTeamPlayers();
      for (int i=0; i<visitorPenalties; i++)
      {
	// Get index of penalized player
	int pindex = Integer.parseInt(req.getParameter("visitorPenalty" + Integer.toString(i)));

	// Get the number penalty minutes for this penalty
	int minutes = Integer.parseInt(req.getParameter("visitorMinutes" + Integer.toString(i)));

	// Get the penaltyID of the infraction
	int penaltyID = Integer.parseInt(req.getParameter("visitorInfraction" + Integer.toString(i)));

	int period = Integer.parseInt(req.getParameter("visitor_penalty_period" + Integer.toString(i)));
	String paramName = "reqnum_visitor_penalty_minutes" + i;
	StringBuffer time = new StringBuffer(Util.padString(req.getParameter(paramName), 2, '0'));
	time.append(":");
	paramName = "reqnum_visitor_penalty_seconds" + i;
	time.append(Util.padString(req.getParameter(paramName), 2, '0'));

	aPlayer = (Player)visitorTeamPlayers.get(pindex);
	aPlayer.addPenaltyMinutes(minutes, selectedSeasonSession);
	Penalty thisPenalty = new Penalty (aPlayer, thisGame.getGame(), penaltyID, minutes,
					   thisGame.getVisitorTeam(), time.toString(), period, dbContext);
	thisGame.addVisitorPenalty(thisPenalty);
      } // End, for all visitor penalties
    } // End, if visitor team had penalties


    // All info for the game has been entered.

    completeGameEntry(req, res);

  } // end of handlePenalties


  /**
   * This routine handles saving the game to the database after all info for
   * game has been input.
   */

  private void completeGameEntry(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException
  {
    boolean err=false;
    HttpSession userSession = req.getSession();
    GameEntry thisGame;
    SeasonSession selectedSeasonSession =
      (SeasonSession)getServletContext().getAttribute("hockeystats.selectedSeasonSession");

    // Create a DBEngine for the transaction
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    DBEngine db = new DBEngine(dbContext.getContextString());

    // Get the GameEntry object from the session.  Major uh-oh if it doesn't exist.

    if ((thisGame = (GameEntry)userSession.getAttribute("hockeystats.newGame")) == null)
    {
      log.error("***** In handleGoals, no GameEntry object in Session. *****");
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }

    thisGame.getGame().setHomeScore(thisGame.getHomeGoalCount());
    thisGame.getGame().setVisitorScore(thisGame.getVisitorGoalCount());

    try {
      db.beginTransaction();
    }
    catch (java.sql.SQLException e) {
      log.error ("*** completeGameEntry(), error on beginTransaction" + e.toString());
    }

    try {
      thisGame.store(dbContext, db);
      db.commit();
    }
    catch (DbStore.DbStoreException e) {
      log.error ("*** completeGameEntry(), error saving game: " + e.toString());
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

    // Return to the admin menu (or error page if error)

    RequestDispatcher rd;
    if (err)
      rd = getServletContext().getRequestDispatcher("/ErrorPage.jsp");
    else
      rd = getServletContext().getRequestDispatcher("/admin.jsp");

    if (rd == null)
      log.error ("***** dispatcher is null *****");
    else
      rd.forward(req, res);
    

  } // End, completeGameEntry()

  public void init() throws ServletException
  {
    log.info ("Initializing servlet GameEntryServlet");
  }

}// end of servlet class
