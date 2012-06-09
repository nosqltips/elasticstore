package com.nosqlrevolution.annotation.index;

import com.nosqlrevolution.util.AnnotationHelper;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class TypeTest {
     @Test
     public void testFieldAnnotation() throws NoSuchMethodException {
         TestNamed t = new TestNamed();

         String type = AnnotationHelper.getIndexTypeValue(t);
         assertNotNull(type);
         assertEquals("myType", type);
     }
   
     @Test
     public void testMethodAnnotation() throws NoSuchMethodException {
         TestDefault t = new TestDefault();

         String type = AnnotationHelper.getIndexTypeValue(t);
         assertNotNull(type);
         assertEquals("testDefault", type);
     }
   
     @Test
     public void testBad() throws NoSuchMethodException {
         TestBad t = new TestBad();

         String type = AnnotationHelper.getIndexTypeValue(t);
         assertNull(type);
     }
   
     // Test classes
     @IndexType("myType")
     private class TestNamed {}

     @IndexType
     private class TestDefault {}
     
     private class TestBad {}
}
