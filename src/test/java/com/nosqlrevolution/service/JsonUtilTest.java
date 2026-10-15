package com.nosqlrevolution.service;

import com.nosqlrevolution.util.JsonUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 *
 * @author cbrown
 */
public class JsonUtilTest {
     @Test
     public void withid() {
         assertEquals("1234", JsonUtil.getId(id, null));                 
     }
     
     @Test
     public void with_id() {
         assertEquals("1234", JsonUtil.getId(_id, null));                 
     }
     
     @Test
     public void withID() {
         assertEquals("1234", JsonUtil.getId(ID, null));                 
     }
     
     @Test
     public void withId() {
         assertEquals("1234", JsonUtil.getId(Id, null));                 
     }
     
     @Test
     public void withiD() {
         assertEquals("1234", JsonUtil.getId(iD, null));                 
     }
     
     @Test
     public void with_ID() {
         assertEquals("1234", JsonUtil.getId(_ID, null));                 
     }
     
     @Test
     public void with_Id() {
         assertEquals("1234", JsonUtil.getId(_Id, null));                 
     }
     
     @Test
     public void with_iD() {
         assertEquals("1234", JsonUtil.getId(_iD, null));                 
     }
     
     @Test
     public void withSomeId() {
         assertEquals("1234", JsonUtil.getId(some, "some"));                 
     }
     
     @Test
     public void withSomeIdNull() {
         assertNull(JsonUtil.getId(some, null));                 
     }
     
     @Test public void identityUsesOnlyTopLevelFieldsAndAcceptsIntegerIds() {
         assertNull(JsonUtil.getId("{\"child\":{\"id\":\"child-id\"}}", null));
         assertNull(JsonUtil.getId("{\"id\":null}", null));
         assertNull(JsonUtil.getId("{\"child\":{\"custom\":\"child-id\"}}", "custom"));
         assertEquals("42", JsonUtil.getId("{\"id\":42}", null));
     }
     @Test public void invalidIdentitiesAndTrailingDocumentsAreRejected() {
         String[] invalid = { "{\"id\":{}}", "{\"id\":[]}", "{\"id\":true}", "{\"id\":\" \"}", "{} {}", "[]", "null" };
         for (String json : invalid) {
             try { JsonUtil.getId(json, null); org.junit.Assert.fail(json); }
             catch (IllegalArgumentException | java.io.UncheckedIOException expected) { }
         }
     }
     @Test public void removingMetadataRetainsChildIdsAndNumericPrecision() {
         String source = "{\"_id\":\"metadata\",\"child\":{\"id\":\"child-id\"},\"decimal\":0.12345678901234567890123456789}";
         String cleaned = JsonUtil.removeIdFromSource(source, null);
         org.junit.Assert.assertFalse(cleaned.contains("\"_id\""));
         org.junit.Assert.assertTrue(cleaned.contains("child-id"));
         org.junit.Assert.assertTrue(cleaned.contains("0.12345678901234567890123456789"));
     }

     String id = "{\"id\": \"1234\"}";
     String _id = "{\"_id\": \"1234\"}";
     String ID = "{\"ID\": \"1234\"}";
     String Id = "{\"Id\": \"1234\"}";
     String iD = "{\"iD\": \"1234\"}";
     String _ID = "{\"_ID\": \"1234\"}";
     String _Id = "{\"_Id\": \"1234\"}";
     String _iD = "{\"_iD\": \"1234\"}";
     String some = "{\"some\": \"1234\"}";
}
