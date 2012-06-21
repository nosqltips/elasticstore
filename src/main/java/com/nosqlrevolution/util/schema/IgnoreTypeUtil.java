package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.enums.Field;
import com.nosqlrevolution.enums.Schema;
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
public class IgnoreTypeUtil {
    public static String generateSchema() {
        try {
            XContentBuilder builder = JsonXContent.contentBuilder();
            builder.startObject()
                    .field(Field.TYPE.getName(), Type.STRING.getName())
                    .field(Field.STORE.getName(), Schema.STORE.NO.name().toLowerCase())
                    .field(Field.ANALYZER.getName(), Schema.INDEX.NO.name().toLowerCase())
                    .field(Field.INCLUDE_IN_ALL.getName(), Schema.INCLUDE_IN_ALL.FALSE.name().toLowerCase());
            builder.endObject();
            
            return builder.string();

        } catch (IOException ex) {
            Logger.getLogger(IgnoreTypeUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
