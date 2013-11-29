package com.nosqlrevolution.annotation;

import static com.nosqlrevolution.annotation.UUIDProvider.Type.RANDOM_64BIT;
import com.nosqlrevolution.model.TestIdAnnotationFieldStatic;
import com.nosqlrevolution.model.TestIdAnnotationMethodStatic;
import com.nosqlrevolution.util.AnnotationHelper;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class DocumentIdTest {
     @Test
     public void withId() {
         TestId t = new TestId();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void with_Id() {
         Test_Id t = new Test_Id();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withLongId() {
         TestFullLongId t = new TestFullLongId();
         t.setId(1234L);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withlongId() {
         TestlongId t = new TestlongId();
         t.setId(1234L);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withIntegerId() {
         TestIntegerId t = new TestIntegerId();
         t.setId(1234);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withintId() {
         TestintId t = new TestintId();
         t.setId(1234);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withPublicId() {
         TestPublicId t = new TestPublicId();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withDefaultId() {
         TestDefaultId t = new TestDefaultId();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withUpperCaseId() {
         TestUpperCaseId t = new TestUpperCaseId();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withMixedCaseId() {
         TestMixedCaseId t = new TestMixedCaseId();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withSomeId() {
         TestSomeId t = new TestSomeId();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, "some"));
     }

     @Test
     public void withSomeCaseId() {
         TestSomeId t = new TestSomeId();
         t.setId("1234");
         assertNull(AnnotationHelper.getDocumentId(t, "SOME"));
     }

     @Test
     public void withSomeIdNull() {
         TestSomeId t = new TestSomeId();
         t.setId("1234");
         assertNull(AnnotationHelper.getDocumentId(t, null));               
     }

     @Test
     public void withAnnotationFieldString() {
         TestIdAnnotationFieldString t = new TestIdAnnotationFieldString();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationFieldLong() {
         TestIdAnnotationFieldFullLong t = new TestIdAnnotationFieldFullLong();
         t.setId(1234L);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationFieldInteger() {
         TestIdAnnotationFieldInteger t = new TestIdAnnotationFieldInteger();
         t.setId(1234);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationFieldlong() {
         TestIdAnnotationFieldlong t = new TestIdAnnotationFieldlong();
         t.setId(1234L);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationFieldint() {
         TestIdAnnotationFieldint t = new TestIdAnnotationFieldint();
         t.setId(1234);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationFieldObject() {
         TestIdAnnotationFieldObject t = new TestIdAnnotationFieldObject();
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationFieldStatic() {
         TestIdAnnotationFieldStatic t = new TestIdAnnotationFieldStatic();
         TestIdAnnotationFieldStatic.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withAnnotationFieldPublic() {
         TestIdAnnotationFieldPublic t = new TestIdAnnotationFieldPublic();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withAnnotationFieldDefault() {
         TestIdAnnotationFieldDefault t = new TestIdAnnotationFieldDefault();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withAnnotationFieldProtected() {
         TestIdAnnotationFieldProtected t = new TestIdAnnotationFieldProtected();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withUUIDFieldString() {
         TestUUIDFieldString t = new TestUUIDFieldString();
         assertNotNull(AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withUUID64BitFieldString() {
         TestUUID64BitFieldString t = new TestUUID64BitFieldString();
         assertNotNull(AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withUUIDFieldLong() {
         TestUUIDFieldLong t = new TestUUIDFieldLong();
         assertNull(AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withUUIDFieldInteger() {
         TestUUIDFieldInteger t = new TestUUIDFieldInteger();
         assertNull(AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withOverrideField() {
         TestOverrideField t = new TestOverrideField();
         t.setId("1234");
         assertEquals("5678", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withAnnotationMethodString() {
         TestIdAnnotationMethodString t = new TestIdAnnotationMethodString();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationMethodLong() {
         TestIdAnnotationMethodFullLong t = new TestIdAnnotationMethodFullLong();
         t.setId(1234L);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationMethodInteger() {
         TestIdAnnotationMethodInteger t = new TestIdAnnotationMethodInteger();
         t.setId(1234);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationMethodlong() {
         TestIdAnnotationMethodlong t = new TestIdAnnotationMethodlong();
         t.setId(1234L);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationMethodint() {
         TestIdAnnotationMethodint t = new TestIdAnnotationMethodint();
         t.setId(1234);
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationMethodObject() {
         TestIdAnnotationMethodObject t = new TestIdAnnotationMethodObject();
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationMethodVoid() {
         TestIdAnnotationMethodVoid t = new TestIdAnnotationMethodVoid();
         assertNull(AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationMethodStatic() {
         TestIdAnnotationMethodStatic t = new TestIdAnnotationMethodStatic();
         TestIdAnnotationMethodStatic.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationMethodPrivate() {
         TestIdAnnotationMethodPrivate t = new TestIdAnnotationMethodPrivate();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationMethodDefault() {
         TestIdAnnotationMethodDefault t = new TestIdAnnotationMethodDefault();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withAnnotationMethodProtected() {
         TestIdAnnotationMethodProtected t = new TestIdAnnotationMethodProtected();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
     }

     @Test
     public void withUUIDMethodString() {
         TestUUIDMethodString t = new TestUUIDMethodString();
         assertNotNull(AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withUUIDMethodLong() {
         TestUUIDMethodLong t = new TestUUIDMethodLong();
         assertNull(AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withOverrideMethod() {
         TestOverrideMethod t = new TestOverrideMethod();
         t.setId("1234");
         assertEquals("5678", AnnotationHelper.getDocumentId(t, null));
     }
     
     @Test
     public void withImplementsMethod() {
         TestImplementsMethod t = new TestImplementsMethod();
         t.setId("1234");
         assertEquals("1234", AnnotationHelper.getDocumentId(t, null));
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
     
     private class TestFullLongId {
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

     private class TestSomeId {
         String some;
         public void setId(String some) { this.some = some; }
     }

     private class TestIdAnnotationFieldString {
         @DocumentId
         private String someid;
         public void setId(String someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationFieldFullLong {
         @DocumentId
         private Long someid;
         public void setId(Long someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationFieldInteger {
         @DocumentId
         private Integer someid;
         public void setId(Integer someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationFieldlong {
         @DocumentId
         private long someid;
         public void setId(long someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationFieldint {
         @DocumentId
         private int someid;
         public void setId(int someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationFieldObject {
         @DocumentId
         public Object obj = new TestObject();
     }

     private class TestIdAnnotationFieldPublic {
         @DocumentId
         public String someid;
         public void setId(String someid) { this.someid = someid; }
     }
     
     private class TestUUIDFieldString {
         @UUIDProvider()
         @DocumentId
         public String someid;
     }
     
     private class TestUUID64BitFieldString {
         @UUIDProvider(RANDOM_64BIT)
         @DocumentId
         public String someid;
     }
     
     private class TestUUIDFieldLong {
         @UUIDProvider()
         @DocumentId
         public Long someid;
     }
     
     private class TestUUIDFieldInteger {
         @UUIDProvider()
         @DocumentId
         public Integer someid;
     }
     
     private class TestOverrideField extends TestIdAnnotationFieldString {
         @DocumentId
         private String someId="5678";
     }
     
     private class TestIdAnnotationFieldDefault {
         @DocumentId
         String someid;
         public void setId(String someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationFieldProtected {
         @DocumentId
         protected String someid;
         public void setId(String someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationMethodString {
         private String someid;
         @DocumentId
         public String getId() { return someid; }
         public void setId(String someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationMethodFullLong {
         private Long someid;
         @DocumentId
         public Long getId() { return someid; }
         public void setId(Long someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationMethodInteger {
         private Integer someid;
         @DocumentId
         public Integer getId() { return someid; }
         public void setId(Integer someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationMethodlong {
         private long someid;
         @DocumentId
         public long getId() { return someid; }
         public void setId(long someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationMethodint {
         private int someid;
         @DocumentId
         public int getId() { return someid; }
         public void setId(int someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationMethodVoid {
         private String someid;
         public String getId() { return someid; }
         @DocumentId
         public void setId(String someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationMethodPrivate {
         private String someid;
         @DocumentId
         private String getId() { return someid; }
         public void setId(String someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationMethodDefault {
         private String someid;
         @DocumentId
         String getId() { return someid; }
         public void setId(String someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationMethodProtected {
         private String someid;
         @DocumentId
         protected String getId() { return someid; }
         public void setId(String someid) { this.someid = someid; }
     }
     
     private class TestIdAnnotationMethodObject {
         @DocumentId
         public Object getObj() { return new TestObject(); }
     }
    
     private class TestObject extends Object {
        @Override
        public String toString() {
            return "1234";
        }         
     }

     private class TestUUIDMethodString {
         @UUIDProvider()
         private String someid;
         @DocumentId
         public String getId() { return someid; }
     }
          
     private class TestUUIDMethodLong {
         @UUIDProvider()
         private Long someid;
         @DocumentId
         public Long getId() { return someid; }
     }

     private class TestOverrideMethod extends TestIdAnnotationMethodString {
         @DocumentId
         private String someId="5678";
     }
     
     private interface TestInterfaceMethod {
         @DocumentId
         public String getId();
     }
     
     private class TestImplementsMethod implements TestInterfaceMethod {
         @DocumentId
         private String someId;
         public String getId() { return someId; }
         public void setId(String someId) { this.someId = someId; }
     }
}
