package com.nosqlrevolution.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 *
 * @author cbrown
 */
public class JsonUtil {    
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    
    /** Document identity is a top-level scalar; nested IDs belong to child objects. */
    public static String getId(String json, String idField) {
        JsonNode root = object(json);
        if (idField != null) return id(root.get(idField));
        for (String name : new String[] { "id", "_id", "Id", "_Id", "ID", "_ID", "iD", "_iD" }) {
            JsonNode value = root.get(name);
            if (value != null && !value.isNull()) return id(value);
        }
        return null;
    }

    private static String id(JsonNode value) {
        if (value == null || value.isNull()) return null;
        if (!value.isTextual() && !value.isIntegralNumber()) {
            throw new IllegalArgumentException("Document ID must be a string or integer");
        }
        String id = value.asText();
        if (id.trim().isEmpty()) throw new IllegalArgumentException("Document ID must not be blank");
        return id;
    }

    private static com.fasterxml.jackson.databind.node.ObjectNode object(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            if (root == null || !root.isObject()) throw new IllegalArgumentException("Document must be a JSON object");
            return (com.fasterxml.jackson.databind.node.ObjectNode) root;
        } catch (IOException ex) {
            throw new java.io.UncheckedIOException("Cannot parse document JSON", ex);
        }
    }

    public static String removeIdFromSource(String source, String idField) {
        com.fasterxml.jackson.databind.node.ObjectNode root = object(source);
        root.remove("_id");
        return root.toString();
    }

    public static String getIdFromSource(String s, String sourceField) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readValue(s, JsonNode.class);
            JsonNode id;
            // Check the specified idField if available
            if (sourceField != null) {
                id = rootNode.findValue(sourceField);
                if (id != null) {
                    return id.asText();
                }
            } else {
                id = rootNode.findValue("source");
                if (id != null) {
                    return id.asText();
                } 
                id = rootNode.findValue("_source");
                if (id != null) {
                    return id.asText();
                } 
            }
        } catch (IOException ex) {
            throw new java.io.UncheckedIOException("Cannot parse document JSON", ex);
        }
                        
        // No id found, return null
        return s;
    }    
}
