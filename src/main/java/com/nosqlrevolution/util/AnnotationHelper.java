package com.nosqlrevolution.util;

import com.nosqlrevolution.annotation.DocumentId;
import com.nosqlrevolution.annotation.Routing;
import com.nosqlrevolution.annotation.UUIDProvider;
import static com.nosqlrevolution.annotation.UUIDProvider.Type.RANDOM;
import static com.nosqlrevolution.annotation.UUIDProvider.Type.RANDOM_64BIT;
import com.nosqlrevolution.annotation.Version;
import com.nosqlrevolution.annotation.index.Index;
import com.nosqlrevolution.annotation.index.IndexType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 *
 * @author cbrown
 */
public class AnnotationHelper {

    /**
     * Return the documentId for this document if found or null. Method looks for @DocumentId, "id", "_id", or specified
     * field value. Will inject a UUID first of the field is annotated @UUIDProvider.
     *
     * @param o
     * @param namedField
     * @return
     */
    public static String getDocumentId(Object o, String namedField) {
        Class<?> clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        // Inject a UUID if annotation is present
        for (Field f : fields) {
            UUIDProvider provider = f.getAnnotation(UUIDProvider.class);
            if (provider != null) {
                String uuid = null;
                if (provider.value() == RANDOM) {
                    uuid = UUID.randomUUID().toString();
                }
                ReflectionUtil.setFieldValue(f, o, uuid);
            }
        }

        return getAnnotationValue(o, DocumentId.class, namedField, "id");
    }

    /**
     * Return the routing value for this document or null. Method looks for @Routing, "routing", "_routing", or
     * specified field value. Routing is used to specify what shard or shards to target when performing an operation.
     *
     * @param o
     * @param namedField
     * @return
     */
    public static String getRoutingValue(Object o, String namedField) {
        return getAnnotationValue(o, Routing.class, namedField, "routing");
    }

    /**
     * Return the document version or null; Method looks for
     *
     * @Version, "version", "_version", or specified field value. Version is used to control the document version
     * externally.
     *
     * @param o
     * @param namedField
     * @return
     */
    public static String getVersionValue(Object o, String namedField) {
        return getAnnotationValue(o, Version.class, namedField, "version");
    }

    /**
     * Return the name of the index to be used or null; Method looks for @Index, or specified field value. A value must
     * be supplied if specified
     *
     * @param o
     * @param namedField
     * @return
     */
    public static String getIndexValue(Class<?> clazz) {
        Index index = ReflectionUtil.getAnnotation(clazz, Index.class);
        return ((index == null) || (index.value().isEmpty())) ? null : index.value();
    }

    /**
     * Return the name of the index type to be used or null; Method looks for @IndexType annotation. Returns the
     * specified value or the camel-cased name of the class
     *
     * @param o
     * @param namedField
     * @return
     */
    public static String getIndexTypeValue(Class<?> clazz) {
        IndexType indexType = ReflectionUtil.getAnnotation(clazz, IndexType.class);
        if (indexType != null) {
            if (! indexType.value().isEmpty()) {
                return indexType.value();
            } else {
                // Return the name of the class camel-cased
                String name = clazz.getSimpleName();
                if (name == null) {
                    throw new IllegalArgumentException("IndexType is not specified and class is not named (anonymous or local class");
                }
                return name.substring(0, 1).toLowerCase() + name.substring(1);
            }
        }
        return null;
    }

    /**
     * Used to return a value from a field or method based on annotation or field names
     *
     * @param o
     * @param annotation
     * @param namedField
     * @param standardField
     * @return
     */
    private static String getAnnotationValue(Object o, Class<? extends Annotation> annotation, String namedField, String standardField) {
        Class<?> clazz = o.getClass();

        Field[] fields = clazz.getDeclaredFields();
        // Check to see if there is an annotation on a field
        for (Field f : fields) {
            if (f.isAnnotationPresent(annotation)) {
                return ReflectionUtil.getFieldValue(f, o);
            }
        }

        // Check to see if there is an annotation on a method
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(annotation)) {
                return ReflectionUtil.getMethodValue(m, o);
            }
        }

        // Check to see if there is a value for the specified id field
        if (namedField != null) {
            try {
                Field f = clazz.getDeclaredField(namedField);
                return ReflectionUtil.getFieldValue(f, o);
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

        if (standardField != null) {
            String withUnderbar = "_" + standardField;
            // Check for a field of id or _id
            for (Field f : fields) {
                String name = f.getName();
                if (name.toLowerCase().equals(standardField) || name.toLowerCase().equals(withUnderbar)) {
                    return ReflectionUtil.getFieldValue(f, o);
                }
            }
        }
        // No annotation, specified or standard field, return null
        return null;
    }
}
