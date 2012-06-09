package com.nosqlrevolution.service;

import com.nosqlrevolution.annotation.DocumentId;
import com.nosqlrevolution.annotation.index.Index;
import com.nosqlrevolution.util.AnnotationHelper;
import com.nosqlrevolution.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class ReflectionUtilTest {
    // TODO: probably need to test polymorphism, overriding, extending, implementing here
     @Test
     public void testAll() throws NoSuchMethodException {
         TestClass t = new TestClass();
         t.setId("1234");
         
         Field[] fields = TestClass.class.getDeclaredFields();
         Field foundField = null;
         // Check to see if there is an annotation on a field
         for (Field f: fields) {
             if (f.isAnnotationPresent(DocumentId.class)) {
                 foundField = f;
             }
         }

         // Test if field is found correctly
         assertNotNull(foundField);
                  
         String s = ReflectionUtil.getFieldValue(foundField, t);
         assertNotNull(s);
         assertEquals(s, "1234");
         
         // Test setting a field value
         ReflectionUtil.setFieldValue(foundField, t, "5678");
         String s2 = ReflectionUtil.getFieldValue(foundField, t);
         assertNotNull(s2);
         assertEquals(s2, "5678");

         Method m = TestClass.class.getMethod("getId", null);
         assertNotNull(m);
         
         String s3 = ReflectionUtil.getMethodValue(m, t);
         assertEquals(s3, "5678");
     }
   
     @Test
     public void testClassAnnotation() throws NoSuchMethodException {
         TestClass t = new TestClass();

         Index index = ReflectionUtil.getAnnotation(t.getClass(), Index.class);
                 
         assertNotNull(index);
         assertEquals("something", index.value());
     }
     
     // Test classes
     @Index("something")
     private class TestClass {
         @DocumentId
         private String id;
         public String getId() { return id; }
         public void setId(String id) { this.id = id; }
     }
}
