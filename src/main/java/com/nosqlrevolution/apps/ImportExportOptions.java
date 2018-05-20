package com.nosqlrevolution.apps;

import org.kohsuke.args4j.Option;

/**
 *
 * @author cbrown
 */
public class ImportExportOptions {
    @Option(name="-if",usage="name of input file")
    private String infilename = null;

    @Option(name="-th",usage="number of threads for processing")
    private Integer threads = 2;

    @Option(name="-b",usage="size of data blocks for processing")
    private int blockSize = 1000;

    @Option(name="-s",usage="keep only every Nth document")
    private int sample = -1;

    @Option(name="-l",usage="limit to the first N documents")
    private int limit = -1;

    @Option(name="-of",usage="name of output file")
    private String outfilename = "out";
    
    @Option(name="-g",usage="gzip the output file")
    private boolean gzip = false;

    public String getInfilename() {
        return infilename;
    }

    public void setInfilename(String infilename) {
        this.infilename = infilename;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getOutfilename() {
        if (isGzip()) {
            if (! outfilename.endsWith(".gz") && ! outfilename.endsWith(".gzip")) {
                return outfilename + ".gz";
            }
        }
        
        return outfilename;
    }

    public void setOutfilename(String outfilename) {
        this.outfilename = outfilename;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public int getSample() {
        return sample;
    }

    public void setSample(int sample) {
        this.sample = sample;
    }
}
