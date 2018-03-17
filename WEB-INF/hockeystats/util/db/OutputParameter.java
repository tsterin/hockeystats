// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/db/OutputParameter.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util.db;


/** This class is used to differentiate output parameters from input parameters for prepared statements.
 *          It is used by DBEngine.
 */
public class OutputParameter  
{
    private Object obj = null;
    private int objType;
    
    
    /** Constructor for a simple container. Given the parameter and the type of the parameter.
     *  @param obj The parameter.
     *  @param objType The type of the parameter. Constant found in java.sql.Types.
     */
    public OutputParameter(Object obj, int objType) 
    {
        this.obj = obj;
        this.objType = objType;
    }
    

    /** Accessor for the parameter object passed in the construtor.
     *  @return The parameter object.
     */
    public Object getObject() 
    {
	return obj;
    }
    
    
    /** The type of the parameter object passed in the constructor.
     *  @return The type of the parameter. Constant found in java.sql.Types.
     */
    public int getType()
    {
        return objType;
    }

    
}




