// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/Log4jUtils.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util;

import java.util.ResourceBundle;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** A class that contains methods to simplify the usage of the log4j util
 *  @author Craig Last
 */
public class Log4jUtils
{
  private static String LOG_PROPS_FILE;
  private static final String LOG_CONFIG_KEY_NAME = "logConfigurationFile";


  /** Initializes (calls it once) to set up the property configurator
   */
  static 
  {
    ResourceBundle properties = ResourceBundle.getBundle("properties.hockeystats");
    PropertyConfigurator.configure(properties.getString(LOG_CONFIG_KEY_NAME));
    //System.out.println ("Log4jUtils:  read config file.");
  }

  /** Creates new Log4jUtils - not used */
  private Log4jUtils()
  {
  }

  /** A static method that initializes the logging system
   *  @param callingObject Pass it this
   *  @return A Category object to do the logging with
   */
  public static Category initLog(Object callingObject)
  {
    //PropertyConfigurator.configure(LOG_PROPS_FILE);
    return Category.getInstance(callingObject.getClass().getName());
  }
}
