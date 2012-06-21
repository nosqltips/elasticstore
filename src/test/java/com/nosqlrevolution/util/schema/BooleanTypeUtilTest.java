package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.BooleanType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class BooleanTypeUtilTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        Field f = Basic.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        String json = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"boolean\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Boost() throws IOException, NoSuchFieldException {
        Field f = Boost.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        String json = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"boolean\",\"boost\":2.2}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        String json = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"boolean\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Index() throws IOException, NoSuchFieldException {
        Field f = Index.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        String json = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"boolean\",\"index\":\"not_analyzed\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        String json = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"boolean\",\"index_name\":\"another\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullValue() throws IOException, NoSuchFieldException {
        Field f = NullValue.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        String json = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"boolean\",\"null_value\":\"NA\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Store() throws IOException, NoSuchFieldException {
        Field f = Store.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        String json = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"boolean\",\"store\":\"yes\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        String json = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(json);
        System.out.println("json=" + json);
        String expected = "{\"type\":\"boolean\",\"index_name\":\"another\",\"store\":\"yes\",\"index\":\"analyzed\",\"boost\":2.2,\"null_value\":\"NA\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        String json = BooleanTypeUtil.generateSchema(null);
        assertNull(json);
    }
     
    public class Basic {
        @BooleanType
        public String field;
    }

    public class Boost {
        @BooleanType(boost=2.2f)
        public String field;
    }

    public class IncludeInAll {
        @BooleanType(include_in_all=Schema.INCLUDE_IN_ALL.TRUE)
        public String field;
    }

    public class Index {
        @BooleanType(index=Schema.INDEX.NOT_ANALYZED)
        public String field;
    }

    public class IndexName {
        @BooleanType(index_name="another")
        public String field;
    }

    public class NullValue {
        @BooleanType(null_value="NA")
        public String field;
    }

    public class Store {
        @BooleanType(store=Schema.STORE.YES)
        public String field;
    }

    public class All {
        @BooleanType(index=Schema.INDEX.ANALYZED, boost=2.2f, include_in_all=Schema.INCLUDE_IN_ALL.TRUE, index_name="another", null_value="NA", store=Schema.STORE.YES)
        public String field;
    }
}
