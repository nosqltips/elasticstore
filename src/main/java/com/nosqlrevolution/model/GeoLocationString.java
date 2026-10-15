package com.nosqlrevolution.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
        if (latlon == null) throw new IllegalArgumentException("Location must contain latitude,longitude");
        String[] values = latlon.split(",", -1);
        if (values.length != 2) throw new IllegalArgumentException("Location must contain latitude,longitude");
        double parsedLat = Double.parseDouble(values[0].trim());
        double parsedLon = Double.parseDouble(values[1].trim());
        if (!Double.isFinite(parsedLat) || !Double.isFinite(parsedLon)) {
            throw new IllegalArgumentException("Location coordinates must be finite");
        }
        lat = parsedLat;
        lon = parsedLon;
    }
}
