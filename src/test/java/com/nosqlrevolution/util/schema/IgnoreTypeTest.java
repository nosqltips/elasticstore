package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.IgnoreType;
import java.io.IOException;
import java.util.Map;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class IgnoreTypeTest {
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Map<String, Object> map = IgnoreTypeUtil.generateSchema();
        assertNotNull(map);
        assertEquals(map.size(), 4);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("store"), "no");
        assertEquals(map.get("analyzer"), "no");
        assertEquals(map.get("include_in_all"), "false");
    }

    public class All {
        @IgnoreType
        public String field;
    }
}
