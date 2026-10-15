package com.nosqlrevolution.apps;

import com.nosqlrevolution.annotation.DocumentId;

/**
 *
 * @author cbrown
 */
public class ExportModel {
    private String id;
    private String json;
    private String routing;

    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public String getRouting() { return routing; }
    public ExportModel setRouting(String routing) { this.routing = routing; return this; }

    @DocumentId
    public String getId() {
        return id;
    }

    public ExportModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getJson() {
        return json;
    }

    public ExportModel setJson(String json) {
        this.json = json;
        return this;
    }
}
