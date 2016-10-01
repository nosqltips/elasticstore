package com.nosqlrevolution.apps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.Index;
import java.io.IOException;
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
    private final String idField;
    private int totalCount = 0;
    private int totalBytes = 0;
    
    public JsonImporter(ElasticStore store, String index, String type, List<String> data, String idField) throws Exception {
        esIndex = store.getIndex(String.class, index, type);
        esIndex.setIdField(idField);
        this.data = data;
        this.idField = idField;
    }
    
    @Override
    public CountsAndBytes call() throws Exception {
        for (String s: data) {
            try {
                // We want to render each JSON object to make sure it is valid before sending to ES.
                HashMap<String, Object> hashValues = mapper.readValue(s, new TypeReference<HashMap<String,Object>>() {});

                esIndex.addBulk(s);
                totalCount += 1;
                totalBytes += s.getBytes().length;
            } catch (IOException ie) {
                // TODO: write to a log file.
                System.out.println("Bad JSON value = " + s);
            }
        }

        esIndex.flushBulk();
        return new CountsAndBytes(totalCount, totalBytes);
    }
}