// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Player.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import hockeystats.util.*;
import hockeystats.util.db.*;
import java.math.*;
import java.sql.*;
import java.util.*;

 // Import log4j classes.
 import org.apache.log4j.Category;
 import org.apache.log4j.PropertyConfigurator;

/** 
 * Class to represent a player in the league.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: Player.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.13  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.12  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.11  2002/06/04 14:58:20  tom
 * In getPlayersByStat, don't return info about the Unknown Player.
 *
 * Revision 1.10  2002/05/30 19:28:14  tom
 * Changes to site to allow viewing and changing of goal data.
 *
 * Revision 1.9  2002/04/14 14:05:51  tom
 * Fixes for updating players.
 *
 * Revision 1.8  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.7  2002/04/08 14:12:07  tom
 * Fix query in getPlayersByStat, return rosters in jersey number order
 * rather than by last name.
 *
 * Revision 1.6  2002/04/08 03:49:52  tom
 * Fix INSERT statement - wrong number of params.
 *
 * Revision 1.5  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.4  2001/12/21 03:33:04  tom
 * Make address, state, phone not required fields.
 *
 * Revision 1.3  2001/12/20 14:19:59  tom
 * Latest changes.
 *
 * Revision 1.2  2001/12/10 07:09:45  tom
 * Add cvs Log section.
 *
 */

