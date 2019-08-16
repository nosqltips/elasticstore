package com.nosqlrevolution.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cbrown
 */
public class JsonUtil {    
    private static final Logger LOGGER = Logger.getLogger(JsonUtil.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    public static String getId(String s, String idField) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readValue(s, JsonNode.class);
            JsonNode id;
            // Check the specified idField if available
            if (idField != null) {
                id = rootNode.findValue(idField);
                if (id != null) {
                    return id.asText();
                }
            // Check for variations of id or _id fields
            } else {
                id = rootNode.findValue("id");
                if (id != null) {
                    return id.asText();
                } 
                id = rootNode.findValue("_id");
                if (id != null) {
                    return id.asText();
                } 
                id = rootNode.findValue("Id");
                if (id != null) {
                    return id.asText();
                } 
                id = rootNode.findValue("_Id");
                if (id != null) {
                    return id.asText();
                } 
                id = rootNode.findValue("ID");
                if (id != null) {
                    return id.asText();
                } 
                id = rootNode.findValue("_ID");
                if (id != null) {
                    return id.asText();
                } 
                id = rootNode.findValue("iD");
                if (id != null) {
                    return id.asText();
                } 
                id = rootNode.findValue("_iD");
                if (id != null) {
                    return id.asText();
                } 
            }
        } catch (IOException ex) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
                        
        // No id found, return null
        return null;
    }
    
    public static String removeIdFromSource(String source, String idField) {
        try {
            Map<String, Object> sourceMap = MAPPER.readValue(source, new TypeReference<HashMap<String, Object>>(){});
            sourceMap.remove("_id");
            sourceMap.remove(idField);
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        
        return null;
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
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
                        
        // No id found, return null
        return s;
    }    
}
