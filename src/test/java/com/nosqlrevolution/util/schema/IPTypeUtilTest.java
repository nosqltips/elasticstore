package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.IPType;
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
public class IPTypeUtilTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        Field f = Basic.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        Map<String, Object> map = IPTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "ip");
    }
     
    @Test
    public void Boost() throws IOException, NoSuchFieldException {
        Field f = Boost.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        Map<String, Object> map = IPTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "ip");
        assertEquals(map.get("boost"), 2.2F);
    }
     
    @Test
    public void Precision() throws IOException, NoSuchFieldException {
        Field f = Precision.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        Map<String, Object> map = IPTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "ip");
        assertEquals(map.get("precision_step"), 5);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        Map<String, Object> map = IPTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "ip");
        assertEquals(map.get("include_in_all"), "true");
    }
     
    @Test
    public void Index() throws IOException, NoSuchFieldException {
        Field f = Index.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        Map<String, Object> map = IPTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "ip");
        assertEquals(map.get("index"), "not_analyzed");
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        Map<String, Object> map = IPTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "ip");
        assertEquals(map.get("index_name"), "another");
    }
     
    @Test
    public void NullValue() throws IOException, NoSuchFieldException {
        Field f = NullValue.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        Map<String, Object> map = IPTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "ip");
        assertEquals(map.get("null_value"), "NA");
    }
     
    @Test
    public void Store() throws IOException, NoSuchFieldException {
        Field f = Store.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        Map<String, Object> map = IPTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "ip");
        assertEquals(map.get("store"), "yes");
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        Map<String, Object> map = IPTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 7);
        assertEquals(map.get("type"), "ip");
        assertEquals(map.get("index_name"), "another");
        assertEquals(map.get("store"), "yes");
        assertEquals(map.get("index"), "analyzed");
        assertEquals(map.get("boost"), 2.2F);
        assertEquals(map.get("null_value"), "NA");
        assertEquals(map.get("include_in_all"), "true");
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        Map<String, Object> map = IPTypeUtil.generateSchema(null);
        assertNull(map);
    }
     
    public class Basic {
        @IPType
        public String field;
    }

    public class Boost {
        @IPType(boost=2.2F)
        public String field;
    }

    public class Precision {
        @IPType(precision_step=5)
        public String field;
    }

    public class IncludeInAll {
        @IPType(include_in_all=Schema.INCLUDE_IN_ALL.TRUE)
        public String field;
    }

    public class Index {
        @IPType(index=Schema.INDEX.NOT_ANALYZED)
        public String field;
    }

    public class IndexName {
        @IPType(index_name="another")
        public String field;
    }

    public class NullValue {
        @IPType(null_value="NA")
        public String field;
    }

    public class Store {
        @IPType(store=Schema.STORE.YES)
        public String field;
    }

    public class All {
        @IPType(index=Schema.INDEX.ANALYZED, 
                boost=2.2F, include_in_all=Schema.INCLUDE_IN_ALL.TRUE, index_name="another", null_value="NA", store=Schema.STORE.YES)
        public String field;
    }
}
