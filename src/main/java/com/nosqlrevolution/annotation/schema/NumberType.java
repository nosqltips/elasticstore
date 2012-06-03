package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * @param <B>type</B>           The type of the number. Can be FLOAT, DOUBLE, INTEGER, LONG, SHORT, BYTE. Defaults to INTEGER
 * @param <B>index_name</B>     The name of the field that will be stored in the index. Defaults to the property/field name.
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
public @interface NumberType {
    public enum Type { FLOAT, DOUBLE, INTEGER, LONG, SHORT, BYTE }
    /**
     * The type of the number. Can be FLOAT, DOUBLE, INTEGER, LONG, SHORT, BYTE. Defaults to INTEGER
     */
    Type type() default Type.INTEGER;
    
    /**
     * The name of the field that will be stored in the index. Defaults to the property/field name.
     */
    String index_name() default "";
    
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
