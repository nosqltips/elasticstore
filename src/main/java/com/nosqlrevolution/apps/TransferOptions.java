package com.nosqlrevolution.apps;

import org.kohsuke.args4j.Option;

/**
 *
 * @author cbrown
 */
public class TransferOptions {
    @Option(name="-sh",usage="source hostname of Elasticsearch server")
    private String sourceHostname = "localhost";
    
    @Option(name="-sc",usage="source name of Elasticsearch cluster")
    private String sourceClustername = "elasticsearch";
    
    @Option(name="-si",usage="source index to use")
    private String sourceIndex = null;
    
    @Option(name="-st",usage="source type to use")
    private String sourceType = null;

    @Option(name="-dh",usage="destination hostname of Elasticsearch server")
    private String destHostname = "localhost";
    
    @Option(name="-dc",usage="destination name of Elasticsearch cluster")
    private String destClustername = "elasticsearch";
    
    @Option(name="-di",usage="destination index to use")
    private String destIndex =  null;
    
    @Option(name="-dt",usage="destination type to use")
    private String destType = null;

    @Option(name="-th",usage="number of threads for processing")
    private Integer threads = 2;

    @Option(name="-b",usage="size of data blocks for processing")
    private int blockSize = 1000;

    public String getSourceHostname() {
        return sourceHostname;
    }

    public void setSourceHostname(String sourceHostname) {
        this.sourceHostname = sourceHostname;
    }

    public String getSourceClustername() {
        return sourceClustername;
    }

    public void setSourceClustername(String sourceClustername) {
        this.sourceClustername = sourceClustername;
    }

    public String getSourceIndex() {
        return sourceIndex;
    }

    public void setSourceIndex(String sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getDestHostname() {
        return destHostname;
    }

    public void setDestHostname(String destHostname) {
        this.destHostname = destHostname;
    }

    public String getDestClustername() {
        return destClustername;
    }

    public void setDestClustername(String destClustername) {
        this.destClustername = destClustername;
    }

    public String getDestIndex() {
        return destIndex;
    }

    public void setDestIndex(String descIndex) {
        this.destIndex = descIndex;
    }

    public String getDestType() {
        return destType;
    }

    public void setDestType(String destType) {
        this.destType = destType;
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
