package com.nosqlrevolution.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Objects;

/** Shared JSON conversion; malformed documents propagate instead of becoming null results. */
public class MappingUtil<T> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonpMapper JSON_MAPPER = new JacksonJsonpMapper(MAPPER);

    public T get(T target, String json) {
        Objects.requireNonNull(target, "Target");
        return (T) get(json, target.getClass());
    }
    public String get(T document) {
        Objects.requireNonNull(document, "Document");
        if (document instanceof JsonData) return ((JsonData) document).toJson().toString();
        try { return MAPPER.writeValueAsString(document); }
        catch (IOException ex) { throw new UncheckedIOException("Cannot serialize document", ex); }
    }
    public <E> E get(String json, Class<E> type) {
        if (json == null) return null;
        Objects.requireNonNull(type, "Document type");
        if (type == String.class) return type.cast(json);
        if (type == JsonData.class) {
            try (jakarta.json.stream.JsonParser parser = JSON_MAPPER.jsonProvider().createParser(new StringReader(json))) {
                return type.cast(JsonData.from(parser, JSON_MAPPER));
            }
        }
        try { return MAPPER.readValue(json, type); }
        catch (IOException ex) { throw new UncheckedIOException("Cannot deserialize document", ex); }
    }
}
