package com.nosqlrevolution.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Conforms to GeoJson encoding of lon, lat
 * @author cbrown
 */
public class GeoLocationArray {
    private double[] lonlat = new double[2];

    @JsonIgnore
    public double getLat() {
        return lonlat[1];
    }

    @JsonIgnore
    public GeoLocationArray setLat(double lat) {
        lonlat[1] = lat;
        return this;
    }

    @JsonIgnore
    public double getLon() {
        return lonlat[0];
    }

    @JsonIgnore
    public GeoLocationArray setLon(double lon) {
        lonlat[0] = lon;
        return this;
    }    

    @JsonProperty("location")
    public double[] toArray() {
        return lonlat;
    }
    
    @JsonProperty("location")
    public void fromArray(double[] lonlat) {
        this.lonlat = lonlat;
    }
}
