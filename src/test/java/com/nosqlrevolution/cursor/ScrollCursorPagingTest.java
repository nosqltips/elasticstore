package com.nosqlrevolution.cursor;

import com.nosqlrevolution.model.Person;
import com.nosqlrevolution.util.QueryUtil;
import com.nosqlrevolution.util.TestDataHelper;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
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
public class ScrollCursorPagingTest {
    private static Client client;
    private static final String index = "test";
    private static final String type = "data";
    private static final String[] ids = new String[]{"1", "2", "3", "4", "5"};
    
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
                .setSize(1);
        
        SearchResponse response = builder.execute().actionGet();
        SearchScrollRequestBuilder scroll = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000));

        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, scroll);
        assertEquals(5, instance.size());
    }

    @Test
    public void testIsEmpty() throws Exception {
        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.SCAN)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setScroll(new TimeValue(600000))
                .setSize(1);
        
        SearchResponse response = builder.execute().actionGet();
        SearchScrollRequestBuilder scroll = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000));

        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, scroll);
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
                .setSize(1);
        
        SearchResponse response = builder.execute().actionGet();
        SearchScrollRequestBuilder scroll = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000));

        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, scroll);
        Iterator<Person> it = instance.iterator();
                
        // Make sure we got a real iterator instance
        assertNotNull(it);
        assertEquals(ids.length, instance.size());
        
        // Run through the iterator and see what we get.
        List<String> idList = Arrays.asList(ids);
        while (it.hasNext()) {
            Person p = it.next();
            assertTrue(idList.contains(p.getId()));
        }
    }

    @Test
    public void testIterator2() throws Exception {
        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.SCAN)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setScroll(new TimeValue(600000))
                .setSize(1);
        
        SearchResponse response = builder.execute().actionGet();
        SearchScrollRequestBuilder scroll = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000));

        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, scroll);
        Iterator<Person> it = instance.iterator();
                
        // Make sure we got a real iterator instance
        assertNotNull(it);
        assertEquals(ids.length, instance.size());
        
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
                .setSize(1);
        
        SearchResponse response = builder.execute().actionGet();
        SearchScrollRequestBuilder scroll = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000));

        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, scroll);
                
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
    public void testArray() throws Exception {
        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.SCAN)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setScroll(new TimeValue(600000))
                .setSize(1);
        
        SearchResponse response = builder.execute().actionGet();
        SearchScrollRequestBuilder scroll = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(600000));

        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, scroll);
        
        Person[] persons = instance.toArray(new Person[instance.size()]);
                
        // Make sure we got a real iterator instance
        assertNotNull(persons);
        assertEquals(ids.length, persons.length);
    }
}
