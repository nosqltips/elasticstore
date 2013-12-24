package com.nosqlrevolution.cursor;

import com.nosqlrevolution.cursor.Cursor;
import com.nosqlrevolution.cursor.MultiGetCursor;
import com.nosqlrevolution.model.Person;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.TestDataHelper;
import java.util.Arrays;
import java.util.Iterator;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class MultiGetCursorTest {
    private static Client client;
    private static final String index = "test";
    private static final String type = "data";
    private static final String[] ids = new String[]{"1", "2", "3", "4", "5"};
    private static QueryService service;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        client = TestDataHelper.createTestClient();
        
        TestDataHelper.indexCursorTestData(client, index, type);
        service = new QueryService(client);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        client.close();
    }

    @Test
    public void testSize() {
        Cursor<Person> instance = new MultiGetCursor(Person.class, service.realTimeMultiGet(index, type, ids));
        assertEquals(ids.length, instance.size());
    }

    @Test
    public void testIsEmpty() {
        Cursor<Person> instance = new MultiGetCursor(Person.class, service.realTimeMultiGet(index, type, ids));
        assertFalse(instance.isEmpty());
    }

    @Test
    public void testIterator() {
        Cursor<Person> instance = new MultiGetCursor(Person.class, service.realTimeMultiGet(index, type, ids));
        Iterator<Person> it = instance.iterator();
                
        // Make sure we got a real iterator instance
        assertNotNull(it);
        
        // Run through the iterator and see what we get.
        while (it.hasNext()) {
            Person p = it.next();
            assertTrue(Arrays.binarySearch(ids, p.getId()) >= 0);
        }
    }

    @Test
    public void testCollection() {
        MultiGetCursor<Person> instance = new MultiGetCursor(Person.class, service.realTimeMultiGet(index, type, ids));
        
        // Run through the collection and see what we get.
        for (Person p: instance) {
            assertTrue(Arrays.binarySearch(ids, p.getId()) >= 0);
        }
    }

    @Test
    public void testArray() {
        MultiGetCursor<Person> instance = new MultiGetCursor(Person.class, service.realTimeMultiGet(index, type, ids));
        
        Person[] persons = instance.toArray(new Person[instance.size()]);
                
        // Make sure we got a real iterator instance
        assertNotNull(persons);
        assertEquals(ids.length, persons.length);
    }
}
