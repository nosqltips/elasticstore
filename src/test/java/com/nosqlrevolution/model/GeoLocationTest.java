package com.nosqlrevolution.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
    public void GeoLocation() throws IOException {
        GeoLocation loc = new GeoLocation();
        loc.setLat(1.0D).setLon(2.0D);
        String value = mapper.writeValueAsString(loc);

        assertNotNull(value);
        assertEquals("{\"lat\":1.0,\"lon\":2.0}", value);

        value = "{\"lat\":-1.1,\"lon\":-2.2}";
        loc = mapper.readValue(value, GeoLocation.class);

        assertEquals(-1.1D, loc.getLat(), .1D);
        assertEquals(-2.2D, loc.getLon(), .1D);
    }

    @Test
    public void GeoLocationString() throws IOException {
        GeoLocationString loc = new GeoLocationString();
        loc.setLat(1.0D).setLon(2.0D);
        String value = mapper.writeValueAsString(loc);

        assertNotNull(value);
        assertEquals("{\"location\":\"1.0,2.0\"}", value);

        value = "{\"location\":\"-1.1,-2.2\"}";
        loc = mapper.readValue(value, GeoLocationString.class);

        assertEquals(-1.1D, loc.getLat(), .1D);
        assertEquals(-2.2D, loc.getLon(), .1D);
    }

    @Test
    public void GeoLocationArray() throws IOException {
        GeoLocationArray loc = new GeoLocationArray();
        loc.setLat(1.0D).setLon(2.0D);
        String value = mapper.writeValueAsString(loc);

        assertNotNull(value);
        assertEquals("{\"location\":[2.0,1.0]}", value);

        value = "{\"location\":[-2.2,-1.1]}";
        loc = mapper.readValue(value, GeoLocationArray.class);

        assertEquals(-1.1D, loc.getLat(), .1D);
        assertEquals(-2.2D, loc.getLon(), .1D);
    }

    @Test
    public void GeoHash() throws IOException {
        GeoHash loc = new GeoHash();
        loc.setGeoHash("drm3btev3e86");
        String value = mapper.writeValueAsString(loc);

        assertNotNull(value);
        assertEquals("{\"location\":\"drm3btev3e86\"}", value);

        value = "{\"location\":\"abc123oijd\"}";
        loc = mapper.readValue(value, GeoHash.class);

        assertEquals("abc123oijd", loc.getGeoHash());
    }
    @Test public void malformedCoordinatesFailWithoutChangingTheLocation() throws Exception {
        GeoLocationString location = new GeoLocationString().setLat(-1).setLon(-2);
        for (String input : new String[] {null, "", "one", "1", "1,2,3", "1,", "50,bad", "NaN,2", "1,Infinity"}) {
            try { location.fromString(input); fail("Accepted invalid location: " + input); }
            catch (IllegalArgumentException expected) { }
            assertEquals(-1, location.getLat(), 0);
            assertEquals(-2, location.getLon(), 0);
        }
        try { mapper.readValue("{\"location\":\"bad\"}", GeoLocationString.class); fail("Accepted invalid JSON location"); }
        catch (com.fasterxml.jackson.core.JsonProcessingException expected) { }
        location.fromString(" 1.5 , -2.5 ");
        assertEquals(1.5, location.getLat(), 0);
        assertEquals(-2.5, location.getLon(), 0);
    }
}
