package com.nosqlrevolution.enums;

/**
 *
 * @author cbrown
 */
public enum Field {
    TEXT("text"),
    SOURCE("source"),
    CREATED_AT("created_at"),

    USER("user"),
    ID("id"),
    SCREEN_NAME("screen_name"),
    
    USER_ID("user.id"),
    USER_SCREEN_NAME("user.screen_name");

    private String name;
    Field(String name) {
        this.name = name;
    }
    public String getName() { return name; }
}
