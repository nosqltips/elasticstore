package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.DateType;
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
public class DateTypeUtilTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        Field f = Basic.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        Map<String, Object> map = DateTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "date");
    }
     
    @Test
    public void Boost() throws IOException, NoSuchFieldException {
        Field f = Boost.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        Map<String, Object> map = DateTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "date");
        assertEquals(map.get("boost"), 2.2F);
    }
     
    @Test
    public void PrecisionStep() throws IOException, NoSuchFieldException {
        Field f = PrecisionStep.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        Map<String, Object> map = DateTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "date");
        assertEquals(map.get("precision_step"), 5);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        Map<String, Object> map = DateTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "date");
        assertEquals(map.get("include_in_all"), "true");
    }
     
    @Test
    public void Index() throws IOException, NoSuchFieldException {
        Field f = Index.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        Map<String, Object> map = DateTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "date");
        assertEquals(map.get("index"), "not_analyzed");
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        Map<String, Object> map = DateTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "date");
        assertEquals(map.get("index_name"), "another");
    }
     
    @Test
    public void Format() throws IOException, NoSuchFieldException {
        Field f = Format.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        Map<String, Object> map = DateTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "date");
        assertEquals(map.get("format"), "datetime");
    }
     
    @Test
    public void NullValue() throws IOException, NoSuchFieldException {
        Field f = NullValue.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        Map<String, Object> map = DateTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "date");
        assertEquals(map.get("null_value"), "NA");
    }
     
    @Test
    public void Store() throws IOException, NoSuchFieldException {
        Field f = Store.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        Map<String, Object> map = DateTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "date");
        assertEquals(map.get("store"), "yes");
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        DateType anno = f.getAnnotation(DateType.class);
        Map<String, Object> map = DateTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 9);
        assertEquals(map.get("type"), "date");
        assertEquals(map.get("boost"), 2.2F);
        assertEquals(map.get("precision_step"), 5);
        assertEquals(map.get("include_in_all"), "true");
        assertEquals(map.get("index"), "analyzed");
        assertEquals(map.get("index_name"), "another");
        assertEquals(map.get("format"), "datetime");
        assertEquals(map.get("null_value"), "NA");
        assertEquals(map.get("store"), "yes");
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        Map<String, Object> map = DateTypeUtil.generateSchema(null);
        assertNull(map);
    }
     
    public class Basic {
        @DateType
        public String field;
    }

    public class Boost {
        @DateType(boost=2.2F)
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
        @DateType(index=Schema.INDEX.ANALYZED, precision_step=5, boost=2.2F, include_in_all=Schema.INCLUDE_IN_ALL.TRUE, 
                index_name="another", format="datetime", null_value="NA", store=Schema.STORE.YES)
        public String field;
    }
}
