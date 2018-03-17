// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/IMJavaLog.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util; // Generated package name

import java.io.*;
import java.util.*;

/**
 * IMJavaLog.java
 * This class will establish a single Instance of a java log for a given IM.  Do not instantiate this 
 * class.  Instead use its static methods.
 *
 * Created: Wed Oct 11 11:40:27 2000
 *
 * @author Bruce Haugland
 * @version 1.0
 */

public class IMJavaLog  {

    private static final String PROPERTY_FILE = "hockeystats_util_IMJavaLogProps";

    private static JavaLog log = null;
    private static ResourceBundle properties = null;

    /**
     * Gets the java log this class is working with.
     *
     * @return a value of type 'JavaLog'
     */
    protected static JavaLog getLog() {
	
	if (log == null) {
	    log = new JavaLog(getProperties().getString("FileName"), getProperties().getString("logLevel"));
	}

	return log;
    }

    /**
     * Gets the properties for this log.
     *
     * @return a value of type 'ResourceBundle'
     */
    protected static ResourceBundle getProperties() {

	try {
	    if (properties == null) {
		properties = ResourceBundle.getBundle(PROPERTY_FILE);
	    }
	} catch (MissingResourceException ex) {
	}

	return properties;
    }
    /**
     * Logs critical Messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public static void logCritical(String message) throws IOException {

	getLog().logCritical(message);
    }

    /**
     * Logs critical messages with a stack dump.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public static void logCritical(String message, Throwable ex) {
	try {
	    getLog().logCritical(message, ex);
	} catch (IOException e) {}
    }

    /**
     * Logs error messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public static void logError(String message) {
	try {
	    getLog().logError(message);
	} catch (IOException e) {}
    }

    /**
     * Describe 'logError' method here.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public static void logError(String message, Throwable ex) {
	try {
	    getLog().logError(message, ex);
	} catch (IOException e) {}
    }

    /**
     * Logs warning messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public static void logWarning(String message) {
	try {
	    getLog().logWarning(message);
	} catch (IOException e) {}
    }
    
    /**
     * Logs warning messages with a stack trace.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public static void logWarning(String message, Throwable ex) {
	try {
	    getLog().logWarning(message, ex);
	} catch (IOException e) {}
    }

    /**
     * Logs info messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public static void logInfo(String message) {
	try {
	    getLog().logInfo(message);
	} catch (IOException e) {}
    }

    /**
     * Logs info messages with a stack trace.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public static void logInfo(String message, Throwable ex) {
	try {
	    getLog().logInfo(message, ex);
	} catch (IOException e) {}
    }

    /**
     * Logs Verbose messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public static void logVerbose(String message) {
	try {
	    getLog().logVerbose(message);
	} catch (IOException e) {}
    }

    
    /**
     * Logs Verbose messages with a stack trace.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public static void logVerbose(String message, Throwable ex) {
	try {
	    getLog().logVerbose(message, ex);
	} catch (IOException e) {}
    }

    

    /**
     * Logs Debug messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public static void logDebug(String message) {
	try {
	    getLog().logDebug(message);
	} catch (IOException e) {}
    }

    /**
     * Logs Debug Messages wtih an exception.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public static void logDebug(String message, Throwable ex) {
	try {
	    getLog().logDebug(message, ex);
	} catch (IOException e) {}
    }

    /**
     * Will close the log file.
     *
     */
    public static void close() {
	getLog().close();
    }

    
    
} // IMJavaLog









