// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/db/Null.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util.db; // Generated package name

/**
 * Null.java
 * < represents an SQL null value
 *
 * Created: Tue Sep 12 11:29:38 2000
 *
 *
 * @version 1.0
 */

public class Null  {

    private int   type;  // column type

    /**
     * Constructor for Null.
     *
     * @param colType a value of type 'int'
     */
    public Null(int colType) {
	type = colType;
    }
    

    /**
     * Gets the type of the Null.  See java.sql.Types for the values.
     *
     * @return a value of type 'int'
     */
    public int getType() {
	return type;
    }

} // Null




