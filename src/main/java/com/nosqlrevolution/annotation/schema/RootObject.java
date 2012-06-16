package com.nosqlrevolution.annotation.schema;

import com.nosqlrevolution.enums.Schema.DYNAMIC;
import com.nosqlrevolution.enums.Schema.ENABLED;
import com.nosqlrevolution.enums.Schema.INCLUDE_IN_ALL;
import java.lang.annotation.*;

/**
 * @param <B>dynamic</B>     Allow to determine schema dynamically. Defaults to <B>true</B>.
 * @param <B>enabled</B>     The enabled flag allows to disable parsing and adding a named object completely. This is handy when a portion of the JSON document passed should not be indexed. Defaults to <B>true</B>.
 * @param <B>path</B>        In the core_types section, a field can have a index_name associated with it in order to control the name of the field that will be stored within the index. When that field exists within an object(s) that are not the root object, the name of the field of the index can either include the full “path” to the field with its index_name, or just the index_name.
 * @param <B>include_in_all</B> Should the field be included in the <B>_all</B> field (if enabled). Defaults to <B>true</B> or to the parent object type setting.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RootObject {
    /**
     * Allow to determine schema dynamically. Defaults to <B>true</B>.
     */
    DYNAMIC dynamic() default DYNAMIC.TRUE;
    
    /**
     * The enabled flag allows to disable parsing and adding a named object completely. This is handy when a portion of the JSON document passed should not be indexed. Defaults to <B>true</B>.
     */
    ENABLED enabled() default ENABLED.DEFAULT;
    
    /**
     * In the core_types section, a field can have a index_name associated with it in order to control the name of the field that will be stored within the index. When that field exists within an object(s) that are not the root object, the name of the field of the index can either include the full “path” to the field with its index_name, or just the index_name.
     */
    String path() default "";
    
    /**
     * Should the field be included in the <B>_all</B> field (if enabled). Defaults to <B>true</B> or to the parent object type setting.
     */
    INCLUDE_IN_ALL include_in_all() default INCLUDE_IN_ALL.DEFAULT;
}
