package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * This sets the field to index:false, store:false, include_in_all:false so the ElasticSearch completely ignores this field.
 * This is useful for fields in your document that you do not want to store or search in any way.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Inherited
public @interface IgnoreType {
}
