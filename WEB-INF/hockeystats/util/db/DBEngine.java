// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/db/DBEngine.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util.db; // Generated package name

import java.sql.*;
import java.util.*;
import java.math.*;


/**
 * DBEngine.java
 * <br> 
 * This provides an easier way to access the database
 *
 * Created: Thu Oct 12 09:35:24 2000
 *
 * @author Bruce Haugland
 * @version 1.0
 */

public class DBEngine  {

  private String DBCachePropFile = null;

    /**
     * Database connection - This is currently used only by method getResultSet and closeConnection
     */
    private Connection connection = null;

    private Transaction trans; // inner class for transactions
    private boolean transacting;

    /**
     * Constructor for DBEngine.
     *
     */
    public DBEngine(String DBCachePropFile) {
        this.DBCachePropFile = DBCachePropFile;
	transacting = false;
    }

    /**
     * Method to tell if the current DBEngine is in the middle of a transaction.  All changes
     * made within a transaction will be committed or rolled back as a unit.
     *
     * @returns boolean
     */
    public boolean isInTransaction() { return transacting; }

    protected Connection getCachedConnection() throws SQLException {
	
	return DBCache.getConnection(DBCachePropFile);
    }


    /**
     * Get the database context string for this DBEngine.
     */
    public String getDbContext() { return DBCachePropFile; }
		

    /**
     * Actual place to get a connection.
     *
     * @return a value of type 'Connection'
     * @exception SQLException if an error occurs
     */
    public Connection getConnection() throws SQLException {
        if (transacting) {
            return trans.getConnection();
        } else {
	    return getCachedConnection();
        }
    }

	

    /**
     * Sets a parameter of the prepared statement passed.
     *
     * @param statement a value of type 'PreparedStatement'
     * @param parameter a value of type 'Object'
     * @param index a value of type 'int'
     * @exception SQLException if an error occurs
     */
    protected void setParameter(PreparedStatement statement, Object parameter, int index) throws SQLException {

	if (parameter instanceof Null) {
	    statement.setNull(index, ((Null)parameter).getType());
        } else if (parameter instanceof OutputParameter) {            
            statement.setObject(index, ((OutputParameter) parameter).getObject());
            ((CallableStatement) statement).registerOutParameter(
                    index, ((OutputParameter) parameter).getType());
        } else {
	    statement.setObject(index, parameter);
	}
    }
    
    
    /**
     * Sets the parameters for the prepared statement passed.
     *
     * @param statement a value of type 'PreparedStatement'
     * @param [] a value of type 'Object'
     * @exception SQLException if an error occurs
     */
    protected void setParameters(PreparedStatement statement, Object [] parameters) throws SQLException {

	for (int index = 0; index < parameters.length; ++index) {
	    setParameter(statement, parameters[index], index + 1);
	}
    }
    
    
    protected void getOutputParameters(CallableStatement statement, Object[] parameters) throws SQLException
    {
        for (int index = 0; index < parameters.length; ++index) {
            if (parameters[index] instanceof OutputParameter) {
                parameters[index] = statement.getObject(index + 1);
            }
        }
    }
	    

    /** Start a transaction
     */
    public void beginTransaction()
	throws SQLException
    {
	trans = new Transaction(getConnection());
	transacting = true;
    }
	
    /** Ends a transaction
     */
    private void endTransaction()
	throws SQLException
    {
	trans.close();
	transacting = false;
    }

    /** Rolls back the current SQL transaction
     */
    public void rollback()
	throws SQLException
    {
	trans.rollbackTransaction();
        endTransaction();
    }
    
