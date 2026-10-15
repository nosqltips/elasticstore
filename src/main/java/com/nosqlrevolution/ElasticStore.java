package com.nosqlrevolution;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.nosqlrevolution.util.AnnotationHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

/** Connection configuration and client lifetime for Elasticsearch's REST API. */
public class ElasticStore implements AutoCloseable {
    private ElasticsearchClient client;
    private boolean ownsClient;
    private String[] endpoints;
    private String scheme = "http";
    private int port = 9200;
    private int connectTimeoutMillis = 20000;
    private int socketTimeoutMillis = 300000;
    private String username;
    private String password;
    private String apiKey;
    private Path caCertificate;
    private String expectedClusterName;

    public ElasticStore withEndpoints(String... endpoints) {
        ensureConfigurable();
        if (endpoints == null || endpoints.length == 0) throw new IllegalArgumentException("At least one endpoint is required");
        for (String endpoint : endpoints) endpoint(endpoint);
        this.endpoints = endpoints.clone();
        return this;
    }

    /** A supplied client is borrowed unless ownership is explicitly transferred. */
    public ElasticStore withClient(ElasticsearchClient client) { return withClient(client, false); }
    public ElasticStore withClient(ElasticsearchClient client, boolean ownsClient) {
        ensureConfigurable();
        this.client = Objects.requireNonNull(client, "Client");
        this.ownsClient = ownsClient;
        return this;
    }

    @Deprecated public ElasticStore asLocal() { return withEndpoints("http://127.0.0.1:9200"); }
    @Deprecated public ElasticStore asMemoryOnly() {
        throw new UnsupportedOperationException("Embedded Elasticsearch is unsupported; start a test server and use withEndpoints()");
    }
    @Deprecated public ElasticStore withMultiCast() {
        throw new UnsupportedOperationException("Multicast discovery is unsupported; use withEndpoints()");
    }
    @Deprecated public ElasticStore withUnicast() { ensureConfigurable(); return this; }
    /** Legacy shorthand for bare HTTP hosts; explicit URL schemes are retained. */
    @Deprecated public ElasticStore withUnicast(String... hosts) {
        ensureConfigurable();
        if (hosts == null || hosts.length == 0) throw new IllegalArgumentException("At least one host is required");
        String[] urls = new String[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            String host = Objects.requireNonNull(hosts[i], "Host");
            if (host.contains("://")) urls[i] = host;
            else {
                endpoint(scheme + "://" + host);
                URI parsed = URI.create(scheme + "://" + host);
                if (parsed.getHost() == null) throw new IllegalArgumentException("Invalid host; bracket IPv6 addresses");
                urls[i] = new HttpHost(parsed.getHost(), parsed.getPort() < 0 ? port : parsed.getPort(), scheme).toURI();
            }
        }
        return withEndpoints(urls);
    }
    @Deprecated public ElasticStore withUnicast(InetSocketAddress... addresses) {
        if (addresses == null || addresses.length == 0) throw new IllegalArgumentException("At least one address is required");
        String[] urls = new String[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            InetSocketAddress address = Objects.requireNonNull(addresses[i], "Address");
            urls[i] = new HttpHost(address.getHostString(), address.getPort(), scheme).toURI();
        }
        return withEndpoints(urls);
    }
    /** HTTPS defaults for legacy callers; prefer explicit endpoint URLs. */
    @Deprecated public ElasticStore asElastic() { ensureConfigurable(); scheme = "https"; return this; }
    @Deprecated public ElasticStore withPort(int port) {
        ensureConfigurable();
        if (port <= 0 || port > 65535) throw new IllegalArgumentException("Port must be between 1 and 65535");
        if (endpoints != null) throw new IllegalStateException("Set the port in each endpoint URL");
        this.port = port;
        return this;
    }
    /** Optional readiness check against the reported cluster name. */
    @Deprecated public ElasticStore withClusterName(String name) {
        ensureConfigurable(); expectedClusterName = text(name, "Cluster name"); return this;
    }
    @Deprecated public ElasticStore withClusterId(String clusterId) {
        throw new UnsupportedOperationException("Legacy cloud cluster IDs are unsupported; supply the deployment's HTTPS endpoint");
    }
    @Deprecated public ElasticStore withRegion(String region) {
        throw new UnsupportedOperationException("Cloud region does not configure a REST endpoint; use withEndpoints()");
    }

