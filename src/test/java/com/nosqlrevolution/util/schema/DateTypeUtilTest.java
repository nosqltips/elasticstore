package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.DateType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class DateTypeUtilTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        Field f = Basic.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        String json = DateTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"date\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Boost() throws IOException, NoSuchFieldException {
        Field f = Boost.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        String json = DateTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"date\",\"boost\":2.2}";
        assertEquals(expected, json);
    }
     
    @Test
    public void PrecisionStep() throws IOException, NoSuchFieldException {
        Field f = PrecisionStep.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        String json = DateTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"date\",\"precision_step\":5}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        String json = DateTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"date\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Index() throws IOException, NoSuchFieldException {
        Field f = Index.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        String json = DateTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"date\",\"index\":\"not_analyzed\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        String json = DateTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"date\",\"index_name\":\"another\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Format() throws IOException, NoSuchFieldException {
        Field f = Format.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        String json = DateTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"date\",\"format\":\"datetime\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullValue() throws IOException, NoSuchFieldException {
        Field f = NullValue.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        String json = DateTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"date\",\"null_value\":\"NA\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Store() throws IOException, NoSuchFieldException {
        Field f = Store.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        String json = DateTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"date\",\"store\":\"yes\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        String json = DateTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"date\",\"index_name\":\"another\",\"format\":\"datetime\",\"store\":\"yes\",\"index\":\"analyzed\",\"precision_step\":5,\"boost\":2.2,\"null_value\":\"NA\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        String json = DateTypeUtil.generateSchema(null);
        assertNull(json);
    }
     
    public class Basic {
        @DateType
        public String field;
    }

    public class Boost {
        @DateType(boost=2.2f)
        public String field;
    }

    public class PrecisionStep {
        @DateType(precision_step=5)
        public String field;
    }

    public class IncludeInAll {
        @DateType(include_in_all=Schema.INCLUDE_IN_ALL.TRUE)
        public String field;
    }

    public class Index {
        @DateType(index=Schema.INDEX.NOT_ANALYZED)
        public String field;
    }

    public class IndexName {
        @DateType(index_name="another")
        public String field;
    }

    public class Format {
        @DateType(format="datetime")
        public String field;
    }

    public class NullValue {
        @DateType(null_value="NA")
        public String field;
    }

    public class Store {
        @DateType(store=Schema.STORE.YES)
        public String field;
    }

    public class All {
        @DateType(index=Schema.INDEX.ANALYZED, precision_step=5, boost=2.2f, include_in_all=Schema.INCLUDE_IN_ALL.TRUE, 
                index_name="another", format="datetime", null_value="NA", store=Schema.STORE.YES)
        public String field;
    }
}
