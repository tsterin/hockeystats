// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/db/RMIThread.java,v 1.1 2002/09/15 01:53:23 tom Exp $
package hockeystats.util.db;

import java.rmi.*;
import java.util.*;
import java.io.*;
import java.rmi.registry.*;


/** This class simply starts up an instance of rmiregistry... 
 *  I tried using the LocateRegistry class, but couldn't start more than one instance..
 *  if you can find a better way to do it, please change this code.
 **/
public class RMIThread extends Thread {

    private int port;
    private Process proc;
    private Registry reg;
    //private String rmicmd = "/home/scohen/projects/rel70/java/hockeystats/util/db/rmi.sh";
    private String rmicmd = "rmiregistry";

    public RMIThread(int port) {
	this.port = port;
    }

    public void run() {
	Runtime r = Runtime.getRuntime();
	try {
	    proc = r.exec(rmicmd + " " + port);
	    Thread.sleep(1000); //this sleep allows for the registry to start properly.
	} catch (InterruptedException inter ) {
	} catch (IOException io ) {
	    System.out.println("could not execute" + io);
	}
	
    }

    public void printStream(String msg, InputStream out) throws IOException{
	int i;
	System.out.println(msg);
	while ((i = out.read()) >= 0 ) {
	    System.out.print((char)i);
	}
    }
}
