package com.nosqlrevolution.util.schema;

import com.nosqlrevolution.annotation.schema.StringType;
import com.nosqlrevolution.enums.Schema;
import java.io.IOException;
import java.lang.reflect.Field;
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
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Analyzer() throws IOException, NoSuchFieldException {
        Field f = Analyzer.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"analyzer\":\"Standard\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Boost() throws IOException, NoSuchFieldException {
        Field f = Boost.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"boost\":2.2}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IncludeInAll() throws IOException, NoSuchFieldException {
        Field f = IncludeInAll.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Index() throws IOException, NoSuchFieldException {
        Field f = Index.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"index\":\"not_analyzed\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IndexAnalyzer() throws IOException, NoSuchFieldException {
        Field f = IndexAnalyzer.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"index_analyzer\":\"something\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void IndexName() throws IOException, NoSuchFieldException {
        Field f = IndexName.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"index_name\":\"another\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullValue() throws IOException, NoSuchFieldException {
        Field f = NullValue.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"null_value\":\"NA\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void OmitNorms() throws IOException, NoSuchFieldException {
        Field f = OmitNorms.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"omit_norms\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void OmitTerms() throws IOException, NoSuchFieldException {
        Field f = OmitTerms.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"omit_term_freq_and_positions\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void SearchAnalyzer() throws IOException, NoSuchFieldException {
        Field f = SearchAnalyzer.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"search_analyzer\":\"analyzer\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Store() throws IOException, NoSuchFieldException {
        Field f = Store.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"store\":\"yes\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void TermVector() throws IOException, NoSuchFieldException {
        Field f = TermVector.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"term_vector\":\"with_positions\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void Analyzers() throws IOException, NoSuchFieldException {
        Field f = Analyzers.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"analyzer\":\"Standard\",\"index_analyzer\":\"anotherspace\",\"search_analyzer\":\"whitespace\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void All() throws IOException, NoSuchFieldException {
        Field f = All.class.getField("field");
        StringType anno = f.getAnnotation(StringType.class);
        String json = StringTypeUtil.generateSchema(anno);
        assertNotNull(json);
        String expected = "{\"type\":\"string\",\"index_name\":\"another\",\"store\":\"yes\",\"index\":\"analyzed\",\"term_vector\":\"no\",\"boost\":2.2,\"null_value\":\"NA\",\"omit_norms\":\"true\",\"omit_term_freq_and_positions\":\"true\",\"analyzer\":\"Standard\",\"index_analyzer\":\"anotherspace\",\"search_analyzer\":\"whitespace\",\"include_in_all\":\"true\"}";
        assertEquals(expected, json);
    }
     
    @Test
    public void NullCase() throws IOException, NoSuchFieldException {
        String json = StringTypeUtil.generateSchema(null);
        assertNull(json);
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
        @StringType(boost=2.2f)
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
                boost=2.2f, include_in_all=Schema.INCLUDE_IN_ALL.TRUE, index_name="another", null_value="NA", omit_norms=Schema.OMIT_NORMS.TRUE, 
                omit_term_freq_and_positions=Schema.OMIT_TERM_FREQ.TRUE, store=Schema.STORE.YES, term_vector=Schema.TERM_VECTOR.NO)
        public String field;
    }
}
