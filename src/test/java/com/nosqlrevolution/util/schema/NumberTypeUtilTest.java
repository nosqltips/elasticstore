package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.NumberType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
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
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Boost() throws IOException, NoSuchFieldException {
        Field f = Boost.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\",\"boost\":2.2}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Index() throws IOException, NoSuchFieldException {
        Field f = Index.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\",\"index\":\"not_analyzed\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\",\"index_name\":\"another\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullValue() throws IOException, NoSuchFieldException {
        Field f = NullValue.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\",\"null_value\":\"NA\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Store() throws IOException, NoSuchFieldException {
        Field f = Store.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\",\"store\":\"yes\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\",\"index_name\":\"another\",\"store\":\"yes\",\"index\":\"analyzed\",\"boost\":2.2,\"null_value\":\"NA\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
    
    @Test
    public void Byte() throws IOException, NoSuchFieldException {
        Field f = Byte.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"byte\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Double() throws IOException, NoSuchFieldException {
        Field f = Double.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"double\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Float() throws IOException, NoSuchFieldException {
        Field f = Float.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"float\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Integer() throws IOException, NoSuchFieldException {
        Field f = Integer.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"integer\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Long() throws IOException, NoSuchFieldException {
        Field f = Long.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"long\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Short() throws IOException, NoSuchFieldException {
        Field f = Short.class.getField("field");
        NumberType anno = f.getAnnotation(NumberType.class);
        String json = NumberTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"short\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        String json = NumberTypeUtil.generateSchema(null);
        assertNull(json);
    }
     
    public class Basic {
        @NumberType
        public String field;
    }

    public class Boost {
        @NumberType(boost=2.2f)
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
                boost=2.2f, include_in_all=Schema.INCLUDE_IN_ALL.TRUE, index_name="another", null_value="NA", store=Schema.STORE.YES)
        public String field;
    }
}
