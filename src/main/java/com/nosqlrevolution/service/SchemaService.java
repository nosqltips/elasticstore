package com.nosqlrevolution.service;

import com.nosqlrevolution.annotation.schema.*;
import com.nosqlrevolution.util.ReflectionUtil;
import com.nosqlrevolution.util.schema.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;

/**
 * Used to generate json schema mapping files for ES.
 * 
 * @author cbrown
 */
public class SchemaService {
    public static String generateSchema(Class<?> clazz, String type) throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject()
                .startObject(type)
                    .startObject("properties");
        
        // Check to see if we need to default fields that are not explicitatly annotated.
        DefaultType classDefault = ReflectionUtil.getAnnotation(clazz, DefaultType.class);
        // We can also ingore all fields that are not annotated.
        IgnoreType classIgnore = ReflectionUtil.getAnnotation(clazz, IgnoreType.class);
        
        // Need to add support for multi-field annotation
        for (Field f : clazz.getFields()) {
            builder.field(f.getName());
            Annotation[] annotations = f.getAnnotations();
            if (annotations != null) {
                for (Annotation a: annotations) {
                    if (a instanceof StringType) {
                        builder.value(StringTypeUtil.generateSchema((StringType) a));
                    } else if (a instanceof NumberType) {
                        builder.value(NumberTypeUtil.generateSchema((NumberType) a));
                    } else if (a instanceof BooleanType) {
                        builder.value(BooleanTypeUtil.generateSchema((BooleanType) a));
                    } else if (a instanceof DateType) {
                        builder.value(DateTypeUtil.generateSchema((DateType) a));
                    } else if (a instanceof IgnoreType) {
                        builder.value(IgnoreTypeUtil.generateSchema());
                    } else if (a instanceof ObjectType) {
                        builder.value(ObjectTypeUtil.generateSchema((ObjectType) a));
                    } else if (a instanceof IPType) {
                        builder.value(IPTypeUtil.generateSchema((IPType) a));
                    } else if (a instanceof GeoPointType) {
                        builder.value(GeoPointTypeUtil.generateSchema((GeoPointType) a));
                    } else if (a instanceof BinaryType) {
                        builder.value(BinaryTypeUtil.generateSchema((BinaryType) a));
                    } else if (a instanceof DefaultType) {
                        builder.value(DefaultTypeUtil.generateSchema(f));
                    } else if (classDefault != null) {
                        builder.value(DefaultTypeUtil.generateSchema(f));
                    }
                }
                builder.endObject();
            } else if (classDefault != null) {
                builder.value(DefaultTypeUtil.generateSchema(f));
            } else if (classIgnore != null) {
                builder.value(IgnoreTypeUtil.generateSchema());
            }
            // Ignore field schema
        }
                //builder.endObject();
            builder.endObject();
        builder.endObject();

        return builder.toString();
    }
}
