package io.fliqa.client.interledger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.ApiError;

import java.time.Instant;

import static io.fliqa.client.interledger.InterledgerApiClient.INTERNAL_SERVER_ERROR;

/**
 * Wrapper around ObjectMapper to provide default mapping and catch serialization/deserialization exceptions
 * We have two mappers:
 * - mapper           - standard mapper
 * - unwrapRootMapper - deserializes content of root JSON element
 */
public class InterledgerObjectMapper {

    private final ObjectMapper mapper;
    private final ObjectMapper unwrapRootMapper;

    public InterledgerObjectMapper() {
        mapper = get();
        unwrapRootMapper = getUnwrapRootMapper();
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
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private static ObjectMapper getUnwrapRootMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);  // Enable root value handling
        return mapper;
    }

    public String writeValueAsString(Object value) throws InterledgerClientException {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new InterledgerClientException(String.format("Failed to serialize value: '%s' to JSON.", value),
                    e, INTERNAL_SERVER_ERROR, null, value != null ? value.toString() : null);
        }
    }

    public <T> T readValue(String content, Class<T> valueType) throws InterledgerClientException {
        try {
            return mapper.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            // String message, Throwable throwable, int code, HttpHeaders responseHeaders, String responseBody
            throw new InterledgerClientException(String.format("Failed to deserialize response to: '%s'.", valueType.getName()),
                    e, INTERNAL_SERVER_ERROR, null, content);
        }
    }

    public ApiError readError(String content) throws InterledgerClientException {
        try {
            return unwrapRootMapper.readValue(content, ApiError.class);
        } catch (JsonProcessingException e) {
            throw new InterledgerClientException(String.format("Failed to deserialize response to: '%s'.", ApiError.class.getName()),
                    e, INTERNAL_SERVER_ERROR, null, content);
        }
    }
}
