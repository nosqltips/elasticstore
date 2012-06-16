package com.nosqlrevolution.annotation.schema;

import com.nosqlrevolution.enums.Schema.INCLUDE_IN_ALL;
import com.nosqlrevolution.enums.Schema.INDEX;
import com.nosqlrevolution.enums.Schema.OMIT_NORMS;
import com.nosqlrevolution.enums.Schema.OMIT_TERM_FREQ;
import com.nosqlrevolution.enums.Schema.STORE;
import com.nosqlrevolution.enums.Schema.VECTOR;
import java.lang.annotation.*;

/**
 * @param <B>index_name</B>     The name of the field that will be stored in the index. Defaults to the <B>property/field name</B>.
 * @param <B>store</B>          Set to <B>yes</B> the store actual field in the index, <B>no</B> to not store it. Defaults to <B>no</B> (note, the JSON document itself is stored, and it can be retrieved from it).
 * @param <B>index </B>         Set to <B>analyzed</B> for the field to be indexed and searchable after being broken down into token using an analyzer. <B>not_analyzed</B> means that its still searchable, but does not go through any analysis process or broken down into tokens. <B>no</B> means that it won’t be searchable at all (as an individual field; it may still be included in <B>_all</B>). Defaults to <B>analyzed</B>.
 * @param <B>term_vector</B>    Possible values are <B>no</B>, <B>yes</B>, <B>with_offsets</B>, <B>with_positions</B>, <B>with_positions_offsets</B>. Defaults to <B>no</B>.
 * @param <B>boost</B>          The boost value. Defaults to <B>1.0</B>.
 * @param <B>null_value</B>     When there is a (JSON) null value for the field, use the <B>null_value</B> as the field value. Defaults to not adding the field at all.
 * @param <B>omit_norms</B>     Boolean value if norms should be omitted or not. Defaults to <B>false</B>.
 * @param <B>omit_term_freq_and_positions</B>     Boolean value if term freq and positions should be omitted. Defaults to <B>false</>.
 * @param <B>analyzer</B>       The analyzer used to analyze the text contents when analyzed during indexing and when searching using a query string. Defaults to the globally configured analyzer.
 * @param <B>index_analyzer</B> The analyzer used to analyze the text contents when analyzed during indexing.
 * @param <B>search_analyzer</B> The analyzer used to analyze the field when part of a query string.
 * @param <B>include_in_all</B> Should the field be included in the <B>_all</B> field (if enabled). Defaults to <B>true</B> or to the parent object type setting.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface StringType {   
    /**
     * The name of the field that will be stored in the index. Defaults to the <B>property/field name</B>.
     */
    String index_name() default "";
    
    /**
     * Set to <B>yes</B> the store actual field in the index, <B>no</B> to not store it. Defaults to <B>no</B> (note, the JSON document itself is stored, and it can be retrieved from it).
     */
    STORE store() default STORE.DEFAULT;
    
    /**
     * Set to <B>analyzed</B> for the field to be indexed and searchable after being broken down into token using an analyzer. <B>not_analyzed</B> means that its still searchable, but does not go through any analysis process or broken down into tokens. <B>no</B> means that it won’t be searchable at all (as an individual field; it may still be included in <B>_all</B>). Defaults to <B>analyzed</B>.
     */
    INDEX index() default INDEX.DEFAULT;
    
    /**
     * Possible values are <B>no</B>, <B>yes</B>, <B>with_offsets</B>, <B>with_positions</B>, <B>with_positions_offsets</B>. Defaults to <B>no</B>.
     */
    VECTOR term_vector() default VECTOR.DEFAULT;
    
    /**
     * The boost value. Defaults to <B>1.0</B>.
     */
    float boost() default 1.0f;
    
    /**
     * When there is a (JSON) null value for the field, use the <B>null_value</B> as the field value. Defaults to not adding the field at all.
     */
    String null_value() default "";
    
    /**
     * Boolean value if norms should be omitted or not. Defaults to <B>false</B>.
     */
    OMIT_NORMS omit_norms() default OMIT_NORMS.DEFAULT;
    
    /**
     * Boolean value if term freq and positions should be omitted. Defaults to <B>false</>.
     */
    OMIT_TERM_FREQ omit_term_freq_and_positions() default OMIT_TERM_FREQ.DEFAULT;
    
    /**
     * The analyzer used to analyze the text contents when analyzed during indexing and when searching using a query string. Defaults to the globally configured analyzer.
     */
    String analyzer() default "";
    
    /**
     * The analyzer used to analyze the text contents when analyzed during indexing.
     */
    String index_analyzer() default "";
    
    /**
     * The analyzer used to analyze the field when part of a query string.
     */
    String search_analyzer() default "";
    
    /**
     * Should the field be included in the <B>_all</B> field (if enabled). Defaults to <B>true</B> or to the parent object type setting.
     */
    INCLUDE_IN_ALL include_in_all() default INCLUDE_IN_ALL.DEFAULT;
}
