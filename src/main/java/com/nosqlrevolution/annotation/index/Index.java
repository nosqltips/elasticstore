package com.nosqlrevolution.annotation.index;

import java.lang.annotation.*;

/**
 * Used to define an index for this document.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface Index {
    /**
     * The name of the index in which this document will be stored.
     */
    String value();    
}
