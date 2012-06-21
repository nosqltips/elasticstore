package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.BinaryType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
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
        String json = BinaryTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"binary\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        BinaryType anno = f.getAnnotation(BinaryType.class);
        String json = BinaryTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"binary\",\"index_name\":\"another\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        String json = BinaryTypeUtil.generateSchema(null);
        assertNull(json);
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
