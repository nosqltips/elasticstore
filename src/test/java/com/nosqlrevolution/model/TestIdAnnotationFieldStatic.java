package com.nosqlrevolution.model;

import com.nosqlrevolution.annotation.DocumentId;

/**
 *
 * @author cbrown
 */
public class TestIdAnnotationFieldStatic {
    @DocumentId
    private static String someid;
    public static void setId(String id) { someid = id; }
}
