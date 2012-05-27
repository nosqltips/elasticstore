package com.nosqlrevolution.util;

import com.nosqlrevolution.annotation.DocumentId;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        
        // Check to see if there is an annotation on a field
        Field[] fields = clazz.getDeclaredFields();
        for (Field f: fields) {
            DocumentId id = f.getAnnotation(DocumentId.class);
            if (id != null) {
                return getFieldValue(f, o);
            }
        }
        
        // Check to see if there is an annotation on a method
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m: methods) {
            DocumentId id = m.getAnnotation(DocumentId.class);
            if (id != null) {
                return getMethodValue(m, o);
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
            Class<?> type = f.getType();
            if ((type.equals(java.lang.String.class)) || (type.equals(java.lang.Object.class))) {
                return f.get(o).toString();
            } else if (type.equals(java.lang.Long.class)) {
                return Long.toString((Long)f.get(o));
            } else if (type.equals(java.lang.Integer.class)) {
                return Integer.toString((Integer)f.get(o));
            } else if (type.equals(long.class)) {
                return Long.toString(f.getLong(o));
            } else if (type.equals(int.class)) {
                return Integer.toString(f.getInt(o));
            }
        } catch (SecurityException ex) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (IllegalArgumentException ex) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (IllegalAccessException ex) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
    }

    private static String getMethodValue(Method m, Object o) {
        try {
            Class<?> returnType = m.getReturnType();
            m.setAccessible(true);
            if ((m.getParameterTypes().length > 0) || m.getReturnType().equals(java.lang.Void.class)) {
                // TODO: maybe exception here? method cannot take arguments
                return null;
            }
            Object[] args = new Object[0];
            if ((returnType.equals(java.lang.String.class)) || (returnType.equals(java.lang.Object.class))) {
                return m.invoke(o, args).toString();
            } else if ((returnType.equals(java.lang.Long.class)) || (returnType.equals(long.class))) {
                return Long.toString((Long)m.invoke(o, args));
            } else if ((returnType.equals(java.lang.Integer.class)) || (returnType.equals(int.class))) {
                return Integer.toString((Integer)m.invoke(o, args));
            }
        } catch (SecurityException ex) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (IllegalArgumentException ex) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (IllegalAccessException ex) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, null, ex);
            }
        } catch (InvocationTargetException ex) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
    }
}