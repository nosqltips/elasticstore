package com.nosqlrevolution;

import com.google.common.base.Joiner;
import com.nosqlrevolution.util.AnnotationHelper;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.elasticsearch.shield.ShieldPlugin;

/**
 *
 * @author cbrown
 */
public class ElasticStore {
    private static final int DEFAULT_PORT = 9300;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_TIMEOUT = "10s";
    private static final String DEFAULT_CLUSTER_NAME = "elasticsearch";
    private static boolean multicast = true;
    private static boolean node = true;
    private static boolean local = false;
    private static boolean memory = false;
    private static boolean elastic = false;
    
    private Client client = null;
    private String[] hosts = null;
    private InetSocketAddress[] addresses = null;
    private String clusterName = DEFAULT_CLUSTER_NAME;
    private int port = DEFAULT_PORT;
    private String timeout = DEFAULT_TIMEOUT;
    
    // for Elastic service
    private String username = null;
    private String password = null;
    private String clusterId = null;
    private String region = null;
    private boolean enableSsl = false;
    
    /**
     * Connect to the ElasticSearch cluster as a node.
     * 
     * @return 
     */
    public ElasticStore asNode() {
        node = true;
        return this;
    }
    
    /**
     * Connect to the ElasticSearch cluster as a TCP transport. This requires a list of address to connect to.
     * 
     * @return 
     */
    public ElasticStore asTransport() {
        node = false;
        return this;
    }

    /**
     * Run a local instance of ElasticSearch.
     * This is useful for unit tests or running ElasticSearch as part of your application.
     * 
     * @return 
     */
    public ElasticStore asLocal() {
        local = true;
        return this;
    }

    /**
     * Run a local instance of ElasticSearch as an in=memory store.
     * This is useful for unit tests or running ElasticSearch as part of your application.
     * 
     * @return 
     */
    public ElasticStore asMemoryOnly() {
        local = true;
        memory = true;
        return this;
    }

    /**
     * Connect to the Elastic.co service. Requires usernamen and password.
     * 
     * @return 
     */
    public ElasticStore asElastic() {
        elastic = true;
        enableSsl = true;
        node = false;
        multicast = false;
        return this;
    }

    /**
     * Specify multicast connection to a localhost cluster.
     * 
     * @return 
     */
    public ElasticStore withMultiCast() {
        multicast = true;
        return this;
    }

    /**
     * Specify unicast connection to a localhost cluster.
     * 
     * @return 
     */
    public ElasticStore withUnicast() {
        multicast = false;
        return this;
    }

    /**
     * Specify unicast connection with a list of nodes in the ElasticSearch cluster.
     * 
     * @param hosts
     * @return 
     */
    public ElasticStore withUnicast(String... hosts) {
        multicast = false;
        this.hosts = hosts;
        return this;
    }

    /**
     * Specify unicast connection with a list of nodes in the ElasticSearch cluster.
     * 
     * @param addresses
     * @return 
     */
    public ElasticStore withUnicast(InetSocketAddress... addresses) {
        multicast = false;
        this.addresses = addresses;
        return this;
    }
    
