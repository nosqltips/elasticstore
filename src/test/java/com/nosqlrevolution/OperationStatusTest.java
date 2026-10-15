package com.nosqlrevolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class OperationStatusTest {
    private OperationStatus.Item item(int status, String result, String error) {
        return new OperationStatus.Item("index", "id", result, status, 1L, 2L, 3L, error, error);
    }

    @Test public void mixedResultsAreOrderedAndImmutable() {
        List<OperationStatus.Item> source = new ArrayList<>(Arrays.asList(
                item(201, "created", null), item(400, null, "mapper_parsing_exception")));
        OperationStatus status = new OperationStatus(source);
        source.clear();
        assertFalse(status.succeeded());
        assertEquals(1, status.getSuccessCount());
        assertEquals(1, status.getFailureCount());
        assertEquals("created", status.getItems().get(0).getResult());
        assertEquals("mapper_parsing_exception", status.getItems().get(1).getErrorType());
        try { status.getItems().clear(); fail(); } catch (UnsupportedOperationException expected) { }
        try { status.setSucceeded(true); fail(); } catch (IllegalStateException expected) { }
        assertSame(status, new OperationException("failed", status).getStatus());
    }

    @Test public void missingDeleteIsIdempotentButOtherErrorsFail() {
        assertTrue(new OperationStatus(Arrays.asList(item(404, "not_found", null))).succeeded());
        assertFalse(new OperationStatus(Arrays.asList(item(404, null, "index_not_found_exception"))).succeeded());
        assertFalse(new OperationStatus(Arrays.asList(item(429, null, null))).succeeded());
        assertTrue(new OperationStatus(new ArrayList<>()).succeeded());
    }
}
