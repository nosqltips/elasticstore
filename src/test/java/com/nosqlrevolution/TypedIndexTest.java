package com.nosqlrevolution;

import com.nosqlrevolution.annotation.index.IndexType;
import com.nosqlrevolution.model.PersonIndex;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class TypedIndexTest {
    private static ElasticStore elasticStore;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
         elasticStore = new ElasticStore().asMemoryOnly().execute();
         assertNotNull(elasticStore);
         assertNotNull(elasticStore.getRestClient());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
         elasticStore.close();
    }
    
    @Test
    public void createSingleIndex() {
        try {
            Index<Object> index = elasticStore.getIndex(Object.class, "index", "type");
            assertNotNull(index);
            assertEquals("index",  index.getIndex());
            assertEquals("type",  index.getType());
        } catch (Exception ex) {
            Logger.getLogger(TypedIndexTest.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
    
    @Test
    public void createSingleIndexAnnotation() {
        try {
            Index<PersonIndex> index = elasticStore.getIndex(PersonIndex.class);
            assertNotNull(index);
            assertEquals("test",  index.getIndex());
            assertEquals("person",  index.getType());
        } catch (Exception ex) {
            Logger.getLogger(TypedIndexTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
     }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMissingType() throws Exception {
        elasticStore.getIndex(TestMissingType.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMissingIndex1() throws Exception {
        elasticStore.getIndex(TestMissingIndex1.class);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMissingIndex2() throws Exception {
        elasticStore.getIndex(TestMissingIndex2.class);
    }

    @com.nosqlrevolution.annotation.index.Index("test")
    public class TestMissingType {}

    @IndexType("person")
    public class TestMissingIndex1 {}

    @IndexType()
    public class TestMissingIndex2 {}
}
