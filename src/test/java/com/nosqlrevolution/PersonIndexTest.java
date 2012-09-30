package com.nosqlrevolution;

import com.nosqlrevolution.model.Person;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import static org.junit.Assert.*;
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
     public void testCRUD() throws Exception {
            index = elasticStore.getIndex(Person.class, "index", "type");
            
            // Create
            Person p = new Person()
                    .setId("2")
                    .setName("Homer Simpson")
                    .setUsername("hsimpson");
            index.write(p);
            index.refresh();
            
            // Read
            Person p2 = index.findById("2");
            
            assertNotNull(p2);
            assertEquals(p2.getId(), "2");
            assertEquals(p2.getName(), "Homer Simpson");
            assertEquals(p2.getUsername(), "hsimpson");   
            
            // Update
            p.setName("Marge Simpson");
            p.setUsername("msimpson");
            index.write(p);
            Person p3 = index.findById("2");
            
            assertNotNull(p3);
            assertEquals(p3.getId(), "2");
            assertEquals(p3.getName(), "Marge Simpson");
            assertEquals(p3.getUsername(), "msimpson");   
            
            System.out.println("p=" + p.getId());
            // Delete
            index.remove(p);
            Person p4 = index.findById("2");
            assertNull(p4);
     }
}
