// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/Util.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util;

// Import log4j classes.
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** 
 * Utility functions
 *
 * @author Tom Sterin
 * @version 0.1
 */

/*
 * $Log: Util.java,v $
 * Revision 1.1  2002/09/15 01:53:23  tom
 * initial commit to CVS
 *
 * Revision 1.1  2002/09/14 23:31:25  tom
 * Changes for 2.1.  Moved from officemax tree to here.
 *
 * Revision 1.1  2002/05/30 20:25:49  tom
 * New class for utility functions.
 *
 *
 */
public class Util {
  /**
   * Pads a string with leading spaces to make it the requested length
   */
  public static String padString(String source, int length, char padChar)
  {
    int spaces = length - source.length();
    StringBuffer returnString = new StringBuffer("");
    if (spaces <= 0) return source;

    for (int i=0; i<spaces; i++) returnString.append(padChar);
    returnString.append(source);

    return returnString.toString();
  } // End, padString

} // End, class Util
