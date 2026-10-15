package com.nosqlrevolution.apps;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nosqlrevolution.DocumentOptions;
import com.nosqlrevolution.ElasticStore;
import com.nosqlrevolution.service.QueryService;
import com.nosqlrevolution.util.JsonUtil;
import java.util.List;
import java.util.concurrent.Callable;

/** Imports raw JSON documents or explicitly selected metadata envelopes. */
public class JsonImporter implements Callable<CountsAndBytes> {
    private final ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private final QueryService service;
    private final String index, idField;
    private final List<String> data;
    private final boolean modelMode;

    public JsonImporter(ElasticStore store, String index, List<String> data, String idField) throws Exception {
        this(store, index, data, idField, false);
    }

    public JsonImporter(ElasticStore store, String index, List<String> data, String idField, boolean modelMode) throws Exception {
        if (modelMode && idField != null) throw new IllegalArgumentException("Model imports use envelope IDs; -id cannot be combined with -m");
        service = new QueryService(store.getClient());
        this.index = index;
        this.idField = idField;
        this.data = data;
        this.modelMode = modelMode;
    }

    @Override
    public CountsAndBytes call() throws Exception {
        String[] sources = new String[data.size()];
        String[] ids = new String[data.size()];
        DocumentOptions[] options = new DocumentOptions[data.size()];
        long bytes = 0;
        // Validate the complete block before sending any writes from it.
        for (int i = 0; i < data.size(); i++) {
            String record = data.get(i);
            JsonNode root = object(record);
            options[i] = DocumentOptions.DEFAULT;
            if (modelMode) {
                if (!root.path("id").isTextual() || root.path("id").asText().trim().isEmpty()
                        || !root.path("json").isTextual()) {
                    throw new IllegalArgumentException("Model record requires a nonblank string id and a JSON string");
                }
                ExportModel model = mapper.treeToValue(root, ExportModel.class);
                sources[i] = model.getJson();
                object(sources[i]);
                ids[i] = model.getId();
                if (root.hasNonNull("routing")) {
                    if (!root.get("routing").isTextual()) throw new IllegalArgumentException("Model routing must be a string");
                    options[i] = options[i].routing(model.getRouting());
                }
            } else {
                sources[i] = record;
                ids[i] = JsonUtil.getId(record, idField);
            }
            bytes += record.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        }
        service.bulkIndexWithIds(index, sources, ids, options);
        return new CountsAndBytes(data.size(), bytes);
    }

    private JsonNode object(String json) throws Exception {
        JsonNode node = mapper.readTree(json);
        if (node == null || !node.isObject()) throw new IllegalArgumentException("Document must be a JSON object");
        return node;
    }
}
