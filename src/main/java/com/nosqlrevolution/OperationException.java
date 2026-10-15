package com.nosqlrevolution;

/** A completed bulk request with failed items; successful items may already be stored. */
public class OperationException extends IllegalStateException {
    private final OperationStatus status;
    public OperationException(String message, OperationStatus status) {
        super(message);
        this.status = status;
    }
    public OperationStatus getStatus() { return status; }
}
