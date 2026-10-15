package com.nosqlrevolution;

/** A write or bulk request with failed results; primary writes may already be stored. */
public class OperationException extends IllegalStateException {
    private final OperationStatus status;
    public OperationException(String message, OperationStatus status) {
        super(message);
        this.status = status;
    }
    public OperationStatus getStatus() { return status; }
}