    /** Commits the current SQL transaction
     */
    public void commit()
	throws SQLException
    {
	trans.commitTransaction();
        endTransaction();
    }
    
    
    /** Executes a stored procedure. The parameters array contains the INPUT and OUTPUT paramaters
     *          for the statement. OutputParameters must be wrapped with the OutputParameter object.
     *  @param statement The callable statement string.
     *  @param parameters The param list to use of INPUT and OUTPUT params. OUTPUT params must be
     *          wrapped in the OutputParameter object.
     */
    public void executeStoredProc(String statement, Object[] parameters) throws SQLException
    {
	Connection connection = null;
        CallableStatement cs = null;
        
        boolean returnValue;
        try {
            connection = getConnection();
            cs = connection.prepareCall(statement);
        
            setParameters(cs, parameters);
        
            cs.executeUpdate();
            
            getOutputParameters(cs, parameters);
	} catch (SQLException ex) {
	    throw ex;
	} finally {
	    try {
		if (cs != null) {
		    cs.close();
		}
	    } catch (SQLException ex) {}

	    try {
		if (connection != null) {
		    connection.close();
		}
	    } catch (SQLException ex) {}
	}
            
    }
    

    /**
     * Executes an update, delete or insert statement.
     *
     * @param sqlStatement a value of type 'String'
     * @param parameters[] a value of type 'Object'
     * @return a value of type 'int' The number of rows affected.
     * @exception SQLException if an error occurs
     */
    public int executeUpdate(String sqlStatement, Object [] parameters) throws SQLException {

	Connection connection = null;
	PreparedStatement statement = null;

	int returnValue = 0;
	try {
	    if ( transacting )
		{
		    // get the connection for handling errors if they occur.
		    // that way we only need one set of error handling code.
		    // connection = trans.getConnection();
		    trans.setParameters(parameters);
		    returnValue = trans.executeUpdate(sqlStatement,parameters);
		    return returnValue;
		}
	    else
		{
		    connection = getConnection();
		    statement = connection.prepareStatement(sqlStatement);
		    
		    setParameters(statement, parameters); 
		    
		    returnValue = statement.executeUpdate();
		    connection.commit();
		    return returnValue;
		}
	} catch (SQLException ex) {
	    try {
		if (connection != null) {
		    connection.rollback();
		}
	    } catch (SQLException e) {}
	    throw ex;
	} finally {
	    try {
		if (statement != null) {
		    statement.close();
		}
	    } catch (SQLException ex) {}

	    try {
		if (connection != null) {
		    connection.close();
		}
	    } catch (SQLException ex) {}
	}
    }

    /**
     * Executes a query.  The return value is a Vector of Hashtables.  Each
     * Hashtable is a row of the result set.  The key for each entry is the name of the
     * column with the value being the actual value of the column.
     *
     * @param sqlStatement a value of type 'String'
     * @param [] a value of type 'Object'
     * @return a value of type 'Vector'
     * @exception SQLException if an error occurs
     */
    public Vector executeQuery(String sqlStatement, Object [] parameters) throws SQLException {

	Connection connection = null;
	PreparedStatement statement = null;
	Vector returnVector = new Vector();
	ResultSet results = null;

	try {
	    connection = getConnection();
	    statement = connection.prepareStatement(sqlStatement);

	    if (parameters != null) {
		setParameters(statement, parameters);
	    }
	    
	    results = statement.executeQuery();

	    while (results.next()) {
		returnVector.addElement(new DBRow(results));
	    }

	    return returnVector;

	} finally {
	    
	    try {
		if (results != null) {
		    results.close();
		}
	    } catch (SQLException ex) {}

	    try {
		if (statement != null) {
		    statement.close();
		}
	    } catch (SQLException ex) {}

	    try {
		if ( transacting )
		    {
			connection = trans.getConnection();
		    } 
		if (connection != null) {
		    connection.close();
		}
	    } catch (SQLException ex) {}
	}
    }

    /**
     * Executes a query.  The return value is a ResultSet containing the rows resulting
     * from the query. 
     *
     * @param sqlStatement a value of type 'String'
     * @param [] a value of type 'Object'
     * @return a value of type 'ResultSet'
     * @exception SQLException if an error occurs
     */
    public ResultSet getResultSet(String sqlStatement, Object [] parameters) throws SQLException {

	PreparedStatement statement = null;
	ResultSet results = null;

	if (this.connection == null) {
		this.connection = getConnection(); // This initializes the instance object
	}

	statement = this.connection.prepareStatement(sqlStatement);

	if (parameters != null) {
		setParameters(statement, parameters);
	}

	results = statement.executeQuery();
		
	return results;

    }

