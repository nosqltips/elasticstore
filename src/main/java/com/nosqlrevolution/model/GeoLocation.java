package com.nosqlrevolution.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author cbrown
 */
public class GeoLocation {
    private double lat;
    private double lon;

    @JsonProperty("lat")
    public double getLat() {
        return lat;
    }

    @JsonProperty("lat")
    public GeoLocation setLat(double lat) {
        this.lat = lat;
        return this;
    }

    @JsonProperty("lon")
    public double getLon() {
        return lon;
    }

    @JsonProperty("lon")
    public GeoLocation setLon(double lon) {
        this.lon = lon;
        return this;
    }    
}
