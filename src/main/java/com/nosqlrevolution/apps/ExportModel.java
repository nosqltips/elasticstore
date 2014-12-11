package com.nosqlrevolution.apps;

import com.nosqlrevolution.annotation.DocumentId;

/**
 *
 * @author cbrown
 */
public class ExportModel {
    private String id;
    private String json;

    @DocumentId
    public String getId() {
        return id;
    }

    @DocumentId
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
