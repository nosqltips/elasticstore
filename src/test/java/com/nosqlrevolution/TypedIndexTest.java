package com.nosqlrevolution;

import com.nosqlrevolution.annotation.index.IndexType;
import com.nosqlrevolution.model.PersonIndex;
import com.nosqlrevolution.support.StoreTestSupport;
import org.junit.Test;
import static org.junit.Assert.*;

public class TypedIndexTest extends StoreTestSupport {
    @Test public void opensExplicitAndAnnotatedHandles() throws Exception {
        Index<Object> explicit = elasticStore.getIndex(Object.class, "documents");
        assertEquals("documents", explicit.getIndex());
        assertEquals("test", elasticStore.getIndex(PersonIndex.class).getIndex());
    }
    @Test public void indexAnnotationNeedsNoObsoleteTypeMarker() throws Exception {
        assertEquals("documents", elasticStore.getIndex(IndexOnly.class).getIndex());
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsTypeWithoutIndex() throws Exception {
        elasticStore.getIndex(TypeOnly.class);
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsEmptyIndexAnnotation() throws Exception {
        elasticStore.getIndex(EmptyIndex.class);
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsBlankExplicitIndex() throws Exception {
        elasticStore.getIndex(Object.class, " ");
    }
    @com.nosqlrevolution.annotation.index.Index("documents") public static class IndexOnly { }
    @IndexType("person") public static class TypeOnly { }
    @com.nosqlrevolution.annotation.index.Index("") public static class EmptyIndex { }
}
