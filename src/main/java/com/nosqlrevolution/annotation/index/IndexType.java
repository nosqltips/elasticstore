package com.nosqlrevolution.annotation.index;

import java.lang.annotation.*;

/**
 * Used to set the type on an index
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface IndexType {
    /**
     * The name of the type in which this document will be stored.
     * Default will take the name of the class as the type.
     */
    String value() default "";
}
