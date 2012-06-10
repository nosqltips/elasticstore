package com.nosqlrevolution;

import com.google.common.base.Joiner;
import com.nosqlrevolution.util.AnnotationHelper;
import java.net.InetSocketAddress;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 *
 * @author cbrown
 */
public class ElasticStore {
    private static int DEFAULT_PORT = 9300;
    private static String DEFAULT_HOST = "127.0.0.1";
    private static String DEFAULT_TIMEOUT = "10s";
    private static String DEFAULT_CLUSTER_NAME = "elasticsearch";
    private static boolean multicast = true;
    private static boolean node = true;
    private static boolean local = false;
    private static boolean memory = false;
    
    private Client client = null;
    private String[] hosts = null;
    private InetSocketAddress[] addresses = null;
    private String clusterName = DEFAULT_CLUSTER_NAME;
    private int port = DEFAULT_PORT;
    private String timeout = DEFAULT_TIMEOUT;

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
    public ElasticStore withUniCast(String... hosts) {
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
     * Create an ElasticStore instance.
     * 
     * @return 
     */
    public ElasticStore execute() {
        if (local) {
            client = nodeBuilder()
                .local(true)
                .data(true)
                .node()
                .client();
        } else if (node) {
            ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
                .put("cluster.name", clusterName)
                .put("discovery.zen.ping.timeout", timeout)
                .put("discovery.zen.ping.multicast.enabled", multicast);
            
            if (memory) {
                builder.put("es.index.storage.type", "memory");
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
        } else {
            TransportClient transClient = new TransportClient();
            if (! multicast) {
                if (addresses != null) {
                    for (InetSocketAddress address: addresses) {
                        transClient.addTransportAddress(new InetSocketTransportAddress(address));
                    }
                }
                if (hosts != null) {
                    for (String host: hosts) {
                        transClient.addTransportAddress(new InetSocketTransportAddress(host, port));
                    }
                } else {
                    transClient.addTransportAddress(new InetSocketTransportAddress(DEFAULT_HOST, port));
                }
            }
            client = new TransportClient()
                    .addTransportAddress(new InetSocketTransportAddress("10.1.10.54", 9300));
            client = transClient;
        }
        
        // Wait for client connection.
        client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
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
            return new JsonIndex<String>(this, index, type);
        }
        
        // TODO: need to catch and throw an exception here
        return new TypedIndex(clazz.newInstance(), this, index, type);
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
        return new TypedIndex(clazz.newInstance(), this, index, type);
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
}
