package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * @param <B>index_name</B>     The name of the field that will be stored in the index. Defaults to the property/field name.
 * @param <B>store</B>          Set to true the store actual field in the index, false to not store it. Defaults to false (note, the JSON document itself is stored, and it can be retrieved from it).
 * @param <B>index </B>         Set to analyzed for the field to be indexed and searchable after being broken down into token using an analyzer. not_analyzed means that its still searchable, but does not go through any analysis process or broken down into tokens. no means that it won’t be searchable at all (as an individual field; it may still be included in _all). Defaults to analyzed.
 * @param <B>term_vector</B>    Possible values are no, yes, with_offsets, with_positions, with_positions_offsets. Defaults to no.
 * @param <B>boost</B>          The boost value. Defaults to 1.0.
 * @param <B>null_value</B>     When there is a (JSON) null value for the field, use the null_value as the field value. Defaults to not adding the field at all.
 * @param <B>omit_norms</B>     Boolean value if norms should be omitted or not. Defaults to false.
 * @param <B>omit_term_freq_and_positions</B>     Boolean value if term freq and positions should be omitted. Defaults to false.
 * @param <B>analyzer</B>       The analyzer used to analyze the text contents when analyzed during indexing and when searching using a query string. Defaults to the globally configured analyzer.
 * @param <B>index_analyzer</B> The analyzer used to analyze the text contents when analyzed during indexing.
 * @param <B>search_analyzer</B> The analyzer used to analyze the field when part of a query string.
 * @param <B>include_in_all</B> Should the field be included in the _all field (if enabled). Defaults to true or to the parent object type setting.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface StringType {
    public enum VECTOR { no, yes, with_offsets, with_positions, with_positions_offsets }
    /**
     * The name of the field that will be stored in the index. Defaults to the property/field name.
     */
    String index_name() default "";
    
    /**
     * Set to true the store actual field in the index, false to not store it. Defaults to false (note, the JSON document itself is stored, and it can be retrieved from it).
     */
    boolean store() default false;
    
    /**
     * Set to analyzed for the field to be indexed and searchable after being broken down into token using an analyzer. not_analyzed means that its still searchable, but does not go through any analysis process or broken down into tokens. no means that it won’t be searchable at all (as an individual field; it may still be included in _all). Defaults to analyzed.
     */
    String index() default "analyzed";
    
    /**
     * Possible values are no, yes, with_offsets, with_positions, with_positions_offsets. Defaults to no.
     */
    VECTOR term_vector() default VECTOR.no;
    
    /**
     * The boost value. Defaults to 1.0.
     */
    float boost() default 1.0f;
    
    /**
     * When there is a (JSON) null value for the field, use the null_value as the field value. Defaults to not adding the field at all.
     */
    String null_value() default "";
    
    /**
     * Boolean value if norms should be omitted or not. Defaults to false.
     */
    boolean omit_norms() default false;
    
    /**
     * Boolean value if term freq and positions should be omitted. Defaults to false.
     */
    boolean omit_term_freq_and_positions() default false;
    
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
     * Should the field be included in the _all field (if enabled). Defaults to true or to the parent object type setting.
     */
    boolean include_in_all() default true;
}
