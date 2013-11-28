package com.nosqlrevolution;

import com.nosqlrevolution.model.Person;
import com.nosqlrevolution.util.QueryUtil;
import java.util.Collection;
import java.util.Iterator;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO: Need to really test the block iterating capability.
 * @author cbrown
 */
public class ScanCursorTest {
    private static Client client;
    private static final String index = "test";
    private static final String type = "data";
    private static String[] ids;
    private static SearchScrollRequestBuilder scrollBuilder;
    private static int totalSize;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        client = nodeBuilder().local(true).data(true).node().client();
        client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
        
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        bulkRequest.add(client.prepareIndex(index, type, "1")
                .setSource(
                    jsonBuilder().startObject().field("id", "1").field("name", "Homer Simpson").field("username", "hsimpson").endObject()
                ));
        bulkRequest.add(client.prepareIndex(index, type, "2")
                .setSource(
                    jsonBuilder().startObject().field("id", "2").field("name", "C Montgomery Burns").field("username", "cburns").endObject()
                ));
        bulkRequest.add(client.prepareIndex(index, type, "3")
                .setSource(
                    jsonBuilder().startObject().field("id", "3").field("name", "Carl Carlson").field("username", "ccarlson").endObject()
                ));
        bulkRequest.add(client.prepareIndex(index, type, "4")
                .setSource(
                    jsonBuilder().startObject().field("id", "4").field("name", "Clancy Wigum").field("username", "cwigum").endObject()
                ));
        bulkRequest.add(client.prepareIndex(index, type, "5")
                .setSource(
                    jsonBuilder().startObject().field("id", "5").field("name", "Lenny Leonard").field("username", "lleonard").endObject()
                ));
        
        bulkRequest.execute().actionGet();
        client.admin().indices().refresh(new RefreshRequest(index)).actionGet();
        
        // Create ids
        ids = new String[5];
        ids[0] = "1";
        ids[1] = "2";
        ids[2] = "3";
        ids[3] = "4";
        ids[4] = "5";

        // Get a list of SearchHits we can work with
        SearchRequestBuilder builder = client.prepareSearch(index)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setTypes(type)
                .setQuery(QueryUtil.getIdQuery(ids))
                .setSize(10);
        
        SearchResponse response = builder.execute().actionGet();
        totalSize = (int)response.getHits().getTotalHits();
        
        scrollBuilder = client.prepareSearchScroll(response.getScrollId())
                .setScroll(new TimeValue(600000));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        client.close();
    }

    @Test
    public void testSize() {
        ScrollCursor<Person> instance = new ScrollCursor(new Person(), scrollBuilder, totalSize);
        assertEquals(5, instance.size());
    }

    @Test
    public void testIsEmpty() {
        ScrollCursor<Person> instance = new ScrollCursor(new Person(), scrollBuilder, totalSize);
        assertFalse(instance.isEmpty());
    }

    @Test
    public void testIterator() {
        ScrollCursor<Person> instance = new ScrollCursor(new Person(), scrollBuilder, totalSize);
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
        ScrollCursor<Person> instance = new ScrollCursor(new Person(), scrollBuilder, totalSize);
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
