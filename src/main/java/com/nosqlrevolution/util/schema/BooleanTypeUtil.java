package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.BooleanType;
import com.nosqlrevolution.enums.Field;
import com.nosqlrevolution.enums.Schema.INCLUDE_IN_ALL;
import com.nosqlrevolution.enums.Schema.INDEX;
import com.nosqlrevolution.enums.Schema.STORE;
import com.nosqlrevolution.enums.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Generate the minimum schema for this type
 * 
 * @author cbrown
 */
public class BooleanTypeUtil {
    public static Map<String, Object> generateSchema(BooleanType anno) {
        // Make sure annotation is valid
        if (anno == null) { return null; }
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Field.TYPE.getName(), Type.BOOLEAN.getName());
        if (! anno.index_name().isEmpty()) {
            map.put(Field.INDEX_NAME.getName(), anno.index_name());
        }
        if (anno.store() != STORE.DEFAULT) {
            map.put(Field.STORE.getName(), anno.store().name().toLowerCase());
        }
        if (anno.index() != INDEX.DEFAULT) {
            map.put(Field.INDEX.getName(), anno.index().name().toLowerCase());
        }
        if (anno.boost() != 1.0f) {
            map.put(Field.BOOST.getName(), new Float(anno.boost()));
        }
        if (! anno.null_value().isEmpty()) {
            map.put(Field.NULL_VALUE.getName(), anno.null_value());
        }
        if (anno.include_in_all() != INCLUDE_IN_ALL.DEFAULT) {
            map.put(Field.INCLUDE_IN_ALL.getName(), anno.include_in_all().name().toLowerCase());
        }
        return map;
    }
}
