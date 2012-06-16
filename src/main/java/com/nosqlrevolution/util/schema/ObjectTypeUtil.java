package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.ObjectType;
import com.nosqlrevolution.enums.Field;
import com.nosqlrevolution.enums.Schema.DYNAMIC;
import com.nosqlrevolution.enums.Schema.ENABLED;
import com.nosqlrevolution.enums.Schema.INCLUDE_IN_ALL;
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
public class ObjectTypeUtil {
    public static String generateSchema(ObjectType anno, String name) {
        try {
            XContentBuilder builder = JsonXContent.contentBuilder();
            builder.startObject(name).field(Field.TYPE.getName(), Type.OBJECT.getName());
                if (anno.dynamic() != DYNAMIC.DEFAULT) {
                    builder.field(Field.DYNAMIC.getName(), anno.dynamic().name());
                }
                if (anno.enabled() != ENABLED.DEFAULT) {
                    builder.field(Field.ENABLED.getName(), anno.enabled().name());
                }
                if (! anno.path().isEmpty()) {
                    builder.field(Field.PATH.getName(), anno.path());
                }
                if (anno.include_in_all() != INCLUDE_IN_ALL.DEFAULT) {
                    builder.field(Field.INCLUDE_IN_ALL.getName(), anno.include_in_all());
                }
            builder.endObject();
            
            return builder.toString();

        } catch (IOException ex) {
            Logger.getLogger(ObjectTypeUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
