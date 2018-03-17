// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/SelectionServlet.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import hockeystats.util.*;
import java.math.*;
import java.util.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Selection servlet
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: SelectionServlet.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.8  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.7  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.6  2002/05/30 19:28:14  tom
 * Changes to site to allow viewing and changing of goal data.
 *
 * Revision 1.5  2002/04/10 22:31:15  tom
 * Take out import of DBParms (no longer used).
 *
 * Revision 1.4  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.3  2002/04/08 03:51:55  tom
 * Don't print out database password to log file.
 *
 * Revision 1.2  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.1  2001/12/20 14:19:59  tom
 * Latest changes.
 *
 */

public class SelectionServlet extends HttpServlet {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/SelectionServlet.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    doPost(req, res);
  }  // End, doGet()

  public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    String selectItem = req.getParameter("selectItem");
    log.debug("selectItem: " + selectItem);
    if (selectItem.equals("Team")) handleTeam(req, res);
    else if (selectItem.equals("Season")) handleSeason(req, res);
    else if (selectItem.equals("League")) handleLeague(req, res);
    else if (selectItem.equals("CurrentSeasonSession")) handleChangeSeasonSession(req, res);
    else
      log.error ("Bad selection: " + selectItem);
  } // End, doPost


  private void handleTeam(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");

    String teamName = req.getParameter("selectedTeam");
    String selection = req.getParameter("selection");  // What do we want the team for?
    SeasonSession selectedSeasonSession =
      (SeasonSession)req.getSession().getAttribute("hockeystats.selectedSeasonSession");


    // Store the team name in the session so the jsp will remember it
    // if we return to that page.
    req.getSession().setAttribute("hockeystats.selectedTeamName", teamName);


    if (selection.equals("roster"))
    {
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/TeamRoster.jsp?selectedTeam=" + teamName);
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }
    if (selection.equals("pickPlayers"))
    {
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/PlayerAssign.jsp?selectedTeam=" + teamName);
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }
    else if (selection.equals("schedule"))
    {
      ArrayList schedule = null;
      try {
        schedule = Game.getSchedule(teamName, selectedSeasonSession, dbContext);
      }
      catch (Team.NoSuchTeamException e) {
	log.error("No team found for team name " + teamName + ": " + e);
      }
      req.getSession().setAttribute("hockeystats.schedule", schedule);
      RequestDispatcher rd = getServletContext().getRequestDispatcher("/Schedule.jsp?sel=schedule");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }
    else if (selection.equals("gameInfo"))
    {
      // If there is an uncompleted game in the session already, drop that game.  We
      // only work on one at a time.

      GameEntry thisGame;
      if ((thisGame = (GameEntry)req.getSession().getAttribute("hockeystats.newGame")) != null)
      {
	log.debug ("Removing previous newGame attribute");
	req.getSession().removeAttribute("hockeystats.newGame");
      }

      if (req.getSession().getAttribute("hockeystats.homeTeam") != null)
      {
	log.debug ("Removing previous homeTeam attribute");
	req.getSession().removeAttribute("hockeystats.homeTeam");
      }

      if (req.getSession().getAttribute("hockeystats.visitorTeam") != null)
      {
	log.debug ("Removing previous visitorTeam attribute");
	req.getSession().removeAttribute("hockeystats.visitorTeam");
      }

      log.debug ("Get a schedule.");
      ArrayList schedule = null;
      try {
	schedule = Game.getSchedule(teamName, selectedSeasonSession, dbContext);
      }
      catch (Team.NoSuchTeamException e) {
	log.error ("No team found for team Name " + teamName + ": " + e);
      }

      log.debug ("Store schedule in session.");
      req.getSession().setAttribute("hockeystats.schedule", schedule);

      log.debug ("Send to Schedule.jsp");
      RequestDispatcher rd = getServletContext().getRequestDispatcher("/Schedule.jsp?sel=gameInfo");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
      {
	log.debug ("Here we go.");
	rd.forward(req, res);
      }
    }
    else if (selection.equals("modifyTeam"))
    {
      RequestDispatcher rd =
	getServletContext().getRequestDispatcher("/Team.jsp?teamName=" + teamName);
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }
    else
    {
      RequestDispatcher rd = getServletContext().getRequestDispatcher("/ErrorPage.jsp");
      if (rd == null)
	log.error ("***** dispatcher is null *****");
      else
	rd.forward(req, res);
    }

  } // End, handleTeam()


  private void handleSeason(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    // Get the index of the selected season in the SeasonSessionsBean from the request
    int seasonIndex = Integer.parseInt(req.getParameter("selectedSeasonSession"));
    log.debug ("season_Index: " + seasonIndex);


    HttpSession userSession = req.getSession(false);

    // Store the selected seasonSession in the session.

    SeasonSessionsBean seasonSessionsBean =
      (SeasonSessionsBean) userSession.getAttribute("hockeystats.seasonSessionsBean");

    SeasonSession[] seasonSessions = seasonSessionsBean.getSeasonSessions();
    req.getSession().setAttribute("hockeystats.selectedSeasonSession", seasonSessions[seasonIndex]);
    log.debug ("Stored season name " + seasonSessions[seasonIndex].getSeason().getSeasonName() +
	       " in the session.");

    RequestDispatcher rd = getServletContext().getRequestDispatcher("/mainpage.jsp");
    if (rd == null)
      log.error ("***** dispatcher is null *****");
    else
      rd.forward(req, res);

  } // End, handleSeason()


  private void handleLeague(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    // Set up to access the database for the selected league.
    String league = req.getParameter("SelectedLeague");

    req.getSession().setAttribute("hockeystats.dbContext",
				  new DbContext("properties.hockeystats.util_db_DBCacheProps_" + league.trim()));
    req.getSession().setAttribute("hockeystats.selectedLeague", league);

    log.debug("Selected league: " + req.getSession().getAttribute("hockeystats.selectedLeague") +
	      "  DB props file: " + 
	      ((DbContext)(req.getSession().getAttribute("hockeystats.dbContext"))).getContextString());


    // Set the available SeasonSessions.  This provides the list of SeasonSessions that 
    // can be navigated to on each page to select the season/session.

    DbContext dbContext = (DbContext)req.getSession().getAttribute("hockeystats.dbContext");
    SeasonSessionsBean seasonSessionsBean = new SeasonSessionsBean(dbContext);
    req.getSession().setAttribute("hockeystats.seasonSessionsBean", seasonSessionsBean);


    // Set the context (back) to the current seasonSession.

    SeasonSession selectedSeasonSession = SeasonSession.getCurrentSeasonSession(dbContext);
    req.getSession().setAttribute("hockeystats.selectedSeasonSession", selectedSeasonSession);


    // Send the user to the main page.

    RequestDispatcher rd = getServletContext().getRequestDispatcher("/mainpage.jsp");
    if (rd == null)
      log.error ("***** dispatcher is null *****");
    else
      rd.forward(req, res);

  } // End, handleLeague()


  private void handleChangeSeasonSession(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException
  {
    // If not going from regular season to post season, set player YTD stats to zero
    // If season change, set all player teams to null
    // Set current flag in SeasonSession

    RequestDispatcher rd = getServletContext().getRequestDispatcher("/mainpage.jsp");
    if (rd == null)
      log.error ("***** dispatcher is null *****");
    else
      rd.forward(req, res);

  } // End, handleChangeSeasonSession()


  public void init(ServletConfig config) throws ServletException
  {
    log.info ("Initializing servlet SelectionServlet");
    super.init(config);
  }

}// end of servlet class
