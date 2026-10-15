package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/** Uses inferred field mapping. On a property, overrides a class-level IgnoreType policy. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Inherited
public @interface DefaultType {
}
