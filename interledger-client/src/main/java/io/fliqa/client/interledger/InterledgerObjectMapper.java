package io.fliqa.client.interledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import io.fliqa.client.interledger.exception.InterledgerClientException;

import java.time.Instant;

/**
 * Wrapper around ObjectMapper to provide default mapping and catch serialization/deserialization exceptions
 */
public class InterledgerObjectMapper {

    private final ObjectMapper mapper;

    public InterledgerObjectMapper() {
        mapper = get();
    }

    public static ObjectMapper get() {
        ObjectMapper mapper = new ObjectMapper();

        // make sure map fields are always in the same order
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

        // register serializer/deserializer for java.time.Instant
        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, InstantSerializer.INSTANCE);
        module.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        mapper.registerModule(module);

        // don't fail on unknown fields
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    public String writeValueAsString(Object value) throws InterledgerClientException {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new InterledgerClientException(e.getMessage(), e);
        }
    }

    public <T> T readValue(String content, Class<T> valueType) throws InterledgerClientException {
        try {
            return mapper.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            throw new InterledgerClientException(e.getMessage(), e);
        }
    }
}
