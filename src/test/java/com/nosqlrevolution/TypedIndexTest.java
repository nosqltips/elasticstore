package com.nosqlrevolution;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonNode;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class TypedIndexTest {
    private static ElasticStore elasticStore;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
         elasticStore = new ElasticStore().asLocal().execute();
         assertNotNull(elasticStore);
         assertNotNull(elasticStore.getClient());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
         elasticStore.close();
    }
    
    @Test
    public void createSingleIndex() {
        try {
            Index<Object> index = elasticStore.getIndex(Object.class, "index", "type");
        } catch (Exception ex) {
            Logger.getLogger(TypedIndexTest.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
    
    // TODO add a negative test case.
}
