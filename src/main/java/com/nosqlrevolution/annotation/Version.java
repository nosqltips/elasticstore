package com.nosqlrevolution.annotation;

import java.lang.annotation.*;

/**
 * Used to mark a field or method whose value is used as the version for this document.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited

// TODO: remember, this value can be read and written too.
public @interface Version {   
}
