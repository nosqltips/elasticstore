package com.nosqlrevolution.apps;

import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author cbrown
 */
public class JsonImporter implements Callable<CountsAndBytes> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Index esIndex;
    private final List<String> data;
    private int totalCount = 0;
    private long totalBytes = 0;
    
    public JsonImporter(ElasticStore store, String index, List<String> data, String idField) throws Exception {
        esIndex = store.getIndex(JsonData.class, index);
        esIndex.setIdField(idField);
        this.data = data;
    }
    
    @Override
    public CountsAndBytes call() throws Exception {
        for (String s: data) {
            // Reject malformed records before adding them to the bulk request.
            mapper.readValue(s, new TypeReference<HashMap<String, Object>>() {});
            esIndex.addBulk(s);
            totalCount++;
            totalBytes += s.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        }

        esIndex.flushBulk();
        return new CountsAndBytes(totalCount, totalBytes);
    }
}