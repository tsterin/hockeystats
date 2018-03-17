// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/GameEntry.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import hockeystats.util.*;
import hockeystats.util.db.*;
import java.util.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * GameEntry
 *
 * This class is used to gather all the information for a game.  The data will
 * be written to the database after all information has been gathered.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: GameEntry.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.5  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.4  2002/08/23 01:59:34  tom
 * Changes for version 2 of site.
 *
 * Revision 1.3  2002/05/30 19:28:14  tom
 * Changes to site to allow viewing and changing of goal data.
 *
 * Revision 1.2  2002/04/14 14:05:51  tom
 * Fixes for updating players.
 *
 * Revision 1.1  2002/04/03 14:04:41  tom
 * First production version.
 *
 *
 */

public class GameEntry {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/GameEntry.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Team homeTeam;
  Team visitorTeam;
  ArrayList homeTeamPlayers = new ArrayList();
  ArrayList visitorTeamPlayers = new ArrayList();
  ArrayList homeTeamSubs = new ArrayList();
  ArrayList visitorTeamSubs = new ArrayList();
  ArrayList homePenaltyList = new ArrayList();
  ArrayList visitorPenaltyList = new ArrayList();
  ArrayList gamePlayers = new ArrayList();
  ArrayList goals = new ArrayList();
  Player homeTeamGoalie;
  Player visitorTeamGoalie;
  int homeGoalCount;
  int visitorGoalCount;
  int homePenalties;
  int visitorPenalties;
  Game thisGame;
  SeasonSession gameSeasonSession;

  Category log = Log4jUtils.initLog(this);

