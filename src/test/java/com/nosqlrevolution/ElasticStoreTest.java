package com.nosqlrevolution;

import org.elasticsearch.client.Client;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class ElasticStoreTest {

     @Test
     public void testLocal() {
         ElasticStore es = new ElasticStore().asLocal().execute();
         assertNotNull(es);

         Client client = es.getClient();
         assertNotNull(client);
         
         es.close();
     }

     @Test
     public void testNode() {
         ElasticStore es = new ElasticStore().asLocal().execute();
         assertNotNull(es);

         ElasticStore esNode = new ElasticStore().asNode().execute();
         Client client = esNode.getClient();
         assertNotNull(client);
         
         es.close();
     }

     @Test
     public void testTransport() {
         ElasticStore es = new ElasticStore().asLocal().execute();
         assertNotNull(es);

         ElasticStore esTransport = new ElasticStore().asTransport().withUnicast("127.0.0.1").execute();
         Client client = esTransport.getClient();
         assertNotNull(client);
         
         es.close();
     }

     @Test
     public void testMemory() {
         ElasticStore es = new ElasticStore().asMemoryOnly().execute();
         assertNotNull(es);

         ElasticStore esNode = new ElasticStore().asMemoryOnly().execute();
         Client client = esNode.getClient();
         assertNotNull(client);
         
         es.close();
     }
}
