package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.DefaultType;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
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
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "string");
    }
     
    @Test
    public void BooleanTest() throws IOException, NoSuchFieldException {
        Field f = DefaultBoolean.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "boolean");
    }
     
    @Test
    public void booleanTest() throws IOException, NoSuchFieldException {
        Field f = Defaultboolean.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "boolean");
    }
     
    @Test
    public void IntegerTest() throws IOException, NoSuchFieldException {
        Field f = DefaultInteger.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "integer");
    }
     
    @Test
    public void intTest() throws IOException, NoSuchFieldException {
        Field f = Defaultint.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "integer");
    }
     
    @Test
    public void LongTest() throws IOException, NoSuchFieldException {
        Field f = DefaultLong.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "long");
    }
     
    @Test
    public void longTest() throws IOException, NoSuchFieldException {
        Field f = Defaultlong.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "long");
    }
     
    @Test
    public void DoubleTest() throws IOException, NoSuchFieldException {
        Field f = DefaultDouble.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "double");
    }
     
    @Test
    public void doubleTest() throws IOException, NoSuchFieldException {
        Field f = Defaultdouble.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "double");
    }
     
    @Test
    public void FloatTest() throws IOException, NoSuchFieldException {
        Field f = DefaultFloat.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "float");
    }
     
    @Test
    public void floatTest() throws IOException, NoSuchFieldException {
        Field f = Defaultfloat.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "float");
    }
     
    @Test
    public void ShortTest() throws IOException, NoSuchFieldException {
        Field f = DefaultShort.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "short");
    }
     
    @Test
    public void shortTest() throws IOException, NoSuchFieldException {
        Field f = Defaultshort.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "short");
    }
     
    @Test
    public void ByteTest() throws IOException, NoSuchFieldException {
        Field f = DefaultByte.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "byte");
    }
     
    @Test
    public void byteTest() throws IOException, NoSuchFieldException {
        Field f = Defaultbyte.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "byte");
    }
     
    @Test
    public void ObjectTest() throws IOException, NoSuchFieldException {
        Field f = DefaultObject.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "object");
    }
     
    @Test
    public void DateTest() throws IOException, NoSuchFieldException {
        Field f = DefaultDate.class.getField("field");
        Map<String, Object> map = DefaultTypeUtil.generateSchema(f);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "date");
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        Map<String, Object> map = DefaultTypeUtil.generateSchema(null);
        assertNull(map);
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
