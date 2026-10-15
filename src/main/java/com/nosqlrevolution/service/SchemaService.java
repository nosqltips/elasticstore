package com.nosqlrevolution.service;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.nosqlrevolution.annotation.schema.*;
import com.nosqlrevolution.enums.Schema.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/** Generates typeless Elasticsearch 8 mappings from the properties Jackson serializes. */
public final class SchemaService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private SchemaService() { }

    public static String generateSchema(Class<?> type) throws IOException {
        Objects.requireNonNull(type, "Document type");
        Map<String, Object> root = new LinkedHashMap<>();
        RootObject options = type.getAnnotation(RootObject.class);
        if (options != null) {
            removed(options);
            dynamic(root, options.dynamic());
            if (options.enabled() != ENABLED.DEFAULT) root.put("enabled", options.enabled() == ENABLED.TRUE);
        }
        if (!Boolean.FALSE.equals(root.get("enabled"))) root.put("properties", properties(MAPPER.constructType(type), new HashSet<>(), type.getSimpleName()));
        return MAPPER.writeValueAsString(root);
    }

    /** @deprecated Mapping types were removed. The old type argument no longer wraps the mapping. */
    @Deprecated
    public static String generateSchema(Class<?> type, String ignoredType) throws IOException {
        return generateSchema(type);
    }

    /** Accept a mapping body, never an index-creation body or obsolete mapping-type wrapper. */
    public static String validateMapping(String json) {
        try {
            JsonNode root = MAPPER.reader().with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(json);
            if (root == null || !root.isObject()) throw new IllegalArgumentException("Mapping must be a JSON object");
            Set<String> allowed = new HashSet<>(Arrays.asList("properties", "dynamic", "enabled", "_source", "_routing", "_meta",
                    "date_detection", "numeric_detection", "dynamic_templates", "dynamic_date_formats", "_field_names", "runtime"));
            Iterator<String> keys = root.fieldNames();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!allowed.contains(key)) throw new IllegalArgumentException("Unsupported mapping root '" + key
                        + "'; use a typeless mapping body without a type or mappings wrapper");
            }
            return root.toString();
        } catch (IOException ex) { throw new java.io.UncheckedIOException("Cannot parse mapping JSON", ex); }
    }

    private static Map<String, Object> properties(JavaType type, Set<Class<?>> ancestors, String path) {
        if (!ancestors.add(type.getRawClass())) throw invalid(path, "Recursive object graph; disable parsing on that property with @ObjectType(enabled=FALSE)");
        try {
            Map<String, Object> properties = new LinkedHashMap<>();
            List<BeanPropertyDefinition> definitions = MAPPER.getSerializationConfig().introspect(type).findProperties();
            definitions.sort(Comparator.comparing(BeanPropertyDefinition::getName));
            for (BeanPropertyDefinition property : definitions) {
                if (!property.couldSerialize() || property.getAccessor() == null) continue;
                String fieldPath = path + "." + property.getName();
                try {
                    properties.put(property.getName(), field(property, type.getRawClass().isAnnotationPresent(IgnoreType.class), ancestors, fieldPath));
                } catch (IllegalArgumentException ex) {
                    throw invalid(fieldPath, ex.getMessage());
                }
            }
            return properties;
        } finally { ancestors.remove(type.getRawClass()); }
    }

    private static <A extends Annotation> A annotation(BeanPropertyDefinition property, Class<A> type) {
        A value = property.getField() == null ? null : property.getField().getAnnotation(type);
        return value != null ? value : property.getAccessor().getAnnotation(type);
    }

    private static Map<String, Object> field(BeanPropertyDefinition property, boolean ignoreUnannotated,
            Set<Class<?>> ancestors, String path) {
        Class<?>[] choices = { StringType.class, NumberType.class, BooleanType.class, DateType.class,
            ObjectType.class, NestedType.class, BinaryType.class, IPType.class, GeoPointType.class, AttachementType.class };
        Annotation selected = null;
        for (Class<?> choice : choices) {
            Annotation candidate = annotation(property, (Class<? extends Annotation>) choice);
            if (candidate != null) {
                if (selected != null) throw new IllegalArgumentException("Conflicting mapping type annotations");
                selected = candidate;
            }
        }
        JavaType javaType = property.getPrimaryType();
        boolean ignored = annotation(property, IgnoreType.class) != null
                || ignoreUnannotated && selected == null && annotation(property, DefaultType.class) == null;
        Map<String, Object> mapping = new LinkedHashMap<>();
        if (selected != null) removed(selected);
        JavaType element = javaType;
        while (element.isArrayType() && element.getRawClass() != byte[].class || element.isCollectionLikeType()) {
            element = element.getContentType();
            if ((element == null || element.hasRawClass(Object.class))
                    && !(selected instanceof ObjectType && ((ObjectType) selected).enabled() == ENABLED.FALSE)) {
                throw new IllegalArgumentException("Collection element type must be specified");
            }
            if (element == null) element = MAPPER.constructType(Object.class);
        }
        String inferred = selected instanceof DateType ? "date"
                : selected instanceof NumberType ? ((NumberType) selected).type().name().toLowerCase(Locale.ROOT) : infer(element);
        String kind = inferred;
        if (kind.equals("text") && selected == null
                && annotation(property, com.nosqlrevolution.annotation.DocumentId.class) != null) kind = "keyword";
        if (selected instanceof StringType) {
            StringType string = (StringType) selected;
            kind = string.type() == STRING_TYPE.KEYWORD || string.index() == INDEX.NOT_ANALYZED ? "keyword" : "text";
            if (string.type() == STRING_TYPE.KEYWORD && string.index() == INDEX.ANALYZED) throw new IllegalArgumentException("Keyword fields cannot be analyzed");
        } else if (selected instanceof NumberType) kind = ((NumberType) selected).type().name().toLowerCase(Locale.ROOT);
        else if (selected instanceof BooleanType) kind = "boolean";
        else if (selected instanceof DateType) kind = "date";
        else if (selected instanceof NestedType) kind = "nested";
        else if (selected instanceof ObjectType) kind = "object";
        else if (selected instanceof IPType) kind = "ip";
        else if (selected instanceof GeoPointType) kind = "geo_point";
        else if (selected instanceof BinaryType) kind = "binary";
        else if (selected instanceof AttachementType) throw new IllegalArgumentException("Attachment mappings were removed; use an ingest attachment pipeline");
        mapping.put("type", kind);
        if (ignored) {
            if (kind.equals("object") || kind.equals("nested")) {
                mapping.put("type", "object"); mapping.put("enabled", false);
            } else if (!kind.equals("binary")) {
                mapping.put("index", false);
                if (!kind.equals("text")) mapping.put("doc_values", false);
            }
            return mapping;
        }
        if (selected != null) common(mapping, selected);
        if (selected instanceof StringType) string(mapping, (StringType) selected, kind);
        if (selected instanceof DateType) {
            String format = ((DateType) selected).format();
            mapping.put("format", "dateOptionalTime".equals(format) ? "strict_date_optional_time||epoch_millis" : format);
        }
        if (selected instanceof ObjectType) {
            ObjectType object = (ObjectType) selected;
            dynamic(mapping, object.dynamic());
            if (object.enabled() != ENABLED.DEFAULT) mapping.put("enabled", object.enabled() == ENABLED.TRUE);
        }
        if (kind.equals("object") || kind.equals("nested")) {
            if (!inferred.equals("object") && !Boolean.FALSE.equals(mapping.get("enabled"))) throw new IllegalArgumentException("Object/nested mapping requires an object element type");
            if (!Boolean.FALSE.equals(mapping.get("enabled")) && !element.isMapLikeType() && !element.hasRawClass(Object.class)) {
                mapping.put("properties", properties(element, ancestors, path));
            }
        }
        MultiFieldType multi = annotation(property, MultiFieldType.class);
        if (multi != null) {
            if (!kind.equals("text")) throw new IllegalArgumentException("@MultiFieldType requires a text field");
            if (multi.keyword().trim().isEmpty() || multi.keyword().contains(".") || multi.ignore_above() <= 0) {
                throw new IllegalArgumentException("Keyword subfield needs a nonblank simple name and positive ignore_above");
            }
            Map<String, Object> keyword = new LinkedHashMap<>();
            keyword.put("type", "keyword"); keyword.put("ignore_above", multi.ignore_above());
            mapping.put("fields", Collections.singletonMap(multi.keyword(), keyword));
        }
        return mapping;
    }

    private static String infer(JavaType type) {
        Class<?> raw = type.getRawClass();
        if (raw == byte[].class) return "binary";
        if (raw == String.class || raw == char.class || raw == Character.class) return "text";
        if (raw.isEnum() || raw == UUID.class) return "keyword";
        if (raw == boolean.class || raw == Boolean.class) return "boolean";
        if (raw == byte.class || raw == Byte.class) return "byte";
        if (raw == short.class || raw == Short.class) return "short";
        if (raw == int.class || raw == Integer.class) return "integer";
        if (raw == long.class || raw == Long.class) return "long";
        if (raw == float.class || raw == Float.class) return "float";
        if (raw == double.class || raw == Double.class) return "double";
        if (java.util.Date.class.isAssignableFrom(raw) || java.util.Calendar.class.isAssignableFrom(raw)) return "date";
        if (Number.class.isAssignableFrom(raw)) throw new IllegalArgumentException("Numeric type requires an explicit @NumberType");
        if (raw.getName().startsWith("java.time.")) throw new IllegalArgumentException("Configure date serialization explicitly and use @DateType");
        return "object";
    }

    private static Object value(Annotation annotation, String name) {
        try { return annotation.annotationType().getMethod(name).invoke(annotation); }
        catch (NoSuchMethodException absent) { return null; }
        catch (ReflectiveOperationException ex) { throw new IllegalArgumentException("Cannot read annotation", ex); }
    }

    private static void common(Map<String, Object> map, Annotation annotation) {
        Object store = value(annotation, "store"), index = value(annotation, "index"), nullValue = value(annotation, "null_value");
        if (store != null && store != STORE.DEFAULT) map.put("store", store == STORE.YES);
        if (index != null && index != INDEX.DEFAULT) map.put("index", index != INDEX.NO);
        if (nullValue != null && !nullValue.equals("")) {
            String kind = (String) map.get("type"), text = (String) nullValue;
            if (kind.equals("text")) throw new IllegalArgumentException("Text fields do not support null_value; use keyword");
            if (kind.equals("boolean")) {
                if (!text.equals("true") && !text.equals("false")) throw new IllegalArgumentException("Boolean null_value must be true or false");
                map.put("null_value", Boolean.valueOf(text));
            } else if (Arrays.asList("byte", "short", "integer", "long", "float", "double").contains(kind)) {
                try {
                    if (kind.equals("float") || kind.equals("double")) {
                        double number = Double.parseDouble(text);
                        if (!Double.isFinite(number)) throw new NumberFormatException();
                        map.put("null_value", number);
                    } else map.put("null_value", Long.parseLong(text));
                } catch (NumberFormatException ex) { throw new IllegalArgumentException("Invalid numeric null_value: " + text); }
            } else map.put("null_value", text);
        }
    }

    private static void string(Map<String, Object> map, StringType string, String kind) {
        boolean text = kind.equals("text");
        String analyzer = string.analyzer().isEmpty() ? string.index_analyzer() : string.analyzer();
        if (!string.analyzer().isEmpty() && !string.index_analyzer().isEmpty() && !string.analyzer().equals(string.index_analyzer())) {
            throw new IllegalArgumentException("analyzer and legacy index_analyzer conflict");
        }
        if (!text && (!analyzer.isEmpty() || !string.search_analyzer().isEmpty() || string.term_vector() != TERM_VECTOR.DEFAULT
                || string.omit_term_freq_and_positions() != OMIT_TERM_FREQ.DEFAULT)) throw new IllegalArgumentException("Text analysis options cannot be applied to keyword fields");
        if (!analyzer.isEmpty()) map.put("analyzer", analyzer);
        if (!string.search_analyzer().isEmpty()) map.put("search_analyzer", string.search_analyzer());
        if (string.term_vector() != TERM_VECTOR.DEFAULT) map.put("term_vector", string.term_vector().name().toLowerCase(Locale.ROOT));
        if (string.omit_norms() != OMIT_NORMS.DEFAULT) map.put("norms", string.omit_norms() == OMIT_NORMS.FALSE);
        if (string.omit_term_freq_and_positions() != OMIT_TERM_FREQ.DEFAULT) {
            map.put("index_options", string.omit_term_freq_and_positions() == OMIT_TERM_FREQ.TRUE ? "docs" : "positions");
        }
    }

    private static void dynamic(Map<String, Object> mapping, DYNAMIC dynamic) {
        if (dynamic != DYNAMIC.DEFAULT) mapping.put("dynamic", dynamic == DYNAMIC.STRICT ? "strict" : dynamic == DYNAMIC.TRUE);
    }

    private static void removed(Annotation annotation) {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("index_name", ""); defaults.put("boost", 1.0f); defaults.put("precision_step", 4);
        defaults.put("include_in_all", INCLUDE_IN_ALL.DEFAULT); defaults.put("path", "");
        defaults.put("lat_lon", LATLON.DEFAULT); defaults.put("geohash", GEOHASH.DEFAULT); defaults.put("geohash_precision", 12);
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            Object actual = value(annotation, entry.getKey());
            if (actual != null && !entry.getValue().equals(actual)) throw new IllegalArgumentException(
                    annotation.annotationType().getSimpleName() + "." + entry.getKey() + " was removed from Elasticsearch; migrate this option before generating a mapping");
        }
    }
    private static IllegalArgumentException invalid(String path, String reason) { return new IllegalArgumentException(path + ": " + reason); }
}
