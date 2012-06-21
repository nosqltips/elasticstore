package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.GeoPointType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
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
        String json = GeoPointTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"geo_point\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Geohash() throws IOException, NoSuchFieldException {
        Field f = Geohash.class.getField("field");
        GeoPointType anno = f.getAnnotation(GeoPointType.class);
        String json = GeoPointTypeUtil.generateSchema(anno);
        assertNotNull(json);
        System.out.println(json);
        String expected = "{\"type\":\"geo_point\",\"geohash\":\"true\"}";
        assertEquals(expected, json);
    }

    @Test
    public void Precision() throws IOException, NoSuchFieldException {
        Field f = Precision.class.getField("field");
        GeoPointType anno = f.getAnnotation(GeoPointType.class);
        String json = GeoPointTypeUtil.generateSchema(anno);
        assertNotNull(json);
        System.out.println(json);
        String expected = "{\"type\":\"geo_point\",\"geohash_precision\":15}";
        assertEquals(expected, json);
    }

    @Test
    public void Latlon() throws IOException, NoSuchFieldException {
        Field f = Latlon.class.getField("field");
        GeoPointType anno = f.getAnnotation(GeoPointType.class);
        String json = GeoPointTypeUtil.generateSchema(anno);
        assertNotNull(json);
        System.out.println(json);
        String expected = "{\"type\":\"geo_point\",\"lat_lon\":\"true\"}";
        assertEquals(expected, json);
    }

    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        GeoPointType anno = f.getAnnotation(GeoPointType.class);
        String json = GeoPointTypeUtil.generateSchema(anno);
        assertNotNull(json);
        System.out.println(json);
        String expected = "{\"type\":\"geo_point\",\"lat_lon\":\"true\",\"geohash\":\"true\",\"geohash_precision\":15}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        String json = GeoPointTypeUtil.generateSchema(null);
        assertNull(json);
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
