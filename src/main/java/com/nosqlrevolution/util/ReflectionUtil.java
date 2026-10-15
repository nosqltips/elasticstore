package com.nosqlrevolution.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** Reflection used by document identity, routing, version and UUID metadata. */
public class ReflectionUtil {
    private static String name(Member member) {
        return member.getDeclaringClass().getName() + "#" + member.getName();
    }

    private static void validateType(Class<?> type, Member member) {
        if (type != String.class && type != Object.class && type != Long.class && type != Integer.class
                && type != long.class && type != int.class) {
            throw new IllegalArgumentException("Unsupported metadata type " + type.getTypeName() + " on " + name(member));
        }
    }

    private static void access(AccessibleObject member) throws IllegalAccessException {
        if (!member.trySetAccessible()) throw new IllegalAccessException("Member is not accessible");
    }

    private static String text(Object value) {
        if (value == null) return null;
        String result = value.toString();
        if (result == null || result.trim().isEmpty()) {
            throw new IllegalArgumentException("Metadata values must not be blank");
        }
        return result;
    }

    public static String getFieldValue(Field field, Object object) {
        validateType(field.getType(), field);
        try {
            access(field);
            return text(field.get(object));
        } catch (IllegalAccessException | RuntimeException ex) {
            throw new IllegalStateException("Cannot read metadata field " + name(field), ex);
        }
    }

    public static String getMethodValue(Method method, Object object) {
        if (method.getParameterCount() != 0 || method.getReturnType() == void.class || method.getReturnType() == Void.class) {
            throw new IllegalArgumentException("Metadata getter must have no parameters and return a value: " + name(method));
        }
        validateType(method.getReturnType(), method);
        try {
            access(method);
            return text(method.invoke(object));
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof Error) throw (Error) cause;
            throw new IllegalStateException("Metadata getter failed: " + name(method), cause);
        } catch (IllegalAccessException | RuntimeException ex) {
            throw new IllegalStateException("Cannot read metadata getter " + name(method), ex);
        }
    }

    static void validateStringTarget(Field field) {
        if (field.getType() != String.class || Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
            throw new IllegalArgumentException("Generated metadata requires a mutable instance String field: " + name(field));
        }
    }

    public static void setFieldValue(Field field, Object object, String value) {
        validateStringTarget(field);
        try {
            access(field);
            field.set(object, value);
        } catch (IllegalAccessException | RuntimeException ex) {
            throw new IllegalStateException("Cannot set metadata field " + name(field), ex);
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