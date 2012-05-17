package com.nosqlrevolution.util;

import com.nosqlrevolution.annotation.Id;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cbrown
 */
public class ReflectionUtil {
    private static final Logger logger = Logger.getLogger(ReflectionUtil.class.getName());
    public static String getId(Object o, String idField) {
        Class<?> clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        // Check to see if there is an annotation
        for (Field f: fields) {
            Id id = f.getAnnotation(Id.class);
            if (id != null) {
                return getFieldValue(f, o);
            }
        }
        
        // Check to see if there is a value for the specified id field
        if (idField != null) {
            try {
                Field f = clazz.getDeclaredField(idField);
                return getFieldValue(f, o);
            } catch (NoSuchFieldException ex) {
//                if (logger.isLoggable(Level.WARNING)) {
//                    logger.log(Level.SEVERE, null, ex);
//                }
            } catch (SecurityException ex) {
//                if (logger.isLoggable(Level.WARNING)) {
//                    logger.log(Level.SEVERE, null, ex);
//                }
            }
        }
        
        // Check for a field of id or _id
        for (Field f: fields) {
            String name = f.getName();
            if (name.toLowerCase().equals("id") || name.toLowerCase().equals("_id")) {
                return getFieldValue(f, o);
            }
        }
        
        // No annotation or id field, return null
        return null;
    }

    private static String getFieldValue(Field f, Object o) {
        try {
            f.setAccessible(true);
            if (f.getType().equals(java.lang.String.class)) {
                return f.get(o).toString();
            } else if(f.getType().equals(java.lang.Long.class)) {
                return Long.toString((Long)f.get(o));
            } else if(f.getType().equals(java.lang.Integer.class)) {
                return Integer.toString((Integer)f.get(o));
            } else if(f.getType().equals(long.class)) {
                return Long.toString(f.getLong(o));
            } else if(f.getType().equals(int.class)) {
                return Integer.toString(f.getInt(o));
            }
        } catch (SecurityException ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (IllegalArgumentException ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (IllegalAccessException ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
    }
}