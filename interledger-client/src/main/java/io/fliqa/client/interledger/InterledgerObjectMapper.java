package io.fliqa.client.interledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.fliqa.client.interledger.exception.InterledgerClientException;

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

        // Add support for Java 8 date/time types
        // mapper.registerModule(new JavaTimeModule());

        // Global serialization/deserialization settings
        // mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
