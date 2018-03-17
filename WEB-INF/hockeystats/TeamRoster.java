// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/TeamRoster.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;

import hockeystats.util.*;
import java.util.*;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * TeamRoster bean
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: TeamRoster.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.6  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.5  2002/04/10 22:24:47  tom
 * Change to use the dbContext to pass to DBEngine the name of the props
 * file to use for the session.  This enables us to support more than
 * one league at a time by using different databases for each league.
 *
 * Revision 1.4  2002/04/03 14:04:41  tom
 * First production version.
 *
 * Revision 1.3  2001/12/20 14:19:59  tom
 * Latest changes.
 *
 * Revision 1.2  2001/12/10 07:09:45  tom
 * Add cvs Log section.
 *
 */

public class TeamRoster {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/TeamRoster.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  Category log = Log4jUtils.initLog(this);
  private ArrayList players = null;
  private String teamName = null;

  public TeamRoster(SeasonSession selectedSeasonSession, DbContext dbContext)
  {
    log.debug("Creating a new TeamRoster object for the league.");
    players = Player.getAllPlayers(selectedSeasonSession, dbContext);
  }

  TeamRoster(Team theTeam, DbContext dbContext)
  {
    log.debug ("Getting roster for team: " + theTeam.getTeamName() + ", Season: " +
	       theTeam.getSeason().getSeasonName());
    this.teamName = teamName;
    players = Player.getPlayersByTeam(theTeam, dbContext);
  }


  /**
   * This method causes the team roster to be sorted by the players' jersey numbers.
   */

  /*  public void sortByNumber()
  {
    Array.sort(players, new RosterNumberComparator());
    }*/ // End, sortByNumber()

  public ArrayList getPlayers() { return players; }

  public String toString()
  {
    return "Roster for " + teamName + ", number of players: " + players.size();
  }

} // end of TeamRoster bean
