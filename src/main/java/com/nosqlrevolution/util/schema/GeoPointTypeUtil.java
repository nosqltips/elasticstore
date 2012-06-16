package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.GeoPointType;
import com.nosqlrevolution.enums.Field;
import com.nosqlrevolution.enums.Schema.GEOHASH;
import com.nosqlrevolution.enums.Schema.LATLON;
import com.nosqlrevolution.enums.Type;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;

/**
 * Generate the minimum schema for this type
 * 
 * @author cbrown
 */
public class GeoPointTypeUtil {
    public static String generateSchema(GeoPointType anno, String name) {
        try {
            XContentBuilder builder = JsonXContent.contentBuilder();
            builder.startObject(name).field(Field.TYPE.getName(), Type.GEO_POINT.getName());
                if (anno.lat_lon() != LATLON.DEFAULT) {
                    builder.field(Field.LAT_LON.getName(), anno.lat_lon().name());
                }
                if (anno.geohash() != GEOHASH.DEFAULT) {
                    builder.field(Field.GEOHASH.getName(), anno.geohash().name());
                }
                if (anno.geohash_precision() != 1.0f) {
                    builder.field(Field.GEOHASH_PRECISION.getName(), anno.geohash_precision());
                }
            builder.endObject();
            
            return builder.toString();

        } catch (IOException ex) {
            Logger.getLogger(GeoPointTypeUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
