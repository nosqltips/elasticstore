package com.nosqlrevolution;

/**
 *
 * @author cbrown
 */
public class OperationStatus {
    private boolean success = false;

    public boolean succeeded() {
        return success;
    }

    public OperationStatus setSucceeded(boolean success) {
        this.success = success;
        return this;
    }
}
