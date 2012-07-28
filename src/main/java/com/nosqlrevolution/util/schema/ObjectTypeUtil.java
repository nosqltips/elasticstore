package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.ObjectType;
import com.nosqlrevolution.enums.Field;
import com.nosqlrevolution.enums.Schema.DYNAMIC;
import com.nosqlrevolution.enums.Schema.ENABLED;
import com.nosqlrevolution.enums.Schema.INCLUDE_IN_ALL;
import com.nosqlrevolution.enums.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Generate the minimum schema for this type
 * 
 * @author cbrown
 */
public class ObjectTypeUtil {
    public static Map<String, Object> generateSchema(ObjectType anno) {
        // Make sure annotation is valid
        if (anno == null) { return null; }
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Field.TYPE.getName(), Type.OBJECT.getName());
        if (anno.dynamic() != DYNAMIC.DEFAULT) {
            map.put(Field.DYNAMIC.getName(), anno.dynamic().name().toLowerCase());
        }
        if (anno.enabled() != ENABLED.DEFAULT) {
            map.put(Field.ENABLED.getName(), anno.enabled().name().toLowerCase());
        }
        if (! anno.path().isEmpty()) {
            map.put(Field.PATH.getName(), anno.path());
        }
        if (anno.include_in_all() != INCLUDE_IN_ALL.DEFAULT) {
            map.put(Field.INCLUDE_IN_ALL.getName(), anno.include_in_all().name().toLowerCase());
        }
        return map;
    }
}
