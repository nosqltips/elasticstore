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
public class StringTypeUtil {
    public static String generateSchema(StringType anno) {
        // Make sure annotation is valid
        if (anno == null) { return null; }
        
        try {
            XContentBuilder builder = JsonXContent.contentBuilder();
            builder.startObject()
                    .field(Field.TYPE.getName()).value(Type.STRING.getName());
                if (! anno.index_name().isEmpty()) {
                    builder.field(Field.INDEX_NAME.getName(), anno.index_name());
                }
                if (anno.store() != STORE.DEFAULT) {
                    builder.field(Field.STORE.getName(), anno.store().name().toLowerCase());
                }
                if (anno.index() != INDEX.DEFAULT) {
                    builder.field(Field.INDEX.getName(), anno.index().name().toLowerCase());
                }
                if (anno.term_vector() != TERM_VECTOR.DEFAULT) {
                    builder.field(Field.TERM_VECTOR.getName(), anno.term_vector().name().toLowerCase());
                }
                if (anno.boost() != 1.0f) {
                    builder.field(Field.BOOST.getName(), anno.boost());
                }
                if (! anno.null_value().isEmpty()) {
                    builder.field(Field.NULL_VALUE.getName(), anno.null_value());
                }
                if (anno.omit_norms() != OMIT_NORMS.DEFAULT) {
                    builder.field(Field.OMIT_NORMS.getName(), anno.omit_norms().name().toLowerCase());
                }
                if (anno.omit_term_freq_and_positions() != OMIT_TERM_FREQ.DEFAULT) {
                    builder.field(Field.OMIT_TERMS.getName(), anno.omit_term_freq_and_positions().name().toLowerCase());
                }
                if (! anno.analyzer().isEmpty()) {
                    builder.field(Field.ANALYZER.getName(), anno.analyzer());
                }
                if (! anno.index_analyzer().isEmpty()) {
                    builder.field(Field.INDEX_ANALYZER.getName(), anno.index_analyzer());
                }
                if (! anno.search_analyzer().isEmpty()) {
                    builder.field(Field.SEARCH_ANALYZER.getName(), anno.search_analyzer());
                }
                if (anno.include_in_all() != INCLUDE_IN_ALL.DEFAULT) {
                    builder.field(Field.INCLUDE_IN_ALL.getName(), anno.include_in_all().name().toLowerCase());
                }
            builder.endObject();
            return builder.string();

        } catch (IOException ex) {
            Logger.getLogger(StringTypeUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
