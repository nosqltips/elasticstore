package com.nosqlrevolution.service;

import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.model.Basic;
import java.io.IOException;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class SchemaServiceTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException, Exception {
        String json = SchemaService.generateSchema(Basic.class, "type");
        assertNotNull(json);
        System.out.println("json=" + json);
        String expected = "{\"type\":{\"properties\":{\"field\":{\"type\":\"string\"}}}}";
        assertEquals(expected, json);

        ElasticStore es = new ElasticStore().asLocal().asMemoryOnly().execute();
        Index<Basic> index = es.getIndex(Basic.class, "twitter", "type");
        index.applyMapping(json);
        
        index.write(new Basic().setField("something"));
    }
}
