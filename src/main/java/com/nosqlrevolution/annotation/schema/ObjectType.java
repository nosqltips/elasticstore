package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * @param <B>dynamic</B>     The name of the field that will be stored in the index. Defaults to the property/field name.
 * @param <B>enabled</B>     The enabled flag allows to disable parsing and adding a named object completely. This is handy when a portion of the JSON document passed should not be indexed.
 * @param <B>path</B>        In the core_types section, a field can have a index_name associated with it in order to control the name of the field that will be stored within the index. When that field exists within an object(s) that are not the root object, the name of the field of the index can either include the full “path” to the field with its index_name, or just the index_name.
 * @param <B>include_in_all</B> Should the field be included in the _all field (if enabled). Defaults to true or to the parent object type setting.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface ObjectType {
    public enum Dynamic { TRUE, FALSE, STRICT }
    /**
     * The name of the field that will be stored in the index. Defaults to the property/field name.
     */
    Dynamic dynamic() default Dynamic.TRUE;
    
    /**
     * The enabled flag allows to disable parsing and adding a named object completely. This is handy when a portion of the JSON document passed should not be indexed.
     */
    boolean enabled() default true;
    
    /**
     * In the core_types section, a field can have a index_name associated with it in order to control the name of the field that will be stored within the index. When that field exists within an object(s) that are not the root object, the name of the field of the index can either include the full “path” to the field with its index_name, or just the index_name.
     */
    String path() default "";
    
    /**
     * Should the field be included in the _all field (if enabled). Defaults to true or to the parent object type setting.
     */
    boolean include_in_all() default true;
}
