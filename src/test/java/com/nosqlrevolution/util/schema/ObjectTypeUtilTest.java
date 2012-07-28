package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.BooleanType;
import com.nosqlrevolution.annotation.schema.ObjectType;
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
public class ObjectTypeUtilTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        Field f = Basic.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        Map<String, Object> map = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "object");
    }
     
    @Test
    public void Dynamic() throws IOException, NoSuchFieldException {
        Field f = Dynamic.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        Map<String, Object> map = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "object");
        assertEquals(map.get("dynamic"), "strict");
    }
     
    @Test
    public void Enabled() throws IOException, NoSuchFieldException {
        Field f = Enabled.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        Map<String, Object> map = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "object");
        assertEquals(map.get("enabled"), "true");
    }
     
    @Test
    public void Path() throws IOException, NoSuchFieldException {
        Field f = Path.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        Map<String, Object> map = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "object");
        assertEquals(map.get("path"), "something");
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        Map<String, Object> map = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "object");
        assertEquals(map.get("include_in_all"), "true");
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        Map<String, Object> map = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 5);
        assertEquals(map.get("type"), "object");
        assertEquals(map.get("dynamic"), "strict");
        assertEquals(map.get("enabled"), "true");
        assertEquals(map.get("path"), "something");
        assertEquals(map.get("include_in_all"), "true");
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        Map<String, Object> map = ObjectTypeUtil.generateSchema(null);
        assertNull(map);
    }
     
    public class Basic {
        @ObjectType
        public String field;
    }

    public class Dynamic {
        @ObjectType(dynamic=Schema.DYNAMIC.STRICT)
        public String field;
    }

    public class Enabled {
        @ObjectType(enabled=Schema.ENABLED.TRUE)
        public String field;
    }

    public class Path {
        @ObjectType(path="something")
        public String field;
    }

    public class IncludeInAll {
        @ObjectType(include_in_all=Schema.INCLUDE_IN_ALL.TRUE)
        public String field;
    }

    public class All {
        @ObjectType(dynamic=Schema.DYNAMIC.STRICT, enabled=Schema.ENABLED.TRUE, path="something", include_in_all=Schema.INCLUDE_IN_ALL.TRUE)
        public String field;
    }
}
