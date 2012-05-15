package com.nosqlrevolution;

import com.nosqlrevolution.model.Person;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
     public void testCRUD() {
        try {
            index = elasticStore.getIndex(Person.class, "index", "type");
            
            // Create
            Person p = new Person()
                    .setId("1")
                    .setName("Homer Simpson")
                    .setUsername("hsimpson");
            index.write(p);
            
            // Read
            Person p2 = index.findOneById("1");
            
            assertNotNull(p2);
            assertEquals(p2.getId(), "1");
            assertEquals(p2.getName(), "Homer Simpson");
            assertEquals(p2.getUsername(), "hsimpson");   
            
            // Update
            p.setName("Marge Simpson");
            p.setUsername("msimpson");
            index.write(p);
            Person p3 = index.findOneById("1");
            
            assertNotNull(p3);
            assertEquals(p3.getId(), "1");
            assertEquals(p3.getName(), "Marge Simpson");
            assertEquals(p3.getUsername(), "msimpson");   
            
            // Delete
            index.remove(p);
            Person p4 = index.findOneById("1");
            
            assertNull(p4);
            
            
        } catch (Exception ex) {
            Logger.getLogger(PersonIndexTest.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
}
