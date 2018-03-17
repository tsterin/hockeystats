// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/BVHashtable.java,v 1.1 2002/09/15 01:53:23 tom Exp $

/*
 * BVHashtable.java
 *
 * Created on February 1, 2001, 5:45 PM
 */

package hockeystats.util;

import java.util.Hashtable;

/**
 * This is a wrapper class of the Hashtable class for usage in javascript with
 * BroadVision.  Javascript does not like the generic Object cast type.
 * @author Craig Last
 */
public class BVHashtable extends Hashtable
{
    /** Creates new BVHashtable by creating an instance of it's base class */
    public BVHashtable()
    {
        super();
    }

    /** Creates new BVHashtable by creating an instance of it's base class */
    public BVHashtable(Hashtable hashtable)
    {
        super(hashtable);
    }
    
    public void putString(String key, String value)
    {
        super.put(key, value);
    }
    
    public String getString(String key)
    {
        return (String) super.get(key);
    }

    public void putInt(String key, int value)
    {
        super.put(key, new Integer(value));
    }
    
    public int getInt(String key)
    {
        return ((Integer) super.get(key)).intValue();
    }
    
    public void putBVHashtable(String key, BVHashtable value)
    {
        super.put(key, value);
    }
    
    public BVHashtable getBVHashtable(String key)
    {
        return (BVHashtable) super.get(key);
    }
}
