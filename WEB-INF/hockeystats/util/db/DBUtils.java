// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/db/DBUtils.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util.db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.StringWriter;
import java.sql.*;

/** A class that contains static method utilities to simplify the process of extracting
 *  the values from the Hashtable row the Database returns
 *  @author Craig Last
 */
public class DBUtils {

    /** Creates new DBUtils - not used */
    private DBUtils() {
    }

    /** A static method that returns an empty string if the result is null or returns
     *  the value
     *  @param colName The database column name
     *  @param row The database row
     *  @return The result (empty string or the value)
     */
    public static String getString(String colName, Hashtable row)
    {
        return getString(colName, row, "");
    }

    /** A static method that returns a default value if the result is null or returns
     *  the value
     *  @param colName The database column name
     *  @param row The database row
     *  @param defaultValue The default value if result is null    
     *  @return The result (default value or the value)
     */
    public static String getString(String colName, Hashtable row, String defaultValue)
    {
	// we return default value if the get results in a null... else return the value
	Object val = row.get(colName);
	if ( val == null || val instanceof Null ) {
	    return defaultValue;
	} else if (val instanceof String) {
	    return (String) val;
        } else {
            return val.toString();
	}
    }
    
    /** A static method that returns a null if the result is null or returns the value
     *  @param colName The database column name
     *  @param row The database row
     *  @return The result (null or the value)
     */
    public static BigDecimal getBigDecimal(String colName, Hashtable row)
    {
        return getBigDecimal(colName, row, null);
    }

    /** A static method that returns the default value if the result is null or returns
     *  the value
     *  @param colName The database column name
     *  @param row The database row
     *  @param defaultValue The default value if result is null
     *  @return The result (default value or the value)
     */
    public static BigDecimal getBigDecimal(String colName, Hashtable row,
        BigDecimal defaultValue)
    {
	Object val = row.get(colName);
	if (val instanceof Null || val == null) {
	    return defaultValue;
        } else if (val instanceof BigDecimal) {
	    return (BigDecimal) val;
        } else if (val instanceof String) {
            try {
                return new BigDecimal((String) val);
            } catch (Exception e) {
                return defaultValue;
            }
	} else {
            return defaultValue;
        }
    }
    

    /** Trys to get a date object out of the given Hashtable, of key colName. Returns defaultValue
     *          if no date could be found. The stored date could be of type java.sql.Date,
     *          java.sql.Timestamp, or java.util.Date. If none of these are found, then the
     *          default value is returned.
     *  @param colName The database column name
     *  @param row The database row
     *  @param defaultValue The default value if result is null
     *  @return The result (default value or the value)
     */
    public static java.util.Date getDate(String colName, Hashtable row, java.util.Date defaultValue)
    {
        Object val = row.get(colName);
        if (val instanceof Null || val == null) {
            return defaultValue;
        } else if (val instanceof java.sql.Date) {
            return (java.util.Date) val;
        } else if (val instanceof java.sql.Timestamp) {
            return (java.util.Date) val;
        } else if (val instanceof java.util.Date) {
            return (java.util.Date) val;
        } else {
            return defaultValue;
        }
    }
    
    
    /** Trys to get a date object out of the given Hashtable, of key colName. Returns defaultValue
     *          of null if no date could be found. The stored date could be of type java.sql.Date,
     *          java.sql.Timestamp, or java.util.Date. If none of these are found, then the
     *          default value null is returned.
     *  @param colName The database column name
     *  @param row The database row
     *  @return The result (default value null or the value)
     */
    public static java.util.Date getDate(String colName, Hashtable row)
    {
        return getDate(colName, row, null);
    }

    /** A static method that returns null if the result is null or returns
     *  the value
     *  @param colName The database column name
     *  @param row The database row
     *  @return The result (default value or the value)
     */
    public static BigInteger getBigInteger(String colName, Hashtable row)
    {
        return getBigInteger(colName, row, null);
    }

