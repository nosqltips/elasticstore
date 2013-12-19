package com.nosqlrevolution;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class JsonIndexTest {
    private static ElasticStore elasticStore;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
         elasticStore = new ElasticStore().asMemoryOnly().execute();
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
            Index<String> index = elasticStore.getIndex(String.class, "index", "type");
        } catch (Exception ex) {
            Logger.getLogger(JsonIndexTest.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
}
