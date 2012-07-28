package com.nosqlrevolution.service;

import com.nosqlrevolution.annotation.schema.StringType;
import java.io.IOException;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class SchemaServiceTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        String json = SchemaService.generateSchema(Basic.class, "twitter");
        assertNotNull(json);
        System.out.println("json=" + json);
        String expected = "{\"twitter\":{\"properties\":{\"field\":{\"type\":\"string\"}}}}";
        assertEquals(expected, json);
    }
     
    public class Basic {
        @StringType
        public String field;
    }
}
