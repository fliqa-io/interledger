package io.fliqa.client.interledger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class InterledgerObjectMapper {

    public static ObjectMapper get() {
        ObjectMapper mapper = new ObjectMapper();

        // make sure map fields are always in the same order
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

        // Add support for Java 8 date/time types
        // mapper.registerModule(new JavaTimeModule());

        // Global serialization/deserialization settings
        // mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }
}
