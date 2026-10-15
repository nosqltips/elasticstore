package com.nosqlrevolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Outcome of an operation. Item results retain request order, including failures. */
public class OperationStatus {
    private boolean success;
    private final List<Item> items;

    public OperationStatus() { items = Collections.emptyList(); }

    public OperationStatus(List<Item> items) {
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        success = this.items.stream().allMatch(Item::succeeded);
    }

    public boolean succeeded() { return success; }
    public List<Item> getItems() { return items; }
    public long getSuccessCount() { return items.stream().filter(Item::succeeded).count(); }
    public long getFailureCount() { return items.size() - getSuccessCount(); }

    /** Legacy setter for operations without document results. */
    public OperationStatus setSucceeded(boolean success) {
        if (!items.isEmpty()) throw new IllegalStateException("Document results determine success");
        this.success = success;
        return this;
    }

    /** Immutable Elasticsearch response metadata; absent metadata is null. */
    public static final class Item {
        private final String index, id, result, errorType, errorReason;
        private final int status;
        private final Long version, sequenceNumber, primaryTerm;

        public Item(String index, String id, String result, int status, Long version,
                Long sequenceNumber, Long primaryTerm, String errorType, String errorReason) {
            this.index = index; this.id = id; this.result = result; this.status = status;
            this.version = version; this.sequenceNumber = sequenceNumber; this.primaryTerm = primaryTerm;
            this.errorType = errorType; this.errorReason = errorReason;
        }
        public String getIndex() { return index; }
        public String getId() { return id; }
        public String getResult() { return result; }
        public int getStatus() { return status; }
        public Long getVersion() { return version; }
        public Long getSequenceNumber() { return sequenceNumber; }
        public Long getPrimaryTerm() { return primaryTerm; }
        public String getErrorType() { return errorType; }
        public String getErrorReason() { return errorReason; }
        public boolean succeeded() { return errorType == null && ((status >= 200 && status < 300) || (status == 404 && "not_found".equals(result))); }
    }
}
