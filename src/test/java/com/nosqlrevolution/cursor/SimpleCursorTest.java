package com.nosqlrevolution.cursor;

import com.nosqlrevolution.cursor.SimpleCursor;
import com.nosqlrevolution.cursor.Cursor;
import com.nosqlrevolution.model.Person;
import com.nosqlrevolution.util.TestDataHelper;
import com.nosqlrevolution.util.QueryUtil;
import java.util.Iterator;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHits;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class SimpleCursorTest {
    private static Client client;
    private static final String index = "test";
    private static final String type = "data";
    private static String[] ids = new String[]{"1", "2", "3", "4", "5"};
    private static SearchHits hits;

    @BeforeClass
    public static void setUpClass() throws Exception {
        client = TestDataHelper.createTestClient();
        
        TestDataHelper.indexCursorTestData(client, index, type);

        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .addSort(QueryUtil.getIdSort());
        
        SearchResponse response = builder.execute().actionGet();
        hits = response.getHits();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        client.close();
    }

    @Test
    public void testSize() {
        Cursor<Person> instance = new SimpleCursor(Person.class, hits);
        assertEquals(ids.length, instance.size());
    }

    @Test
    public void testIsEmpty() {
        Cursor<Person> instance = new SimpleCursor(Person.class, hits);
        assertFalse(instance.isEmpty());
    }

    @Test
    public void testIterator() {
        Cursor<Person> instance = new SimpleCursor(Person.class, hits);
        Iterator<Person> it = instance.iterator();
                
        // Make sure we got a real iterator instance
        assertNotNull(it);
        
        // Run through the iterator and see what we get.
        int id = 1;
        while (it.hasNext()) {
            Person p = it.next();
            assertEquals(Integer.toString(id), p.getId());
            id ++;
        }
    }

    @Test
    public void testCollection() {
        SimpleCursor<Person> instance = new SimpleCursor(Person.class, hits);
                
        // Make sure we got a real iterator instance
        assertNotNull(instance);
        
        // Run through the collection and see what we get.
        int id = 1;
        for (Person p: instance) {
            assertEquals(Integer.toString(id), p.getId());
            id ++;
        }
    }

    @Test
    public void testArray() {
        SimpleCursor<Person> instance = new SimpleCursor(Person.class, hits);
        
        Person[] persons = instance.toArray(new Person[instance.size()]);
                
        // Make sure we got a real iterator instance
        assertNotNull(persons);
        assertEquals(ids.length, persons.length);
    }
}
