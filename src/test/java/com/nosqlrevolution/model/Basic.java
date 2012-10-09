package com.nosqlrevolution.model;

import com.nosqlrevolution.annotation.schema.StringType;

/**
 *
 * @author cbrown
 */
public class Basic {
    @StringType
    public String field;
    
    public String getField() { return field; }
    public Basic setField(String field) { this.field = field; return this; }
}
