package com.nosqlrevolution.apps;

import org.kohsuke.args4j.Option;

/**
 *
 * @author cbrown
 */
public class ImportOptions {
    @Option(name="-h",usage="hostname of Elasticsearch server")
    private String hostname = "localhost";
    
    @Option(name="-c",usage="name of Elasticsearch cluster")
    private String clustername = "elasticsearch";
    
    @Option(name="-i",usage="index to use")
    private String index = null;
    
    @Option(name="-t",usage="type to use")
    private String type = null;

    @Option(name="-f",usage="name of input file")
    private String infilename = null;

    @Option(name="-th",usage="number of threads for processing")
    private Integer threads = 2;

    @Option(name="-b",usage="size of data blocks for processing")
    private int blockSize = 1000;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getClustername() {
        return clustername;
    }

    public void setClustername(String clustername) {
        this.clustername = clustername;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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
}
