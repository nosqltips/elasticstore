package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.BinaryType;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class BinaryTypeUtilTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        Field f = Basic.class.getField("field");
        BinaryType anno = f.getAnnotation(BinaryType.class);
        Map<String, Object> map = BinaryTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "binary");
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        BinaryType anno = f.getAnnotation(BinaryType.class);
        Map<String, Object> map = BinaryTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "binary");
        assertEquals(map.get("index_name"), "another");
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        Map<String, Object> map = BinaryTypeUtil.generateSchema(null);
        assertNull(map);
    }
     
    public class Basic {
        @BinaryType
        public String field;
    }

    public class IndexName {
        @BinaryType(index_name="another")
        public String field;
    }
}
