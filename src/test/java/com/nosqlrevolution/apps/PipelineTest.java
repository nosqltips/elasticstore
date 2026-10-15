package com.nosqlrevolution.apps;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class PipelineTest {
    private static class Reader extends AbstractBlocker {
        final int count;
        boolean closed;
        Reader(int count) { super(2, 0, 0); this.count = count; }
        public Integer call() throws Exception {
            for (int i = 0; i < count; i++) bufferNext(String.valueOf(i));
            bufferNext(null);
            return totalCount;
        }
        public long getTotalDocs() { return count; }
        public void shudown() { closed = true; }
    }
    private static abstract class Runner extends AbstractPoolRunner {
        public void run(String[] args) { }
        void execute(Reader reader, int threads) throws Exception { run(reader, threads); }
    }

    @Test(timeout = 5000)
    public void drainsPartialBlockAndCompletesWithoutFinalPickup() throws Exception {
        Reader reader = new Reader(3);
        reader.call();
        assertEquals(Arrays.asList("0", "1"), reader.getNext());
        assertEquals(Arrays.asList("2"), reader.getNext());
        assertNull(reader.getNext());
        Reader empty = new Reader(0);
        empty.call();
        assertNull(empty.getNext());
    }

    @Test(timeout = 5000)
    public void hitBlockerDrainsPartialBlock() throws Exception {
        AbstractHitBlocker reader = new AbstractHitBlocker(2, 0, 0) {
            public Integer call() throws Exception {
                bufferNext(co.elastic.clients.elasticsearch.core.search.Hit.of(h -> h
                        .index("test").id("1")));
                bufferNext(null);
                return totalCount;
            }
            public long getTotalDocs() { return 1; }
            public void shudown() { }
        };
        reader.call();
        assertEquals("1", reader.getNext().get(0).id());
        assertNull(reader.getNext());
    }

    @Test(timeout = 5000)
    public void readerFailureWakesConsumerAndClosesResources() throws Exception {
        Reader reader = new Reader(0) {
            public Integer call() throws IOException { throw new IOException("read failed"); }
        };
        Runner runner = new Runner() {
            protected Callable<CountsAndBytes> getNextCallable(List<String> block) {
                return () -> new CountsAndBytes(block.size(), 0);
            }
        };
        try { runner.execute(reader, 2); fail("Expected failure"); }
        catch (IllegalStateException ex) { assertEquals("read failed", ex.getCause().getMessage()); }
        assertTrue(reader.closed);
    }

    @Test(timeout = 5000)
    public void workerFailureStopsReaderWaitingForPickup() throws Exception {
        Reader reader = new Reader(100000);
        Runner runner = new Runner() {
            protected Callable<CountsAndBytes> getNextCallable(List<String> block) {
                return () -> { throw new IOException("write failed"); };
            }
        };
        try { runner.execute(reader, 1); fail("Expected failure"); }
        catch (IOException ex) { assertEquals("write failed", ex.getMessage()); }
        assertTrue(reader.closed);
    }

    @Test(timeout = 5000)
    public void fastWorkerReceivesNextBlockBeforeSlowWorkerFinishes() throws Exception {
        CountDownLatch third = new CountDownLatch(1);
        Reader reader = new Reader(6);
        Runner runner = new Runner() {
            protected Callable<CountsAndBytes> getNextCallable(List<String> block) {
                return () -> {
                    if (block.get(0).equals("0")) {
                        assertTrue("Slow worker blocked scheduling", third.await(2, TimeUnit.SECONDS));
                    }
                    if (block.get(0).equals("4")) third.countDown();
                    return new CountsAndBytes(block.size(), 0);
                };
            }
        };
        runner.execute(reader, 2);
        assertTrue(reader.closed);
    }

    @Test(timeout = 5000)
    public void interruptIsFailureRatherThanEndOfStream() throws Exception {
        Reader reader = new Reader(0);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> result = executor.submit(() -> {
                Thread.currentThread().interrupt();
                try { reader.getNext(); return false; }
                catch (IllegalStateException ex) { return Thread.currentThread().isInterrupted(); }
            });
            assertTrue(result.get());
        } finally { executor.shutdownNow(); }
    }

    @Test(timeout = 5000)
    public void cancelReleasesProducerBlockedOnFullQueue() throws Exception {
        Reader reader = new Reader(100000);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> producer = executor.submit(reader);
            assertNotNull(reader.getNext());
            reader.cancel();
            try { producer.get(2, TimeUnit.SECONDS); fail("Expected cancellation"); }
            catch (ExecutionException ex) {
                assertTrue(ex.getCause() instanceof IllegalStateException);
            }
        } finally { executor.shutdownNow(); }
    }

    @Test(timeout = 5000)
    public void cleanupFailureDoesNotHideReaderFailure() throws Exception {
        final IOException cleanup = new IOException("close failed");
        AbstractBlocker reader = new AbstractBlocker(2, 0, 0) {
            public Integer call() throws IOException { throw new IOException("read failed"); }
            public long getTotalDocs() { return 0; }
            public void shudown() throws IOException { throw cleanup; }
        };
        Runner runner = new Runner() {
            protected Callable<CountsAndBytes> getNextCallable(List<String> block) {
                return () -> new CountsAndBytes(0, 0);
            }
        };
        try { runner.run(reader, 1); fail("Expected failure"); }
        catch (IllegalStateException ex) {
            assertEquals("read failed", ex.getCause().getMessage());
            assertSame(cleanup, ex.getSuppressed()[0]);
        }
    }

    @Test(timeout = 5000)
    public void samplingKeepsEveryNthDocumentAndLimitCountsKeptDocuments() throws Exception {
        AbstractBlocker reader = new AbstractBlocker(2, 2, 3) {
            public Integer call() throws Exception {
                for (int i = 1; i <= 10; i++) {
                    if (!bufferNext(String.valueOf(i))) break;
                }
                bufferNext(null);
                return totalCount;
            }
            public long getTotalDocs() { return 10; }
            public void shudown() { }
        };
        assertEquals(Integer.valueOf(2), reader.call());
        assertEquals(Arrays.asList("3", "6"), reader.getNext());
        assertNull(reader.getNext());
    }

    @Test(timeout = 5000)
    public void fileBlockerReadsUtf8AndRejectsInvalidGzip() throws Exception {
        java.nio.file.Path file = java.nio.file.Files.createTempFile("elasticstore", ".json");
        java.nio.file.Path invalid = java.nio.file.Files.createTempFile("elasticstore", ".gz");
        try {
            java.nio.file.Files.write(file, "{\"name\":\"é\"}\n\n{}\n".getBytes(StandardCharsets.UTF_8));
            try (FileBlocker reader = new FileBlocker(file.toString(), 2, 0, 0)) {
                assertEquals(2, reader.getTotalDocs());
                assertEquals(Integer.valueOf(2), reader.call());
                assertEquals(Arrays.asList("{\"name\":\"é\"}", "{}"), reader.getNext());
                assertNull(reader.getNext());
            }
            try { new FileBlocker(invalid.toString(), 2, 0, 0); fail("Expected invalid gzip failure"); }
            catch (IOException expected) { }
        } finally {
            java.nio.file.Files.delete(file);
            java.nio.file.Files.delete(invalid);
        }
    }

    @Test(timeout = 5000)
    public void concurrentExportsKeepRecordsIntactAndCountUtf8Bytes() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BufferedOutputStream stream = new BufferedOutputStream(output, 8);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<CountsAndBytes>> futures = new ArrayList<>();
        String record = "{\"value\":\"é\"}";
        try {
            for (int i = 0; i < 4; i++) {
                futures.add(executor.submit(new JsonExporter(stream,
                        java.util.Collections.nCopies(200, record))));
            }
            long bytes = 0;
            for (Future<CountsAndBytes> future : futures) {
                CountsAndBytes counts = future.get();
                assertEquals(200, counts.getCounts());
                bytes += counts.getBytes();
            }
            stream.flush();
            assertEquals(output.size(), bytes);
            String[] lines = new String(output.toByteArray(), StandardCharsets.UTF_8).split("\\R");
            assertEquals(800, lines.length);
            for (String line : lines) assertEquals(record, line);
        } finally { executor.shutdownNow(); stream.close(); }
    }
    @Test public void invalidWorkerCountsStillCloseTheReader() throws Exception {
        Runner runner = new Runner() {
            protected Callable<CountsAndBytes> getNextCallable(List<String> block) {
                throw new AssertionError("Invalid runner started work");
            }
        };
        for (int threads : new int[] {0, -1}) {
            Reader reader = new Reader(0);
            try { runner.execute(reader, threads); fail("Accepted invalid worker count"); }
            catch (IllegalArgumentException expected) { }
            assertTrue(reader.closed);
        }
    }

    @Test public void malformedUtf8FailsInBothCountingAndReadingPlainOrGzipFiles() throws Exception {
        byte[] invalid = new byte[] {'{', '"', 'x', '"', ':', '"', (byte) 0xc3, '"', '}', '\n'};
        for (String extension : new String[] {".json", ".gz"}) {
            java.nio.file.Path file = java.nio.file.Files.createTempFile("invalid-utf8", extension);
            try {
                if (extension.equals(".gz")) {
                    try (java.util.zip.GZIPOutputStream out = new java.util.zip.GZIPOutputStream(java.nio.file.Files.newOutputStream(file))) { out.write(invalid); }
                } else java.nio.file.Files.write(file, invalid);
                try { new FileBlocker(file.toString(), 2, 0, 0); fail("Count accepted invalid UTF-8"); }
                catch (java.nio.charset.CharacterCodingException expected) { }
                try (FileBlocker reader = new FileBlocker(file.toString(), 2, 1, 0)) {
                    try { reader.call(); fail("Read accepted invalid UTF-8"); }
                    catch (java.nio.charset.CharacterCodingException expected) { }
                }
            } finally { java.nio.file.Files.delete(file); }
        }
    }
}