    /**
     * Closes the database connection.  
     *
     * @param None
     * @exception SQLException if an error occurs
     */
    public void closeConnection() throws SQLException {

		if (this.connection != null) {
			this.connection.close();
			this.connection = null;
		}
    }

    /**
     * Gets the first row of a query.
     *
     * @param sqlStatement a value of type 'String'
     * @param parameters [] a value of type 'Object'
     * @return a value of type 'Hashtable'
     * @exception SQLException if an error occurs
     */
    public Hashtable getFirstRow(String sqlStatement, Object [] parameters) throws SQLException {

	Vector results = executeQuery(sqlStatement, parameters);

	if (results.size() == 0) {
	    return null;
	} else {
	    return (Hashtable)results.firstElement();
	}
    }

    /**
     * Gets a single value from a query.
     *
     * @param sqlStatement a value of type 'String'
     * @param [] a value of type 'Object'
     * @param columnWanted a value of type 'String'
     * @return a value of type 'Object'
     * @exception SQLException if an error occurs
     */
    public Object getSingleValue(String sqlStatement, Object [] parameters, String columnWanted) throws SQLException {

	Hashtable result = getFirstRow(sqlStatement, parameters);

	if (result == null) {
	    return null;
	} else {
	    return result.get(columnWanted);
	}
    }

    /**
     * A quick way to see if a row exists from a SQL Statement.
     *
     * @param sqlStatement a value of type 'String'
     * @param parameters[] a value of type 'Object'
     * @return a value of type 'boolean'
     * @exception SQLException if an error occurs
     */
    public boolean rowExists(String sqlStatement, Object [] parameters) throws SQLException {

	if (getFirstRow(sqlStatement, parameters) != null) {
	    return true;
	} else {
	    return false;
	}
    }
    


    private class Transaction
    {
	private Connection conn;
	private boolean finalized;
	private PreparedStatement stmt;

	Transaction(Connection c)
	{
	    conn = c;
	    finalized = false;
            try {
	        conn.setAutoCommit(false);
            } catch (Exception e) {
                // we'll just catch this again in executeUpdate
            }
	}
	
	protected void beginTransaction()
	{
	}
	
	protected int executeUpdate(String query,Object[] params)
	    throws SQLException
	{

	    int retval;
	    
	    stmt = conn.prepareStatement(query);
	    conn.setAutoCommit(false);
	    this.setParameters(stmt, params); 
	    retval = stmt.executeUpdate();
            stmt.close();
	    return retval;
	}
	
	protected void setParameters(Object[] params)
	{
	}
	
	protected void commitTransaction()
	    throws SQLException
	{
	    conn.commit();
	    finalized = true;
	}
	
	protected void rollbackTransaction()
	    throws SQLException
	{
	    conn.rollback();
	    finalized = true;
	}
	
	protected void close()
	    throws SQLException
	{
	    conn.close();
	    if ( !finalized )
		{
		    conn.close();
		    throw new SQLException("The transaction has not been committed or rolled back");
		}
	}


	protected void setParameter(PreparedStatement statement, Object parameter, int index) throws SQLException {
	    
	    if (parameter instanceof Null) {
		statement.setNull(index, ((Null)parameter).getType());
	    } else if (parameter instanceof OutputParameter) {            
		statement.setObject(index, ((OutputParameter) parameter).getObject());
		((CallableStatement) statement).registerOutParameter(
								     index, ((OutputParameter) parameter).getType());
	    } else {
		statement.setObject(index, parameter);
	    }
	}
	
	
	/**
	 * Sets the parameters for the prepared statement passed.
	 *
	 * @param statement a value of type 'PreparedStatement'
	 * @param [] a value of type 'Object'
	 * @exception SQLException if an error occurs
	 */
	protected void setParameters(PreparedStatement statement, Object [] parameters) throws SQLException {
	    
	    for (int index = 0; index < parameters.length; ++index) {
		this.setParameter(statement, parameters[index], index + 1);
	    }
	}

	protected Connection getConnection()
	{
	    return conn;
	}
    } // End, inner class Transaction

} // End, class DBEngine









