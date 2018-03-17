// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/RegularExpressions.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util;

import java.util.Vector;

/**
 *
 * @author Craig Last
 */
public class RegularExpressions
{
    /** Creates new RegularExpressions */
    private RegularExpressions()
    {
    }
    
    public static boolean verifyNotInString(String string, String regularExpression)
    {
        Vector instances = verifyNotInString("", string, regularExpression);
        
        if (instances.isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static Vector verifyNotInString(String message, String string, String regularExpression)
    {
        Vector instances = new Vector();
        int pos = -1;

        for (int i=0;i<regularExpression.length();i++)
        {
            while ((pos = string.indexOf((int) regularExpression.charAt(i), pos+1)) != -1)
            {
                instances.add(message + " instance of '" + regularExpression.charAt(i)
                    + "' at position " + (pos+1));
            }
        }
        
        return instances;
    }

    public static boolean verifyInString(String string, String regularExpression)
    {
        Vector instances = verifyInString("", string, regularExpression);
        
        if (instances.isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static Vector verifyInString(String message, String string, String regularExpression)
    {
        Vector instances = new Vector();
        int pos = -1;
        boolean flag;

        for (int i=0;i<string.length();i++)
        {
            if (regularExpression.indexOf((int) string.charAt(i)) == -1)
            {
                instances.add(message + " instance of '" + string.charAt(i)
                    + "' at position " + (i+1));
            }
        }
        
        return instances;
    }
}
