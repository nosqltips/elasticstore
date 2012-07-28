package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.StringType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author cbrown
 */
public class StringTypeUtilTest {
    @Test
    public void Basic() throws IOException, NoSuchFieldException {
        Field f = Basic.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 1);
        assertEquals(map.get("type"), "string");
    }
     
    @Test
    public void Analyzer() throws IOException, NoSuchFieldException {
        Field f = Analyzer.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("analyzer"), "Standard");
    }
     
    @Test
    public void Boost() throws IOException, NoSuchFieldException {
        Field f = Boost.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("boost"), 2.2F);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("include_in_all"), "true");
    }
     
    @Test
    public void Index() throws IOException, NoSuchFieldException {
        Field f = Index.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("index"), "not_analyzed");
    }
     
    @Test
    public void IndexAnalyzer() throws IOException, NoSuchFieldException {
        Field f = IndexAnalyzer.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("index_analyzer"), "something");
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("index_name"), "another");
    }
     
    @Test
    public void NullValue() throws IOException, NoSuchFieldException {
        Field f = NullValue.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("null_value"), "NA");
    }
     
    @Test
    public void OmitNorms() throws IOException, NoSuchFieldException {
        Field f = OmitNorms.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("omit_norms"), "true");
    }
     
    @Test
    public void OmitTerms() throws IOException, NoSuchFieldException {
        Field f = OmitTerms.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("omit_term_freq_and_positions"), "true");
    }
     
    @Test
    public void SearchAnalyzer() throws IOException, NoSuchFieldException {
        Field f = SearchAnalyzer.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("search_analyzer"), "analyzer");
    }
     
    @Test
    public void Store() throws IOException, NoSuchFieldException {
        Field f = Store.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("store"), "yes");
    }
     
    @Test
    public void TermVector() throws IOException, NoSuchFieldException {
        Field f = TermVector.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 2);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("term_vector"), "with_positions");
    }
     
    @Test
    public void Analyzers() throws IOException, NoSuchFieldException {
        Field f = Analyzers.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 4);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("analyzer"), "Standard");
        assertEquals(map.get("index_analyzer"), "anotherspace");
        assertEquals(map.get("search_analyzer"), "whitespace");
        assertEquals(map.get("type"), "string");
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        Map<String, Object> map = StringTypeUtil.generateSchema(anno);
        assertNotNull(map);
        assertEquals(map.size(), 13);
        assertEquals(map.get("type"), "string");
        assertEquals(map.get("index_name"), "another");
        assertEquals(map.get("store"), "yes");
        assertEquals(map.get("index"), "analyzed");
        assertEquals(map.get("term_vector"), "no");
        assertEquals(map.get("boost"), 2.2F);
        assertEquals(map.get("null_value"), "NA");
        assertEquals(map.get("omit_norms"), "true");
        assertEquals(map.get("analyzer"), "Standard");
        assertEquals(map.get("index_analyzer"), "anotherspace");
        assertEquals(map.get("search_analyzer"), "whitespace");
        assertEquals(map.get("omit_term_freq_and_positions"), "true");
        assertEquals(map.get("include_in_all"), "true");
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        Map<String, Object> map = StringTypeUtil.generateSchema(null);
        assertNull(map);
    }
     
    public class Basic {
        @StringType
        public String field;
    }

    public class Analyzer {
        @StringType(analyzer="Standard")
        public String field;
    }

    public class Boost {
        @StringType(boost=2.2F)
        public String field;
    }

    public class IncludeInAll {
        @StringType(include_in_all=Schema.INCLUDE_IN_ALL.TRUE)
        public String field;
    }

    public class Index {
        @StringType(index=Schema.INDEX.NOT_ANALYZED)
        public String field;
    }

    public class IndexAnalyzer {
        @StringType(index_analyzer="something")
        public String field;
    }

    public class IndexName {
        @StringType(index_name="another")
        public String field;
    }

    public class NullValue {
        @StringType(null_value="NA")
        public String field;
    }

    public class OmitNorms {
        @StringType(omit_norms=Schema.OMIT_NORMS.TRUE)
        public String field;
    }

    public class OmitTerms {
        @StringType(omit_term_freq_and_positions=Schema.OMIT_TERM_FREQ.TRUE)
        public String field;
    }

    public class SearchAnalyzer {
        @StringType(search_analyzer="analyzer")
        public String field;
    }

    public class Store {
        @StringType(store=Schema.STORE.YES)
        public String field;
    }

    public class TermVector {
        @StringType(term_vector=Schema.TERM_VECTOR.WITH_POSITIONS)
        public String field;
    }

    public class Analyzers {
        @StringType(analyzer="Standard", search_analyzer="whitespace", index_analyzer="anotherspace")
        public String field;
    }

    public class All {
        @StringType(index=Schema.INDEX.ANALYZED, analyzer="Standard", search_analyzer="whitespace", index_analyzer="anotherspace", 
                boost=2.2F, include_in_all=Schema.INCLUDE_IN_ALL.TRUE, index_name="another", null_value="NA", omit_norms=Schema.OMIT_NORMS.TRUE, 
                omit_term_freq_and_positions=Schema.OMIT_TERM_FREQ.TRUE, store=Schema.STORE.YES, term_vector=Schema.TERM_VECTOR.NO)
        public String field;
    }
}
