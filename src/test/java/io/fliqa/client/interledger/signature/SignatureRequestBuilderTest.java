package io.fliqa.client.interledger.signature;

import io.fliqa.client.interledger.model.AccessAction;
import io.fliqa.client.interledger.model.AccessItemType;
import io.fliqa.client.interledger.model.GrantAccessRequest;
import io.fliqa.client.interledger.model.WalletAddress;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Set;

import static io.fliqa.client.interledger.signature.SignatureRequestBuilder.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SignatureRequestBuilder cryptographic signing functionality.
 * <p>
 * These tests use hardcoded test-specific keys and certificates that are independent
 * of the main test configuration. This ensures the tests are self-contained and
 * reproducible regardless of the test-config.properties setup.
 * <p>
 * NOTE: The private key and certificates used here are for testing purposes only
 * and should never be used in production environments.
 */
class SignatureRequestBuilderTest {

    // Test-specific Ed25519 private key (for testing only - not for production)
    private static final String TEST_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MC4CAQAwBQYDK2VwBCIEIEaqXUhYHbfgxCjARYYTTo8azSkMCJYOKVU77qdkPqva
            -----END PRIVATE KEY-----
            """;

    // Test-specific key ID (UUID format)
    private static final String TEST_KEY_ID = "test-key-761fbd9c-16a6-4e19-a4cf-0f4076d78469";

    // Test-specific wallet address
    private static final String TEST_WALLET_ADDRESS = "https://test.interledger.example/test-wallet";

    /**
     * Creates a test private key from the hardcoded PEM string
     *
     * @return Ed25519 private key for testing
     * @throws Exception if key parsing fails
     */
    private static PrivateKey getTestPrivateKey() throws Exception {
        // Remove PEM headers and decode Base64
        String privateKeyBase64 = TEST_PRIVATE_KEY
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }

    @Test
    public void generateSignature() throws Exception {

        long createdTime = 1741002284L;
        String testJsonPayload = "{\"access_token\":{\"access\":[{\"type\":\"incoming-payment\",\"actions\":[\"read\",\"complete\",\"create\"]}]}," +
                "\"client\":\"" + TEST_WALLET_ADDRESS + "\"}";

        SignatureRequestBuilder builder = new SignatureRequestBuilder(getTestPrivateKey(), TEST_KEY_ID)
                .method("POST")
                .target(URI.create("https://auth.test.interledger.example"))
                .json(testJsonPayload)
                .build(createdTime);

        // check signature base structure (content will be different due to test values)
        String base = builder.getSignatureBase();
        String expectedSignatureParams = "(\"@method\" \"@target-uri\" \"content-digest\" \"content-length\" \"content-type\");" +
                "keyid=\"" + TEST_KEY_ID + "\";created=" + createdTime;

        // Verify the signature base contains the expected components
        assertTrue(base.contains("\"@method\": POST"));
        assertTrue(base.contains("\"@target-uri\": https://auth.test.interledger.example/"));
        assertTrue(base.contains("\"content-digest\": sha-512=:"));
        assertTrue(base.contains("\"content-length\": " + testJsonPayload.length()));
        assertTrue(base.contains("\"content-type\": application/json"));
        assertTrue(base.contains("\"@signature-params\": " + expectedSignatureParams));

        // Verify signature is generated (actual value depends on test key)
        String signature = builder.getSignature();
        assertNotNull(signature);
        assertFalse(signature.isEmpty());

        // Verify headers are properly set
        LinkedHashMap<String, String> headers = builder.getHeaders();
        assertTrue(headers.get(SIGNATURE_HEADER).startsWith("sig1=:"));
        assertTrue(headers.get(SIGNATURE_HEADER).endsWith(":"));
        assertTrue(headers.get(CONTENT_DIGEST_HEADER).startsWith("sha-512=:"));
        assertTrue(headers.get(CONTENT_DIGEST_HEADER).endsWith(":"));
        assertEquals("application/json", headers.get(CONTENT_TYPE_HEADER));
        assertEquals("application/json", headers.get(ACCEPT_HEADER));
    }

    @Test
    public void generateSignatureWithGrantRequest() throws Exception {

        GrantAccessRequest grantRequest = GrantAccessRequest.build(new WalletAddress(TEST_WALLET_ADDRESS),
                AccessItemType.incomingPayment,
                Set.of(AccessAction.read, AccessAction.complete, AccessAction.create));

        SignatureRequestBuilder builder = new SignatureRequestBuilder(getTestPrivateKey(), TEST_KEY_ID)
                .method("POST")
                .target(URI.create("https://auth.test.interledger.example"))
                .json(grantRequest)
                .build(1741002284L);

        // Verify signature generation works with GrantAccessRequest objects
        String signature = builder.getSignature();
        assertNotNull(signature);
        assertFalse(signature.isEmpty());

        // Verify the signature is Base64 encoded (should contain only valid Base64 characters)
        assertTrue(signature.matches("^[A-Za-z0-9+/]*={0,2}$"));

        // Verify headers are properly set
        LinkedHashMap<String, String> headers = builder.getHeaders();
        assertTrue(headers.get(SIGNATURE_HEADER).contains(signature));
        assertEquals("application/json", headers.get(CONTENT_TYPE_HEADER));
        assertEquals("application/json", headers.get(ACCEPT_HEADER));
    }
}