  public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }
  public void setVisitorTeam(Team visitorTeam) { this.visitorTeam = visitorTeam; }
  public void setHomeGoalCount(int homeGoals) { this.homeGoalCount = homeGoals; }
  public void setVisitorGoalCount(int visitorGoals) { this.visitorGoalCount = visitorGoals; }
  public void setHomePenalties(int homePenalties) { this.homePenalties = homePenalties; }
  public void setVisitorPenalties(int visitorPenalties) { this.visitorPenalties = visitorPenalties; }
  public void setGame(Game game) { this.thisGame = game; }

  public void setHomeTeamGoalie(Player goalie)
  {
    this.homeTeamGoalie = goalie;
  } // End, setHomeTeamGoalie()

  public void setVisitorTeamGoalie(Player goalie)
  {
    this.visitorTeamGoalie = goalie;
    Iterator iter;
    for (iter=gamePlayers.iterator(); iter.hasNext();)
    {
      GamePlayer gp = (GamePlayer)iter.next();
      if (gp.getTeam().equals(visitorTeam) && gp.getPlayer() != null)
      {
	if (gp.getPlayer().equals(goalie))
	  gp.setGoalie();
	else
	  gp.clearGoalie();
      } // End, if
    } // End, for
  } // End, setVisitorTeamGoalie()

  public void setGameSeasonSession(SeasonSession seasonSession) { gameSeasonSession = seasonSession; }

  public Team getHomeTeam() { return homeTeam; }
  public Team getVisitorTeam() { return visitorTeam; }
  public int getHomeGoalCount() { return homeGoalCount; }
  public int getVisitorGoalCount() { return visitorGoalCount; }
  public int getHomePenalties() { return homePenalties; }
  public int getVisitorPenalties() { return visitorPenalties; }
  public Game getGame() { return thisGame; }
  public Player getHomeTeamGoalie() { return homeTeamGoalie; }
  public Player getVisitorTeamGoalie() { return visitorTeamGoalie; }
  public SeasonSession getGameSeasonSession() { return gameSeasonSession; }

  public void addHomeTeamPlayer (Player aPlayer) { log.debug("Add to home team " + aPlayer); homeTeamPlayers.add(aPlayer); }
  public void addVisitorTeamPlayer (Player aPlayer) { log.debug("Add to visitor team " + aPlayer); visitorTeamPlayers.add(aPlayer); }
  public void addHomeTeamSub (Player aPlayer) { log.debug("Add to home subs " + aPlayer); homeTeamSubs.add(aPlayer); }
  public void addVisitorTeamSub (Player aPlayer) { log.debug("Add to visitor " + aPlayer); visitorTeamSubs.add(aPlayer); }
  public void clearHomeTeamPlayers() { log.debug("********************clear1");homeTeamPlayers.clear(); }
  public void clearVisitorTeamPlayers() { log.debug("********************clear2"); visitorTeamPlayers.clear(); }
  public void clearHomeTeamSubs() { log.debug("********************clear2"); homeTeamSubs.clear(); }
  public void clearVisitorTeamSubs() { log.debug("********************clear4"); visitorTeamSubs.clear(); }

  public ArrayList getHomeTeamPlayers()
  {
    ArrayList players = new ArrayList(homeTeamPlayers);
    players.addAll(homeTeamSubs);
    log.debug("Home team players:");
    for (Iterator iter=players.iterator(); iter.hasNext();)
      log.debug(((Player)iter.next()).toString());
    return players;
  } // end, getHomeTeamPlayers()
  public ArrayList getHomeTeamPlayersOnly() { return new ArrayList(homeTeamPlayers); }

  public ArrayList getVisitorTeamPlayers()
  {
    ArrayList players = new ArrayList(visitorTeamPlayers);
    players.addAll(visitorTeamSubs);
    log.debug("Visitor team players:");
    for (Iterator iter=players.iterator(); iter.hasNext();)
      log.debug(((Player)iter.next()).toString());
    return players;
  } // end, getVisitorTeamPlayers()
  public ArrayList getVisitorTeamPlayersOnly() { return new ArrayList(visitorTeamPlayers); }

  public int getHomeTeamPlayersCount() { return homeTeamPlayers.size(); }
  public int getHomeTeamSubsCount() { return homeTeamSubs.size(); }
  public int getVisitorTeamPlayersCount() { return visitorTeamPlayers.size(); }
  public int getVisitorTeamSubsCount() { return visitorTeamSubs.size(); }

  public void addHomePenalty(Penalty aPenalty) { homePenaltyList.add(aPenalty); }
  public void addVisitorPenalty(Penalty aPenalty) { visitorPenaltyList.add(aPenalty); }
  public ArrayList getHomePenaltyList() { return homePenaltyList; }
  public ArrayList getVisitorPenaltyList() { return visitorPenaltyList; }
  public void clearHomePenalties() { homePenaltyList.clear(); }
  public void clearVisitorPenalties() { visitorPenaltyList.clear(); }

  public void addGoal(GameGoal goal) { goals.add(goal); }
  public ArrayList getGoals() { return goals; }
  public void clearGoals() { goals.clear(); }

  private ArrayList getGamePlayers(DbContext dbContext)
  {
    Iterator iter;
    boolean isGoalie;

    gamePlayers.clear();

    for (iter=homeTeamPlayers.iterator(); iter.hasNext();)
    {
      Player aPlayer = (Player)iter.next();
      isGoalie = aPlayer.equals(homeTeamGoalie);
      GamePlayer gp = new GamePlayer(getGame(), aPlayer, homeTeam, false, isGoalie, dbContext);
      gamePlayers.add(gp);
    } // End, for

    for (iter=homeTeamSubs.iterator(); iter.hasNext();)
    {
      Player aPlayer = (Player)iter.next();
      isGoalie = aPlayer.equals(homeTeamGoalie);
      GamePlayer gp = new GamePlayer(getGame(), aPlayer, homeTeam, true, isGoalie, dbContext);
      gamePlayers.add(gp);
    } // End, for

    for (iter=visitorTeamPlayers.iterator(); iter.hasNext();)
    {
      Player aPlayer = (Player)iter.next();
      isGoalie = aPlayer.equals(visitorTeamGoalie);
      GamePlayer gp = new GamePlayer(getGame(), aPlayer, visitorTeam, false, isGoalie, dbContext);
      gamePlayers.add(gp);
    } // End, for

    for (iter=visitorTeamSubs.iterator(); iter.hasNext();)
    {
      Player aPlayer = (Player)iter.next();
      isGoalie = aPlayer.equals(visitorTeamGoalie);
      GamePlayer gp = new GamePlayer(getGame(), aPlayer, visitorTeam, true, isGoalie, dbContext);
      gamePlayers.add(gp);
    } // End, for

    return gamePlayers;
  }

  public void store(DbContext dbContext, DBEngine db) throws DbStore.DbStoreException
  {
    Iterator iter;
    thisGame.store(db);

    ArrayList allHomeTeamPlayers = getHomeTeamPlayers();
    for (iter=allHomeTeamPlayers.iterator(); iter.hasNext();)
    {
      Player p = (Player)iter.next();
      log.debug ("--------------> Storing game data for home team player " + p);
      p.store(db);
    }

    ArrayList allVisitorTeamPlayers = getVisitorTeamPlayers();
    for (iter=allVisitorTeamPlayers.iterator(); iter.hasNext();)
    {
      Player p = (Player)iter.next();
      log.debug ("--------------> Storing game data for visitor team player " + p);
      p.store(db);
    }

    for (int i=0; i<homePenaltyList.size(); i++)
      ((Penalty)homePenaltyList.get(i)).store(db);

    for (int i=0; i<visitorPenaltyList.size(); i++)
      ((Penalty)visitorPenaltyList.get(i)).store(db);

    ArrayList gamePlayers = getGamePlayers(dbContext);
    for (int i=0; i<gamePlayers.size(); i++)
      ((GamePlayer)gamePlayers.get(i)).store(db);

    for (int i=0; i<goals.size(); i++)
      ((GameGoal)goals.get(i)).store(db);

    homeTeam.store(db);
    visitorTeam.store(db);

  } // End, store()

} // end of GameEntry class
