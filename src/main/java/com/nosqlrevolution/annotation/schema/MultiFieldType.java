package com.nosqlrevolution.annotation.schema;

import java.lang.annotation.*;

/** Adds a keyword subfield to a text mapping for exact matching and sorting. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface MultiFieldType {
    /** Keyword subfield added to a text field for sorting and exact matching. */
    String keyword() default "keyword";
    int ignore_above() default 256;
}
