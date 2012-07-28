package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.enums.Field;
import com.nosqlrevolution.enums.Schema;
import com.nosqlrevolution.enums.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Generate the minimum schema for this type
 * 
 * @author cbrown
 */
public class IgnoreTypeUtil {
    public static Map<String, Object> generateSchema() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Field.TYPE.getName(), Type.STRING.getName());
        map.put(Field.STORE.getName(), Schema.STORE.NO.name().toLowerCase());
        map.put(Field.ANALYZER.getName(), Schema.INDEX.NO.name().toLowerCase());
        map.put(Field.INCLUDE_IN_ALL.getName(), Schema.INCLUDE_IN_ALL.FALSE.name().toLowerCase());
        return map;
    }
}
