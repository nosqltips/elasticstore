package com.nosqlrevolution.util;

import com.nosqlrevolution.JsonIndex;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author cbrown
 */
public class JsonUtil {    
    private static final Logger logger = Logger.getLogger(JsonUtil.class.getName());
    public static String getId(String s, String idField) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readValue(s, JsonNode.class);
            JsonNode id;
            // Check the specified idField if available
            if (idField != null) {
                id = rootNode.findValue(idField);
                if (id != null) {
                    return id.getTextValue();
                }
            // Check for variations of id or _id fields
            } else {
                id = rootNode.findValue("id");
                if (id != null) {
                    return id.getTextValue();
                } 
                id = rootNode.findValue("_id");
                if (id != null) {
                    return id.getTextValue();
                } 
                id = rootNode.findValue("Id");
                if (id != null) {
                    return id.getTextValue();
                } 
                id = rootNode.findValue("_Id");
                if (id != null) {
                    return id.getTextValue();
                } 
                id = rootNode.findValue("ID");
                if (id != null) {
                    return id.getTextValue();
                } 
                id = rootNode.findValue("_ID");
                if (id != null) {
                    return id.getTextValue();
                } 
                id = rootNode.findValue("iD");
                if (id != null) {
                    return id.getTextValue();
                } 
                id = rootNode.findValue("_iD");
                if (id != null) {
                    return id.getTextValue();
                } 
            }
        } catch (IOException ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
                        
        // No id found, return null
        return null;
    }    
}
