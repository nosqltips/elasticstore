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
    private boolean done = false;
    
    private final int blockSize;
    private final int limit;
    private final int sample;
    private int sampleCount = 0;

    public AbstractBlocker(int blocksize, int limit, int sample) {
        this.blockSize = blocksize;
        this.limit = limit;
        this.sample = sample;
    }
    
    /**
     * This is the main implementation for the buffering algorithm.
     * Null means no more data and drain existing buffers.
     * 
     * True return value means reached limit.
     * 
     * @param s 
     * @return  
     * @throws java.lang.InterruptedException 
     */
    protected boolean bufferNext(String s) throws InterruptedException {
        if (s != null) {
            // Take the sample rate into account. -1 means no sampling.
            if (sample <= 0 || (sample > 0 && sampleCount >= sample)) {
                secondBuffer.add(s);
                totalCount ++;
                sampleCount ++;
            }
            
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
            
            // Check to see if we've hit the limit of the number of documents to return.
            if (limit > 0 && totalCount >= limit) {
                return false;
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
        
        // True means continue sending data.
        return true;
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
     * Return the total number of expected documents for calculations.
     * 
     * @return 
     */
    public abstract long getTotalDocs();
    
    /**
     * Close all of the opened resources.
     * 
     * @throws IOException 
     */
    public abstract void shudown() throws IOException;
}