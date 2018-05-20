package com.nosqlrevolution.apps;

import org.kohsuke.args4j.Option;

/**
 *
 * @author cbrown
 */
public class ExportOptions {
    @Option(name="-h",usage="hostname of Elasticsearch server")
    private String hostname = "localhost";
    
    @Option(name="-c",usage="name of Elasticsearch cluster")
    private String clustername = "elasticsearch";
    
    @Option(name="-i",usage="index to use")
    private String index = null;
    
    @Option(name="-t",usage="type to use")
    private String type = null;

    @Option(name="-f",usage="name of output file")
    private String outfilename = "out";
    
    @Option(name="-g",usage="gzip the output file")
    private boolean gzip = false;

    @Option(name="-b",usage="size of data blocks for processing")
    private int blockSize = 10000;

    @Option(name="-m",usage="use ExportModel to export data\nthis is useful for exporting documents that do not have an integral id field")
    private boolean modelMode = false;

    @Option(name="-l",usage="limit to the first N documents")
    private int limit = -1;

    @Option(name="-s",usage="keep only every Nth document")
    private int sample = -1;

    @Option(name="-n",usage="connect to cluster as node instead of as transport client")
    private boolean node = false;

    // For Elastic service
    @Option(name="-e",usage="connect to elastic service cluster")
    private boolean elastic = false;

    @Option(name="-su",usage="shield username")
    private String shieldUsername = null;

    @Option(name="-sp",usage="shield password")
    private String shieldPassword = null;

    @Option(name="-eid",usage="elastic clusterId for Elastic")
    private String elasticClusterId = null;

    @Option(name="-ere",usage="elastic region for Elastic")
    private String elasticRegion = null;

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

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public boolean isModelMode() {
        return modelMode;
    }

    public void setModelMode(boolean modelMode) {
        this.modelMode = modelMode;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSample() {
        return sample;
    }

    public void setSample(int sample) {
        this.sample = sample;
    }

    public boolean isNode() {
        return node;
    }

    public void setNode(boolean node) {
        this.node = node;
    }

    public boolean isElastic() {
        return elastic;
    }

    public void setElastic(boolean elastic) {
        this.elastic = elastic;
    }

    public String getShieldUsername() {
        return shieldUsername;
    }

    public void setShieldUsername(String shieldUsername) {
        this.shieldUsername = shieldUsername;
    }

    public String getShieldPassword() {
        return shieldPassword;
    }

    public void setShieldPassword(String shieldPassword) {
        this.shieldPassword = shieldPassword;
    }

    public String getElasticClusterId() {
        return elasticClusterId;
    }

    public void setElasticClusterId(String elasticClusterId) {
        this.elasticClusterId = elasticClusterId;
    }

    public String getElasticRegion() {
        return elasticRegion;
    }

    public void setElasticRegion(String elasticRegion) {
        this.elasticRegion = elasticRegion;
    }
}
