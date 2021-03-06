package com.nosqlrevolution.annotation;

import java.lang.annotation.*;

/**
 * Used to mark a field or method whose value is the id for this document.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface DocumentId {   
}
