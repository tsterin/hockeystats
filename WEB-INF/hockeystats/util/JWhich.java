// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/JWhich.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util;


/**
 * Prints the absolute pathname of the class file
 * containing the specified class name, as prescribed
 * by the current classpath.
 *
 * @param className Name of the class.
 */
public class JWhich 
{


    public static void which(String className) 
    {
        if (!className.startsWith("/")) {
            className = "/" + className;
        }
        className = className.replace('.', '/');
        className = className + ".class";

        java.net.URL classUrl = new JWhich().getClass().getResource(className);

        if (classUrl != null) {
            System.out.println("\nClass '" + className +
                    "' found in \n'" + classUrl.getFile() + "'");
        } else {
            System.out.println("\nClass '" + className +
                    "' not found in \n'" +
            System.getProperty("java.class.path") + "'");
        }
    }


    public static void main(String args[]) 
    {
        if (args.length > 0) {
            JWhich.which(args[0]);
        } else {
            System.err.println("Usage: java JWhich <classname>");
        }
    }


}