    /** A static method that returns the default value if the result is null or returns
     *  the value
     *  @param colName The database column name
     *  @param row The database row
     *  @param defaultValue The default value if result is null
     *  @return The result (default value or the value)
     */
    public static BigInteger getBigInteger(String colName, Hashtable row,
        BigInteger defaultValue)
    {
        BigDecimal bd = getBigDecimal(colName, row);

        if (bd == null)
        {
            return defaultValue;
        }
        else
        {
            return bd.toBigInteger();
        }
    }
    
    public static int getInt(Object val)
    {
	if ( val instanceof BigDecimal )
	    {
		return ((BigDecimal)val).intValue();
	    }
	else if ( val instanceof BigInteger )
	    {
		return ((BigInteger)val).intValue();
	    }
	else if ( val instanceof String )
	    {
		try
		    {
			int i;
			
			i = Integer.parseInt((String)val);
			return i;
		    }
		catch (NumberFormatException nfe)
		    {
			return -1;
		    }
	    }
	else if  ( val instanceof java.sql.Timestamp )
	    {
		// i don't know why you'd want this
		return ((Timestamp)val).getNanos();
	    }
	else if ( val instanceof java.sql.Date )
	    {
		// if it's a date, get the millis
		return (int) ((java.sql.Date)val).getTime();
	    }
	else
	    {
		return -1;
	    }

    }

    public static double getDouble(Object val)
    {
	if ( val instanceof BigDecimal )
	    {
		return ((BigDecimal)val).doubleValue();
	    }
	else if ( val instanceof BigInteger )
	    {
		return ((BigInteger)val).doubleValue();
	    }
	else if ( val instanceof String )
	    {
		try
		    {
			double d;
			
			d = Double.parseDouble((String)val);
			return d;
		    }
		catch (NumberFormatException nfe)
		    {
			return (double)-1.0;
		    }
	    }
	else if  ( val instanceof java.sql.Timestamp )
	    {
		// i don't know why you'd want this
		return (double) ((Timestamp)val).getNanos();
	    }
	else if ( val instanceof java.sql.Date )
	    {
		// if it's a date, get the millis
		return (double) ((java.sql.Date)val).getTime();
	    }
	else
	    {
		return (double)-1.0;
	    }
    }

    public static String buildUpdate(Hashtable data)
    {
	// basically we assume that data has as names the column names, and as values
	// the column values.

	StringWriter sw = new StringWriter();
	Enumeration e;
	
	e = data.keys();
	
	while ( e.hasMoreElements() )
	    {
		String name = (String)e.nextElement();
		Object value = data.get(name);
		if ( ! ( value instanceof Null ) )
		    {
			sw.write(name);
			sw.write("=");
			if ( value instanceof java.sql.Date )
			    {
				sw.write("to_date('");
				sw.write((String)value);
				sw.write("','YYYY-MM-DD') ,");
			    }
			else if ( value instanceof java.sql.Timestamp )
			    {
				String time = ((Timestamp)value).toString();
				sw.write("to_date('");
				sw.write(time.substring(0,time.indexOf(".")));
				sw.write("','YYYY-MM-DD HH24-MI-SS') ,");
			    }
			else if  ( value instanceof BigInteger )
			    {
				sw.write(((BigInteger)value).toString());
			    }
			else if  ( value instanceof BigDecimal )
			    {
				sw.write(((BigDecimal)value).toString());
				sw.write(" ,");
			    }
			else // strings and other things
			    {
				sw.write("'");
				sw.write((String)value);
				sw.write("' ,");
			    }
			

		    }
	    }
	if ( sw.toString().endsWith(","))
	    return sw.toString().substring(0,sw.toString().length() - 1);
	else
	    return sw.toString();
	
    }

