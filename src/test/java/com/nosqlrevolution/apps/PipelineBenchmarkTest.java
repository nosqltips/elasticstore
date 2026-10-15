package com.nosqlrevolution.apps;

import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.query.ElasticStoreQuery;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import org.junit.*;
import static org.junit.Assert.*;

/** Opt-in measurements, with no timing assertions or production-index access. */
public class PipelineBenchmarkTest {
    private static class Runner extends AbstractPoolRunner {
        final ElasticStore store;
        final String index;
        final boolean importing;
        final BufferedOutputStream sink = new BufferedOutputStream(OutputStream.nullOutputStream());
        long count, bytes;
        Runner(ElasticStore store, String index, boolean importing) {
            this.store = store; this.index = index; this.importing = importing;
        }
        public void run(String[] args) { throw new UnsupportedOperationException(); }
        protected Callable<CountsAndBytes> getNextCallable(List<String> block) throws Exception {
            Callable<CountsAndBytes> work = importing ? new JsonImporter(store, index, block, null)
                    : new JsonExporter(sink, block);
            return () -> {
                CountsAndBytes result = work.call();
                synchronized (this) { count += result.getCounts(); bytes += result.getBytes(); }
                return result;
            };
        }
        void execute(AbstractBlocker source, int workers) throws Exception { run(source, workers); sink.flush(); }
    }

