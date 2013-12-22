package com.nosqlrevolution;

import com.nosqlrevolution.model.Person;
import com.nosqlrevolution.util.QueryUtil;
import com.nosqlrevolution.util.TestDataHelper;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO: Need to really test the block iterating capability.
 * @author cbrown
 */
public class ScrollCursorTest {
    private static Client client;
    private static final String index = "test";
    private static final String type = "data";
    private static String[] ids = new String[]{"1", "2", "3", "4", "5"};
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        client = TestDataHelper.createTestClient();
        
        TestDataHelper.indexCursorTestData(client, index, type);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        client.close();
    }

    @Test
    public void testSize() throws Exception {
        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.SCAN)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setScroll(new TimeValue(600000))
                .setSize(10);
        
        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, builder, client);
        assertEquals(ids.length, instance.size());
    }

    @Test
    public void testIsEmpty() throws Exception {
        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.SCAN)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setScroll(new TimeValue(600000))
                .setSize(10);
        
        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, builder, client);
        assertFalse(instance.isEmpty());
    }

    @Test
    public void testIterator() throws Exception {
        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.SCAN)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setScroll(new TimeValue(600000))
                .setSize(10);
        
        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, builder, client);
        Iterator<Person> it = instance.iterator();
                
        // Make sure we got a real iterator instance
        assertNotNull(it);
        assertEquals(5, instance.size());
        
        // Run through the iterator and see what we get.
        List<String> idList = Arrays.asList(ids);
        while (it.hasNext()) {
            Person p = it.next();
            assertTrue(idList.contains(p.getId()));
        }
    }

    @Test
    public void testCollection() throws Exception {
        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.SCAN)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setScroll(new TimeValue(600000))
                .setSize(10);
        
        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, builder, client);
                
        // Make sure we got a real iterator instance
        assertNotNull(instance);
        assertEquals(ids.length, instance.size());

        // Run through the collection and see what we get.
        List<String> idList = Arrays.asList(ids);
        for (Person p: instance) {
            assertTrue(idList.contains(p.getId()));
        }
    }

    @Test
    public void testBadScanType() throws Exception {
        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setScroll(new TimeValue(600000))
                .setSize(10);
        
        try {
            ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, builder, client);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testArray() throws Exception {
        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.SCAN)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setScroll(new TimeValue(600000))
                .setSize(10);
        
        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, builder, client);
        
        Person[] persons = instance.toArray(new Person[instance.size()]);
                
        // Make sure we got a real iterator instance
        assertNotNull(persons);
        assertEquals(ids.length, persons.length);
    }
}
