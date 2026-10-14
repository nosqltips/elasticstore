package com.nosqlrevolution.apps;


public abstract class AbstractBlocker extends BufferedBlocker<String> {
    public AbstractBlocker(int blockSize, int limit, int sample) {
        super(blockSize, limit, sample);
    }
}