    /**
     * Specify the port of the ElasticSearch cluster to connect to.
     * 
     * @param port
     * @return 
     */
    public ElasticStore withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Specify the name of the ElasticSearch cluster to connect to.
     * 
     * @param clusterName
     * @return 
     */
    public ElasticStore withClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }
    
    /**
     * Specify a timeout when connecting to the ElasticSearch cluster.
     * 
     * @param timeout
     * @return 
     */
    public ElasticStore withTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }
    
    /**
     * Specify a username to use with the Elastic.co service.
     * 
     * @param username
     * @return 
     */
    public ElasticStore withUsername(String username) {
        this.username = username;
        return this;
    }
    
    /**
     * Specify a username to use with the Elastic.co service.
     * 
     * @param password
     * @return 
     */
    public ElasticStore withPassword(String password) {
        this.password = password;
        return this;
    }
    
    /**
     * Specify a clusterId to use with the Elastic.co service.
     * 
     * @param clusterId
     * @return 
     */
    public ElasticStore withClusterId(String clusterId) {
        this.clusterId = clusterId;
        return this;
    }
    
    /**
     * Specify a username to use with the Elastic.co service.
     * 
     * @param region
     * @return 
     */
    public ElasticStore withRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * Create an ElasticStore instance.
     * 
     * @return 
     * @throws java.lang.Exception 
     */
    public ElasticStore execute() throws Exception {
        Settings.Builder builder = Settings.settingsBuilder()
            .put("cluster.name", clusterName)
            .put("discovery.zen.ping.timeout", timeout)
            .put("discovery.zen.ping.multicast.enabled", multicast);
        
        if (local) {
            client = nodeBuilder()
                .local(true)
                .data(true)
                .node()
                .client();
        } else if (node) {            
            if (memory) {
                builder.put("es.index.storage.type", "memory");
                builder.put("index.number_of_shards", "1");
                builder.put("index.number_of_replicas", "0");
            }
            
            if (! multicast) {
                if (hosts != null) {
                    builder.put("discovery.zen.ping.unicast.hosts", Joiner.on(",").join(hosts));
                } else {
                    builder.put("discovery.zen.ping.unicast.hosts", DEFAULT_HOST);
                }
            }
            
            client = nodeBuilder()
                    .settings(builder.build())
                    .client(true)
                    .data(false)
                    .node()
                    .client();
        } else if (elastic) {
            // Build the settings for our client.
            if (clusterId == null && region == null && username == null && password == null) {
                throw new Exception("Elastic service not configured properly, aborting");
            }
            Settings settings = Settings.settingsBuilder()
                .put("transport.ping_schedule", "5s")
                .put("transport.sniff", false)
                .put("cluster.name", clusterId)
                .put("action.bulk.compress", false)
                .put("shield.transport.ssl", enableSsl)
                .put("request.headers.X-Found-Cluster", clusterId)
                .put("shield.user", username + ":" + password)
                .build();
            
            Map<String, String> settingsMap= settings.getAsMap();
            for (String s: settingsMap.keySet()) {
                System.out.println("key=" + s + " value=" + settingsMap.get(s));
            }
            String hostname = clusterId + "." + region + ".aws.found.io";
            try {
                // Instantiate a TransportClient and add the cluster to the list of addresses to connect to.
                // Only port 9343 (SSL-encrypted) is currently supported.
                client = TransportClient.builder()
                        .addPlugin(ShieldPlugin.class)
                        .settings(settings)
                        .build()
                        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostname), 9343));
            } catch (UnknownHostException ex) {
                Logger.getLogger(ElasticStore.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            TransportClient transClient = TransportClient.builder().settings(builder).build();
            if (! multicast) {
                if (addresses != null) {
                    for (InetSocketAddress address: addresses) {
                        transClient.addTransportAddress(new InetSocketTransportAddress(address));
                    }
                }
                if (hosts != null) {
                    for (String host: hosts) {                        
                        transClient.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(host, port)));
                    }
                } else {
                    transClient.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(DEFAULT_HOST, port)));
                }
            }
            client = transClient;
        }
        
        // Wait for client connection.
        client.admin()
                .cluster()
                .prepareHealth()
                .setWaitForYellowStatus()
                .execute()
                .actionGet();
    
        return this;
    }
    
    /**
     * Check to see if ElasticStore has been initialized.
     * @return 
     */
    public boolean isInitialized() {
        return client != null;
    }

    /**
     * Access the ElasticSearch client.
     * 
     * @return 
     */
    public Client getClient() {
        return client;
    }
    
    /**
     * Close ElasticStore
     */
    public void close() {
        client.close();
    }

    /**
     * Create strongly typed access to an index and type.
     * 
     * @param clazz
     * @param index
     * @param type
     * @return
     * @throws Exception 
     */
    // TODO: Need to type the exception more strongly
    public Index getIndex(Class clazz, String index, String type) throws Exception {
        if (client == null) {
            throw new IllegalArgumentException("ElasticStore is not executed");
        }

        if (clazz == String.class) {
            return new JsonIndex<>(this, index, type);
        }
        
        // TODO: need to catch and throw an exception here
        return new TypedIndex(clazz, this, index, type);
    }

    /**
     * Create strongly typed access to an index and type.
     * @Index and @IndexType must both be defined on the object to define the index and type.
     * 
     * @param clazz
     * @return
     * @throws Exception 
     */
    // TODO: Need to type the exception more strongly
    public Index getIndex(Class clazz) throws Exception {
        if (client == null) {
            throw new IllegalArgumentException("ElasticStore is not executed");
        }

        String index = AnnotationHelper.getIndexValue(clazz);
        String type = AnnotationHelper.getIndexTypeValue(clazz);
        if (index == null || type == null) {
            throw new IllegalArgumentException("Both @Index and @IndexType must be defined for object based mapping.");
        }

        // TODO: need to catch and throw an exception here
        return new TypedIndex(clazz, this, index, type);
    }

    /**
     * Remove an index from ElasticStore as specified by an Index.
     * 
     * @param index
     * @throws Exception 
     */
    public void removeIndex(Index index) throws Exception {
        client.admin()
                .indices()
                .delete(new DeleteIndexRequest(index.getIndex()))
                .actionGet();
    }

    /**
     * Remove an index from ElasticStore as specified by an index name..
     * 
     * @param index
     * @throws Exception 
     */
    public void removeIndex(String index) throws Exception {
        client.admin()
                .indices()
                .delete(new DeleteIndexRequest(index))
                .actionGet();
    }

    /**
     * Refresh the index,
     * 
     * @param index
     * @throws Exception 
     */
    public void refreshIndex(Index index) throws Exception {
        client.admin()
                .indices()
                .refresh(new RefreshRequest(index.getIndex()))
                .actionGet();
    }

    /**
     * Apply type mapping to one or more indexes.
     * If ignoreConflicts is false, then the mapping will be rejected if there are conflicts.
     * If ignoreConflits is true, then conflicts will be ignored.
     * 
     * @param mapping
     * @param indexes
     * @param type
     */
    protected void applyMapping(String mapping, String type, String... indexes) {
        // Check to see if indexes exist and create if missing.
        for (String index: indexes) {
            if (! client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists()) {
                client.admin().indices().create(new CreateIndexRequest(index)).actionGet();
            }
        }
        
        // Apply the new mapping to the index
        client.admin()
                .indices()
                .putMapping(new PutMappingRequest(indexes)
                    .source(mapping)
                    .type(type)
                ).actionGet();
    }
}