    public static String buildInsert(Hashtable data)
    {
	// basically we assume that data has as names the column names, and as values
	// the column values.

	StringWriter keys = new StringWriter();
	StringWriter values = new StringWriter();
	String keyString;
	String valueString;
	Enumeration e;

	e = data.keys();
	
	keys.write("(");
	values.write("(");
	while ( e.hasMoreElements() )
	    {
		String name = (String)e.nextElement();
		Object value = data.get(name);
		if ( ! ( value instanceof Null ) )
		    {
			keys.write(name);
			if ( value instanceof java.sql.Date )
			    {
				values.write("to_date('");
				values.write((String)value);
				values.write("','YYYY-MM-DD HH24:MI') ,");
			    }
			else if ( value instanceof java.sql.Timestamp )
			    {
				String time = ((Timestamp)value).toString();
				values.write("to_date('");
				values.write(time.substring(0,time.indexOf(".")));
				values.write("','YYYY-MM-DD HH24-MI-SS') ,");
			    }
			else if  ( value instanceof BigInteger )
			    {
				values.write(((BigInteger)value).toString());
			    }
			else if  ( value instanceof BigDecimal )
			    {
				values.write(((BigDecimal)value).toString());
				values.write(" ,");
			    }
			else // strings and other things
			    {
				values.write("'");
				values.write((String)value);
				values.write("' ,");
			    }
			keys.write(",");
			
		    }
	    }
	keyString  = keys.toString();
	valueString = values.toString();

	if (keyString.endsWith(","))
	    keyString = keyString.substring(0,keyString.length() - 1);
	if (valueString.endsWith(","))
	    valueString = valueString.substring(0,valueString.length() -1 );
	return keyString + ") values " + valueString + ")";
    }


    /** Method gets a value that is ready to be added to a param list to be sent to DBEngine.
     *          If no value is passed, then a Null object is returned of the given type.
     *  @param type Constant that can be found in java.sql.Types.
     *  @param key The Hashtable key to try to get a value from.
     *  @param record The database record.
     *  @return The value suitable for the DBEngine param list array.
     */
    public static Object getDBValue(int type, String key, Hashtable record)
    {
        Object value;
        
        if (type == java.sql.Types.NUMERIC) {
            value = DBUtils.getBigDecimal(key, record);
            if (value != null && ((BigDecimal) value).intValue() == -1) {
                value = null;
            }
        } else if (type == java.sql.Types.DATE) {
            value = DBUtils.getDate(key, record);
            if (value != null) {
                value = new java.sql.Timestamp(((java.util.Date) value).getTime());
            }
        } else {
            value = DBUtils.getString(key, record);
        }
        
        if (value == null) {
            value = new Null(type);
        }
        
        return value;
    }
    
    
    /** This is only meant for debugging purposes. This method will try to construct a SQL
     *          statement from a prepared statement and a list of parameters. This will not produce
     *          valid SQL. Can be used to write the result to a log for debugging.
     *  @param sql The prepared statement.
     *  @param params The list of params for the prepared statement.
     *  @return The SQL statement suitable for debugging only.
     */
    public static String getSQLStatementFromPreparedStatement(String sql, Object[] params)
    {
        String rv = "";
        
        for (int x = 0; x < params.length && sql.indexOf("?") >= 0; x++) {
            rv += sql.substring(0, sql.indexOf("?"));
            sql = sql.substring(sql.indexOf("?") + 1);
            
            if (params[x] instanceof String) {
                rv += "'" + ((String) params[x]).toString() + "'";
            } else if (params[x] instanceof BigDecimal) {
                rv += ((BigDecimal) params[x]).toString();
            } else if (params[x] instanceof java.util.Date) {
                rv += "to_date('" + ((java.util.Date) params[x]).toString() + "', 'YYYY-MM-DD HH24:MI')";
            } else if (params[x] instanceof Null) {
                rv += "null";
            } else {
                rv += "unknown type";
            }
            
        }
        
        rv += sql;
        return rv;
    }
    
    
    /** Simple method that will check the passed in value for null, if it is, will return a Null object
     *          of the given type. Useful for populating a param list to be passed to DBEngine.
     *  @param type Constant that can be found in java.sql.Types.
     *  @param val This value is checked for null, if null, a Null object of the given type is returned.
     *  @return The value suitable for the DBEngine param list array.
     */
    public static Object getDBValue(int type, Object val)
    {
        if (val == null) {
            return new Null(type);
        } else {
            return val;
        }
    }


}
