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
    private String index = "index";
    
    @Option(name="-t",usage="type to use")
    private String type = "type";

    @Option(name="-f",usage="name of output file")
    private String outfilename = "out";
    
    @Option(name="-g",usage="gzip the output file")
    private boolean gzip = false;

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
}
