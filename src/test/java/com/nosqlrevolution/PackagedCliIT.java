package com.nosqlrevolution;

import com.nosqlrevolution.service.QueryService;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;

/** Runs the actual shaded artifact without the test classpath. */
public class PackagedCliIT {
    private String launch(int expected, String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-jar");
        command.add(System.getProperty("elasticstore.cli.jar"));
        command.addAll(Arrays.asList(args));
        Path output = Files.createTempFile("elasticstore-cli-", ".log");
        Process process = null;
        try {
            process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
            assertTrue("CLI timed out: " + Arrays.toString(args), process.waitFor(45, TimeUnit.SECONDS));
            String text = Files.readString(output, StandardCharsets.UTF_8);
            if (expected == 0) assertEquals(text, 0, process.exitValue());
            else assertNotEquals(text, 0, process.exitValue());
            return text;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
            Files.deleteIfExists(output);
        }
    }

    @Test public void helpLoadsPackagedDependencies() throws Exception {
        assertTrue(launch(0, "--help").contains("Usage:"));
        for (String command : Arrays.asList("import", "export", "transfer")) {
            assertTrue(launch(0, command, "--help").contains("-b"));
        }
    }

    @Test public void invalidArgumentsAndFileFailuresExitNonzero() throws Exception {
        assertTrue(launch(1, "unknown").contains("Unknown command"));
        assertTrue(launch(1, "import").contains("Index must be specified"));
        assertTrue(launch(1, "transfer").contains("Source index must be specified"));
        assertTrue(launch(1, "export", "-bad").contains("not a valid option"));
        Path directory = Files.createTempDirectory("elasticstore-cli-files-");
        Path existing = directory.resolve("existing.json");
        try {
            Files.writeString(existing, "keep me");
            assertTrue(launch(1, "import", "-i", "unused", "-f", directory.resolve("missing").toString())
                    .contains("Input file does not exist"));
            assertTrue(launch(1, "export", "-i", "unused", "-f", existing.toString())
                    .contains("Output file already exists"));
            assertEquals("keep me", Files.readString(existing));
        } finally { Files.deleteIfExists(existing); Files.deleteIfExists(directory); }
    }

    @Test public void packagedRoundTripPreservesMetadata() throws Exception {
        String endpoint = System.getProperty("elasticstore.test.url");
        Assume.assumeNotNull(endpoint);
        String prefix = "elasticstore-cli-" + UUID.randomUUID();
        String source = prefix + "-source", restored = prefix + "-restored", copied = prefix + "-copied";
        Path directory = Files.createTempDirectory("elasticstore-cli-roundtrip-");
        Path file = directory.resolve("documents.json.gz");
        List<String> created = new ArrayList<>();
        try (ElasticStore store = new ElasticStore().withEndpoints(endpoint).execute()) {
            try {
                for (String index : Arrays.asList(source, restored, copied)) {
                    store.getClient().indices().create(r -> r.index(index).mappings(m -> m.routing(t -> t.required(true)))
                            .settings(s -> s.numberOfReplicas("0")));
                    created.add(index);
                }
                QueryService service = new QueryService(store.getClient());
                for (int i = 0; i < 3; i++) service.index(source, "{\"value\":\"é " + i + "\"}", "id-" + i,
                        DocumentOptions.DEFAULT.routing("tenant"));
                service.refresh(source);
                launch(0, "export", "-h", endpoint, "-i", source, "-f", file.toString(), "-m", "-g", "-b", "2");
                launch(0, "import", "-h", endpoint, "-i", restored, "-f", file.toString(), "-m", "-b", "2");
                launch(0, "transfer", "-sh", endpoint, "-dh", endpoint, "-si", source, "-di", copied, "-b", "2");
                for (String index : Arrays.asList(restored, copied)) {
                    service.refresh(index);
                    assertEquals(3, store.getIndex(String.class, index).count());
                    for (int i = 0; i < 3; i++) assertEquals("{\"value\":\"é " + i + "\"}",
                            service.realTimeGet(index, "id-" + i, "tenant"));
                }
            } finally { for (String index : created) store.getClient().indices().delete(r -> r.index(index)); }
        } finally { Files.deleteIfExists(file); Files.deleteIfExists(directory); }
    }
}
