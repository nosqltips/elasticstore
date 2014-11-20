package com.nosqlrevolution.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nosqlrevolution.annotation.DocumentId;
import com.nosqlrevolution.annotation.index.Index;
import com.nosqlrevolution.annotation.index.IndexType;

/**
 *
 * @author cbrown
 */
@Index("test")
@IndexType("person")
public class PersonIndex {
    @DocumentId
    private String id;
    private String name;
    private String username;

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public PersonIndex setId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public PersonIndex setName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty
    public String getUsername() {
        return username;
    }

    @JsonProperty
    public PersonIndex setUsername(String username) {
        this.username = username;
        return this;
    }
}
