package com.nosqlrevolution;

import com.nosqlrevolution.model.Person;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.QueryUtil;
import com.nosqlrevolution.util.TestDataHelper;
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
 * TODO: Need to really test the block iterating capability.
 * @author cbrown
 */
public class BlockCursorTest {
    private static Client client;
    private static final String index = "test";
    private static final String type = "data";
    private static String[] ids;
    private static SearchHits hits;
    private static SearchRequestBuilder builder;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        client = nodeBuilder().local(true).data(true).node().client();
        client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
        
        TestDataHelper.indexCursorTestData(client, index, type);
        
        // Create ids
        ids = new String[]{"1", "2", "3", "4", "5"};

        // Get a list of SearchHits we can work with
        builder = client.prepareSearch(index)
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
    public void count() {
        long count = new QueryService(client).count(index, type);
        assertEquals(5, count);
    }
    
    @Test
    public void testSize() {
        BlockCursor<Person> instance = new BlockCursor<Person>(Person.class, hits, builder, 0, 10);
        assertEquals(5, instance.size());
    }

    @Test
    public void testIsEmpty() {
        BlockCursor<Person> instance = new BlockCursor<Person>(Person.class, hits, builder, 0, 10);
        assertFalse(instance.isEmpty());
    }

    @Test
    public void testIterator() {
        BlockCursor<Person> instance = new BlockCursor<Person>(Person.class, hits, builder, 0, 10);
        Iterator<Person> it = instance.iterator();
                
        // Make sure we got a real iterator instance
        assertNotNull(it);
        assertEquals(5, instance.size());
        
        // Run through the iterator and see what we get.
        int id = 0;
        while (it.hasNext()) {
            Person p = it.next();
            assertEquals(Integer.toString(++id), p.getId());
        }
        assertEquals(5, id);
    }

    @Test
    public void testCollection() {
        BlockCursor<Person> instance = new BlockCursor<Person>(Person.class, hits, builder, 0, 10);
        Collection<Person> coll = instance.collection();
                
        // Make sure we got a real iterator instance
        assertNotNull(coll);
        assertEquals(5, coll.size());
        
        // Run through the collection and see what we get.
        int id = 0;
        for (Person p: coll) {
            assertEquals(Integer.toString(++id), p.getId());
        }
        assertEquals(5, id);
    }
}
