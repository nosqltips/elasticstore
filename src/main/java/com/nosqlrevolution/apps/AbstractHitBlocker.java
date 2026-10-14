package com.nosqlrevolution.apps;

import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;

public abstract class AbstractHitBlocker extends BufferedBlocker<Hit<JsonData>> {
    public AbstractHitBlocker(int blockSize, int limit, int sample) {
        super(blockSize, limit, sample);
    }
}
