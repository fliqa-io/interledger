package io.fliqa.client.interledger.model;

import io.fliqa.client.interledger.InterledgerObjectMapper;
import io.fliqa.client.interledger.exception.InterledgerClientException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTest {

    private static final InterledgerObjectMapper mapper = new InterledgerObjectMapper();
    private static final String JSON_ERROR_NO_CODE = "{\"error\":{\"description\":\"Received error validating OpenAPI request: body must have required property 'access_token'\"}}";
    private static final String JSON_ERROR_WITH_CODE = "{\"error\":{\"code\":\"invalid_client\",\"description\":\"invalid signature\"}}";

    @Test
    void testDeserialization() throws InterledgerClientException {
        ApiError error = mapper.readError(JSON_ERROR_NO_CODE, 400);

        assertNotNull(error);
        assertEquals("Received error validating OpenAPI request: body must have required property 'access_token'", error.description);
        assertNull(error.code);

        error = mapper.readError(JSON_ERROR_WITH_CODE, 400);
        assertNotNull(error);
        assertEquals("invalid signature", error.description);
        assertEquals("invalid_client", error.code);
    }
}