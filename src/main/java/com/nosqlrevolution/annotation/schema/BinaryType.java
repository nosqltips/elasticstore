package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * @param <B>index_name</B>     The name of the field that will be stored in the index. Defaults to the <B>property/field name</B>.
 * 
 * @author cbrown
*/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface BinaryType {
    /**
     * The name of the field that will be stored in the index. Defaults to the <B>property/field name</B>.
     */
    String index_name() default "";
}
