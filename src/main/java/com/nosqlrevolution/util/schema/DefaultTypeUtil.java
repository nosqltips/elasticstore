package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.*;
import com.nosqlrevolution.enums.Schema;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a meta util that will determine the field type, then call the actual type util for generation.
 * 
 * @author cbrown
 */
public class DefaultTypeUtil {
    public static String generateSchema(Field f) {
        // Make sure field is valid
        if (f == null) { return null; }
        
        try {
            Class<?> type = f.getType();
            // String
            if (type.equals(java.lang.String.class)) {
                Field field = DefaultStringType.class.getField("field");
                StringType anno = field.getAnnotation(StringType.class);
                return StringTypeUtil.generateSchema(anno);
                // Boolean
            } else if ((type.equals(java.lang.Boolean.class)) || (type.equals(boolean.class))){
                Field field = DefaultBooleanType.class.getField("field");
                BooleanType anno = field.getAnnotation(BooleanType.class);
                return BooleanTypeUtil.generateSchema(anno);
                // Long
            } else if ((type.equals(java.lang.Long.class)) || (type.equals(long.class))){
                Field field = DefaultLongType.class.getField("field");
                NumberType anno = field.getAnnotation(NumberType.class);
                return NumberTypeUtil.generateSchema(anno);
                //Integer
            } else if ((type.equals(java.lang.Integer.class)) || (type.equals(int.class))) {
                Field field = DefaultIntegerType.class.getField("field");
                NumberType anno = field.getAnnotation(NumberType.class);
                return NumberTypeUtil.generateSchema(anno);
                //Double
            } else if ((type.equals(java.lang.Double.class)) || (type.equals(double.class))) {
                Field field = DefaultDoubleType.class.getField("field");
                NumberType anno = field.getAnnotation(NumberType.class);
                return NumberTypeUtil.generateSchema(anno);
                //Float
            } else if ((type.equals(java.lang.Float.class)) || (type.equals(float.class))) {
                Field field = DefaultFloatType.class.getField("field");
                NumberType anno = field.getAnnotation(NumberType.class);
                return NumberTypeUtil.generateSchema(anno);
                // Short
            } else if ((type.equals(java.lang.Short.class)) || (type.equals(short.class))) {
                Field field = DefaultShortType.class.getField("field");
                NumberType anno = field.getAnnotation(NumberType.class);
                return NumberTypeUtil.generateSchema(anno);
                // Byte
            } else if ((type.equals(java.lang.Byte.class)) || (type.equals(byte.class))) {
                Field field = DefaultByteType.class.getField("field");
                NumberType anno = field.getAnnotation(NumberType.class);
                return NumberTypeUtil.generateSchema(anno);
                // Date
            } else if (type.equals(java.util.Date.class)) {
                Field field = DefaultDateType.class.getField("field");
                DateType anno = field.getAnnotation(DateType.class);
                return DateTypeUtil.generateSchema(anno);
                // Assume this is an object
                // We can probably drill down later
            } else {
                Field field = DefaultObjectType.class.getField("field");
                ObjectType anno = field.getAnnotation(ObjectType.class);
                return ObjectTypeUtil.generateSchema(anno);
            }
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(DefaultTypeUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(DefaultTypeUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private final class DefaultStringType {
        @StringType public String field;
    }

    private final class DefaultBooleanType {
        @BooleanType public String field;
    }

    private final class DefaultByteType {
        @NumberType(type=Schema.NUMERIC_TYPE.BYTE) public String field;
    }

    private final class DefaultDoubleType {
        @NumberType(type=Schema.NUMERIC_TYPE.DOUBLE) public String field;
    }

    private final class DefaultFloatType {
        @NumberType(type=Schema.NUMERIC_TYPE.FLOAT) public String field;
    }

    private final class DefaultIntegerType {
        @NumberType public String field;
    }

    private final class DefaultLongType {
        @NumberType(type=Schema.NUMERIC_TYPE.LONG) public String field;
    }

    private final class DefaultShortType {
        @NumberType(type=Schema.NUMERIC_TYPE.SHORT) public String field;
    }

    private final class DefaultObjectType {
        @ObjectType public String field;
    }

    private final class DefaultDateType {
        @DateType public Date field;
    }
}
