package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.BooleanType;
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
public class BooleanTypeUtilTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        Field f = Basic.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        Map<String, Object> map = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "boolean");
    }
     
    @Test
    public void Boost() throws IOException, NoSuchFieldException {
        Field f = Boost.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        Map<String, Object> map = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "boolean");
        assertEquals(map.get("boost"), 2.2F);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        Map<String, Object> map = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "boolean");
        assertEquals(map.get("include_in_all"), "true");
    }
     
    @Test
    public void Index() throws IOException, NoSuchFieldException {
        Field f = Index.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        Map<String, Object> map = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "boolean");
        assertEquals(map.get("index"), "not_analyzed");
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        Map<String, Object> map = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "boolean");
        assertEquals(map.get("index_name"), "another");
    }
     
    @Test
    public void NullValue() throws IOException, NoSuchFieldException {
        Field f = NullValue.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        Map<String, Object> map = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "boolean");
        assertEquals(map.get("null_value"), "NA");
    }
     
    @Test
    public void Store() throws IOException, NoSuchFieldException {
        Field f = Store.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        Map<String, Object> map = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "boolean");
        assertEquals(map.get("store"), "yes");
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        BooleanType anno = f.getAnnotation(BooleanType.class);
        Map<String, Object> map = BooleanTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 7);
        assertEquals(map.get("type"), "boolean");
        assertEquals(map.get("index_name"), "another");
        assertEquals(map.get("store"), "yes");
        assertEquals(map.get("index"), "analyzed");
        assertEquals(map.get("boost"), 2.2F);
        assertEquals(map.get("include_in_all"), "true");
        assertEquals(map.get("null_value"), "NA");
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        Map<String, Object> map = BooleanTypeUtil.generateSchema(null);
        assertNull(map);
    }
     
    public class Basic {
        @BooleanType
        public String field;
    }

    public class Boost {
        @BooleanType(boost=2.2F)
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
        @BooleanType(index=Schema.INDEX.ANALYZED, boost=2.2F, include_in_all=Schema.INCLUDE_IN_ALL.TRUE, index_name="another", null_value="NA", store=Schema.STORE.YES)
        public String field;
    }
}
