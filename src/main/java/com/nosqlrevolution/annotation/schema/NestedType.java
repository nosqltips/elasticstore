package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/** Maps an object or object collection as nested documents, retaining per-object query boundaries. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
// TODO: Add more docs, posible object examples with all properties.
public @interface NestedType {    
}
