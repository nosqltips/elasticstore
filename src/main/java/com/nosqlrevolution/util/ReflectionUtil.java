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
    public static String getId(Object o) {
        Class<?> clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        // Check to see if there is an annotation
        for (Field f: fields) {
            Id id = f.getAnnotation(Id.class);
            if (id != null) {
                return getFieldValue(f, o);
            }
        }
        
        // Check for a field of id or _id
        for (Field f: fields) {
            String name = f.getName();
            if (name.equals("id") || name.equals("_id")) {
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
            Logger.getLogger(ReflectionUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ReflectionUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ReflectionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}