    public ElasticStore withUsername(String username) { ensureConfigurable(); this.username = text(username, "Username"); return this; }
    public ElasticStore withPassword(String password) { ensureConfigurable(); this.password = Objects.requireNonNull(password, "Password"); return this; }
    /** The encoded API key, without an Authorization prefix. */
    public ElasticStore withApiKey(String apiKey) { ensureConfigurable(); this.apiKey = text(apiKey, "API key"); return this; }
    public ElasticStore withCaCertificate(Path certificate) {
        ensureConfigurable(); caCertificate = Objects.requireNonNull(certificate, "CA certificate"); return this;
    }
    public ElasticStore withConnectTimeout(Duration timeout) {
        ensureConfigurable(); connectTimeoutMillis = milliseconds(timeout); return this;
    }
    public ElasticStore withSocketTimeout(Duration timeout) {
        ensureConfigurable(); socketTimeoutMillis = milliseconds(timeout); return this;
    }
    /** Legacy timeout applies to both connection and socket I/O; bare numbers mean seconds. */
    public ElasticStore withTimeout(String timeout) {
        java.util.regex.Matcher value = java.util.regex.Pattern.compile("([0-9]+)(ms|s|m)?").matcher(text(timeout, "Timeout"));
        if (!value.matches()) throw new IllegalArgumentException("Timeout must be a positive number followed by ms, s or m");
        long count = Long.parseLong(value.group(1));
        String unit = value.group(2);
        Duration duration = "ms".equals(unit) ? Duration.ofMillis(count)
                : "m".equals(unit) ? Duration.ofMinutes(count) : Duration.ofSeconds(count);
        return withConnectTimeout(duration).withSocketTimeout(duration);
    }

    public ElasticStore execute() throws Exception {
        if (client != null) return this;
        if ((username == null) != (password == null)) throw new IllegalStateException("Both username and password are required for basic authentication");
        if (apiKey != null && username != null) throw new IllegalStateException("Choose basic authentication or an API key");
        String[] urls = endpoints == null ? new String[] { scheme + "://127.0.0.1:" + port } : endpoints.clone();
        HttpHost[] hosts = new HttpHost[urls.length];
        for (int i = 0; i < urls.length; i++) hosts[i] = endpoint(urls[i]);
        final SSLContext ssl = caCertificate == null ? null : trust(caCertificate);
        if (ssl != null) {
            for (HttpHost host : hosts) if (!"https".equals(host.getSchemeName())) {
                throw new IllegalStateException("A CA certificate requires HTTPS endpoints");
            }
        }
        RestClientBuilder builder = RestClient.builder(hosts)
                .setRequestConfigCallback(config -> config.setConnectTimeout(connectTimeoutMillis).setSocketTimeout(socketTimeoutMillis))
                .setHttpClientConfigCallback(config -> {
                    if (username != null) {
                        BasicCredentialsProvider credentials = new BasicCredentialsProvider();
                        credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                        config.setDefaultCredentialsProvider(credentials);
                    }
                    if (ssl != null) config.setSSLContext(ssl);
                    return config;
                });
        if (apiKey != null) builder.setDefaultHeaders(new Header[] { new BasicHeader("Authorization", "ApiKey " + apiKey) });
        ElasticsearchClient candidate = new ElasticsearchClient(new RestClientTransport(builder.build(), new JacksonJsonpMapper()));
        try {
            String version = candidate.info().version().number();
            String[] components = version.split("\\.");
            int major = Integer.parseInt(components[0]), minor = Integer.parseInt(components[1]);
            if (major < 8 || major == 8 && minor < 19) {
                throw new IllegalStateException("Elasticsearch 8.19 or newer is required; server reports " + version);
            }
            String name = candidate.cluster().health().clusterName();
            if (expectedClusterName != null && !expectedClusterName.equals(name)) {
                throw new IllegalStateException("Connected cluster name does not match the configured name");
            }
            client = candidate;
            ownsClient = true;
        } catch (Exception | Error failure) {
            try { candidate._transport().close(); } catch (Exception cleanup) { failure.addSuppressed(cleanup); }
            throw failure;
        }
        return this;
    }

