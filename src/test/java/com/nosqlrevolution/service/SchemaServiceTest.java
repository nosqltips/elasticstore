package com.nosqlrevolution.service;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.nosqlrevolution.annotation.DocumentId;
import com.nosqlrevolution.annotation.schema.*;
import com.nosqlrevolution.enums.Schema.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class SchemaServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();
    public static class Parent { @DocumentId public String id; }
    public static class Address { @StringType(index=INDEX.NOT_ANALYZED) public String city; public int count; }
    @RootObject(dynamic=DYNAMIC.STRICT)
    public static class Document extends Parent {
        @StringType(analyzer="standard", omit_norms=OMIT_NORMS.TRUE) @MultiFieldType public String title;
        @StringType(type=STRING_TYPE.KEYWORD, null_value="missing", store=STORE.YES) public String category;
        @NumberType(type=NUMERIC_TYPE.LONG, null_value="42") public long revision;
        @BooleanType(null_value="false") public boolean active;
        @DateType public Date timestamp;
        @NestedType public List<Address> addresses;
        @ObjectType(enabled=ENABLED.FALSE) public Map<String, Object> payload;
        @IPType public String ip;
        @GeoPointType public String location;
        @BinaryType public byte[] binary;
        public List<Integer> scores;
        @JsonIgnore public String secret;
        @JsonProperty("renamed") public String internalName;
        private String privateName;
        public String getPrivateName() { return privateName; }
    }
    private JsonNode mapping(Class<?> type) throws Exception { return mapper.readTree(SchemaService.generateSchema(type)); }
    @Test public void generatesTypelessSerializableInheritedPropertiesAndModernTypes() throws Exception {
        JsonNode root = mapping(Document.class), fields = root.get("properties");
        assertEquals("strict", root.get("dynamic").asText());
        assertEquals("keyword", fields.path("id").path("type").asText());
        assertEquals("text", fields.path("title").path("type").asText());
        assertFalse(fields.path("title").path("norms").asBoolean(true));
        assertEquals("keyword", fields.path("title").path("fields").path("keyword").path("type").asText());
        assertEquals("keyword", fields.path("category").path("type").asText());
        assertTrue(fields.path("category").path("store").isBoolean());
        assertEquals(42, fields.path("revision").path("null_value").asInt());
        assertTrue(fields.path("active").path("null_value").isBoolean());
        assertEquals("strict_date_optional_time||epoch_millis", fields.path("timestamp").path("format").asText());
        assertEquals("nested", fields.path("addresses").path("type").asText());
        assertEquals("keyword", fields.path("addresses").path("properties").path("city").path("type").asText());
        assertFalse(fields.path("payload").path("enabled").asBoolean(true));
        assertEquals("ip", fields.path("ip").path("type").asText());
        assertEquals("geo_point", fields.path("location").path("type").asText());
        assertEquals("binary", fields.path("binary").path("type").asText());
        assertEquals("integer", fields.path("scores").path("type").asText());
        assertFalse(fields.has("secret"));
        assertFalse(fields.has("internalName"));
        assertTrue(fields.has("renamed"));
        assertTrue(fields.has("privateName"));
        assertEquals(root, mapper.readTree(SchemaService.generateSchema(Document.class, "obsolete-type")));
    }
    @IgnoreType public static class Ignored {
        public String hidden;
        @DefaultType public int included;
        @StringType public String explicit;
    }
    @Test public void ignoredPropertiesRemainInSourceWithoutIndexing() throws Exception {
        JsonNode fields = mapping(Ignored.class).get("properties");
        assertFalse(fields.path("hidden").path("index").asBoolean(true));
        assertFalse(fields.path("included").has("index"));
        assertFalse(fields.path("explicit").has("index"));
    }
    public static class Cycle { public Cycle child; }
    public static class DisabledCycle { @ObjectType(enabled=ENABLED.FALSE) public DisabledCycle child; }
    @Test public void cyclesFailClearlyUnlessParsingDisabled() throws Exception {
        rejects(Cycle.class, "Recursive");
        assertFalse(mapping(DisabledCycle.class).path("properties").path("child").path("enabled").asBoolean(true));
    }
    public static class Removed { @StringType(boost=2) public String field; }
    public static class RemovedGeo { @GeoPointType(lat_lon=LATLON.TRUE) public String field; }
    public static class BadKeyword { @StringType(type=STRING_TYPE.KEYWORD, analyzer="standard") public String field; }
    public static class Conflicting { @StringType @NumberType public String field; }
    public static class RawCollection { public List field; }
    public static class BadNested { @NestedType public List<String> field; }
    @Test public void unsupportedSettingsAndInvalidShapesFailWithPropertyPaths() throws Exception {
        rejects(Removed.class, "boost"); rejects(RemovedGeo.class, "lat_lon"); rejects(BadKeyword.class, "analysis");
        rejects(Conflicting.class, "Conflicting"); rejects(RawCollection.class, "element type"); rejects(BadNested.class, "object element");
    }
    @Test public void rejectsTypeAndIndexWrappersAndTrailingMappingBodies() {
        for (String json : new String[] { "{\"type\":{\"properties\":{}}}", "{\"mappings\":{}}", "[]", "{} {}" }) {
            try { SchemaService.validateMapping(json); fail(json); }
            catch (IllegalArgumentException | java.io.UncheckedIOException expected) { }
        }
    }
    public static class ArraysAndNumbers {
        public List<List<Integer>> matrix;
        @NumberType(type=NUMERIC_TYPE.DOUBLE) public java.math.BigDecimal decimal;
    }
    @Test public void infersNestedCollectionElementsAndSupportsExplicitDecimalMapping() throws Exception {
        JsonNode fields = mapping(ArraysAndNumbers.class).path("properties");
        assertEquals("integer", fields.path("matrix").path("type").asText());
        assertEquals("double", fields.path("decimal").path("type").asText());
    }
    private void rejects(Class<?> type, String message) throws Exception {
        try { mapping(type); fail("Expected invalid mapping"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage(), expected.getMessage().contains(message)); }
    }
}
