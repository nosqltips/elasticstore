package com.nosqlrevolution.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author cbrown
 */
public class GeoHash {
    private String geoHash;

    @JsonProperty("location")
    public String getGeoHash() {
        return geoHash;
    }

    @JsonProperty("location")
    public void setGeoHash(String hashValue) {
        this.geoHash = hashValue;
    }
}
