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
    private String index = "index";
    
    @Option(name="-t",usage="type to use")
    private String type = "type";

    @Option(name="-o",usage="name of input file")
    private String infilename = "in";

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
}
