package com.nosqlrevolution.annotation.index;

import com.nosqlrevolution.util.AnnotationHelper;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class IndexTest {
     @Test
     public void testClassAnnotation() {
         TestClass t = new TestClass();

         String index = AnnotationHelper.getIndexValue(t.getClass());
         assertNotNull(index);
         assertEquals("myIndex", index);
     }
   
     @Test
     public void testBlankIndex() {
         TestBlankIndex t = new TestBlankIndex();

         String index = AnnotationHelper.getIndexValue(t.getClass());
         assertNull(index);
     }
   
     @Test
     public void testBothAnnotation1() {
         TestBoth1 t = new TestBoth1();

         String index = AnnotationHelper.getIndexValue(t.getClass());
         String type = AnnotationHelper.getIndexTypeValue(t.getClass());
         assertNotNull(index);
         assertNotNull(type);
         assertEquals("myIndex", index);
         assertEquals("myType", type);
     }
   
     @Test
     public void testBothAnnotation2() {
         TestBoth2 t = new TestBoth2();

         String index = AnnotationHelper.getIndexValue(t.getClass());
         String type = AnnotationHelper.getIndexTypeValue(t.getClass());
         assertNotNull(index);
         assertNotNull(type);
         assertEquals("myIndex", index);
         assertEquals("testBoth2", type);
     }
   
     @Test
     public void testBad() {
         TestBad t = new TestBad();

         String index = AnnotationHelper.getIndexValue(t.getClass());
         assertNull(index);
     }
   
     // Test classes     
     @Index("myIndex")
     private class TestClass {}

     @Index("")
     private class TestBlankIndex {}

     @Index("myIndex")
     @IndexType("myType")
     private class TestBoth1 {}

     @Index("myIndex")
     @IndexType
     private class TestBoth2 {}
     
     private class TestBad {}
}
