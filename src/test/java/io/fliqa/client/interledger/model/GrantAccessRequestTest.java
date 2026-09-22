/*
 * Copyright 2025 Fliqa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fliqa.client.interledger.model;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class GrantAccessRequestTest {

    private static final String CLIENT_NONCE = "client-nonce-1";
    private static final String AS_NONCE = "as-nonce-1";
    private static final String INTERACT_REF = "interact-ref-1";
    private static final URI GRANT_ENDPOINT = URI.create("https://auth.interledger-test.dev/");

    // computed independently via: BASE64URL(SHA-256(clientNonce + "\n" + asNonce + "\n" + interactRef + "\n" + grantEndpoint))
    private static final String EXPECTED_HASH = "CWTDjs_kP6YlL7ztVgShXIQQWvGhb36CWDTL6J6KJIw";

    @Test
    public void testVerifyInteractionHash_valid() {
        boolean result = GrantAccessRequest.verifyInteractionHash(CLIENT_NONCE, AS_NONCE, INTERACT_REF, EXPECTED_HASH, GRANT_ENDPOINT);
        assertTrue(result);
    }

    @Test
    public void testVerifyInteractionHash_standardBase64WithPadding() {
        // some authorization servers send the hash as standard base64 (+, /, = padding) instead of
        // the base64url-without-padding the GNAP spec calls for - must still verify correctly.
        String standardBase64Hash = EXPECTED_HASH.replace('-', '+').replace('_', '/') + "=";
        boolean result = GrantAccessRequest.verifyInteractionHash(CLIENT_NONCE, AS_NONCE, INTERACT_REF, standardBase64Hash, GRANT_ENDPOINT);
        assertTrue(result);
    }

    @Test
    public void testVerifyInteractionHash_malformedBase64() {
        boolean result = GrantAccessRequest.verifyInteractionHash(CLIENT_NONCE, AS_NONCE, INTERACT_REF, "not valid base64!!", GRANT_ENDPOINT);
        assertFalse(result);
    }

    @Test
    public void testVerifyInteractionHash_tamperedHash() {
        boolean result = GrantAccessRequest.verifyInteractionHash(CLIENT_NONCE, AS_NONCE, INTERACT_REF, "not-the-right-hash", GRANT_ENDPOINT);
        assertFalse(result);
    }

    @Test
    public void testVerifyInteractionHash_tamperedInteractRef() {
        boolean result = GrantAccessRequest.verifyInteractionHash(CLIENT_NONCE, AS_NONCE, "different-interact-ref", EXPECTED_HASH, GRANT_ENDPOINT);
        assertFalse(result);
    }

    @Test
    public void testVerifyInteractionHash_wrongGrantEndpoint() {
        boolean result = GrantAccessRequest.verifyInteractionHash(CLIENT_NONCE, AS_NONCE, INTERACT_REF, EXPECTED_HASH,
                URI.create("https://auth.interledger-test.dev/other"));
        assertFalse(result);
    }

    @Test
    public void testVerifyInteractionHash_requiresAllArguments() {
        assertThrows(IllegalArgumentException.class, () ->
                GrantAccessRequest.verifyInteractionHash(null, AS_NONCE, INTERACT_REF, EXPECTED_HASH, GRANT_ENDPOINT));
        assertThrows(IllegalArgumentException.class, () ->
                GrantAccessRequest.verifyInteractionHash(CLIENT_NONCE, AS_NONCE, INTERACT_REF, EXPECTED_HASH, null));
    }
}
