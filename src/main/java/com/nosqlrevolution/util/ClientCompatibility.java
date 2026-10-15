package com.nosqlrevolution.util;

import co.elastic.clients.elasticsearch._types.WriteResponseBase;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Bridges write metadata getters that changed from primitive to boxed in 8.x. */
public final class ClientCompatibility {
    private static final MethodHandle SEQUENCE_NUMBER = getter("seqNo");
    private static final MethodHandle PRIMARY_TERM = getter("primaryTerm");

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

    public static Long sequenceNumber(WriteResponseBase response) { return read(SEQUENCE_NUMBER, response); }
    public static Long primaryTerm(WriteResponseBase response) { return read(PRIMARY_TERM, response); }
}
