package com.nosqlrevolution.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.junit.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;

import static org.junit.Assert.*;
import static org.elasticsearch.node.NodeBuilder.*;
import static org.elasticsearch.common.xcontent.XContentFactory.*;

/**
 *
 * @author cbrown
 */
public class QueryServiceTest {
    private static Client client;
    private static String index="test";
    private static String type = "data";
    private QueryService service = new QueryService(client);
    private ObjectMapper mapper = new ObjectMapper();

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
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        client.close();
    }

    @Test
    public void testGet() throws IOException {
        String s = service.realTimeGet(index, type, "1");
        assertNotNull(s);
        assertValues(s, "1", "Homer Simpson", "hsimpson");
        
        s = service.realTimeGet(index, type, "2");
        assertNotNull(s);
        assertValues(s, "2", "C Montgomery Burns", "cburns");

        s = service.realTimeGet(index, type, "3");
        assertNotNull(s);
        assertValues(s, "3", "Carl Carlson", "ccarlson");

        s = service.realTimeGet(index, type, "4");
        assertNotNull(s);
        assertValues(s, "4", "Clancy Wigum", "cwigum");

        s = service.realTimeGet(index, type, "5");
        assertNotNull(s);
        assertValues(s, "5", "Lenny Leonard", "lleonard");
    }
    
    @Test
    public void testMultiGet() throws IOException {
        String[] ids = new String[5];
        ids[0] = "1";
        ids[1] = "2";
        ids[2] = "3";
        ids[3] = "4";
        ids[4] = "5";
        
        String[] s = service.realTimeMultiGet(index, type, ids);
        // Put the array into a map for easier validation
        Map<String, String> map = new HashMap<String, String>();
        for (String json: s) {
            JsonNode rootNode = mapper.readValue(json, JsonNode.class);
            String id = rootNode.findValue("id").getTextValue();
            map.put(id, json);
        }
        
        assertNotNull(map.get("1"));
        assertValues(map.get("1"), "1", "Homer Simpson", "hsimpson");
        assertNotNull(map.get("2"));
        assertValues(map.get("2"), "2", "C Montgomery Burns", "cburns");
        assertNotNull(map.get("3"));
        assertValues(map.get("3"), "3", "Carl Carlson", "ccarlson");
        assertNotNull(map.get("4"));
        assertValues(map.get("4"), "4", "Clancy Wigum", "cwigum");
        assertNotNull(map.get("5"));
        assertValues(map.get("5"), "5", "Lenny Leonard", "lleonard");
    }
    
    @Test
    public void testIndex() throws IOException {
        XContentBuilder builder = jsonBuilder().startObject().field("id", "6").field("name", "Bart Simpson").field("username", "bsimpson").endObject();
        String json = new String(builder.copiedBytes());
        service.index(index, type, json, "6");
        
        String s = service.realTimeGet(index, type, "6");
        assertValues(s, "6", "Bart Simpson", "bsimpson");
    }

    @Test
    public void testBulkIndex() throws IOException {
        String[] json = new String[2];
        XContentBuilder builder = jsonBuilder().startObject().field("id", "7").field("name", "Maggie Simpson").field("username", "msimpson").endObject();
        json[0] = new String(builder.copiedBytes());
        builder = jsonBuilder().startObject().field("id", "8").field("name", "Lisa Simpson").field("username", "lsimpson").endObject();
        json[1] = new String(builder.copiedBytes());
        service.bulkIndex(index, type, json);
        
        String s = service.realTimeGet(index, type, "7");
        assertNotNull(s);
        assertValues(s, "7", "Maggie Simpson", "msimpson");

        s = service.realTimeGet(index, type, "8");
        assertNotNull(s);
        assertValues(s, "8", "Lisa Simpson", "lsimpson");
    }
    
    @Test 
    public void testDelete() throws IOException {
        String s = service.realTimeGet(index, type, "6");
        assertNotNull(s);
        service.delete(index, type, "6");
         s = service.realTimeGet(index, type, "6");
        assertNull(s);

        s = service.realTimeGet(index, type, "7");
        assertNotNull(s);
        service.delete(index, type, "7");
        s = service.realTimeGet(index, type, "7");
        assertNull(s);

        s = service.realTimeGet(index, type, "8");
        assertNotNull(s);
        service.delete(index, type, "8");
        s = service.realTimeGet(index, type, "8");
        assertNull(s);
    }
    
    private void assertValues(String json, String id, String name, String username) throws IOException {
        JsonNode rootNode = mapper.readValue(json, JsonNode.class);
        String myId = rootNode.findValue("id").getTextValue();
        String myName = rootNode.findValue("name").getTextValue();
        String myUsername = rootNode.findValue("username").getTextValue();

        assertNotNull(rootNode);
        assertEquals(myId, id);
        assertEquals(myName, name);
        assertEquals(myUsername, username);               
    }
}
