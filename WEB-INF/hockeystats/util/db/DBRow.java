// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/db/DBRow.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util.db; // Generated package name

import java.util.*;
import java.sql.*;



/**
 * DBRow.java
 *<br>
 * Used to represent a row in the database
 *
 * Created: Wed Sep  6 08:26:20 2000
 *
 * @version
 */

public class DBRow extends Hashtable {
    

    
    /**
     * Describe constructor here.
     *
     * @param rSet a value of type 'ResultSet'
     */

    public DBRow(Hashtable hashT) {
       super(hashT);  
    }

    public DBRow(ResultSet rSet) throws SQLException {

	super();

	ResultSetMetaData rSetMeta = null;  // result meta data object.
	int		  colCount = 0;	    // number of columns in result set
	int		  curCol = 0;	    // current column offset
	int		  colType;	    // current column type
	String  	  colName = null;   // name of the column
	Object		  colValue = null;  // value assoicated with colName
	

	rSetMeta = rSet.getMetaData();
	colCount = rSetMeta.getColumnCount();
	

	/*
	 * Build a hash of columns for this row only.
	 */
	for (curCol = 1; curCol <= colCount; curCol++) {
	    colName = rSetMeta.getColumnName(curCol);
	    colValue = rSet.getObject(colName);
	    colType = rSetMeta.getColumnType(curCol);
	    
	    colValue = (colValue == null) ? new Null(colType) : colValue;
	    put(colName, colValue);
	}
		

    } 


    
} // DBRow




