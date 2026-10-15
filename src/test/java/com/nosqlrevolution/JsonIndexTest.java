package com.nosqlrevolution;

import co.elastic.clients.json.JsonData;
import com.nosqlrevolution.support.StoreTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;

/** Opening index handles does not require an Elasticsearch server. */
public class JsonIndexTest extends StoreTestSupport {
    @Test public void opensStringAndJsonDataHandlesWithoutCreatingIndices() throws Exception {
        Index<String> strings = elasticStore.getIndex(String.class, "documents");
        Index<JsonData> json = elasticStore.getIndex(JsonData.class, "documents");
        assertTrue(strings instanceof JsonIndex);
        assertTrue(json instanceof TypedIndex);
        assertEquals("documents", strings.getIndex());
        assertEquals("documents", json.getIndex());
    }
}
