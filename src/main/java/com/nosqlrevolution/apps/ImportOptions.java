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

    @Option(name="-l",usage="limit to the first N documents")
    private int limit = -1;

    @Option(name="-s",usage="keep only every Nth document")
    private int sample = -1;

    @Option(name="-n",usage="connect to cluster as node instead of as transport client")
    private boolean node = false;

    @Option(name="-id",usage="name of id field")
    private String idField = null;

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

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
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
