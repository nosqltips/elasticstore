package com.nosqlrevolution.query;

import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import com.nosqlrevolution.model.Person;
import org.junit.Test;
import static com.nosqlrevolution.query.Condition.*;
import static com.nosqlrevolution.query.Query.*;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;

/**
 *
 * @author cbrown
 */
public class QueryTest {
    private static String[] ids = new String[]{"1", "2", "3", "4", "5"};
    private static ElasticStore elasticStore;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
         elasticStore = new ElasticStore().asMemoryOnly().withTimeout("1").execute();
         assertNotNull(elasticStore);
         assertNotNull(elasticStore.getClient());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        elasticStore.close();
    }
    
    @Test
    // Just testing to see how the structure looks.
    public void testSomething() {
        select()
            .where(
                field("something").equal("one"), 
                and(
                    field("something").gt("some"),
                    field("something").lt("another")
                )
            );
    }
    
    public void testIn() {
        select()
            .where(
                field("id").in(ids)
            );
    }
}