    private static HttpHost endpoint(String value) {
        URI uri;
        try { uri = URI.create(text(value, "Endpoint")); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid endpoint URL"); }
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null
                || !(uri.getPath() == null || uri.getPath().isEmpty() || "/".equals(uri.getPath()))
                || uri.getPort() == 0 || uri.getPort() > 65535) {
            throw new IllegalArgumentException("Endpoint must be an HTTP(S) URL without credentials, query, fragment or path");
        }
        return new HttpHost(uri.getHost(), uri.getPort() < 0 ? ("https".equals(uri.getScheme()) ? 443 : 80) : uri.getPort(), uri.getScheme());
    }
    private static String text(String value, String label) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(label + " must not be blank");
        return value;
    }
    private static int milliseconds(Duration value) {
        Objects.requireNonNull(value, "Timeout");
        long milliseconds = value.toMillis();
        if (milliseconds <= 0 || milliseconds > Integer.MAX_VALUE) throw new IllegalArgumentException("Timeout must fit a positive millisecond int");
        return (int) milliseconds;
    }
    private static SSLContext trust(Path certificate) throws Exception {
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, null);
        try (InputStream input = Files.newInputStream(certificate)) {
            int index = 0;
            for (java.security.cert.Certificate ca : CertificateFactory.getInstance("X.509").generateCertificates(input)) {
                store.setCertificateEntry("ca-" + index++, ca);
            }
            if (index == 0) throw new IllegalArgumentException("CA file contains no certificates");
        }
        return SSLContexts.custom().loadTrustMaterial(store, null).build();
    }
    private void ensureConfigurable() { if (client != null) throw new IllegalStateException("Close the store before reconfiguring it"); }
    public boolean isInitialized() { return client != null; }
    public ElasticsearchClient getClient() { return client; }
    @Override public void close() {
        ElasticsearchClient closing = client;
        client = null;
        if (closing != null && ownsClient) {
            try { closing._transport().close(); }
            catch (IOException ex) { throw new UncheckedIOException("Cannot close Elasticsearch client", ex); }
        }
        ownsClient = false;
    }

    private static final co.elastic.clients.json.JsonpMapper MAPPING_MAPPER = new JacksonJsonpMapper() {
        @Override public boolean ignoreUnknownFields() { return false; }
    };

    /** Creates a new index with the model's generated typeless mapping. */
    public <T> Index<T> createIndex(Class<T> model, String index) throws Exception {
        String mapping = com.nosqlrevolution.service.SchemaService.generateSchema(model);
        createIndex(index, mapping);
        return getIndex(model, index);
    }

    public void createIndex(String index, String mapping) {
        if (client == null) throw new IllegalStateException("ElasticStore is not executed");
        if (index == null || index.trim().isEmpty()) throw new IllegalArgumentException("Index name is required");
        String json = com.nosqlrevolution.service.SchemaService.validateMapping(mapping);
        co.elastic.clients.elasticsearch._types.mapping.TypeMapping definition;
        try (jakarta.json.stream.JsonParser parser = MAPPING_MAPPER.jsonProvider().createParser(new java.io.StringReader(json))) {
            definition = new co.elastic.clients.elasticsearch._types.mapping.TypeMapping.Builder().withJson(parser, MAPPING_MAPPER).build();
        }
        try {
            if (!client.indices().create(r -> r.index(index).mappings(definition)).acknowledged()) {
                throw new IllegalStateException("Index creation was not acknowledged");
            }
        } catch (IOException ex) { throw new UncheckedIOException("Elasticsearch index creation failed", ex); }
    }

    /** Opens an index handle; does not create the index or change its mapping. */
    @SuppressWarnings("unchecked")
    public <T> Index<T> getIndex(Class<T> clazz, String index) throws Exception {
        java.util.Objects.requireNonNull(clazz, "Document type");
        if (index == null || index.trim().isEmpty()) throw new IllegalArgumentException("Index name is required");
        if (client == null) {
            throw new IllegalArgumentException("ElasticStore is not executed");
        }

        if (clazz == String.class) {
            // This branch guarantees T is String; retain the legacy JsonIndex facade.
            return (Index<T>) new JsonIndex<>(this, index);
        }
        
        // TODO: need to catch and throw an exception here
        return new TypedIndex<>(clazz, this, index, client);
    }

    /**
     * Create strongly typed access to an index and type.
     * @Index supplies the index name; legacy @IndexType is no longer required.
     * 
     * @param clazz
     * @return
     * @throws Exception 
     */
    // TODO: Need to type the exception more strongly
    public <T> Index<T> getIndex(Class<T> clazz) throws Exception {
        if (client == null) {
            throw new IllegalArgumentException("ElasticStore is not executed");
        }

        String index = AnnotationHelper.getIndexValue(clazz);
        if (index == null) throw new IllegalArgumentException("@Index must define the index name");

        // TODO: need to catch and throw an exception here
        return new TypedIndex<>(clazz, this, index, client);
    }

