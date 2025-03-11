package io.fliqa.client.interledger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.ApiError;
import io.fliqa.client.interledger.serializer.InstantSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Set;

import static io.fliqa.client.interledger.InterledgerApiClient.INTERNAL_SERVER_ERROR;

/**
 * Wrapper around ObjectMapper to provide default mapping and catch serialization/deserialization exceptions
 * We have two mappers:
 * - mapper       - standard mapper
 * - unwrapMapper - deserializes content of root JSON element
 */
public class InterledgerObjectMapper {

    /**
     * Some errors are not returned as JSON / mitigate this
     */
    private static final Set<String> COMMON_ERRORS = Set.of(
            "could not get wallet address",
            "unauthorized",
            "forbidden");

    private final ObjectMapper mapper;
    private final ObjectMapper unwrapMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(InterledgerObjectMapper.class);

    public InterledgerObjectMapper() {
        mapper = get();
        unwrapMapper = getUnwrapMapper();
    }

    public static ObjectMapper get() {
        ObjectMapper mapper = new ObjectMapper();

        // make sure map fields are always in the same order
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

        // register serializer/deserializer for java.time.Instant
        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, new InstantSerializer());

        module.addDeserializer(Instant.class, InstantDeserializer.INSTANT);
        mapper.registerModule(module);

        // don't fail on unknown fields
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    private static ObjectMapper getUnwrapMapper() {
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

    public ApiError readError(String content, int httpResponseCode) throws InterledgerClientException {

        // This is just a dumb way to mitigate the fact that not all errors are return in JSON format
        if (COMMON_ERRORS.contains(content.toLowerCase())) {
            throw new InterledgerClientException(content, httpResponseCode, null, content);
        }

        try {
            return unwrapMapper.readValue(content, ApiError.class);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Failed to deserialize response: '{}' to: '{}'.", content, ApiError.class.getName());

            throw new InterledgerClientException(content, httpResponseCode, null, content);
        }
    }
}
