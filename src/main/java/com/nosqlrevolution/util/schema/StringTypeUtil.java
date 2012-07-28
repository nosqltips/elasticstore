package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.StringType;
import com.nosqlrevolution.enums.Field;
import com.nosqlrevolution.enums.Schema.INCLUDE_IN_ALL;
import com.nosqlrevolution.enums.Schema.INDEX;
import com.nosqlrevolution.enums.Schema.OMIT_NORMS;
import com.nosqlrevolution.enums.Schema.OMIT_TERM_FREQ;
import com.nosqlrevolution.enums.Schema.STORE;
import com.nosqlrevolution.enums.Schema.TERM_VECTOR;
import com.nosqlrevolution.enums.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Generate the minimum schema for this type
 * 
 * @author cbrown
 */
public class StringTypeUtil {
    public static Map<String, Object> generateSchema(StringType anno) {
        // Make sure annotation is valid
        if (anno == null) { return null; }
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Field.TYPE.getName(), Type.STRING.getName());
        if (! anno.index_name().isEmpty()) {
            map.put(Field.INDEX_NAME.getName(), anno.index_name());
        }
        if (anno.store() != STORE.DEFAULT) {
            map.put(Field.STORE.getName(), anno.store().name().toLowerCase());
        }
        if (anno.index() != INDEX.DEFAULT) {
            map.put(Field.INDEX.getName(), anno.index().name().toLowerCase());
        }
        if (anno.term_vector() != TERM_VECTOR.DEFAULT) {
            map.put(Field.TERM_VECTOR.getName(), anno.term_vector().name().toLowerCase());
        }
        if (anno.boost() != 1.0f) {
            map.put(Field.BOOST.getName(), new Float(anno.boost()));
        }
        if (! anno.null_value().isEmpty()) {
            map.put(Field.NULL_VALUE.getName(), anno.null_value());
        }
        if (anno.omit_norms() != OMIT_NORMS.DEFAULT) {
            map.put(Field.OMIT_NORMS.getName(), anno.omit_norms().name().toLowerCase());
        }
        if (anno.omit_term_freq_and_positions() != OMIT_TERM_FREQ.DEFAULT) {
            map.put(Field.OMIT_TERMS.getName(), anno.omit_term_freq_and_positions().name().toLowerCase());
        }
        if (! anno.analyzer().isEmpty()) {
            map.put(Field.ANALYZER.getName(), anno.analyzer());
        }
        if (! anno.index_analyzer().isEmpty()) {
            map.put(Field.INDEX_ANALYZER.getName(), anno.index_analyzer());
        }
        if (! anno.search_analyzer().isEmpty()) {
            map.put(Field.SEARCH_ANALYZER.getName(), anno.search_analyzer());
        }
        if (anno.include_in_all() != INCLUDE_IN_ALL.DEFAULT) {
            map.put(Field.INCLUDE_IN_ALL.getName(), anno.include_in_all().name().toLowerCase());
        }
        return map;
    }
}