//    /**
//     * Remove an index from ElasticStore as specified by an Index.
//     * 
//     * @param index
//     * @throws Exception 
//     */
//    public void removeIndex(Index index) throws Exception {
//        client.admin()
//                .indices()
//                .delete(new DeleteIndexRequest(index.getIndex()))
//                .actionGet();
//    }
//
//    /**
//     * Remove an index from ElasticStore as specified by an index name..
//     * 
//     * @param index
//     * @throws Exception 
//     */
//    public void removeIndex(String index) throws Exception {
//        client.admin()
//                .indices()
//                .delete(new DeleteIndexRequest(index))
//                .actionGet();
//    }
//
//    /**
//     * Refresh the index,
//     * 
//     * @param index
//     * @throws Exception 
//     */
//    public void refreshIndex(Index index) throws Exception {
//        client.admin()
//                .indices()
//                .refresh(new RefreshRequest(index.getIndex()))
//                .actionGet();
//    }

    /**
     * Apply type mapping to one or more indexes.
     * If ignoreConflicts is false, then the mapping will be rejected if there are conflicts.
     * If ignoreConflits is true, then conflicts will be ignored.
     * 
     * @param mapping
     * @param indexes
     */
    protected void applyMapping(String mapping, String... indexes) {
        if (client == null) throw new IllegalStateException("ElasticStore is not executed");
        if (indexes == null || indexes.length == 0) throw new IllegalArgumentException("At least one index is required");
        for (String index : indexes) if (index == null || index.trim().isEmpty()) throw new IllegalArgumentException("Index name is required");
        String json = com.nosqlrevolution.service.SchemaService.validateMapping(mapping);
        co.elastic.clients.elasticsearch.indices.PutMappingRequest request;
        try (jakarta.json.stream.JsonParser parser = MAPPING_MAPPER.jsonProvider().createParser(new java.io.StringReader(json))) {
            request = new co.elastic.clients.elasticsearch.indices.PutMappingRequest.Builder()
                    .withJson(parser, MAPPING_MAPPER).index(java.util.Arrays.asList(indexes)).build();
        }
        try {
            if (!client.indices().putMapping(request).acknowledged()) throw new IllegalStateException("Mapping update was not acknowledged");
        } catch (IOException ex) { throw new UncheckedIOException("Elasticsearch mapping update failed", ex); }
    }
}
