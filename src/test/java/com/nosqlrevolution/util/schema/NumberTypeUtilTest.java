package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.NumberType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class NumberTypeUtilTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        Field f = Basic.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "integer");
    }
     
    @Test
    public void Boost() throws IOException, NoSuchFieldException {
        Field f = Boost.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "integer");
        assertEquals(map.get("boost"), 2.2F);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "integer");
        assertEquals(map.get("include_in_all"), "true");
    }
     
    @Test
    public void Index() throws IOException, NoSuchFieldException {
        Field f = Index.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "integer");
        assertEquals(map.get("index"), "not_analyzed");
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "integer");
        assertEquals(map.get("index_name"), "another");
    }
     
    @Test
    public void NullValue() throws IOException, NoSuchFieldException {
        Field f = NullValue.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "integer");
        assertEquals(map.get("null_value"), "NA");
    }
     
    @Test
    public void Store() throws IOException, NoSuchFieldException {
        Field f = Store.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "integer");
        assertEquals(map.get("store"), "yes");
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 7);
        assertEquals(map.get("type"), "integer");
        assertEquals(map.get("index_name"), "another");
        assertEquals(map.get("store"), "yes");
        assertEquals(map.get("index"), "analyzed");
        assertEquals(map.get("boost"), 2.2F);
        assertEquals(map.get("null_value"), "NA");
        assertEquals(map.get("include_in_all"), "true");
    }
    
    @Test
    public void Byte() throws IOException, NoSuchFieldException {
        Field f = Byte.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "byte");
    }
     
    @Test
    public void Double() throws IOException, NoSuchFieldException {
        Field f = Double.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "double");
    }
     
    @Test
    public void Float() throws IOException, NoSuchFieldException {
        Field f = Float.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "float");
    }
     
    @Test
    public void Integer() throws IOException, NoSuchFieldException {
        Field f = Integer.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "integer");
    }
     
    @Test
    public void Long() throws IOException, NoSuchFieldException {
        Field f = Long.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "long");
    }
     
    @Test
    public void Short() throws IOException, NoSuchFieldException {
        Field f = Short.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        Map<String, Object> map = NumberTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "short");
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        Map<String, Object> map = NumberTypeUtil.generateSchema(null);
        assertNull(map);
    }
     
    public class Basic {
        @NumberType
        public String field;
    }

    public class Boost {
        @NumberType(boost=2.2F)
        public String field;
    }

    public class IncludeInAll {
        @NumberType(include_in_all=Schema.INCLUDE_IN_ALL.TRUE)
        public String field;
    }

    public class Index {
        @NumberType(index=Schema.INDEX.NOT_ANALYZED)
        public String field;
    }

    public class IndexName {
        @NumberType(index_name="another")
        public String field;
    }

    public class NullValue {
        @NumberType(null_value="NA")
        public String field;
    }

    public class Store {
        @NumberType(store=Schema.STORE.YES)
        public String field;
    }

    public class Byte {
        @NumberType(type=Schema.NUMERIC_TYPE.BYTE)
        public String field;
    }

    public class Double {
        @NumberType(type=Schema.NUMERIC_TYPE.DOUBLE)
        public String field;
    }

    public class Float {
        @NumberType(type=Schema.NUMERIC_TYPE.FLOAT)
        public String field;
    }

    public class Integer {
        @NumberType(type=Schema.NUMERIC_TYPE.INTEGER)
        public String field;
    }

    public class Long {
        @NumberType(type=Schema.NUMERIC_TYPE.LONG)
        public String field;
    }

    public class Short {
        @NumberType(type=Schema.NUMERIC_TYPE.SHORT)
        public String field;
    }

    public class All {
        @NumberType(index=Schema.INDEX.ANALYZED, 
                boost=2.2F, include_in_all=Schema.INCLUDE_IN_ALL.TRUE, index_name="another", null_value="NA", store=Schema.STORE.YES)
        public String field;
    }
}
