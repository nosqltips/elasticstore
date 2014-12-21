package com.nosqlrevolution.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Take a file input and break the file into blocks for processing.
 * 
 * @author cbrown
 */
public class FileBlocker implements Callable<Integer> {
    private final String filename;
    private int totalCount;
    private final File inFile;
    private final FileInputStream fis;
    private final GZIPInputStream gis;
    private final Reader reader;
    private final BufferedReader bufReader;
    private List<String> buffer = null;
    private List<String> secondBuffer = new ArrayList<>();
    private final Object synchronizer = new Object();
    private final int blockSize;
    private boolean done = false;
    
    /**
     * Take a file input and break the file into blocks for processing.
     * 
     * @param file
     * @param blockSize
     * @throws IOException 
     */
    public FileBlocker(String file, int blockSize) throws IOException {
        this.blockSize = blockSize;
        this.filename = file;
        inFile = new File(filename);
        fis = new FileInputStream(inFile);
        if (filename.endsWith(".gz") || filename.endsWith("gzip")) {
            gis = new GZIPInputStream(fis);
            reader = new InputStreamReader(gis);
        } else {
            gis = null;
            reader = new InputStreamReader(fis);
        }
        
        bufReader = new BufferedReader(reader);
    }
    
    /**
     * Break the file contents into blocks and return total number of documents when finished.
     * 
     * @return
     * @throws Exception 
     */
    @Override    
    public Integer call() throws Exception {
        String s;
        while ((s = bufReader.readLine()) != null) {
            if (s != null && ! s.isEmpty()) {
                secondBuffer.add(s);
                totalCount ++;
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
        }

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
        
        return totalCount;
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
                    Logger.getLogger(FileBlocker.class.getName()).log(Level.SEVERE, null, ex);
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
    public void shudown() throws IOException {
        bufReader.close();
        reader.close();
        if (gis != null) {
            gis.close();
        }
        fis.close();
    }
}