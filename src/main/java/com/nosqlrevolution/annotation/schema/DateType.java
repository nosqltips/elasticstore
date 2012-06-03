package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * @param <B>index_name</B>     The name of the field that will be stored in the index. Defaults to the property/field name.
 * @param <B>format</B>         The date format. Defaults to dateOptionalTime.
 * @param <B>store</B>          Set to true the store actual field in the index, false to not store it. Defaults to false (note, the JSON document itself is stored, and it can be retrieved from it).
 * @param <B>index </B>         Set to false if the value should not be indexed. In this case, <b>store</b> should be set to true, since if its not indexed and not stored, there is nothing to do with it.
 * @param <B>precision_step</B> The precision step (number of terms generated for each number value). Defaults to 4.
 * @param <B>boost</B>          The boost value. Defaults to 1.0.
 * @param <B>null_value</B>     When there is a (JSON) null value for the field, use the null_value as the field value. Defaults to not adding the field at all.
 * @param <B>include_in_all</B> Should the field be included in the _all field (if enabled). Defaults to true or to the parent object type setting.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface DateType {
    /**
     * The name of the field that will be stored in the index. Defaults to the property/field name.
     */
    String index_name() default "";
    
    /**
     * The name of the field that will be stored in the index. Defaults to the property/field name.
     */
    // TODO: Add information on possible date formats.
    String format() default "dateOptionalTime";
    
    /**
     * Set to true the store actual field in the index, false to not store it. Defaults to false (note, the JSON document itself is stored, and it can be retrieved from it).
     */
    boolean store() default false;
    
    /**
     * Set to false if the value should not be indexed. In this case, <b>store</b> should be set to true, since if its not indexed and not stored, there is nothing to do with it.
     */
    boolean index() default true;
    
    /**
     * The precision step (number of terms generated for each number value). Defaults to 4.
     */
    int precision_step() default 4;
    
    /**
     * The boost value. Defaults to 1.0.
     */
    float boost() default 1.0f;
    
    /**
     * When there is a (JSON) null value for the field, use the null_value as the field value. Defaults to not adding the field at all.
     */
    String null_value() default "";
    
    /**
     * Should the field be included in the _all field (if enabled). Defaults to true or to the parent object type setting.
     */
    boolean include_in_all() default true;
}
