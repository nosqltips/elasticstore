package com.nosqlrevolution;

/** Immutable per-request routing and concurrency constraints. */
public final class DocumentOptions {
    public static final DocumentOptions DEFAULT = new DocumentOptions(null, null, null, null);
    private final String routing;
    private final Long sequenceNumber, primaryTerm, externalVersion;

    private DocumentOptions(String routing, Long sequenceNumber, Long primaryTerm, Long externalVersion) {
        this.routing = routing; this.sequenceNumber = sequenceNumber;
        this.primaryTerm = primaryTerm; this.externalVersion = externalVersion;
    }
    public DocumentOptions routing(String routing) {
        if (routing == null || routing.trim().isEmpty()) throw new IllegalArgumentException("Routing must not be blank");
        return new DocumentOptions(routing, sequenceNumber, primaryTerm, externalVersion);
    }
    public DocumentOptions ifUnchanged(long sequenceNumber, long primaryTerm) {
        if (sequenceNumber < 0 || primaryTerm <= 0) throw new IllegalArgumentException("Sequence number must be nonnegative and primary term positive");
        if (externalVersion != null) throw new IllegalArgumentException("Choose external versioning or sequence-number concurrency");
        return new DocumentOptions(routing, sequenceNumber, primaryTerm, null);
    }
    public DocumentOptions externalVersion(long version) {
        if (version <= 0) throw new IllegalArgumentException("External version must be positive");
        if (sequenceNumber != null) throw new IllegalArgumentException("Choose external versioning or sequence-number concurrency");
        return new DocumentOptions(routing, null, null, version);
    }
    public String getRouting() { return routing; }
    public Long getSequenceNumber() { return sequenceNumber; }
    public Long getPrimaryTerm() { return primaryTerm; }
    public Long getExternalVersion() { return externalVersion; }
}
