package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * This sets the field to index:false, store:false so the ElasticSearch completely ignores this field.
 * This is helpful if you was to access the field from _all, but don't want to search or query on this field.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface IgnoreType {
}
