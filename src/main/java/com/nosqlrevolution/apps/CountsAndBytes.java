package com.nosqlrevolution.apps;

/**
 *
 * @author cbrown
 */
public class CountsAndBytes {
    private int counts;
    private long bytes;

    public CountsAndBytes(int counts, long bytes) {
        this.counts = counts;
        this.bytes = bytes;
    }
    
    public int getCounts() {
        return counts;
    }

    public void setCounts(int counts) {
        this.counts = counts;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }
}
