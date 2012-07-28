package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.GeoPointType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class GeoPointTypeUtilTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        Field f = Basic.class.getField("field");
        GeoPointType anno = f.getAnnotation(GeoPointType.class);
        Map<String, Object> map = GeoPointTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "geo_point");
    }
     
    @Test
    public void Geohash() throws IOException, NoSuchFieldException {
        Field f = Geohash.class.getField("field");
        GeoPointType anno = f.getAnnotation(GeoPointType.class);
        Map<String, Object> map = GeoPointTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "geo_point");
        assertEquals(map.get("geohash"), "true");
    }

    @Test
    public void Precision() throws IOException, NoSuchFieldException {
        Field f = Precision.class.getField("field");
        GeoPointType anno = f.getAnnotation(GeoPointType.class);
        Map<String, Object> map = GeoPointTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "geo_point");
        assertEquals(map.get("geohash_precision"), 15);
    }

    @Test
    public void Latlon() throws IOException, NoSuchFieldException {
        Field f = Latlon.class.getField("field");
        GeoPointType anno = f.getAnnotation(GeoPointType.class);
        Map<String, Object> map = GeoPointTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "geo_point");
        assertEquals(map.get("lat_lon"), "true");
    }

    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        GeoPointType anno = f.getAnnotation(GeoPointType.class);
        Map<String, Object> map = GeoPointTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 4);
        assertEquals(map.get("type"), "geo_point");
        assertEquals(map.get("lat_lon"), "true");
        assertEquals(map.get("geohash"), "true");
        assertEquals(map.get("geohash_precision"), 15);
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        Map<String, Object> map = GeoPointTypeUtil.generateSchema(null);
        assertNull(map);
    }
     
    public class Basic {
        @GeoPointType
        public String field;
    }

    public class Geohash {
        @GeoPointType(geohash=Schema.GEOHASH.TRUE)
        public String field;
    }

    public class Precision {
        @GeoPointType(geohash_precision=15)
        public String field;
    }

    public class Latlon {
        @GeoPointType(lat_lon=Schema.LATLON.TRUE)
        public String field;
    }

    public class All {
        @GeoPointType(geohash=Schema.GEOHASH.TRUE, geohash_precision=15, lat_lon=Schema.LATLON.TRUE)
        public String field;
    }
}
