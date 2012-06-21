package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.ObjectType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
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
        String json = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"object\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Dynamic() throws IOException, NoSuchFieldException {
        Field f = Dynamic.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        String json = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"object\",\"dynamic\":\"strict\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Enabled() throws IOException, NoSuchFieldException {
        Field f = Enabled.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        String json = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"object\",\"enabled\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Path() throws IOException, NoSuchFieldException {
        Field f = Path.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        String json = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"object\",\"path\":\"something\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        String json = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"object\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        ObjectType anno = f.getAnnotation(ObjectType.class);
        String json = ObjectTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"object\",\"dynamic\":\"strict\",\"enabled\":\"true\",\"path\":\"something\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        String json = ObjectTypeUtil.generateSchema(null);
        assertNull(json);
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
