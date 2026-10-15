package com.nosqlrevolution;

import org.junit.Test;
import static org.junit.Assert.*;
import com.nosqlrevolution.annotation.*;
import com.nosqlrevolution.util.AnnotationHelper;

public class DocumentOptionsTest {
    static class Parent {
        @DocumentId @UUIDProvider private String key;
        @Routing private String tenant = "tenant";
        @Version private Long revision = 7L;
    }
    static class Child extends Parent { }
    @Test public void inheritedMetadataAndUuidRemainStable() {
        Child document = new Child();
        String id = AnnotationHelper.getDocumentId(document, null);
        assertNotNull(id);
        assertEquals(id, AnnotationHelper.getDocumentId(document, null));
        assertEquals("tenant", AnnotationHelper.getRoutingValue(document, null));
        assertEquals("7", AnnotationHelper.getVersionValue(document, null));
        assertEquals(id, AnnotationHelper.findDocumentId(document, null));
    }
    static class GetterParent {
        int calls;
        @Routing public String tenant() { calls++; return "getter-tenant"; }
    }
    static class GetterChild extends GetterParent { }
    @Test public void inheritedMetadataGetterIsInvokedOnce() {
        GetterChild document = new GetterChild();
        assertEquals("getter-tenant", AnnotationHelper.getRoutingValue(document, null));
        assertEquals(1, document.calls);
    }
    @Test public void optionsAreImmutableAndRejectConflictingConstraints() {
        DocumentOptions routed = DocumentOptions.DEFAULT.routing("tenant");
        DocumentOptions conditional = routed.ifUnchanged(0, 1);
        assertNull(DocumentOptions.DEFAULT.getRouting());
        assertNull(routed.getSequenceNumber());
        assertEquals(Long.valueOf(0), conditional.getSequenceNumber());
        assertEquals("tenant", conditional.getRouting());
        invalid(() -> conditional.externalVersion(2));
        invalid(() -> routed.externalVersion(2).ifUnchanged(0, 1));
        invalid(() -> routed.externalVersion(0));
        invalid(() -> routed.ifUnchanged(-1, 1));
        invalid(() -> routed.ifUnchanged(0, 0));
        invalid(() -> routed.routing(" "));
    }
    private void invalid(Runnable operation) {
        try { operation.run(); fail("Expected invalid options"); }
        catch (IllegalArgumentException expected) { }
    }
}
