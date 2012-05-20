package com.nosqlrevolution;

import com.nosqlrevolution.model.Person;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.QueryUtil;
import java.util.Iterator;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class CursorTest {
    private static Client client;
    private static String index = "test";
    private static String type = "data";
    private static String[] ids;
    private static SearchHits hits;

    @BeforeClass
    public static void setUpClass() throws Exception {
        client = nodeBuilder().local(true).data(true).node().client();
        
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
                .addSort(QueryUtil.getIdSort());
        
        SearchResponse response = builder.execute().actionGet();
        hits = response.hits();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        client.close();
    }

    /**
     * Test of size method, of class Cursor.
     */
    @Test
    public void testSize() {
        Cursor instance = new Cursor(new Person(), hits);
        assertEquals(5, instance.size());
    }

    /**
     * Test of isEmpty method, of class Cursor.
     */
    @Test
    public void testIsEmpty() {
        Cursor instance = new Cursor(new Person(), hits);
        assertFalse(instance.isEmpty());
    }

    /**
     * Test of iterator method, of class Cursor.
     */
    @Test
    public void testIterator() {
        Cursor<Person> instance = new Cursor(new Person(), hits);
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
}
