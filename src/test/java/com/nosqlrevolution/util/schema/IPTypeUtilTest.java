package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.IPType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
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
        String json = IPTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"ip\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Boost() throws IOException, NoSuchFieldException {
        Field f = Boost.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        String json = IPTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"ip\",\"boost\":2.2}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Precision() throws IOException, NoSuchFieldException {
        Field f = Precision.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        String json = IPTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"ip\",\"precision_step\":5}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        String json = IPTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"ip\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Index() throws IOException, NoSuchFieldException {
        Field f = Index.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        String json = IPTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"ip\",\"index\":\"not_analyzed\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        String json = IPTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"ip\",\"index_name\":\"another\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullValue() throws IOException, NoSuchFieldException {
        Field f = NullValue.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        String json = IPTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"ip\",\"null_value\":\"NA\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Store() throws IOException, NoSuchFieldException {
        Field f = Store.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        String json = IPTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"ip\",\"store\":\"yes\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        IPType anno = f.getAnnotation(IPType.class);
        String json = IPTypeUtil.generateSchema(anno);
        assertNotNull(json);
        System.out.println(json);
        String expected = "{\"type\":\"ip\",\"index_name\":\"another\",\"store\":\"yes\",\"index\":\"analyzed\",\"boost\":2.2,\"null_value\":\"NA\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        String json = IPTypeUtil.generateSchema(null);
        assertNull(json);
    }
     
    public class Basic {
        @IPType
        public String field;
    }

    public class Boost {
        @IPType(boost=2.2f)
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
                boost=2.2f, include_in_all=Schema.INCLUDE_IN_ALL.TRUE, index_name="another", null_value="NA", store=Schema.STORE.YES)
        public String field;
    }
}
