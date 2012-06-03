package com.nosqlrevolution.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author cbrown
 */
public class GeoLocationString {
    private double lat;
    private double lon;

    @JsonIgnore
    public double getLat() {
        return lat;
    }

    @JsonIgnore
    public GeoLocationString setLat(double lat) {
        this.lat = lat;
        return this;
    }

    @JsonIgnore
    public double getLon() {
        return lon;
    }

    @JsonIgnore
    public GeoLocationString setLon(double lon) {
        this.lon = lon;
        return this;
    }    

    @Override
    @JsonProperty("location")
    public String toString() {
        return new StringBuilder().append(lat).append(",").append(lon).toString();
    }
    
    @JsonProperty("location")
    public void fromString(String latlon) {
        // TODO: possibly thro exception here
        String[] values = latlon.split(",");
        if (values.length == 2) {
            lat = Double.parseDouble(values[0]);
            lon = Double.parseDouble(values[1]);
        }        
    }
}
