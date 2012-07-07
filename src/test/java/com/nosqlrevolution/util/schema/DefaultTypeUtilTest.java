package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.DefaultType;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class DefaultTypeUtilTest {
    @Test
    public void StringTest() throws IOException, NoSuchFieldException {
        Field f = DefaultString.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"string\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void BooleanTest() throws IOException, NoSuchFieldException {
        Field f = DefaultBoolean.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"boolean\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void booleanTest() throws IOException, NoSuchFieldException {
        Field f = Defaultboolean.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"boolean\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IntegerTest() throws IOException, NoSuchFieldException {
        Field f = DefaultInteger.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void intTest() throws IOException, NoSuchFieldException {
        Field f = Defaultint.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void LongTest() throws IOException, NoSuchFieldException {
        Field f = DefaultLong.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"long\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void longTest() throws IOException, NoSuchFieldException {
        Field f = Defaultlong.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"long\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void DoubleTest() throws IOException, NoSuchFieldException {
        Field f = DefaultDouble.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"double\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void doubleTest() throws IOException, NoSuchFieldException {
        Field f = Defaultdouble.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"double\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void FloatTest() throws IOException, NoSuchFieldException {
        Field f = DefaultFloat.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"float\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void floatTest() throws IOException, NoSuchFieldException {
        Field f = Defaultfloat.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"float\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void ShortTest() throws IOException, NoSuchFieldException {
        Field f = DefaultShort.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"short\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void shortTest() throws IOException, NoSuchFieldException {
        Field f = Defaultshort.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"short\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void ByteTest() throws IOException, NoSuchFieldException {
        Field f = DefaultByte.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"byte\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void byteTest() throws IOException, NoSuchFieldException {
        Field f = Defaultbyte.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"byte\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void ObjectTest() throws IOException, NoSuchFieldException {
        Field f = DefaultObject.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"object\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void DateTest() throws IOException, NoSuchFieldException {
        Field f = DefaultDate.class.getField("field");
        String json = DefaultTypeUtil.generateSchema(f);
        assertNotNull(json);
        String expected = "{\"type\":\"date\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        String json = DefaultTypeUtil.generateSchema(null);
        assertNull(json);
    }
     
    public class DefaultString {
        @DefaultType
        public String field;
    }
     
    public class DefaultBoolean {
        @DefaultType
        public Boolean field;
    }
     
    public class Defaultboolean {
        @DefaultType
        public boolean field;
    }
     
    public class DefaultInteger {
        @DefaultType
        public Integer field;
    }
     
    public class Defaultint {
        @DefaultType
        public int field;
    }
     
    public class DefaultLong {
        @DefaultType
        public Long field;
    }
     
    public class Defaultlong {
        @DefaultType
        public long field;
    }
     
    public class DefaultDouble {
        @DefaultType
        public Double field;
    }
     
    public class Defaultdouble {
        @DefaultType
        public double field;
    }
     
    public class DefaultFloat {
        @DefaultType
        public Float field;
    }
     
    public class Defaultfloat {
        @DefaultType
        public float field;
    }
     
    public class DefaultShort {
        @DefaultType
        public Short field;
    }
     
    public class Defaultshort {
        @DefaultType
        public short field;
    }
     
    public class DefaultByte {
        @DefaultType
        public Byte field;
    }
     
    public class Defaultbyte {
        @DefaultType
        public byte field;
    }
     
    public class DefaultObject {
        @DefaultType
        public Object field;
    }
     
    public class DefaultDate {
        @DefaultType
        public Date field;
    }
}
