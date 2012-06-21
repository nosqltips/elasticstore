package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.BooleanType;
import com.nosqlrevolution.enums.Field;
import com.nosqlrevolution.enums.Schema.INCLUDE_IN_ALL;
import com.nosqlrevolution.enums.Schema.INDEX;
import com.nosqlrevolution.enums.Schema.STORE;
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
public class BooleanTypeUtil {
    public static String generateSchema(BooleanType anno) {
        // Make sure annotation is valid
        if (anno == null) { return null; }
        
        try {
            XContentBuilder builder = JsonXContent.contentBuilder();
            builder.startObject()
                    .field(Field.TYPE.getName(), Type.BOOLEAN.getName());
                if (! anno.index_name().isEmpty()) {
                    builder.field(Field.INDEX_NAME.getName(), anno.index_name());
                }
                if (anno.store() != STORE.DEFAULT) {
                    builder.field(Field.STORE.getName(), anno.store().name().toLowerCase());
                }
                if (anno.index() != INDEX.DEFAULT) {
                    builder.field(Field.INDEX.getName(), anno.index().name().toLowerCase());
                }
                if (anno.boost() != 1.0f) {
                    builder.field(Field.BOOST.getName(), anno.boost());
                }
                if (! anno.null_value().isEmpty()) {
                    builder.field(Field.NULL_VALUE.getName(), anno.null_value());
                }
                if (anno.include_in_all() != INCLUDE_IN_ALL.DEFAULT) {
                    builder.field(Field.INCLUDE_IN_ALL.getName(), anno.include_in_all().name().toLowerCase());
                }
            builder.endObject();
            
            return builder.string();

        } catch (IOException ex) {
            Logger.getLogger(BooleanTypeUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
