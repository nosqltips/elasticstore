package com.nosqlrevolution.apps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Take a file input and break the file into blocks for processing.
 * 
 * @author cbrown
 */
public abstract class AbstractBlocker implements Callable<Integer> {
    protected int totalCount;
    private List<String> buffer = null;
    private List<String> secondBuffer = new ArrayList<>();
    private final Object synchronizer = new Object();
    private final int blockSize;
    private boolean done = false;

    public AbstractBlocker(int blocksize) {
        this.blockSize = blocksize;
    }
    
    /**
     * This is the main implementation for the buffering algorithm.
     * Null means no more data and drain existing buffers.
     * 
     * @param s 
     * @throws java.lang.InterruptedException 
     */
    protected void bufferNext(String s) throws InterruptedException {
        if (s != null) {
            secondBuffer.add(s);
            totalCount ++;
            
            if (secondBuffer.size() >= blockSize) {
                synchronized(synchronizer) {
                    // Both buffers are full, so we can wait for a pickup.
                    if (buffer != null) {
                        synchronizer.notifyAll();
                        synchronizer.wait();
                    }
                    
                    if (buffer == null) {
                        buffer = secondBuffer;
                        secondBuffer = new ArrayList<>();
                        synchronizer.notifyAll();
                    }
                }
            }
        } else {

            // Wait for the pools to be drained
            synchronized(synchronizer) {
                if (buffer != null) {
                    synchronizer.notifyAll();
                    synchronizer.wait();
                }

                if (secondBuffer.size() > 0) {
                    buffer = secondBuffer;
                    synchronizer.notifyAll();
                    synchronizer.wait();
                } else {
                    synchronizer.notifyAll();
                }

                done = true;
            }
        }
    }
    
    /**
     * Return available data or wait till ready.
     * Null return means no more data.
     * 
     * @return 
     */
    public List<String> getNext() {
        synchronized(synchronizer) {
            // Returning null signals that we're all done.
            if (done) {
                return null;
            }
        
            // If there is no data ready, then just wait until notified.
            if (buffer == null) {
                try {
                    synchronizer.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(AbstractBlocker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            // Copy the data reference then null to continue.
            List<String> returnBuffer = buffer;
            buffer = null;
            synchronizer.notifyAll();
            
            return returnBuffer;
        }
    }
    
    /**
     * Close all of the opened resources.
     * 
     * @throws IOException 
     */
    public abstract void shudown() throws IOException;
}