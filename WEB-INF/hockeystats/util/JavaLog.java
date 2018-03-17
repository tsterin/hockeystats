// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/JavaLog.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util; // Generated package name

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * JavaLog.java
 * This represents a log where java processes can log to.
 *
 * Created: Wed Oct 11 09:49:16 2000
 *
 * @author Bruce Haugland
 * @version 1.0
 */

public class JavaLog  {
    
    /**
     * The file name of this java log
     */
    private String fileName;


    /**
     * The level of logging
     */
    private String logLevel;
    
    /**
     * The format string for reporting errors.
     */
    private static final String LOG_STRING = "{0}\t{1}\t{2}\t{3}";

    // levels of reporting
    private static final String DEBUG = "debug";
    private static final String VERBOSE = "verbose";
    private static final String INFO = "info";
    private static final String WARNING = "warning";
    private static final String ERROR = "error";
    private static final String CRITICAL = "critical";

    
    private static final int DEBUG_LEVEL = 0;
    private static final int VERBOSE_LEVEL = 1;
    private static final int INFO_LEVEL = 2;
    private static final int WARNING_LEVEL = 3;
    private static final int ERROR_LEVEL = 4;
    private static final int CRITICAL_LEVEL = 5;

    /**
     * The print writer for the log file
     */
    private PrintWriter logFile = null;

    /**
     * The properties bundle for the log
     */
    private ResourceBundle properties = null;

    /**
     * Hash table to detail the levels
     */
    private Hashtable levels = null;

    /**
     * Constructor for JavaLog.
     *
     */
    public JavaLog(String aFileName, String aLogLevel) {

	fileName = aFileName;
	logLevel = aLogLevel;
	
    }
    

    
    /**
     * Gives the hash table that contains the level name to the level number mapping.
     *
     * @return a value of type 'Hashtable'
     */
    protected synchronized Hashtable getLevels() {

	if (levels == null) {
	    levels = new Hashtable();
	    levels.put(DEBUG, new Integer(DEBUG_LEVEL));
	    levels.put(VERBOSE, new Integer(VERBOSE_LEVEL));
	    levels.put(INFO, new Integer(INFO_LEVEL));
	    levels.put(WARNING, new Integer(WARNING_LEVEL));
	    levels.put(ERROR, new Integer(ERROR_LEVEL));
	    levels.put(CRITICAL, new Integer(CRITICAL_LEVEL));
	}

	return levels;
    }

    /**
     * Will get the level number for the given level mapping string.
     * If the string passed does not exist then the level returned will be -1.
     *
     * @param level a value of type 'String'
     * @return a value of type 'int'
     */
    protected int getLevel(String level) {

	Integer iLevel = (Integer)getLevels().get(level);
	if (iLevel == null) {
	    return -1;
	} else {
	    return iLevel.intValue();
	}

    }



    /**
     * Gets the print writer for the log.  If one does not exist then one is created.
     *
     * @return a value of type 'PrintWriter'
     * @exception IOException if an error occurs
     */
    protected synchronized PrintWriter getWriter() throws IOException {
	
	if (logFile == null) {
	    logFile = new PrintWriter(new FileWriter(fileName, true), true);
	}

	return logFile;
    }


    /**
     * The main logging function for JavaLog.  It will print a stack trace if ex is not null.
     *
     * @param level a value of type 'int'.  This is the level that this message is for.
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    protected void log(int level, String message, Throwable ex) throws IOException {

	Object [] params;

	PrintWriter writer = getWriter();

	synchronized(writer) {
	    
	    if (doWrite(level)) {
		params = new Object [4];
		Date now = new Date();
		params[0] = now.toString();				// date/time
		params[1] = new Long( now.getTime() );	// milliseconds since epoch
		params[2] = new Integer(level);
		params[3] = message == null ? "null" : message;
		writer.println(MessageFormat.format(LOG_STRING, params));
		
		if (ex != null) {
		    ex.printStackTrace(writer);
		}
	    }
	}
    }

    /**
     * Checks to see if this message should be logged given the debug level.
     *
     * @param level a value of type 'int'
     * @return a value of type 'boolean'
     */
    protected boolean doWrite(int level) {
	
	return level <= getLevel(logLevel);
    }

    /**
     * Logs critical Messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public void logCritical(String message) throws IOException {

	log(CRITICAL_LEVEL, message, null);
    }

    /**
     * Logs critical messages with a stack dump.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public void logCritical(String message, Throwable ex) throws IOException {
	log(CRITICAL_LEVEL, message, ex);
    }

    /**
     * Logs error messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public void logError(String message) throws IOException {
	log(ERROR_LEVEL, message, null);
    }

    /**
     * Describe 'logError' method here.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public void logError(String message, Throwable ex) throws IOException {
	log(ERROR_LEVEL, message, ex);
    }

    /**
     * Logs warning messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public void logWarning(String message) throws IOException {
	log(WARNING_LEVEL, message, null);
    }
    
    /**
     * Logs warning messages with a stack trace.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public void logWarning(String message, Throwable ex) throws IOException {
	log(WARNING_LEVEL, message, ex);
    }

    /**
     * Logs info messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public void logInfo(String message) throws IOException {
	log(INFO_LEVEL, message, null);
    }

    /**
     * Logs info messages with a stack trace.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public void logInfo(String message, Throwable ex) throws IOException {
	log(INFO_LEVEL, message, ex);
    }

    /**
     * Logs Verbose messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public void logVerbose(String message) throws IOException {
	log(VERBOSE_LEVEL, message, null);
    }

    
    /**
     * Logs Verbose messages with a stack trace.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public void logVerbose(String message, Throwable ex) throws IOException {
	log(VERBOSE_LEVEL, message, ex);
    }

    

    /**
     * Logs Debug messages.
     *
     * @param message a value of type 'String'
     * @exception IOException if an error occurs
     */
    public void logDebug(String message) throws IOException {
	log(DEBUG_LEVEL, message, null);
    }

    /**
     * Logs Debug Messages wtih an exception.
     *
     * @param message a value of type 'String'
     * @param ex a value of type 'Throwable'
     * @exception IOException if an error occurs
     */
    public void logDebug(String message, Throwable ex) throws IOException {
	log(DEBUG_LEVEL, message, ex);
    }

    /**
     * Will close the log file.
     *
     */
    public synchronized void close() {
	synchronized(logFile) {

	    if (logFile != null) {
		
		logFile.close();
	    }
	    logFile = null;
	}
    }
  
	
    
    
} // JavaLog



