package com.nosqlrevolution.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to product a UUIDProvider, usually for an ID field.
 * Can be used with @DocumentId to retrieve the UUID automatically to set the document id.
 * Uses org.elasticsearch.common.UUIDProvider.
 * Can only be used with String fields. All of other types are skipped.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UUIDProvider {
    Type value() default Type.RANDOM;
    
    // TODO: need to possibly look at other types of UUIDs.
    public enum Type {
        RANDOM,
        RANDOM_64BIT;
    }
}