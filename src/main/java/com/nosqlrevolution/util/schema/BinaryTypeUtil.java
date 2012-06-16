package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.BinaryType;
import com.nosqlrevolution.enums.Field;
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
public class BinaryTypeUtil {
    public static String generateSchema(BinaryType anno, String name) {
        try {
            XContentBuilder builder = JsonXContent.contentBuilder();
            builder.startObject(name).field(Field.TYPE.getName(), Type.BINARY.getName());
                if (! anno.index_name().isEmpty()) {
                    builder.field(Field.INDEX_NAME.getName(), anno.index_name());
                }
            builder.endObject();
            
            return builder.toString();

        } catch (IOException ex) {
            Logger.getLogger(BinaryTypeUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
