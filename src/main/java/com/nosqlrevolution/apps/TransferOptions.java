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

    @Option(name="-sn",usage="connect to source cluster as node instead of as transport client")
    private boolean sourceNode = false;

    @Option(name="-dh",usage="destination hostname of Elasticsearch server")
    private String destHostname = "localhost";
    
    @Option(name="-dc",usage="destination name of Elasticsearch cluster")
    private String destClustername = "elasticsearch";
    
    @Option(name="-di",usage="destination index to use")
    private String destIndex =  null;
    
    @Option(name="-dt",usage="destination type to use")
    private String destType = null;

    @Option(name="-dn",usage="connect to destination cluster as node instead of as transport client")
    private boolean destNode = false;

    @Option(name="-th",usage="number of threads for processing")
    private Integer threads = 2;

    @Option(name="-b",usage="size of data blocks for processing")
    private int blockSize = 1000;

    @Option(name="-l",usage="limit to the first N documents")
    private int limit = -1;

    @Option(name="-s",usage="keep only every Nth document")
    private int sample = -1;

    @Option(name="-id",usage="name of id field")
    private String idField = null;

    @Option(name="-f",usage="name of id field")
    private String field = null;

    // For Elastic service
    @Option(name="-se",usage="connect to source elastic service cluster")
    private boolean sourceElastic = false;

    @Option(name="-su",usage="source shield username")
    private String sourceUsername = null;

    @Option(name="-sp",usage="source shield password")
    private String sourcePassword = null;

    @Option(name="-sid",usage="source clusterId for Elastic")
    private String sourceClusterId = null;

    @Option(name="-sre",usage="source region for Elastic")
    private String sourceRegion = null;
    
    @Option(name="-de",usage="connect to destination elastic service cluster")
    private boolean destElastic = false;

    @Option(name="-du",usage="destination shield username")
    private String destUsername = null;

    @Option(name="-dp",usage="destination shield password")
    private String destPassword = null;

    @Option(name="-did",usage="destination clusterId for Elastic")
    private String destClusterId = null;

    @Option(name="-dre",usage="destination region for Elastic")
    private String destRegion = null;
    
    
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

    public boolean isSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(boolean sourceNode) {
        this.sourceNode = sourceNode;
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

    public boolean isDestNode() {
        return destNode;
    }

    public void setDestNode(boolean destNode) {
        this.destNode = destNode;
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

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getSourceUsername() {
        return sourceUsername;
    }

    public void setSourceUsername(String sourceUsername) {
        this.sourceUsername = sourceUsername;
    }

    public String getSourcePassword() {
        return sourcePassword;
    }

    public void setSourcePassword(String sourcePassword) {
        this.sourcePassword = sourcePassword;
    }

    public String getSourceClusterId() {
        return sourceClusterId;
    }

    public void setSourceClusterId(String sourceClusterId) {
        this.sourceClusterId = sourceClusterId;
    }

    public String getSourceRegion() {
        return sourceRegion;
    }

    public void setSourceRegion(String sourceRegion) {
        this.sourceRegion = sourceRegion;
    }

    public String getDestUsername() {
        return destUsername;
    }

    public void setDestUsername(String destUsername) {
        this.destUsername = destUsername;
    }

    public String getDestPassword() {
        return destPassword;
    }

    public void setDestPassword(String destPassword) {
        this.destPassword = destPassword;
    }

    public String getDestClusterId() {
        return destClusterId;
    }

    public void setDestClusterId(String destClusterId) {
        this.destClusterId = destClusterId;
    }

    public String getDestRegion() {
        return destRegion;
    }

    public void setDestRegion(String destRegion) {
        this.destRegion = destRegion;
    }

    public boolean isSourceElastic() {
        return sourceElastic;
    }

    public void setSourceElastic(boolean sourceElastic) {
        this.sourceElastic = sourceElastic;
    }

    public boolean isDestElastic() {
        return destElastic;
    }

    public void setDestElastic(boolean destElastic) {
        this.destElastic = destElastic;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
