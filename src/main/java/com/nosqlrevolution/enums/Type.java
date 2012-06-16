package com.nosqlrevolution.enums;

/**
 *
 * @author cbrown
 */
public enum Type {
    STRING("string"),
    DATE("date"),
    BOOLEAN("boolean"),
    GEO_POINT("geo_point"),
    BINARY("binary"),
    OBJECT("object"),
    NESTED("nested"),
    MULTI_FIELD("multi_field"),
    ATTACHEMENT("attachement"),
    IP("ip");

    private String name;
    Type(String name) {
        this.name = name;
    }
    public String getName() { return name; }
}
