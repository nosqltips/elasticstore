package com.nosqlrevolution.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nosqlrevolution.annotation.DocumentId;

/**
 *
 * @author cbrown
 */
public class Person {
    @DocumentId
    private String id;
    private String name;
    private String username;

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public Person setId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public Person setName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty
    public String getUsername() {
        return username;
    }

    @JsonProperty
    public Person setUsername(String username) {
        this.username = username;
        return this;
    }

    @Override
    public String toString() {
        return "id=" + id;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        System.out.println("obj=" + obj);
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Person)) return false;

        Person p = (Person) obj;
        if (this.id.equals(p.getId())) {
            return true;
        } else {
            return false;
        }
    }
}
