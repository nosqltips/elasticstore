package com.nosqlrevolution;

import com.nosqlrevolution.model.Person;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class PersonIndexTest extends com.nosqlrevolution.support.LiveIndexTestSupport {
     @Test(timeout=30000)
     public void testCRUD() throws Exception {
            Index<Person> index = elasticStore.getIndex(Person.class, indexName);
            
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
            
            // Delete
            index.remove(p);
            Person p4 = index.findById("2");
            assertNull(p4);
     }
}
