package com.nosqlrevolution;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.nosqlrevolution.apps.ElasticStoreUtil;
import com.nosqlrevolution.apps.Import;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.time.Duration;
import java.security.KeyStore;
import javax.net.ssl.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;

/** Real REST transport tests using local HTTP and HTTPS servers. */
public class ConnectionTest {
    private HttpServer server;
    private String expectedAuthorization;
    private String authorization;
    private boolean fail;
    private long delay;

    @Before public void setup() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        handler(server);
        server.start();
    }
    private String endpoint() { return "http://127.0.0.1:" + server.getAddress().getPort(); }
    private void handler(HttpServer target) {
        target.createContext("/", exchange -> {
            authorization = exchange.getRequestHeaders().getFirst("Authorization");
            boolean authenticated = expectedAuthorization == null || expectedAuthorization.equals(authorization);
            int status = !authenticated ? 401 : fail ? 503 : 200;
            String body = status == 200 ? "{\"cluster_name\":\"connection-test\",\"status\":\"green\",\"timed_out\":false,"
                    + "\"number_of_nodes\":1,\"number_of_data_nodes\":1,\"active_primary_shards\":0,\"active_shards\":0,"
                    + "\"relocating_shards\":0,\"initializing_shards\":0,\"unassigned_shards\":0,\"delayed_unassigned_shards\":0,"
                    + "\"number_of_pending_tasks\":0,\"number_of_in_flight_fetch\":0,\"task_max_waiting_in_queue_millis\":0,\"active_shards_percent_as_number\":100.0}"
                    : "{\"error\":{\"type\":\"test_failure\",\"reason\":\"unavailable\"},\"status\":" + status + "}";
            try {
                if (delay > 0) Thread.sleep(delay);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("X-Elastic-Product", "Elasticsearch");
                if (!authenticated) exchange.getResponseHeaders().set("WWW-Authenticate", "Basic realm=\"test\"");
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(status, bytes.length);
                exchange.getResponseBody().write(bytes);
            } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            finally { exchange.close(); }
        });
    }
    @After public void cleanup() { if (server != null) server.stop(0); }

    @Test(timeout=10000) public void httpNeedsNoCredentialsAndOwnedTransportCloses() throws Exception {
        ElasticStore store = new ElasticStore().withEndpoints(endpoint()).execute();
        ElasticsearchClient client = store.getClient();
        assertTrue(store.isInitialized());
        store.close();
        assertFalse(store.isInitialized());
        store.close();
        try { client.cluster().health(); fail("Expected closed transport"); }
        catch (RuntimeException ex) { assertTrue(ex.getMessage().contains("STOPPED")); }
    }
    @Test(timeout=10000) public void borrowingAClientDoesNotCloseItsTransport() throws Exception {
        try (ElasticStore owner = new ElasticStore().withEndpoints(endpoint()).execute()) {
            ElasticStore borrowed = new ElasticStore().withClient(owner.getClient());
            assertSame(borrowed, borrowed.execute());
            borrowed.close();
            assertFalse(borrowed.isInitialized());
            assertEquals("connection-test", owner.getClient().cluster().health().clusterName());
        }
    }
    @Test(timeout=10000) public void credentialsWorkWithoutCloudMode() throws Exception {
        expectedAuthorization = "Basic " + Base64.getEncoder().encodeToString("test-user:test-password".getBytes(StandardCharsets.UTF_8));
        try (ElasticStore store = new ElasticStore().withEndpoints(endpoint()).withUsername("test-user").withPassword("test-password").execute()) {
            assertEquals(expectedAuthorization, authorization);
        }
    }
    @Test(timeout=10000) public void apiKeyIsSentAsAuthorization() throws Exception {
        expectedAuthorization = "ApiKey encoded-test-key";
        try (ElasticStore store = new ElasticStore().withEndpoints(endpoint()).withApiKey("encoded-test-key").execute()) {
            assertEquals(expectedAuthorization, authorization);
        }
    }
    @Test(timeout=10000) public void failedStartupCanBeRetriedWithoutPublishedClient() throws Exception {
        fail = true;
        ElasticStore store = new ElasticStore().withEndpoints(endpoint());
        try { store.execute(); fail("Expected startup failure"); } catch (Exception expected) { }
        assertFalse(store.isInitialized());
        fail = false;
        try { store.execute(); assertTrue(store.isInitialized()); }
        finally { store.close(); }
    }
    @Test(timeout=10000) public void configuredSocketTimeoutIsUsed() throws Exception {
        delay = 2500;
        ElasticStore store = new ElasticStore().withEndpoints(endpoint()).withSocketTimeout(Duration.ofMillis(30));
        try { store.execute(); fail("Expected timeout"); }
        catch (IOException expected) { }
        finally { store.close(); }
        assertFalse(store.isInitialized());
    }
    @Test(timeout=10000) public void legacyHostsAndPortAreUsable() throws Exception {
        try (ElasticStore store = new ElasticStore().withPort(server.getAddress().getPort()).withUnicast("127.0.0.1").withTimeout("2s").execute()) {
            assertTrue(store.isInitialized());
        }
        assertEquals(9200, ElasticStoreUtil.getAddresses("localhost")[0].getPort());
        assertArrayEquals(new String[] { "one", "two" }, ElasticStoreUtil.getHosts("one:9200,two:9201"));
        new ElasticStore().withEndpoints("http://[::1]:9200");
    }
    @Test(timeout=10000) public void cliImportAppliesCredentialsAndDoesNotPrintThem() throws Exception {
        expectedAuthorization = "Basic " + Base64.getEncoder().encodeToString("test-user:test-password".getBytes(StandardCharsets.UTF_8));
        Path input = Files.createTempFile("elasticstore-empty", ".json");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(output));
            new Import().run(new String[] { "-h", endpoint(), "-i", "test", "-f", input.toString(), "-su", "test-user", "-sp", "test-password" });
        } finally { System.setOut(original); Files.delete(input); }
        assertEquals(expectedAuthorization, authorization);
        assertFalse(new String(output.toByteArray(), StandardCharsets.UTF_8).contains("test-password"));
    }
    @Test(timeout=30000) public void httpsUsesProvidedCaAndRejectsUntrustedCertificates() throws Exception {
        Path directory = Files.createTempDirectory("elasticstore-tls");
        HttpsServer https = null;
        try {
            Path keyStore = directory.resolve("server.p12");
            Process process = new ProcessBuilder(System.getProperty("java.home") + "/bin/keytool", "-genkeypair", "-alias", "server", "-keyalg", "RSA",
                    "-dname", "CN=localhost", "-ext", "san=ip:127.0.0.1,dns:localhost", "-validity", "1", "-storetype", "PKCS12",
                    "-keystore", keyStore.toString(), "-storepass", "test-password", "-noprompt").redirectErrorStream(true).redirectOutput(directory.resolve("keytool.log").toFile()).start();
            if (!process.waitFor(15, TimeUnit.SECONDS)) { process.destroyForcibly(); fail("keytool timed out"); }
            assertEquals(0, process.exitValue());
            KeyStore keys = KeyStore.getInstance("PKCS12");
            try (InputStream input = Files.newInputStream(keyStore)) { keys.load(input, "test-password".toCharArray()); }
            Path certificate = directory.resolve("ca.der");
            Files.write(certificate, keys.getCertificate("server").getEncoded());
            KeyManagerFactory managers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            managers.init(keys, "test-password".toCharArray());
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(managers.getKeyManagers(), null, null);
            https = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            https.setHttpsConfigurator(new HttpsConfigurator(context));
            handler(https); https.start();
            String endpoint = "https://127.0.0.1:" + https.getAddress().getPort();
            try (ElasticStore store = new ElasticStore().withEndpoints(endpoint).withCaCertificate(certificate).execute()) { assertTrue(store.isInitialized()); }
            try (ElasticStore store = new ElasticStore().withEndpoints(endpoint)) {
                try { store.execute(); fail("Expected untrusted certificate rejection"); }
                catch (IOException expected) { assertFalse(store.isInitialized()); }
            }
        } finally {
            if (https != null) https.stop(0);
            try (java.util.stream.Stream<Path> paths = Files.walk(directory)) {
                for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) Files.delete(path);
            }
        }
    }
    @Test(expected=IllegalArgumentException.class) public void embeddedCredentialsAreRejected() { new ElasticStore().withEndpoints("http://user:password@localhost:9200"); }
    @Test(expected=IllegalArgumentException.class) public void invalidPortsAreRejected() { ElasticStoreUtil.getAddresses("localhost:bad"); }
    @Test(expected=IllegalArgumentException.class) public void invalidTimeoutsAreRejected() { new ElasticStore().withTimeout("0s"); }
    @Test(expected=IllegalStateException.class) public void partialCredentialsAreRejected() throws Exception { new ElasticStore().withUsername("user").execute(); }
    @Test(expected=IllegalStateException.class) public void conflictingCredentialsAreRejected() throws Exception { new ElasticStore().withUsername("user").withPassword("password").withApiKey("key").execute(); }
    @Test(expected=UnsupportedOperationException.class) public void embeddedModeIsRejectedClearly() { new ElasticStore().asMemoryOnly(); }
}