    @Test public void measureFileBulkAndSearchReader() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("elasticstore.benchmark"));
        String endpoint = System.getProperty("elasticstore.test.url");
        Assume.assumeNotNull(endpoint);
        int documents = Integer.getInteger("elasticstore.benchmark.documents", 10000);
        int samples = Integer.getInteger("elasticstore.benchmark.samples", 3);
        if (documents < 1 || samples < 1) throw new IllegalArgumentException("Benchmark sizes must be positive");
        Path output = Paths.get(System.getProperty("elasticstore.benchmark.output", "target/benchmarks/pipeline.csv"));
        Files.createDirectories(output.toAbsolutePath().getParent());
        Path file = Files.createTempFile("elasticstore-benchmark-", ".jsonl");
        String index = "elasticstore-benchmark-" + UUID.randomUUID();
        String payload = "abcdefghij".repeat(80);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (int i = 0; i < documents; i++) {
                writer.write("{\"id\":\"doc-" + i + "\",\"value\":" + i
                        + ",\"name\":\"é benchmark\",\"nested\":{\"active\":true},\"payload\":\"" + payload + "\"}\n");
            }
        }
        PrintStream console = System.out;
        try (ElasticStore store = new ElasticStore().withEndpoints(endpoint).execute();
                PrintWriter csv = new PrintWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8));
                PrintStream quiet = new PrintStream(OutputStream.nullOutputStream())) {
            csv.println("operation,block_size,workers,sample,documents,bytes,seconds,docs_per_second,mib_per_second");
            try {
                store.getClient().indices().create(r -> r.index(index).settings(s -> s.numberOfShards("1").numberOfReplicas("0")));
                for (int block : new int[] {250, 1000}) {
                    for (int workers : new int[] {1, 4}) {
                        for (int sample = 0; sample <= samples; sample++) {
                            // Construction, file counting and PIT setup are included in wall time.
                            Runner importer = new Runner(store, index, true);
                            System.setOut(quiet);
                            long start = System.nanoTime();
                            importer.execute(new FileBlocker(file.toString(), block, 0, 0), workers);
                            long elapsed = System.nanoTime() - start;
                            System.setOut(console);
                            assertEquals(documents, importer.count);
                            store.getClient().indices().refresh(r -> r.index(index));
                            assertEquals(documents, store.getIndex(String.class, index).count());
                            if (sample > 0) record(csv, "import", block, workers, sample, importer, elapsed);
                        }
                    }
                    // Reads are sequential; worker count is deliberately one for exports.
                    for (int sample = 0; sample <= samples; sample++) {
                        Runner exporter = new Runner(store, index, false);
                        System.setOut(quiet);
                        long start = System.nanoTime();
                        exporter.execute(new JsonBlocker(store, new ElasticStoreQuery().pageSize(block), index, block, 0, 0), 1);
                        long elapsed = System.nanoTime() - start;
                        System.setOut(console);
                        assertEquals(documents, exporter.count);
                        if (sample > 0) record(csv, "read", block, 1, sample, exporter, elapsed);
                    }
                }
            } finally {
                System.setOut(console);
                if (store.getClient().indices().exists(r -> r.index(index)).value()) store.getClient().indices().delete(r -> r.index(index));
            }
        } finally { System.setOut(console); Files.deleteIfExists(file); }
        System.out.println("Benchmark CSV: " + output.toAbsolutePath());
    }

    @Test public void measurePreparationAndSerializationWithoutNetwork() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("elasticstore.benchmark"));
        int documents = Integer.getInteger("elasticstore.benchmark.documents", 10000);
        int samples = Integer.getInteger("elasticstore.benchmark.samples", 5);
        if (documents < 1 || samples < 1) throw new IllegalArgumentException("Benchmark sizes must be positive");
        List<String> block = new ArrayList<>();
        for (int i = 0; i < 1000; i++) block.add("{\"id\":\"doc-" + i + "\",\"value\":" + i
                + ",\"name\":\"é benchmark\",\"nested\":{\"active\":true},\"payload\":\"" + "abcdefghij".repeat(80) + "\"}");
        com.sun.management.ThreadMXBean allocation = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        Assume.assumeTrue(allocation.isThreadAllocatedMemorySupported());
        allocation.setThreadAllocatedMemoryEnabled(true);
        long thread = Thread.currentThread().getId();
        co.elastic.clients.json.JsonpMapper mapper = new co.elastic.clients.json.jackson.JacksonJsonpMapper();
        org.elasticsearch.client.RestClient http = org.elasticsearch.client.RestClient.builder(new org.apache.http.HttpHost("127.0.0.1", 1)).build();
        co.elastic.clients.elasticsearch.ElasticsearchClient sink = new co.elastic.clients.elasticsearch.ElasticsearchClient(
                new co.elastic.clients.transport.rest_client.RestClientTransport(http, mapper)) {
            private final java.util.Map<Integer, co.elastic.clients.elasticsearch.core.BulkResponse> responses = new java.util.HashMap<>();
            @Override public co.elastic.clients.elasticsearch.core.BulkResponse bulk(co.elastic.clients.elasticsearch.core.BulkRequest request) {
                for (co.elastic.clients.elasticsearch.core.bulk.BulkOperation operation : request.operations()) {
                    try (jakarta.json.stream.JsonGenerator generator = mapper.jsonProvider().createGenerator(Writer.nullWriter())) {
                        ((co.elastic.clients.json.JsonData) operation.index().document()).serialize(generator, mapper);
                    }
                }
                return responses.computeIfAbsent(request.operations().size(), size -> {
                    java.util.List<co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem> items = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        final String id = "doc-" + i;
                        items.add(co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem.of(b -> b
                                .operationType(co.elastic.clients.elasticsearch.core.bulk.OperationType.Index)
                                .index("unused").id(id).status(201).result("created")));
                    }
                    return co.elastic.clients.elasticsearch.core.BulkResponse.of(r -> r.took(0).errors(false).items(items));
                });
            }
        };
        Path output = Paths.get(System.getProperty("elasticstore.benchmark.cpu.output", "target/benchmarks/preparation.csv"));
        Files.createDirectories(output.toAbsolutePath().getParent());
        try (ElasticStore store = new ElasticStore().withClient(sink, true);
                PrintWriter csv = new PrintWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {
            csv.println("sample,documents,seconds,docs_per_second,allocated_bytes_per_document");
            for (int sample = -2; sample <= samples; sample++) {
                long bytes = allocation.getThreadAllocatedBytes(thread), start = System.nanoTime();
                long processed = 0;
                while (processed < documents) {
                    int size = (int) Math.min(block.size(), documents - processed);
                    CountsAndBytes counts = new JsonImporter(store, "unused", block.subList(0, size), null).call();
                    processed += counts.getCounts();
                }
                double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
                long allocated = allocation.getThreadAllocatedBytes(thread) - bytes;
                if (sample > 0) csv.printf(Locale.ROOT, "%d,%d,%.6f,%.2f,%.2f%n", sample, processed, seconds,
                        processed / seconds, allocated / (double) processed);
            }
        }
        System.out.println("Preparation CSV (JSON source serialization, no HTTP/server): " + output.toAbsolutePath());
    }

    private static void record(PrintWriter csv, String operation, int block, int workers, int sample, Runner runner, long nanos) {
        double seconds = nanos / 1_000_000_000.0;
        csv.printf(Locale.ROOT, "%s,%d,%d,%d,%d,%d,%.6f,%.2f,%.2f%n", operation, block, workers, sample,
                runner.count, runner.bytes, seconds, runner.count / seconds, runner.bytes / 1048576.0 / seconds);
        csv.flush();
    }
}
