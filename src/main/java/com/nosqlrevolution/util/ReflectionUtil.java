package com.nosqlrevolution.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.annotation.Annotation;

/**
 *
 * @author cbrown
 */
public class ReflectionUtil {
    private static final Logger logger = Logger.getLogger(ReflectionUtil.class.getName());

    public static String getFieldValue(Field f, Object o) {
        try {
            f.setAccessible(true);
            Class<?> type = f.getType();
            if (f.get(o) == null) {
                return null;
            } else if ((type.equals(java.lang.String.class)) || (type.equals(java.lang.Object.class))) {
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

    public static String getMethodValue(Method m, Object o) {
        try {
            Class<?> returnType = m.getReturnType();
            m.setAccessible(true);
            if ((m.getParameterTypes().length > 0) || m.getReturnType().equals(java.lang.Void.class)) {
                // TODO: maybe exception here? method cannot take arguments
                return null;
            }
            Object[] args = new Object[0];
            if (m.invoke(o, args) == null) {
                return null;
            } else if ((returnType.equals(java.lang.String.class)) || (returnType.equals(java.lang.Object.class))) {
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

    public static void setFieldValue(Field f, Object o, String s) {
        try {
            f.setAccessible(true);
            Class<?> type = f.getType();
            if ((type.equals(java.lang.String.class))) {
                f.set(o, s);
            }
        } catch (IllegalAccessException ex) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationType) {
        T result = clazz.getAnnotation(annotationType);
        if (result == null) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getAnnotation(superclass, annotationType);
            } else {
                return null;
            }
        } else {
            return result;
        }
    }
}