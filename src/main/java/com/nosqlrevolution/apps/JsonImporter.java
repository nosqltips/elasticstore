package com.nosqlrevolution.apps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.Index;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author cbrown
 */
public class JsonImporter implements Callable<Integer> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Index index;
    private final List<String> data;
    private int totalCount = 0;
    
    public JsonImporter(Index index, List<String> data) {
        this.index = index;
        this.data = data;
    }
    
    @Override
    public Integer call() throws Exception {
        for (String s: data) {
            // We want to render each JSON object to make sure it is valid before sending to ES.
            try {
                mapper.readValue(s, new TypeReference<HashMap<String,Object>>() {});
                index.addBulk(s);
                totalCount += 1;
            } catch (IOException ie) {
                // TODO: write to a log file.
                System.out.println("Bad JSON value = " + s);
            }
        }

        index.flushBulk();
        return totalCount;
    }
}