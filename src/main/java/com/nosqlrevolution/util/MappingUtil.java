package com.nosqlrevolution.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author cbrown
 * @param <T>
 */
public class MappingUtil<T> {
    private static final Logger logger = Logger.getLogger(MappingUtil.class.getName());
    ObjectMapper mapper = new ObjectMapper();
    
    public T get(T t, String s) {
        if (s == null) { return null; }
        
        try {
            if (t.getClass() == String.class) {
                return (T) s;
            }
            return (T) mapper.readValue(s, t.getClass());
        } catch (IOException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, e);
            }
            return null;
        }
    }
    
    // TODO: probably need a throws here to bubble the exception up.
    public String get(T t) {
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
    public <T>T get(String json, Class<T> clazz) {
        try {
            if (clazz == String.class) {
                return (T) json;
            }
            return mapper.readValue(json, clazz);
        } catch (Exception ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
}
