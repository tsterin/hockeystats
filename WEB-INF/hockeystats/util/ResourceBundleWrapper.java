package hockeystats.util;

import java.util.ResourceBundle;


public class ResourceBundleWrapper
{
    private ResourceBundle props = null;

    public ResourceBundleWrapper(String propFile)
    {
        this.props = ResourceBundle.getBundle(propFile);
    }


    public String getString(String prop)
    {
        return props.getString(prop);
    }

}