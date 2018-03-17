
package hockeystats.util;

import java.util.*;

public class OMXProperties extends java.util.Properties
{
    protected String filename;

    public OMXProperties()
    {
	super();
    }

    public void setFilename(String name){
	filename = name;
    }
    
    public String getFilename() {
	return filename;
    }

    public OMXProperties(Properties p)
    {
	super(p);
    }

    public String getProperty(String propName)
	throws MissingResourceException
    {
	String propVal;
	propVal = super.getProperty(propName);
	if (propVal == null )
	    {
		throw new MissingResourceException("Could not find property " + propName,
						   "OMXProperties",propName);
	    } 
	else
	    return propVal;
    }

}
