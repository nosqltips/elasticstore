package com.nosqlrevolution.annotation;

import com.nosqlrevolution.util.AnnotationHelper;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class VersionTest {
     @Test
     public void testFieldAnnotation() {
         TestField t = new TestField();
         t.setVersion("1");

         String verison = AnnotationHelper.getVersionValue(t, null);
         assertNotNull(verison);
         assertEquals("1", verison);
     }
   
     @Test
     public void testMethodAnnotation() {
         TestMethod t = new TestMethod();
         t.setVersion("1");

         String verison = AnnotationHelper.getVersionValue(t, null);
         assertNotNull(verison);
         assertEquals("1", verison);
     }
   
     @Test
     public void testStandardField1() {
         TestStandardField1 t = new TestStandardField1();
         t.setVersion("1");

         String verison = AnnotationHelper.getVersionValue(t, null);
         assertNotNull(verison);
         assertEquals("1", verison);
     }
   
     @Test
     public void testStandardField2() {
         TestStandardField2 t = new TestStandardField2();
         t.setVersion("1");

         String verison = AnnotationHelper.getVersionValue(t, null);
         assertNotNull(verison);
         assertEquals("1", verison);
     }
   
     @Test
     public void testNamedField() {
         TestNamedField t = new TestNamedField();
         t.setVersion("1");

         String verison = AnnotationHelper.getVersionValue(t, "otherVersion");
         assertNotNull(verison);
         assertEquals("1", verison);
     }
   
     @Test
     public void testLongField() {
         TestLongField t = new TestLongField();
         t.setVersion(10000L);

         String verison = AnnotationHelper.getVersionValue(t, "otherVersion");
         assertNotNull(verison);
         assertEquals("10000", verison);
     }
   
     @Test
     public void testlongField() {
         TestlongField t = new TestlongField();
         t.setVersion(10000l);

         String verison = AnnotationHelper.getVersionValue(t, "otherVersion");
         assertNotNull(verison);
         assertEquals("10000", verison);
     }
   
     @Test
     public void testIntegerField() {
         TestIntegerField t = new TestIntegerField();
         t.setVersion(100);

         String verison = AnnotationHelper.getVersionValue(t, "otherVersion");
         assertNotNull(verison);
         assertEquals("100", verison);
    }
   
     @Test
     public void testintField() {
         TestintField t = new TestintField();
         t.setVersion(100);

         String verison = AnnotationHelper.getVersionValue(t, "otherVersion");
         assertNotNull(verison);
         assertEquals("100", verison);
     }
   
     @Test
     public void testBad() {
         TestBad t = new TestBad();
         t.setVersion("1");

         String verison = AnnotationHelper.getVersionValue(t, null);
         assertNull(verison);
     }
   
     // Test classes     
     private class TestField {
         @Version
         private String thisVersion;
         public String getVersion() { return thisVersion; }
         public void setVersion(String thisVersion) { this.thisVersion = thisVersion; }
     }

     private class TestMethod {
         private String thisVersion;
         @Version
         public String getVersion() { return thisVersion; }
         public void setVersion(String thisVersion) { this.thisVersion = thisVersion; }
     }

     private class TestStandardField1 {
         private String version;
         public String getVersion() { return version; }
         public void setVersion(String version) { this.version = version; }
     }

     private class TestStandardField2 {
         private String _version;
         public String getVersion() { return _version; }
         public void setVersion(String _version) { this._version = _version; }
     }

     private class TestNamedField {
         private String otherVersion;
         public String getVersion() { return otherVersion; }
         public void setVersion(String otherVersion) { this.otherVersion = otherVersion; }
     }
     
     private class TestLongField {
         private Long otherVersion;
         public Long getVersion() { return otherVersion; }
         public void setVersion(Long otherVersion) { this.otherVersion = otherVersion; }
     }
     
     private class TestlongField {
         private long otherVersion;
         public long getVersion() { return otherVersion; }
         public void setVersion(long otherVersion) { this.otherVersion = otherVersion; }
     }
     
     private class TestIntegerField {
         private Integer otherVersion;
         public Integer getVersion() { return otherVersion; }
         public void setVersion(Integer otherVersion) { this.otherVersion = otherVersion; }
     }
     
     private class TestintField {
         private int otherVersion;
         public int getVersion() { return otherVersion; }
         public void setVersion(int otherVersion) { this.otherVersion = otherVersion; }
     }
     
     private class TestBad {
         private String otherVersion;
         public String getVersion() { return otherVersion; }
         public void setVersion(String otherVersion) { this.otherVersion = otherVersion; }
     }
}
