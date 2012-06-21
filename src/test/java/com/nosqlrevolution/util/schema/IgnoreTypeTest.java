package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.IgnoreType;
import java.io.IOException;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class IgnoreTypeTest {
    @Test
    public void All() throws IOException, NoSuchFieldException {
        String json = IgnoreTypeUtil.generateSchema();
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"store\":\"no\",\"analyzer\":\"no\",\"include_in_all\":\"false\"}";
        assertEquals(expected, json);
    }

    public class All {
        @IgnoreType
        public String field;
    }
}
