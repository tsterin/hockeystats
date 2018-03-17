// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/db/DBCache.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util.db; // Generated package name

import java.sql.*;
import javax.sql.*;
import oracle.jdbc.driver.*;
import oracle.jdbc.pool.*;
import java.util.*;
import hockeystats.util.*;

 // Import log4j classes.
 import org.apache.log4j.Category;
 import org.apache.log4j.PropertyConfigurator;

/**
 * DBCache.java
 *
 *
 * Created: Tue Oct 17 11:21:32 2000
 *
 * @author Bruce Haugland
 * @version
 */

public class DBCache  {

    /**
     * Connection cache - one for each properties file
     */
    private static HashMap connectionMap = new HashMap(6);
    
    /**
     * Gets the property bundle for the DBCache.
     *
     * @return a value of type 'ResourceBundle'
     */
    protected static ResourceBundle getProperties(String propertyFile) {
        ResourceBundle properties = ResourceBundle.getBundle(propertyFile);
	return properties;
    }

    /**
     * Gets a connection to the database.
     *
     * @return a value of type 'Connection'
     * @exception SQLException if an error occurs
     */
    public static synchronized Connection getConnection(String propertyFile) throws SQLException {

      Category log = Log4jUtils.initLog("Player");

      //log.debug ("Getting a connection from the cache for propfile: " + propertyFile);
      OracleConnectionCacheImpl ods = (OracleConnectionCacheImpl)connectionMap.get(propertyFile);

      if (ods == null) {
	//log.debug ("Connection not found, creating a new one.");
	ResourceBundle props = getProperties(propertyFile);

	ods = new OracleConnectionCacheImpl();
	//System.out.println(getProperties().getString("URL"));
	//	    ods.setURL(getProperties().getString("URL"));

	// Use the following instead of the above setURL() call
	ods.setDriverType("thin");
	ods.setServerName("spock");
	ods.setPortNumber(1521);
	ods.setDatabaseName("db2");
	ods.setNetworkProtocol("tcp");

	ods.setUser(props.getString("User"));
	ods.setPassword(props.getString("Password"));
	ods.setMaxLimit(Integer.valueOf(props.getString("MaxConnections")).intValue());

	connectionMap.put(propertyFile, ods);
      }
      //else
	//log.debug ("Found a connection.");

      return ods.getConnection();

    } // End, getConnection()

    
} // DBCache





