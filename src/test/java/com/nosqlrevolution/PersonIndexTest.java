package com.nosqlrevolution;

import com.nosqlrevolution.model.Person;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class PersonIndexTest {
    private static ElasticStore elasticStore;
    private static Index<Person> index;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
         elasticStore = new ElasticStore().asLocal().execute();
         assertNotNull(elasticStore);
         assertNotNull(elasticStore.getClient());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        elasticStore.removeIndex(index);
        elasticStore.close();
    }
    
     @Test
     public void createSingleIndex() {
        try {
            index = elasticStore.getIndex(Person.class, "index", "type");
            Person p = new Person()
                    .setId("1")
                    .setName("Homer Simpson")
                    .setUsername("hsimpson");
            index.write(p);
            Person get = index.findOneById("1");
            
            assertNotNull(get);
            assertEquals(get.getId(), "1");
            assertEquals(get.getName(), "Homer Simpson");
            assertEquals(get.getUsername(), "hsimpson");               
        } catch (Exception ex) {
            Logger.getLogger(PersonIndexTest.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
}
