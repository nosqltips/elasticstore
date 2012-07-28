package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * Type the field based on the field type and ElasticSearch property defaults for that type.
 * When added at the class level, denotes that all fields are to be defaulted.
 * TODO: Make type level at some point.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Inherited
public @interface DefaultType {
}
