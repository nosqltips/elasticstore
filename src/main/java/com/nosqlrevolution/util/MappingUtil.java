package com.nosqlrevolution.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author cbrown
 */
public class MappingUtil<T> {
    private static final Logger logger = Logger.getLogger(MappingUtil.class.getName());
    ObjectMapper mapper = new ObjectMapper();
    
    public T get(T t, String s) {
        if (s == null) { return null; }
        
        try {
            return (T) mapper.readValue(s, t.getClass());
        } catch (IOException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, e);
            }
            return null;
        }
    }
    
    // TODO: probably need a throws here to bubble the exception up.
    public String asString(T t) {
        try {
            return mapper.writeValueAsString(t);
        } catch (IOException ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
    
    // TODO: probably need a throws here to bubble the exception up.
    public Object asClass(String json, Class clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
}