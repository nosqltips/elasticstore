package com.nosqlrevolution.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test to make sure all of our geolocation classes are serialing to/from JSON properly.
 * 
 * @author cbrown
 */
public class GeoLocationTest {
    ObjectMapper mapper = new ObjectMapper();
    
    @Test
    public void GeoLocation() {
        try {
            GeoLocation loc = new GeoLocation();
            loc.setLat(1.0D).setLon(2.0D);
            String value = mapper.writeValueAsString(loc);
            
            assertNotNull(value);
            assertEquals("{\"lat\":1.0,\"lon\":2.0}", value);
            
            value = "{\"lat\":-1.1,\"lon\":-2.2}";
            loc = mapper.readValue(value, GeoLocation.class);
            
            assertEquals(-1.1D, loc.getLat(), .1D);
            assertEquals(-2.2D, loc.getLon(), .1D);
        } catch (IOException ex) {
            Logger.getLogger(GeoLocationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void GeoLocationString() {
        try {
            GeoLocationString loc = new GeoLocationString();
            loc.setLat(1.0D).setLon(2.0D);
            String value = mapper.writeValueAsString(loc);

            assertNotNull(value);
            assertEquals("{\"location\":\"1.0,2.0\"}", value);
            
            value = "{\"location\":\"-1.1,-2.2\"}";
            loc = mapper.readValue(value, GeoLocationString.class);
            
            assertEquals(-1.1D, loc.getLat(), .1D);
            assertEquals(-2.2D, loc.getLon(), .1D);
        } catch (IOException ex) {
            Logger.getLogger(GeoLocationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void GeoLocationArray() {
        try {
            GeoLocationArray loc = new GeoLocationArray();
            loc.setLat(1.0D).setLon(2.0D);
            String value = mapper.writeValueAsString(loc);

            assertNotNull(value);
            assertEquals("{\"location\":[2.0,1.0]}", value);
            
            value = "{\"location\":[-2.2,-1.1]}";
            loc = mapper.readValue(value, GeoLocationArray.class);
            
            assertEquals(-1.1D, loc.getLat(), .1D);
            assertEquals(-2.2D, loc.getLon(), .1D);
        } catch (IOException ex) {
            Logger.getLogger(GeoLocationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void GeoHash() {
        try {
            GeoHash loc = new GeoHash();
            loc.setGeoHash("drm3btev3e86");
            String value = mapper.writeValueAsString(loc);

            assertNotNull(value);
            assertEquals("{\"location\":\"drm3btev3e86\"}", value);
            
            value = "{\"location\":\"abc123oijd\"}";
            loc = mapper.readValue(value, GeoHash.class);
            
            assertEquals("abc123oijd", loc.getGeoHash());
        } catch (IOException ex) {
            Logger.getLogger(GeoLocationTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
