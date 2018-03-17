// $Header: /u02/cvsroot/hockeystats/WEB-INF/hockeystats/util/Queue.java,v 1.1 2002/09/15 01:53:23 tom Exp $

package hockeystats.util;


import java.util.Vector;


public class Queue
{
    private Vector queue;
    
    
    public Queue()
    {
        queue = new Vector();
    }
    
    
    public synchronized void enqueue(Object o)
    {
        queue.add(o);
    }
    
    
    public synchronized Object dequeue()
    {
        if (size() > 0) {
            Object o = queue.get(0);
            queue.remove(0);
            return o;
        } else {
            return null;
        }
    }
    
    
    public synchronized Object peek()
    {
        if (size() > 0) {
            return queue.get(0);
        } else {
            return null;
        }
    }
    
    
    public synchronized int size()
    {
        return queue.size();
    }
    
}