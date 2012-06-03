package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/**
 * This type will combine multiple Type annotations to a multi_field. This allows a single field to be indexed multiple ways
 * with a single reference. For example, you would specify that a field be indexed without and without analysis to allow
 * a field to be searchable, but still preserve the original field value as well.
 * 
 * @author cbrown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface MultiFieldType {
    
}
