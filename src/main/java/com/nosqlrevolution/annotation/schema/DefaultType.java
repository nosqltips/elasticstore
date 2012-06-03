package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * Type the field based on the field type and ElasticSearch property defaults for that type.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface DefaultType {
}
