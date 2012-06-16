package com.nosqlrevolution.annotation.schema;

import com.nosqlrevolution.enums.Schema.INCLUDE_IN_ALL;
import com.nosqlrevolution.enums.Schema.INDEX;
import com.nosqlrevolution.enums.Schema.STORE;
import java.lang.annotation.*;

/**
 * @param <B>index_name</B>     The name of the field that will be stored in the index. Defaults to the <B>property/field name</B>.
 * @param <B>format</B>         The date format. Defaults to dateOptionalTime.
 * @param <B>store</B>          Set to <B>yes</B> the store actual field in the index, <B>no</B> to not store it. Defaults to <B>no</B> (note, the JSON document itself is stored, and it can be retrieved from it).
 * @param <B>index </B>         Set to <B>analyzed</B> for the field to be indexed and searchable after being broken down into token using an analyzer. <B>not_analyzed</B> means that its still searchable, but does not go through any analysis process or broken down into tokens. <B>no</B> means that it won’t be searchable at all (as an individual field; it may still be included in <B>_all</B>). Defaults to <B>analyzed</B>.
 * @param <B>precision_step</B> The precision step (number of terms generated for each number value). Defaults to <B>4</B>.
 * @param <B>boost</B>          The boost value. Defaults to <B>1.0</B>.
 * @param <B>null_value</B>     When there is a (JSON) null value for the field, use the <B>null_value</B> as the field value. Defaults to not adding the field at all.
 * @param <B>include_in_all</B> Should the field be included in the <B>_all</B> field (if enabled). Defaults to <B>true</B> or to the parent object type setting.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface DateType {
    /**
     * The name of the field that will be stored in the index. Defaults to the <B>property/field name</B>.
     */
    String index_name() default "";
    
    /**
     * The name of the field that will be stored in the index. Defaults to the property/field name.
     */
    // TODO: Add information on possible date formats.
    String format() default "dateOptionalTime";
    
    /**
     * Set to <B>yes</B> the store actual field in the index, <B>no</B> to not store it. Defaults to <B>no</B> (note, the JSON document itself is stored, and it can be retrieved from it).
     */
    STORE store() default STORE.DEFAULT;
    
    /**
     * Set to <B>analyzed</B> for the field to be indexed and searchable after being broken down into token using an analyzer. <B>not_analyzed</B> means that its still searchable, but does not go through any analysis process or broken down into tokens. <B>no</B> means that it won’t be searchable at all (as an individual field; it may still be included in <B>_all</B>). Defaults to <B>analyzed</B>.
     */
    INDEX index() default INDEX.DEFAULT;
    
    /**
     * The precision step (number of terms generated for each number value). Defaults to 4.
     */
    int precision_step() default 4;
    
    /**
     * The boost value. Defaults to <B>1.0</B>.
     */
    float boost() default 1.0f;
    
    /**
     * When there is a (JSON) null value for the field, use the <B>null_value</B> as the field value. Defaults to not adding the field at all.
     */
    String null_value() default "";
    
    /**
     * Should the field be included in the <B>_all</B> field (if enabled). Defaults to <B>true</B> or to the parent object type setting.
     */
    INCLUDE_IN_ALL include_in_all() default INCLUDE_IN_ALL.DEFAULT;
}
