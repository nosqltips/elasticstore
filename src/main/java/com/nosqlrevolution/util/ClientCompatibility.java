package com.nosqlrevolution.util;

import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeResponse;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Bridges response metadata APIs that changed across Elasticsearch 8.x clients. */
public final class ClientCompatibility {
    private static final MethodHandle SEQUENCE_NUMBER = getter("seqNo");
    private static final MethodHandle PRIMARY_TERM = getter("primaryTerm");
    private static final MethodHandle PIT_SHARDS = pitShardsGetter();

    private ClientCompatibility() { }

    private static MethodHandle getter(String name) {
        try {
            return MethodHandles.publicLookup().unreflect(WriteResponseBase.class.getMethod(name))
                    .asType(MethodType.methodType(Long.class, WriteResponseBase.class));
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static Long read(MethodHandle getter, WriteResponseBase response) {
        try { return (Long) getter.invokeExact(response); }
        catch (RuntimeException | Error ex) { throw ex; }
        catch (Throwable ex) { throw new IllegalStateException("Cannot read Elasticsearch write metadata", ex); }
    }

    private static MethodHandle pitShardsGetter() {
        try { return MethodHandles.publicLookup().unreflect(OpenPointInTimeResponse.class.getMethod("shards"))
                .asType(MethodType.methodType(ShardStatistics.class, OpenPointInTimeResponse.class)); }
        catch (NoSuchMethodException absentInOlderClients) { return null; }
        catch (IllegalAccessException ex) { throw new ExceptionInInitializerError(ex); }
    }

    /** Older clients discard PIT shard metadata; the server still opens PITs strictly by default. */
    public static ShardStatistics pitShards(OpenPointInTimeResponse response) {
        if (PIT_SHARDS == null) return null;
        try { return (ShardStatistics) PIT_SHARDS.invokeExact(response); }
        catch (RuntimeException | Error ex) { throw ex; }
        catch (Throwable ex) { throw new IllegalStateException("Cannot read Elasticsearch PIT metadata", ex); }
    }

    public static Long sequenceNumber(WriteResponseBase response) { return read(SEQUENCE_NUMBER, response); }
    public static Long primaryTerm(WriteResponseBase response) { return read(PRIMARY_TERM, response); }
}