public class Player extends DbStore implements GlobalVars {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/Player.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);

  private static Hashtable playerLists = new Hashtable(MaxDbContexts);

  private BigDecimal oid;
  private String fName;
  private String lName;
  private String address;
  private String city;
  private String state;
  private int zip;
  private String phoneNumber;
  private String emailAddr;
  private int jerseyNumber;
  private int totalGoals;
  private int totalAssists;
  private int totalPenaltyMinutes;
  private int totalPenalties;
  private int totalGamesPlayed; // Games played, goalie or not
  private int goalieGamesWon; // Total games won as a goalie
  private int goalieGamesLost;
  private int goalieGamesTied;
  private int totalGoalsAgainst;
  private boolean displayOnLine; // Does the player want personal info displayed online?
  private Team team;
  private BigDecimal teamOID;
  private String skillLevel;  // A,B, or C
  private String prefPosition;
  private boolean canPlayGoal;
  private SeasonStats seasonStats = null;
  private DbContext myDbContext = null;

  private static String selectStmt = "SELECT p.oid player_oid, f_name, l_name, " +
  "nvl(address, '') address, nvl(city, '') city, nvl(state, '') state, nvl(zip,'') zip, " +
  "nvl(phone_number, '          ') phone_number, nvl(jersey_number, -1) jersey_number, " +
  "total_goals, total_assists, total_penalty_minutes, total_games_played, total_penalties, " +
  "nvl(total_goals_against, -1) total_goals_against, display_on_line, p.team, " +
  "nvl(email_addr, ' ') email_addr, nvl(pref_position, 'ANY') pref_position, can_play_goal, " +
  "skill_level, nvl(user_id, -1) user_id, goalie_games_won, goalie_games_lost, " +
  "goalie_games_tied, total_goals_against FROM player p ";

  private boolean statsModified = false;

  private Player() {}

  /**
   * Only this class can create an empty Player object.
   */
  private Player(DbContext dbContext)
  {
    this.inserted = true;
    this.modified = false;
    this.statsModified = false;
    this.myDbContext = dbContext;
  }


  /**
   * Constructor called to create a brand new player.
   */
  public Player(String fName, String lName, String address, String city, String state,
		int zip, String phoneNumber, boolean displayOnLine, DbContext dbContext)
  {
    oid = OIDSequencer.next(dbContext);

    this.fName = fName;
    this.lName = lName;
    this.address = address;
    this.city = city;
    this.state = state;
    this.zip = zip;
    this.phoneNumber = phoneNumber;
    this.displayOnLine = displayOnLine;
    this.totalGoals = 0;
    this.totalAssists = 0;
    this.totalPenaltyMinutes = 0;
    this.totalPenalties = 0;
    this.totalGamesPlayed = 0;
    this.totalGoalsAgainst = -1;
    this.team = null;
    this.teamOID = null;

    this.inserted = false;
    this.modified = false;
    this.statsModified = false;
    this.myDbContext = dbContext;

    // If we have a list of all players cached, add this player to that list.
    if (playerLists.containsKey(dbContext))
    {
      ArrayList thisList = (ArrayList)playerLists.get(dbContext);
      thisList.add(this);
    }

  } // End, Player()


  /**
   * Constructor to create a Player and populate attributes with data from the database.
   *
   * @param oid The oid of the Player to be created.
   * @param dbContext The database context, retrieved from session attribute "hockeystats.dbContext"
   */

  public Player(BigDecimal oid, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    String query = selectStmt + "WHERE oid = ?";

    inserted = true;
    modified = false;
    statsModified = false;

    Object[] params = new Object[1];
    params[0] = oid;

    log.debug ("Get a player, SQL: " +
	       DBUtils.getSQLStatementFromPreparedStatement (query, params));

    try {
      Vector results = db.executeQuery (query, params);
      Hashtable record = (Hashtable) results.get(0);
      //log.debug ("record: " + record);
      this.setPlayerInfo(record);
      log.debug ("Player object from database, player: " + getFName() + " " + getLName());
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting player info: " + sqle);
    }

    this.myDbContext = dbContext;

  } // End, Player()


  /**
   * Get the player's first name
   */
  public String getFName()
  {
    return fName;
  }

  /**
   * Set the player's first name.
   *
   * @param fName A String with the player's first name.
   */
  public void setFName(String fName) { this.fName = fName; modified = true; }

  /**
   * Get the player's last name
   */
  public String getLName() { return lName; }

  /**
   * Set the player's last name.
   *
   * @param lName A String with the player's last name.
   */
  public void setLName(String lName) { this.lName = lName; modified = true; }

  /**
   * Get the player's address
   */
  public String getAddress() { return address; }

  /**
   * Set the player's street address
   *
   * @param address A String with the player's street address
   */
  private void setAddress(String setAddress) { this.address = setAddress; modified = true; }

  /**
   * Get the player's city
   */
  public String getCity() { return city; }

  /**
   * Set the player's city.
   *
   * @param city A String with the player's city
   */
  private void setCity(String city) { this.city = city; modified = true; }

  /**
   * Get the player's state
   */
  public String getState() { return state; }

  /**
   * Set the player's state.
   *
   * @param state A String with the player's state.
   */
  private void setState(String state) { this.state = state; modified = true; }

  /**
   * Get the player's zip code
   */
  public int getZip() { return zip; }

  /**
   * Set the player's zip code.
   *
   * @param zip An int with the player's zip code.
   */
  private void setZip(int zip) { this.zip = zip; modified = true; }

  /**
   * Get the player's phone number
   */
  public String getPhoneNumber() { return phoneNumber; }

  /**
   * Set the player's phone number.
   *
   * @param phone A String with the player's phone number.
   */
  private void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; modified = true; }

  /**
   * Get the player's email address
   */
  public String getEmailAddr() { return emailAddr; }

  /**
   * Set the player's email address.
   *
   * @param emailAddr A String with the player's email address
   */
  public void setEmailAddr(String emailAddr) { this.emailAddr = emailAddr; modified = true; }

  /**
   * Get the player's jersey number
   */
  public int getJerseyNumber() { return jerseyNumber; }

  /**
   * Set the player's jersey number.
   *
   * @param jerseyNumber A String with the player's jersey number.
   */
  public void setJerseyNumber(int jerseyNumber) { this.jerseyNumber = jerseyNumber; modified = true; }

  /**
   * Get the player's goals for a season-session.
   *
   * @param seasonSession Specifies which season's goals are to be returned
   *
   */
  public int getGoals(SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);
    return seasonStats.getGoals();
  }


  /**
   * Get the player's total goals in the league.
   */
  public int getTotalGoals() { return totalGoals; }

  /**
   * Set the player's total goals in the league.
   */
  private void setTotalGoals(int totalGoals)
  { this.totalGoals = totalGoals; modified = true; }

  /**
   * Add some goals to the player's total.
   *
   * Pre-season goals affect should affect YTD goals but not total goals.
   *
   * @param numGoals The number of goals to be added to the player's totals
   * @param seasonSession The season-session describing when the goals were scored
   *
   */
  public void addGoal (int numGoals, SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);

    seasonStats.incrementGoals(numGoals);
    if (!seasonSession.getSessionName().equals("PRE SEASON"))
    {
      totalGoals += numGoals;
      modified = true;
    }

    statsModified = true;

  } // End, addGoal()


  /**
   * Add a goal to the player's total.
   *
   * Pre-season goals affect should affect YTD goals but not total goals.
   *
   * @param seasonSession The season-session describing when the goal was scored
   */
  public void addGoal (SeasonSession seasonSession) { addGoal(1, seasonSession); } // End, addAGoal()


  /**
   * Get the player's assists for the specified season-session
   *
   * @param seasonSession Specifies which season's assists are to be returned
   *
   */
  public int getAssists(SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);
    return seasonStats.getAssists();
  } // End, getAssists()


  /**
   * Get the player's total assists in the league.
   */
  public int getTotalAssists() { return totalAssists; }


  /**
   * Set the player's total assists in the league.
   */
  private void setTotalAssists(int totalAssists)
  { this.totalAssists = totalAssists; modified = true; }


  /**
   * Add some assists to the player's total.
   *
   * Pre-season assists affect should affect YTD assists but not total assists.
   *
   * @param numAssists The number of assists to be added to the player's totals
   * @param seasonSession The season-session describing when the assists were made
   */
  public void addAssist (int numAssists, SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);

    seasonStats.incrementAssists(numAssists);
    if (!seasonSession.getSessionName().equals("PRE SEASON"))
    {
      totalAssists += numAssists;
      modified = true;
    }

    statsModified = true;

  } // End, addAssist()


  /**
   * Add one assist to the player's total.
   *
   * Pre-season assists affect should affect YTD assists but not total assists.
   *
   * @param seasonSession The season-session describing when the assists were made
   */
  public void addAssist (SeasonSession seasonSession) { addAssist(1, seasonSession); }


  /**
   * Get the player's penalty minutes for a season-session.
   *
   * @param seasonSession The season-session whose penalty minutes are wanted
   *
   */
  public int getPenaltyMinutes(SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);
    return seasonStats.getPenaltyMinutes();
  } // End, getPenaltyMinutes()


  /**
   * Get the player's total penalty minutes.
   */
  public int getTotalPenaltyMinutes() { return totalPenaltyMinutes; }


  /**
   * Set the player's total penalty minutes.
   */
  private void setTotalPenaltyMinutes(int totalPenaltyMinutes)
  { this.totalPenaltyMinutes = totalPenaltyMinutes; modified = true; }


  /**
   * Add penalty minutes to the player's total.  Pre-season penalty
   * minutes affect YTD penalty minutes but not total penalty minutes.
   * 
   * @param PenaltyMinutes The number of penalty minutes to be added to the player's total
   * @param seasonSession The season-session describing when the penalties were made
   *
   */
  public void addPenaltyMinutes (int penaltyMinutes, SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);

    seasonStats.incrementPenaltyMinutes(penaltyMinutes);
    seasonStats.incrementPenalties();
    if (!seasonSession.getSessionName().equals("PRE SEASON"))
    {
      totalPenaltyMinutes += penaltyMinutes;
      totalPenalties++;
      modified = true;
    }

    statsModified = true;

  } // End, addPenaltyMinutes()

  /**
   * Get the player's number of penalties for a season-session.
   *
   * @param seasonSession The season-session whose penalties are wanted
   *
   */
  public int getPenalties(SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);
    return seasonStats.getPenalties();
  } // End, getPenalties()


  /**
   * Get the player's total penalties.
   */
  public int getTotalPenalties() { return totalPenalties; }


  /**
   * Set the player's total penalties.
   */
  private void setTotalPenalties(int totalPenalties)
  { this.totalPenalties = totalPenalties; modified = true; }


  /**
   * Add penalties to the player's total.
   *
   * Pre-season penalties affect should affect YTD penalties but not total penalties.
   *
   * @param numPenalties The number of penalties to be added to the player's totals
   * @param seasonSession The season-session describing when the penalties were assessed
   *
   */
  private void addPenalty (int numPenalties, SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);

    seasonStats.incrementPenalties(numPenalties);
    if (!seasonSession.getSessionName().equals("PRE SEASON"))
    {
      totalPenalties += numPenalties;
      modified = true;
    }

    statsModified = true;

  } // End, addPenalty()


  /**
   * Add a penalty to the player's total.
   *
   * Pre-season penalties affect should affect YTD penalties but not total penalties.
   *
   * @param seasonSession The season-session describing when the penalty was assessed
   */
  private void addPenalty (SeasonSession seasonSession) { addPenalty(1, seasonSession); } // End, addPenalty()


  /**
   * Get the player's year-to-date number of games played, goalie or not.
   *
   * @param seasonSession The season-session whose number of games are wanted
   *
   */
  public int getGamesPlayed(SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);
    return seasonStats.getGamesPlayed();
  } // End, getGamesPlayed()


  /**
   * Get the player's total number of games played in the league, goalie or not.
   */
  public int getTotalGamesPlayed() { return totalGamesPlayed; }


  /**
   * Set the player's total number of games played in the league, goalie or not.
   */
  private void setTotalGamesPlayed(int totalGamesPlayed)
  { this.totalGamesPlayed = totalGamesPlayed; modified = true; }


  /**
   * Increment by one the player's number of games played.  Pre-season games
   * affect YTD stats but not total stats.
   *
   * @param seasonSession The season-session describing when the game was played.
   */
  public void incrementGamesPlayed (SeasonSession seasonSession)
  {
    incrementGamesPlayed(1, seasonSession);
  } // End, incrementGamesPlayed()


  /**
   * Increment by the amount specified the player's count of games played.  Pre-season
   * games affect YTD stats but not total stats.
   *
   * @param incrementAmt The number of games to increment by.
   * @param seasonSession The season-session describing when the games were played
   */
  public void incrementGamesPlayed(int incrementAmt, SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);

    seasonStats.incrementGames(incrementAmt);
    if (!seasonSession.getSessionName().equals("PRE SEASON"))
    {
      totalGamesPlayed += incrementAmt;
      modified = true;
    }

    statsModified = true;

  } // End, incrementGamesPlayed()


  /**
   * Get the player's year-to-date number of games played as a goalie.
   *
   * @param seasonSession The season-session whose number of games are wanted
   *
   */
  public int getGoalieGames(SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);
    return seasonStats.getGoalieGamesWon() + seasonStats.getGoalieGamesLost() + 
      seasonStats.getGoalieGamesTied();
  } // End, getGoalieGames()


  /**
   * Get the player's total number of games played as a goalie.
   *
   * @return int
   */
  public int getGoalieGamesPlayed() { return goalieGamesWon + goalieGamesLost + goalieGamesTied; }


  /**
   * Get the player's number of games won when playing as a goalie for a season.
   *
   * @param seasonSession The season of interest
   * @return int
   */
  public int getGoalieGamesWon(SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);
    return seasonStats.getGoalieGamesWon();
  }


  /**
   * Get the player's total number of games won when playing as a goalie.
   *
   * @return int
   */
  public int getGoalieGamesWon() { return goalieGamesWon; }


  /**
   * Get the player's number of games lost when playing as a goalie for a season.
   *
   * @param seasonSession The season of interest
   * @return int
   */
  public int getGoalieGamesLost(SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);
    return seasonStats.getGoalieGamesLost();
  }


  /**
   * Get the player's total number of games lost when playing as a goalie.
   *
   * @return int
   */
  public int getGoalieGamesLost() { return goalieGamesLost; }


  /**
   * Get the player's number of games tied when playing as a goalie for a season.
   *
   * @param seasonSession The season of interest
   * @return int
   */
  public int getGoalieGamesTied(SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);
    return seasonStats.getGoalieGamesTied();
  }


  /**
   * Get the player's total number of games tied when playing as a goalie.
   *
   * @return int
   */
  public int getGoalieGamesTied() { return goalieGamesTied; }


  private void setGoalieGamesWon(int games) { goalieGamesWon = games; }
  private void setGoalieGamesLost(int games) { goalieGamesLost = games; }
  private void setGoalieGamesTied(int games) { goalieGamesTied = games; }

  /**
   * Set the player's total number of games played in the league as a goalie.
   */
  /*
  private void setTotalGoalieGames(int totalGoalieGames)
  { this.totalGoalieGames = totalGoalieGames; modified = true; }
  */

  /**
   * Increment by one the player's number of games won as a goalie.  Pre-season games
   * affect YTD stats but not total stats.
   *
   * @param seasonSession The season-session describing when the game was played.
   */
  public void incrementGoalieGamesWon (SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);

    seasonStats.incrementGoalieGamesWon();
    if (!seasonSession.getSessionName().equals("PRE SEASON"))
    {
      goalieGamesWon++;
      modified = true;
    }

    statsModified = true;

  } // End, incrementGoalieGamesWon()


  /**
   * Increment by one the player's number of games lost as a goalie.  Pre-season games
   * affect YTD stats but not total stats.
   *
   * @param seasonSession The season-session describing when the game was played.
   */
  public void incrementGoalieGamesLost (SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);

    seasonStats.incrementGoalieGamesLost();
    if (!seasonSession.getSessionName().equals("PRE SEASON"))
    {
      goalieGamesLost++;
      modified = true;
    }

    statsModified = true;

  } // End, incrementGoalieGamesLost()


  /**
   * Increment by one the player's number of games tied as a goalie.  Pre-season games
   * affect YTD stats but not total stats.
   *
   * @param seasonSession The season-session describing when the game was played.
   */
  public void incrementGoalieGamesTied (SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);

    seasonStats.incrementGoalieGamesTied();
    if (!seasonSession.getSessionName().equals("PRE SEASON"))
    {
      goalieGamesLost++;
      modified = true;
    }

    statsModified = true;

  } // End, incrementGoalieGamesTied()


  /**
   * Get the player's year-to-date goals against.  Returns -1
   * if the player is not and has never been a goalie.
   */
  public int getGoalsAgainst(SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);
    return seasonStats.getGoalsAgainst();
  } // End, getGoalsAgainst()


  /**
   * Get the player's total goals against.  Returns -1
   * if the player has never been a goalie.
   */
  public int getTotalGoalsAgainst() { return totalGoalsAgainst; }

  /**
   * Set the player's total goals against.  -1 means
   * the player is not or has never been a goalie.
   */
  private void setTotalGoalsAgainst(int totalGoalsAgainst)
  { this.totalGoalsAgainst = totalGoalsAgainst; modified = true; }


  /**
   * Add some goals-against to the player's total.
   *
   * Pre-season goals affect should affect YTD stats but not total stats.
   *
   * @param numGoals The number of goals-against to be added to the player's totals
   * @param seasonSession The season-session describing when the goals were scored
   *
   */
  public void addGoalAgainst (int numGoals, SeasonSession seasonSession)
  {
    setSeasonForStats(seasonSession);

    seasonStats.incrementGoalsAgainst(numGoals);
    if (!seasonSession.getSessionName().equals("PRE SEASON"))
    {
      totalGoalsAgainst += numGoals;
      modified = true;
    }

    statsModified = true;

  } // End, addGoalAgainst()


  /**
   * Add a goal-against to the player's total.
   *
   * Pre-season goals affect should affect YTD stats but not total stats.
   *
   * @param seasonSession The season-session describing when the goal was scored
   */
  public void addGoalAgainst (SeasonSession seasonSession)
  {
    addGoalAgainst(1, seasonSession);
  } // End, addGoalAgainst()


  /**
   * Get the flag showing if the user wants his/her personal information
   * to be displayed on-line
   */
  public boolean displayOnLine() { return displayOnLine; }

  /**
   * Set the flag showing if the user wants his/her personal information
   * to be displayed on-line
   *
   * @param displayOnLine Boolean telling if user wants personal info displayed
   */
  private void setDisplayOnLine(boolean displayOnLine)
  {
    this.displayOnLine = displayOnLine; modified = true; 
  }

  /**
   * Get the flag showing if the player can/will play goalie.
   */
  public boolean canPlayGoal() { return canPlayGoal; }

  /**
   * Set the flag showing if the player can/will play goalie.
   *
   * @param canPlayGoal boolean, true if the player can play goalie
   */
  public void setCanPlayGoal(boolean canPlayGoal)
  {
    this.canPlayGoal = canPlayGoal; modified = true; 
  }

  /**
   * Player year-to-date stats have to be associated with a season-session.
   * Use this method to set the season that all stat modifications apply to.
   */
  private void setSeasonForStats(SeasonSession seasonSession)
  {
    if (seasonStats == null || !(seasonStats.getSeasonSession().equals(seasonSession)))
    {
      log.debug("Need to set season for player " + this.toString());
      if (statsModified)
      {
	log.info("**** Player stats modified - modified stats are being lost for player " + this);
	/*
	try {
	  seasonStats.store(new DBEngine(myDbContext.getContextString()));
	}
	catch (DbStoreException e) {
	  log.error ("**** Major error storing season stats for player: " + this + ": " + e);
	}
	*/
	statsModified = false;
      }
      seasonStats = new SeasonStats(getOID(), seasonSession.getOID(), myDbContext);
    }
  } // End, setSeasonForStats()


  /**
   * Get the team.
   *
   * @param dbContext Database context object, retrieved from session
   * attribute "hockeystats.dbContext"
   */

  public Team getTeam()
  {
    // teamOID of 0 means the player is not assigned to a team
    if (team == null && teamOID != null && teamOID.compareTo(new BigDecimal(0)) == 1)
      setTeam(Team.getTeam(teamOID, myDbContext));

    return team;

  } // End, getTeam()


  /**
   * Set the team the player is on.
   */

  public void setTeam (Team team)
  {
    this.team = team;
    if (team == null)
      this.teamOID = new BigDecimal(0);
    else
      this.teamOID = team.getOID();
    modified = true;
  } // End, setTeam();


  /**
   * Get the preferred position
   */

  public String getPrefPosition() { return prefPosition; }

  /**
   * Set the prefPosition
   */

  public void setPrefPosition (String prefPosition)
  {
    this.prefPosition = prefPosition; modified = true; 
  }

  /**
   * Get the player's skill level.
   */

  public String getSkillLevel() { return skillLevel; }

  /**
   * Set the player's skill level.
   */

  public void setSkillLevel (String skillLevel)
  {
    this.skillLevel = skillLevel; modified = true; 
  }

  /**
   * Get the system USER object associated with the player.
   */

  //  public BigDecimal getUser() { return user; }

  /**
   * Set the system USER object associated with the player.
   */

  //  private void setUser (User user) { this.user = user; modified = true; }


  /**
   * Set the OID for this player.  This should only be called when retrieving player
   * info from the database.
   */
  private void setOID(BigDecimal oid) { this.oid = oid; }

  /**
   * Get the OID for this player.
   */
  public BigDecimal getOID() { return oid; }


  public void store(DBEngine db) throws DbStoreException
  {
    // If we don't need to do anything, bail out now.
    if (inserted && !modified && !statsModified) 
    {
      log.debug("store called for player " + this + ", nothing to save.");
      return;
    }

    if (!db.isInTransaction())
      throw new
	DbStoreException("For Player object, store() method must be called within a transaction.");

    int numRows;

    String teamVal;
    Object[] params = null;
    if (teamOID.compareTo(new BigDecimal(0)) == 0)
    {
      teamVal = "NULL";
      params = new Object[18];
    }
    else
    {
      teamVal = "?";
      params = new Object[19];
    }

    if (!inserted || modified)
    {
      String storePlayer =
	"INSERT INTO player (f_name, l_name, address, city, state, zip, phone_number, " +
	" jersey_number, total_goals, total_assists, " +
	" total_penalty_minutes, total_penalties, " +
	" total_games_played, total_goals_against, " +
	" display_on_line, team, email_addr, skill_level, oid) " +
	"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + teamVal +
	", ?, ?, ?)";

      String updatePlayer =
	"UPDATE player SET f_name = ?, l_name = ?, address = ?, city = ?, state = ?, " +
	" zip = ?, phone_number = ?, jersey_number = ?, total_goals = ?, " +
	" total_assists = ?, total_penalty_minutes = ?, total_penalties = ?, " +
	"total_games_played = ?, total_goals_against = ?, display_on_line = ?, team = " + teamVal +
	", email_addr = ?, skill_level = ? WHERE oid = ?";

      Team myTeam = getTeam();

      int x=0;
      params[x++] = getFName();
      params[x++] = getLName();
      params[x++] = getAddress();
      params[x++] = getCity();
      params[x++] = getState();
      params[x++] = new BigDecimal(getZip());
      params[x++] = getPhoneNumber();
      params[x++] = new BigDecimal(getJerseyNumber());
      params[x++] = new BigDecimal(getTotalGoals());
      params[x++] = new BigDecimal(getTotalAssists());
      params[x++] = new BigDecimal(getTotalPenaltyMinutes());
      params[x++] = new BigDecimal(getTotalPenalties());
      params[x++] = new BigDecimal(getTotalGamesPlayed());
      params[x++] = new BigDecimal(getTotalGoalsAgainst());
      params[x++] = displayOnLine() ? "Y" : "N";
      if (teamOID.compareTo(new BigDecimal(0)) != 0)
	params[x++] = teamOID;
      params[x++] = getEmailAddr();
      params[x++] = getSkillLevel();
      params[x++] = getOID();

      String query;
      if (inserted)
	query = updatePlayer;
      else
	query = storePlayer;

      try {
	log.debug ("SQL: " +
		   DBUtils.getSQLStatementFromPreparedStatement (query, params));
	if ((numRows = db.executeUpdate(query, params)) != 1)
	  log.error ("Update only updated " + numRows + " rows.");
      }
      catch (java.sql.SQLException e) {
	throw new DbStoreException(e.toString());
      }
    } // End, if this object needs to be written to database

   // Now store the player's stats if they have changed.
    if (statsModified)
    {
      log.debug ("Save stat for player " + this);
      seasonStats.store(db);
    }

  } // End, store()


  /**
   * Set all player info from the info retrieved from the DB.
   *
   */

  private void setPlayerInfo(Hashtable record)
  {
    setOID(DBUtils.getBigDecimal("PLAYER_OID", record));
    setFName(DBUtils.getString("F_NAME", record));
    setLName(DBUtils.getString("L_NAME", record));
    setAddress(DBUtils.getString("ADDRESS", record));
    setCity(DBUtils.getString("CITY", record));
    setState(DBUtils.getString("STATE", record));
    setZip(DBUtils.getBigDecimal("ZIP", record).intValue());
    setPhoneNumber(DBUtils.getString("PHONE_NUMBER", record));
    setJerseyNumber(DBUtils.getBigDecimal("JERSEY_NUMBER", record).intValue());
    setTotalGoals(DBUtils.getBigDecimal("TOTAL_GOALS", record).intValue());
    setTotalAssists(DBUtils.getBigDecimal("TOTAL_ASSISTS", record).intValue());
    setTotalPenaltyMinutes(DBUtils.getBigDecimal("TOTAL_PENALTY_MINUTES", record).intValue());
    setTotalPenalties(DBUtils.getBigDecimal("TOTAL_PENALTIES", record).intValue());
    setTotalGamesPlayed(DBUtils.getBigDecimal("TOTAL_GAMES_PLAYED", record).intValue());
    setTotalGoalsAgainst(DBUtils.getBigDecimal("TOTAL_GOALS_AGAINST", record).intValue());
    setDisplayOnLine(DBUtils.getString("DISPLAY_ON_LINE", record).equals("Y"));
    setEmailAddr(DBUtils.getString("EMAIL_ADDR", record));
    setCanPlayGoal(DBUtils.getString("CAN_PLAY_GOAL", record).equals("Y"));
    setSkillLevel(DBUtils.getString("SKILL_LEVEL", record));
    setGoalieGamesWon(DBUtils.getBigDecimal("GOALIE_GAMES_WON", record).intValue());
    setGoalieGamesLost(DBUtils.getBigDecimal("GOALIE_GAMES_LOST", record).intValue());
    setGoalieGamesTied(DBUtils.getBigDecimal("GOALIE_GAMES_TIED", record).intValue());
    setTotalGoalsAgainst(DBUtils.getBigDecimal("TOTAL_GOALS_AGAINST", record).intValue());
    teamOID = DBUtils.getBigDecimal("TEAM", record);
    /* User object is not implemented yet.
     try {
      setUser(new User(DBUtils.getBigDecimal("USER_ID", record)));
    }
    catch (hockeystats.Team.NoSuchUserException e) {
      log.error ("Could not get user object for " + getFName() + " " + getLName() + ": " + e);
      setTeam(null);
    }
    */
  } // End, setPlayerInfo()


  public String toString()
  {
    return fName + " " + lName;
  } // End, toString()

  public boolean equals(Object target)
  {
    if (!(target instanceof Player))
      return false;

    if (((Player)target).getOID().equals(this.getOID()))
      return true;
    else
      return false;
  } // End, equals()

  public static void main (String[] args)
  {
    DbContext dbContext = new DbContext("this won't work, but at least it compiles");
    Player player = new Player("Tom", "Sterin", "3379 Chalfant Rd.", "Shaker", "OH",
			       44120, "2165610194", true, dbContext);

    player.setJerseyNumber(12);

    //    try {
    //      player.store();
    //    }
    //    catch (DbStoreException e) {}
  }

  /*************************************************************************************/
  /*************************************************************************************/
  /*              FACTORY METHODS                                                      */
  /*************************************************************************************/
  /*************************************************************************************/

  /**
   * Get all current players in the league.
   *
   * @return players ArrayList of Player objects
   */
  public static ArrayList getAllPlayers(SeasonSession selectedSeasonSession, DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    DBEngine db = new DBEngine(dbContext.getContextString());

    //log.debug("Getting all players for a season.");

    BigDecimal seasonOID = selectedSeasonSession.getSeason().getOID();

    String query = selectStmt + ", team t WHERE t.oid = team AND t.season = ? ORDER BY l_name";

    return getPlayers(query, selectedSeasonSession.getSeason().getOID(), log, dbContext);

  } // End, getAllPlayers()


  /**
   * Get all players in the league whether or not they are currently on a team.
   *
   * @return players ArrayList of Player objects
   */
  public static ArrayList getAllPlayers(DbContext dbContext)
  {
    ArrayList thisList = null;
    Category log = Log4jUtils.initLog("Player");
    DBEngine db = new DBEngine(dbContext.getContextString());

    //log.debug("Getting all the players in the league (on a team or not)");

    String query = selectStmt + "ORDER BY l_name";

    // Is the list already cached?  Return the list if so.
    if (playerLists.containsKey(dbContext))
    {
      //log.debug("Using cached list");
      thisList = (ArrayList)playerLists.get(dbContext);
    }
    else
    {
      //log.debug("List is not cached - get it from the database.");
      thisList = getPlayers(query, log, dbContext);
      playerLists.put(dbContext, thisList);
    }

    return thisList;

  } // End, getAllPlayers()


  /**
   * Get all the players for a specific team or all teams
   *
   * @return players ArrayList of Player objects
   */
  public static ArrayList getPlayersByTeam(Team theTeam, DbContext dbContext)
  {
    class TeamComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	int returnVal = 0;
	Player a = (Player)o1;
	Player b = (Player)o2;

	Team aTeam = a.getTeam();
	Team bTeam = b.getTeam();

	if (aTeam == null && bTeam == null)
	  returnVal = 0;
	else if (aTeam == null)
	  returnVal = -1;
	else if (bTeam == null)
	  returnVal = 1;
	else
	  returnVal =  a.getTeam().getTeamName().compareTo(b.getTeam().getTeamName());

	return returnVal;
      } // End, compare()
    } // End, local class teamComparator

    class JerseyNumberComparator implements Comparator
    {
      public int compare(Object o1, Object o2) throws ClassCastException
      {
	int returnVal = 0;
	Player a = (Player)o1;
	Player b = (Player)o2;

	if (a.getJerseyNumber() == b.getJerseyNumber())
	  returnVal = 0;
	else if (a.getJerseyNumber() > b.getJerseyNumber())
	  returnVal = 1;
	else
	  returnVal = -1;

	return returnVal;

      } // End, compare()
    } // End, class jerseyNumberComparator


    Category log = Log4jUtils.initLog("Player");
    boolean allTeams = false;

    if (theTeam == null)
    {
      //log.debug("Getting players for all teams.");
      allTeams = true;
    }
    //else
    //log.debug("Getting all players for team " + theTeam.getTeamName());

    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    if (allTeams)
    {
      for (Iterator iter=players.iterator(); iter.hasNext(); )
      {
	Player p = (Player)iter.next();
	if (p.getTeam() != null)
	  returnPlayers.add(p);
      } // End, for
      Collections.sort(returnPlayers, new TeamComparator());
    }// End, if allTeams
    else
    {
      for (Iterator iter=players.iterator(); iter.hasNext(); )
      {
	Player p = (Player)iter.next();
	if (p.getTeam() != null && p.getTeam().equals(theTeam))
	  returnPlayers.add(p);
      } // End, for
      Collections.sort(returnPlayers, new JerseyNumberComparator());
    } // End, else allTeams

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getPlayersByTeam()


  private static ArrayList getPlayers(String query, BigDecimal oid, Category log, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Object[] params = new Object[1];
    params[0] = oid;

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
	Player player = new Player(dbContext);
	player.setPlayerInfo(record);
	players.add(player);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting player info: " + sqle);
    }

    return players;
    
  } // End, getPlayers()

  private static ArrayList getPlayers(String query, Category log, DbContext dbContext)
  {
    DBEngine db = new DBEngine(dbContext.getContextString());
    Object[] params = new Object[0];

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
	Player player = new Player(dbContext);
	player.setPlayerInfo(record);
	players.add(player);
      }
    }
    catch (java.sql.SQLException sqle) {
      log.error("Error getting player info by team: " + sqle);
    }

    return players;
    
  } // End, getPlayers()


  /**
   * Get all the players ordered by number of goals in a season-session
   *
   * @param seasonSession The season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getPlayersByGoalCount(final SeasonSession selectedSeasonSession,
						DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all players by number of goals in a season.");

    class GoalCountComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	int aGoals = a.getGoals(selectedSeasonSession);
	int bGoals = b.getGoals(selectedSeasonSession);

	if (aGoals == bGoals) return 0;
	else if (aGoals < bGoals) return 1;
	return -1;
      } // End, compare()
    } // End, class GoalCountComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getGoals(selectedSeasonSession) > 0 && !(p.getFName().equals("Unknown")))
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new GoalCountComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getPlayersByGoalCount()


  /**
   * Get all the players ordered by total number of goals
   *
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getPlayersByGoalCount(DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all players by total number of goals.");

    class GoalCountComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	int aGoals = a.getTotalGoals();
	int bGoals = b.getTotalGoals();

	if (aGoals == bGoals) return 0;
	else if (aGoals < bGoals) return 1;
	return -1;
      } // End, compare()
    } // End, class GoalCountComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getTotalGoals() > 0 && !(p.getFName().equals("Unknown")))
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new GoalCountComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getPlayersByGoalCount()


  /**
   * Get all the players ordered by number of assists in a season-session
   *
   * @param seasonSession The season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getPlayersByAssistCount(final SeasonSession selectedSeasonSession,
						  DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all players by number of assists in a season.");

    class AssistCountComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	int aAssists = a.getAssists(selectedSeasonSession);
	int bAssists = b.getAssists(selectedSeasonSession);

	if (aAssists == bAssists) return 0;
	else if (aAssists < bAssists) return 1;
	return -1;
      } // End, compare()
    } // End, class AssistCountComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getAssists(selectedSeasonSession) > 0 && !(p.getFName().equals("Unknown")))
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new AssistCountComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getPlayersByAssistCount()


  /**
   * Get all the players ordered by number of total assists
   *
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getPlayersByAssistCount(DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all players by total number of assists.");

    class AssistCountComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	int aAssists = a.getTotalAssists();
	int bAssists = b.getTotalAssists();

	if (aAssists == bAssists) return 0;
	else if (aAssists < bAssists) return 1;
	return -1;
      } // End, compare()
    } // End, class AssistCountComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getTotalAssists() > 0 && !(p.getFName().equals("Unknown")))
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new AssistCountComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getPlayersByAssistCount()


  /**
   * Get all the players ordered by number of points in a season-session
   *
   * @param seasonSession The season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getPlayersByPoints(final SeasonSession selectedSeasonSession,
					     DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all players by number of points in the season.");

    class PointComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	int aPoints = a.getGoals(selectedSeasonSession) + a.getAssists(selectedSeasonSession);
	int bPoints = b.getGoals(selectedSeasonSession) + b.getAssists(selectedSeasonSession);

	if (aPoints == bPoints) return 0;
	else if (aPoints < bPoints) return 1;
	return -1;
      } // End, compare()
    } // End, class PointsComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getGoals(selectedSeasonSession) + p.getAssists(selectedSeasonSession) > 0)
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new PointComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getPlayersByPoints()


  /**
   * Get all the players ordered by total number of points
   *
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getPlayersByPoints(DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all players by total number of assists.");

    class PointComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	int aPoints = a.getTotalGoals() + a.getTotalAssists();
	int bPoints = b.getTotalGoals() + b.getTotalAssists();

	if (aPoints == bPoints) return 0;
	else if (aPoints < bPoints) return 1;
	return -1;
      } // End, compare()
    } // End, class PointsComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getTotalGoals() + p.getTotalAssists() > 0 && !(p.getFName().equals("Unknown")))
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new PointComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getPlayersByPoints()


  /**
   * Get all the players ordered by number of penalties in a season-session
   *
   * @param seasonSession The season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getPlayersByPenaltyCount(final SeasonSession selectedSeasonSession,
						   DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all players by number of penalties in a season.");

    class PenaltyCountComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	int aPenalties = a.getPenalties(selectedSeasonSession);
	int bPenalties = b.getPenalties(selectedSeasonSession);

	if (aPenalties == bPenalties) return 0;
	else if (aPenalties < bPenalties) return 1;
	return -1;
      } // End, compare()
    } // End, class PenaltyCountComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getPenalties(selectedSeasonSession) > 0 && !(p.getFName().equals("Unknown")))
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new PenaltyCountComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getPlayersByPenaltyCount()


  /**
   * Get all the players ordered by number of penalty minutes in a season-session
   *
   * @param seasonSession The season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getPlayersByPenaltyMinutes(final SeasonSession selectedSeasonSession,
						     DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all players by number of penalty minutes in a season.");

    class PenaltyMinuteComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	int aPenaltyMinutes = a.getPenaltyMinutes(selectedSeasonSession);
	int bPenaltyMinutes = b.getPenaltyMinutes(selectedSeasonSession);

	if (aPenaltyMinutes == bPenaltyMinutes) return 0;
	else if (aPenaltyMinutes < bPenaltyMinutes) return 1;
	return -1;
      } // End, compare()
    } // End, class PenaltyMinuteComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getPenaltyMinutes(selectedSeasonSession) > 0 && !(p.getFName().equals("Unknown")))
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new PenaltyMinuteComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getPlayersByPenaltyMinutes()


  /**
   * Get all the players ordered by total number of penalties
   *
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getPlayersByPenaltyCount(DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all players by number of total penalty minutes.");

    class PenaltyMinuteComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	int aPenaltyMinutes = a.getTotalPenaltyMinutes();
	int bPenaltyMinutes = b.getTotalPenaltyMinutes();

	if (aPenaltyMinutes == bPenaltyMinutes) return 0;
	else if (aPenaltyMinutes < bPenaltyMinutes) return 1;
	return -1;
      } // End, compare()
    } // End, class PenaltyMinuteComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getTotalPenaltyMinutes() > 0 && !(p.getFName().equals("Unknown")))
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new PenaltyMinuteComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getPlayersByPenaltyCount()


  /**
   * Get all goalies ordered by goals-against for the specified season
   *
   * @param seasonSession The season-session of interest
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getGoalies(final SeasonSession selectedSeasonSession, DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all goalies by goals against average.");

    class GAAComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	double aGAA = a.getGoalsAgainst(selectedSeasonSession) /
	  a.getGoalieGames(selectedSeasonSession);
	double bGAA = b.getGoalsAgainst(selectedSeasonSession) /
	  b.getGoalieGames(selectedSeasonSession);

	if (aGAA == bGAA) return 0;
	else if (aGAA > bGAA) return 1;
	return -1;
      } // End, compare()
    } // End, class GAAComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getGoalieGamesWon(selectedSeasonSession) != -1)
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new GAAComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getGoalies()


  /**
   * Get all the goalies ordered by goals against average
   *
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static ArrayList getGoalies(DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting all goalies by goals against average.");

    class GAAComparator implements Comparator
    {
      public int compare(Object o1, Object o2)
      {
	Player a = (Player)o1;
	Player b = (Player)o2;

	double aGAA = a.getTotalGoalsAgainst() / a.getGoalieGamesPlayed();
	double bGAA = b.getTotalGoalsAgainst() / b.getGoalieGamesPlayed();

	if (aGAA == bGAA) return 0;
	else if (aGAA > bGAA) return 1;
	return -1;
      } // End, compare()
    } // End, class GAAComparator
    
    ArrayList players = getAllPlayers(dbContext);
    ArrayList returnPlayers = new ArrayList(players.size());

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getGoalieGamesWon() != -1)
	returnPlayers.add(p);
    } // End, for

    Collections.sort(returnPlayers, new GAAComparator());

    returnPlayers.trimToSize();
    return returnPlayers;

  } // End, getGoalies()


  /**
   * Get a player object from its OID
   *
   * @param oid The oid of the player object we want
   * @param dbContext The dbContext for the query
   * @return players ArrayList of Player objects
   */
  public static Player getPlayer(BigDecimal oid, DbContext dbContext)
  {
    Category log = Log4jUtils.initLog("Player");
    //log.debug("Getting a player from an oid");

    ArrayList players = getAllPlayers(dbContext);

    for (Iterator iter=players.iterator(); iter.hasNext(); )
    {
      Player p = (Player)iter.next();
      if (p.getOID().equals(oid))
      {
	log.debug("Player found is " + p);
	return p;
      }
    } // End, for

    log.error ("No player found in getPlayer()");
    return null;

  } // End, getPlayer()

} // End, class Player

