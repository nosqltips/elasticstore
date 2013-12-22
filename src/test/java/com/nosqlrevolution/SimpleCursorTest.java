package com.nosqlrevolution;

import com.nosqlrevolution.model.Person;
import com.nosqlrevolution.util.TestDataHelper;
import com.nosqlrevolution.util.QueryUtil;
import java.util.Collection;
import java.util.Iterator;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
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
    private static String[] ids;
    private static SearchHits hits;

    @BeforeClass
    public static void setUpClass() throws Exception {
        client = nodeBuilder().local(true).data(true).node().client();
        client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
        
        TestDataHelper.indexCursorTestData(client, index, type);
        
        // Create ids
        ids = new String[]{"1", "2", "3", "4", "5"};

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
        assertEquals(5, instance.size());
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
        Collection<Person> coll = instance.collection();
                
        // Make sure we got a real iterator instance
        assertNotNull(coll);
        
        // Run through the collection and see what we get.
        int id = 1;
        for (Person p: coll) {
            assertEquals(Integer.toString(id), p.getId());
            id ++;
        }
    }

    @Test
    public void testArray() {
        SimpleCursor<Person> instance = new SimpleCursor(Person.class, hits);
        
        Collection<Person> coll = instance.collection();
        Person[] persons = instance.toArray(new Person[coll.size()]);
                
        // Make sure we got a real iterator instance
        assertNotNull(persons);
        assertEquals(5, persons.length);
    }
}
