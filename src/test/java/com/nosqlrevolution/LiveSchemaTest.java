package com.nosqlrevolution;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import com.nosqlrevolution.service.SchemaService;
import com.nosqlrevolution.service.SchemaServiceTest;
import com.nosqlrevolution.annotation.schema.StringType;
import com.nosqlrevolution.enums.Schema.STRING_TYPE;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import static com.nosqlrevolution.query.Condition.*;
import static com.nosqlrevolution.query.ElasticStoreQuery.select;

/** Opt-in ES8 mapping conformance and write/query validation. */
public class LiveSchemaTest {
    private ElasticStore store;
    private String name;
    @Before public void connect() throws Exception {
        String endpoint = System.getProperty("elasticstore.test.url");
        Assume.assumeNotNull(endpoint);
        store = new ElasticStore().withEndpoints(endpoint).execute();
        name = "elasticstore-schema-" + UUID.randomUUID();
    }
    @After public void close() throws Exception {
        try { if (store != null && store.getClient().indices().exists(r -> r.index(name)).value()) store.getClient().indices().delete(r -> r.index(name)); }
        finally { if (store != null) store.close(); }
    }
    private Map<String, Property> properties() throws Exception {
        return store.getClient().indices().getMapping(r -> r.index(name)).result().get(name).mappings().properties();
    }
    @Test(timeout=30000) public void createsGeneratedMappingAndIndexesNestedGeoAndMultiFieldDocuments() throws Exception {
        Index<SchemaServiceTest.Document> index = store.createIndex(SchemaServiceTest.Document.class, name);
        Map<String, Property> properties = properties();
        assertTrue(properties.get("id").isKeyword());
        assertTrue(properties.get("title").isText());
        assertTrue(properties.get("title").text().fields().get("keyword").isKeyword());
        assertTrue(properties.get("addresses").isNested());
        assertTrue(properties.get("location").isGeoPoint());
        SchemaServiceTest.Document doc = new SchemaServiceTest.Document();
        doc.id = "document"; doc.title = "Quick Brown Fox"; doc.category = "animal"; doc.timestamp = new Date(0);
        doc.active = true; doc.revision = 42; doc.ip = "127.0.0.1"; doc.location = "40,-70";
        doc.binary = new byte[] { 1, 2 }; doc.payload = Collections.singletonMap("hidden", "not-indexed");
        SchemaServiceTest.Address address = new SchemaServiceTest.Address(); address.city = "NY"; address.count = 2;
        doc.addresses = Collections.singletonList(address); doc.scores = Arrays.asList(1, 2);
        index.write(doc); index.refresh();
        assertEquals(1, index.count(select().where(field("title").match("quick"))));
        assertEquals(1, index.count(select().where(field("title.keyword").equal("Quick Brown Fox"))));
        assertEquals(0, index.count(select().where(field("payload.hidden").exists())));
        assertEquals(1, index.count(select().setQuery(co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q.nested(
                n -> n.path("addresses").query(query -> query.term(t -> t.field("addresses.city").value("NY"))))))));
    }
    public static class Extra { @StringType(type=STRING_TYPE.KEYWORD) public String extra; }
    @Test(timeout=30000) public void appliesAdditiveMappingsAndReportsIncompatibleUpdates() throws Exception {
        Index<SchemaServiceTest.Document> index = store.createIndex(SchemaServiceTest.Document.class, name);
        index.applyMapping(Extra.class);
        assertTrue(properties().get("extra").isKeyword());
        store.getIndex(String.class, name).write("{\"id\":\"a\",\"extra\":\"new\"}");
        index.refresh();
        assertEquals(1, index.count(select().where(field("extra").equal("new"))));
        try { index.applyMapping("{\"properties\":{\"title\":{\"type\":\"keyword\"}}}"); fail("Expected incompatible mapping"); }
        catch (RuntimeException expected) { assertTrue(properties().get("title").isText()); }
        try { store.createIndex(name, SchemaService.generateSchema(Extra.class)); fail("Expected existing index error"); }
        catch (RuntimeException expected) { assertTrue(properties().get("extra").isKeyword()); }
    }
    @Test(timeout=30000) public void rejectsWrappedAndUnknownOptionsBeforeCreatingIndex() throws Exception {
        for (String mapping : new String[] { "{\"oldType\":{\"properties\":{}}}",
                "{\"properties\":{\"field\":{\"type\":\"keyword\",\"not_a_mapping_option\":true}}}" }) {
            try { store.createIndex(name, mapping); fail("Expected invalid mapping"); }
            catch (RuntimeException expected) { assertFalse(store.getClient().indices().exists(r -> r.index(name)).value()); }
        }
    }
    @com.nosqlrevolution.annotation.schema.RootObject(enabled=com.nosqlrevolution.enums.Schema.ENABLED.FALSE)
    public static class DisabledRoot { public String value; }
    @Test(timeout=30000) public void rootBooleanFlagsArePreservedAndDisabledSourceIsNotIndexed() throws Exception {
        store.createIndex(DisabledRoot.class, name);
        co.elastic.clients.elasticsearch._types.mapping.TypeMapping mapping =
                store.getClient().indices().getMapping(r -> r.index(name)).result().get(name).mappings();
        assertEquals(Boolean.FALSE, mapping.enabled());
        assertEquals(co.elastic.clients.elasticsearch._types.mapping.DynamicMapping.True, mapping.dynamic());
        Index<String> index = store.getIndex(String.class, name);
        index.write("{\"id\":\"source\",\"value\":\"stored only\"}");
        index.refresh();
        assertEquals(1, index.count());
        assertEquals(0, index.count(select().where(field("value").exists())));
        assertTrue(index.findById("source").contains("stored only"));
    }

    @com.nosqlrevolution.annotation.index.Index("annotation-name")
    public static class Annotated { public String value; }
    @Test public void annotatedIndexNoLongerRequiresIndexType() throws Exception {
        assertEquals("annotation-name", store.getIndex(Annotated.class).getIndex());
    }
}
