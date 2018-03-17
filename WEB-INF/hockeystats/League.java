// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/League.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats;
import java.math.*;

/** 
 * Class to represent the league.
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: League.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.3  2002/09/14 23:25:18  tom
 * Changes for 2.1
 *
 * Revision 1.2  2001/12/10 07:09:45  tom
 * Add cvs Log section.
 *
 */

public class League {

  private final static String CVSId = "$Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/League.java,v 1.1 2002/09/15 01:53:23 tom Exp $";

  private String name;
  private String seasonYear;
  private String seasonSession;

  public static final String PRE_SEASON = "PRE_SEASON";
  public static final String REGULAR_SEASON = "REGULAR_SEASON";
  public static final String POST_SEASON = "POST_SEASON";

  public static class InvalidSeasonException extends Exception
  {
    public InvalidSeasonException(String message)
    {
      super(message);
    }
  }

  /**
   * Constructor
   */
  public League(String name)
  {
    this.name = name;
  }

  /**
   * Constructor
   */
  public League()
  {
  }

  /**
   * Get the season-year (eg, Winter 2001-02)
   */
  public String getSeasonYear() { return seasonYear; };

  /**
   * Set the season-year (eg, Winter 2001-02)
   *
   * @param seasonYear String containing the season
   */
  public void setSeasonYear(String seasonYear) { this.seasonYear = seasonYear; };

  /**
   * Get the season-session (ie, PRE_SEASON, REGULAR_SEASON, POST_SEASON)
   */
  public String getSeasonSession() { return seasonSession; };

  /**
   * Set the season-session (ie, PRE_SEASON, REGULAR_SEASON, POST_SEASON)
   *
   * @param seasonYear String containing the season
   * @exception InvalidSeasonException Thrown if an invalid value for session is passed
   */
  public void setSeasonSession(String seasonSession) throws InvalidSeasonException
  {
    if (!seasonSession.equals(PRE_SEASON) &&
	!seasonSession.equals(REGULAR_SEASON) &&
	!seasonSession.equals(POST_SEASON))
      this.seasonYear = seasonYear;
    else
      throw new InvalidSeasonException (seasonSession + " is not a valid season session");
  } // End, setSeasonSession()

} // End, class League
