package com.nosqlrevolution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.model.Person;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class JsonTypedIndexTest {
    private static ElasticStore elasticStore;
    private static Index<String> index;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
         elasticStore = new ElasticStore().asMemoryOnly().execute();
         assertNotNull(elasticStore);
         assertNotNull(elasticStore.getRestClient());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
//        elasticStore.removeIndex(index);
        elasticStore.close();
    }
    
     @Test
     public void testCRUD() throws Exception {
        index = elasticStore.getIndex(String.class, "json", "string");
        ObjectMapper mapper = new ObjectMapper();

        // Create
        Person p = new Person()
                .setId("1")
                .setName("Homer Simpson")
                .setUsername("hsimpson");
        index.write(mapper.writeValueAsString(p));
        
        // Read
        String s2 = index.findById("1");
        
        Person p2 = mapper.readValue(s2, Person.class);
        assertNotNull(p2);
        assertEquals(p2.getId(), "1");
        assertEquals(p2.getName(), "Homer Simpson");
        assertEquals(p2.getUsername(), "hsimpson");   

        // Update
        p.setName("Marge Simpson");
        p.setUsername("msimpson");
        index.write(mapper.writeValueAsString(p));
        String s3 = index.findById("1");

        Person p3 = mapper.readValue(s3, Person.class);
        assertNotNull(p3);
        assertEquals(p3.getId(), "1");
        assertEquals(p3.getName(), "Marge Simpson");
        assertEquals(p3.getUsername(), "msimpson");   

        // Delete
        index.remove(s3);
        String s4 = index.findById("1");            
        assertNull(s4);                        
     }
}
