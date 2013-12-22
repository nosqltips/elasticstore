package com.nosqlrevolution;

import com.nosqlrevolution.model.Person;
import com.nosqlrevolution.util.QueryUtil;
import com.nosqlrevolution.util.TestDataHelper;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
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
    private static String[] ids;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        client = nodeBuilder().local(true).data(true).node().client();
        client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
        
        TestDataHelper.indexCursorTestData(client, index, type);
        
        // Create ids
        ids = new String[]{"1", "2", "3", "4", "5"};
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
        
        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, builder, client);
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
                .setSize(1);
        
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
    public void testIterator2() throws Exception {
        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.SCAN)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setScroll(new TimeValue(600000))
                .setSize(1);
        
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
                .setSize(1);
        
        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, builder, client);
        Collection<Person> coll = instance.collection();
                
        // Make sure we got a real iterator instance
        assertNotNull(coll);
        assertEquals(5, coll.size());
        
        // Run through the collection and see what we get.
        List<String> idList = Arrays.asList(ids);
        for (Person p: coll) {
            assertTrue(idList.contains(p.getId()));
        }
    }

    @Test
    public void testBadScanType() {
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
                .setSize(1);
        
        ScrollCursor<Person> instance = new ScrollCursor<Person>(Person.class, builder, client);
        
        Collection<Person> coll = instance.collection();
        Person[] persons = instance.toArray(new Person[coll.size()]);
                
        // Make sure we got a real iterator instance
        assertNotNull(persons);
        assertEquals(5, persons.length);
    }
}
