package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.BinaryType;
import com.nosqlrevolution.enums.Field;
import com.nosqlrevolution.enums.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Generate the minimum schema for this type
 * 
 * @author cbrown
 */
public class BinaryTypeUtil {
    public static Map<String, Object> generateSchema(BinaryType anno) {
        // Make sure annotation is valid
        if (anno == null) { return null; }
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Field.TYPE.getName(), Type.BINARY.getName());
        if (! anno.index_name().isEmpty()) {
            map.put(Field.INDEX_NAME.getName(), anno.index_name());
        }
        return map;
    }
}
