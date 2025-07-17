package io.fliqa.client;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Helper class for test configuration and utilities
 * <p>
 * This class loads configuration from test-config.properties file
 * to avoid hardcoding sensitive information in source code.
 * <p>
 * See test-config.properties.template for configuration instructions.
 */
public final class TestHelper {

    private TestHelper() {
        // hide constructor
    }

    /**
     * Gets the client wallet address from configuration
     *
     * @return the wallet address for the payment initiator
     */
    public static String getClientWalletAddress() {
        return TestConfiguration.getClientWalletAddress();
    }

    /**
     * Gets the client key ID from configuration
     *
     * @return the key ID for the private key
     */
    public static String getClientKeyId() {
        return TestConfiguration.getClientKeyId();
    }

    /**
     * Gets the sender wallet address from configuration
     *
     * @return the sender wallet address for tests
     */
    public static String getSenderWalletAddress() {
        return TestConfiguration.getSenderWalletAddress();
    }

    /**
     * Gets the receiver wallet address from configuration
     *
     * @return the receiver wallet address for tests
     */
    public static String getReceiverWalletAddress() {
        return TestConfiguration.getReceiverWalletAddress();
    }

    /**
     * Loads and parses the private key from configuration
     *
     * @return the parsed private key for request signing
     * @throws Exception if the private key cannot be loaded or parsed
     */
    public static PrivateKey getPrivateKey() throws Exception {
        String privateKeyPem = TestConfiguration.getClientPrivateKey();

        // Remove PEM headers and decode Base64
        String privateKeyBase64 = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }
}
