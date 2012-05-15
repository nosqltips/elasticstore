package com.nosqlrevolution.service;

import com.nosqlrevolution.annotation.Id;
import com.nosqlrevolution.util.ReflectionUtil;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class ReflectionUtilTest {
     @Test
     public void withId() {
         TestId t = new TestId();
         t.setId("1234");
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }

     @Test
     public void with_Id() {
         Test_Id t = new Test_Id();
         t.setId("1234");
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }

     @Test
     public void withAnnotation() {
         TestIdAnnotation t = new TestIdAnnotation();
         t.setId("1234");
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }

     @Test
     public void withLongId() {
         TestLongId t = new TestLongId();
         t.setId(1234L);
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }

     @Test
     public void withlongId() {
         TestlongId t = new TestlongId();
         t.setId(1234L);
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }

     @Test
     public void withIntegerId() {
         TestIntegerId t = new TestIntegerId();
         t.setId(1234);
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }

     @Test
     public void withintId() {
         TestintId t = new TestintId();
         t.setId(1234);
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }
     
     @Test
     public void withPublicId() {
         TestPublicId t = new TestPublicId();
         t.setId("1234");
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }

     @Test
     public void withDefaultId() {
         TestDefaultId t = new TestDefaultId();
         t.setId("1234");
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }

     @Test
     public void withUpperCaseId() {
         TestUpperCaseId t = new TestUpperCaseId();
         t.setId("1234");
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }

     @Test
     public void withMixedCaseId() {
         TestMixedCaseId t = new TestMixedCaseId();
         t.setId("1234");
         assertEquals("1234", ReflectionUtil.getId(t));                 
     }
     
     // Test classes     
     private class TestId {
         private String id;
         public void setId(String id) { this.id = id; }
     }
     
     private class Test_Id {
         private String _id;
         public void setId(String _id) { this._id = _id; }
     }
     
     private class TestIdAnnotation {
         @Id
         private String someid;
         public void setId(String someid) { this.someid = someid; }
     }
     
     private class TestLongId {
         private Long id;
         public void setId(Long id) { this.id = id; }
     }
     
     private class TestlongId {
         private long id;
         public void setId(long id) { this.id = id; }
     }
     
     private class TestIntegerId {
         private Integer id;
         public void setId(Integer id) { this.id = id; }
     }
     
     private class TestintId {
         private int id;
         public void setId(int id) { this.id = id; }
     }

     private class TestPublicId {
         public String id;
         public void setId(String id) { this.id = id; }
     }
     
     private class TestDefaultId {
         String id;
         public void setId(String id) { this.id = id; }
     }
     
     private class TestUpperCaseId {
         String ID;
         public void setId(String ID) { this.ID = ID; }
     }
     
     private class TestMixedCaseId {
         String _Id;
         public void setId(String _Id) { this._Id = _Id; }
     }
}
