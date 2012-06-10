package com.nosqlrevolution.annotation;

import com.nosqlrevolution.util.AnnotationHelper;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class RoutingTest {
     @Test
     public void testFieldAnnotation() {
         TestField t = new TestField();
         t.setRouting("simpson");

         String routing = AnnotationHelper.getRoutingValue(t, null);
         assertNotNull(routing);
         assertEquals("simpson", routing);
     }
   
     @Test
     public void testMethodAnnotation() {
         TestMethod t = new TestMethod();
         t.setRouting("simpson");

         String routing = AnnotationHelper.getRoutingValue(t, null);
         assertNotNull(routing);
         assertEquals("simpson", routing);
     }
   
     @Test
     public void testStandardField1() {
         TestStandardField1 t = new TestStandardField1();
         t.setRouting("simpson");

         String routing = AnnotationHelper.getRoutingValue(t, null);
         assertNotNull(routing);
         assertEquals("simpson", routing);
     }
   
     @Test
     public void testStandardField2() {
         TestStandardField2 t = new TestStandardField2();
         t.setRouting("simpson");

         String routing = AnnotationHelper.getRoutingValue(t, null);
         assertNotNull(routing);
         assertEquals("simpson", routing);
     }
   
     @Test
     public void testNamedField() {
         TestNamedField t = new TestNamedField();
         t.setRouting("simpson");

         String routing = AnnotationHelper.getRoutingValue(t, "otherRouting");
         assertNotNull(routing);
         assertEquals("simpson", routing);
     }
   
     @Test
     public void testBad() {
         TestBad t = new TestBad();
         t.setRouting("simpson");

         String routing = AnnotationHelper.getRoutingValue(t, null);
         assertNull(routing);
     }
   
     // Test classes     
     private class TestField {
         @Routing
         private String route;
         public String getRouting() { return route; }
         public void setRouting(String route) { this.route = route; }
     }

     private class TestMethod {
         private String route;
         @Routing
         public String getRouting() { return route; }
         public void setRouting(String route) { this.route = route; }
     }

     private class TestStandardField1 {
         private String routing;
         public String getRouting() { return routing; }
         public void setRouting(String routing) { this.routing = routing; }
     }

     private class TestStandardField2 {
         private String _routing;
         public String getRouting() { return _routing; }
         public void setRouting(String _routing) { this._routing = _routing; }
     }

     private class TestNamedField {
         private String otherRouting;
         public String getRouting() { return otherRouting; }
         public void setRouting(String otherRouting) { this.otherRouting = otherRouting; }
     }
     
     private class TestBad {
         private String otherName;
         public String getRouting() { return otherName; }
         public void setRouting(String otherName) { this.otherName = otherName; }
     }
}
