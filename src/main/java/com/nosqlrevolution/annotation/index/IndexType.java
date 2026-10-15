package com.nosqlrevolution.annotation.index;

import java.lang.annotation.*;

/**
 * Legacy mapping-type marker; Elasticsearch 8 mappings are typeless and ignore this annotation.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Deprecated
public @interface IndexType {
    /**
     * The name of the type in which this document will be stored.
     * Default will take the name of the class as the type.
     */
    String value() default "";
}
