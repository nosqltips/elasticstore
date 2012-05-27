package com.nosqlrevolution.model;

import com.nosqlrevolution.annotation.DocumentId;

/**
 *
 * @author cbrown
 */
public class TestIdAnnotationMethodStatic {
    private static String someid;
    @DocumentId
    public static String getId() { return someid; }
    public static void setId(String id) { someid = id; }
}
