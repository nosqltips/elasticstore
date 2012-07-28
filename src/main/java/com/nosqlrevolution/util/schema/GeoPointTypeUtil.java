package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.GeoPointType;
import com.nosqlrevolution.enums.Field;
import com.nosqlrevolution.enums.Schema.GEOHASH;
import com.nosqlrevolution.enums.Schema.LATLON;
import com.nosqlrevolution.enums.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Generate the minimum schema for this type
 * 
 * @author cbrown
 */
public class GeoPointTypeUtil {
    public static Map<String, Object> generateSchema(GeoPointType anno) {
        // Make sure annotation is valid
        if (anno == null) { return null; }
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Field.TYPE.getName(), Type.GEO_POINT.getName());
        if (anno.lat_lon() != LATLON.DEFAULT) {
            map.put(Field.LAT_LON.getName(), anno.lat_lon().name().toLowerCase());
        }
        if (anno.geohash() != GEOHASH.DEFAULT) {
            map.put(Field.GEOHASH.getName(), anno.geohash().name().toLowerCase());
        }
        if (anno.geohash_precision() != 12) {
            map.put(Field.GEOHASH_PRECISION.getName(), new Integer(anno.geohash_precision()));
        }
        return map;
    }
}
