package io.fliqa.interledger;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * References to test wallet setup on
 * https://wallet.interledger-test.dev/account/29c7f1fe-22a2-4d90-9ba7-ec99aad7f318
 * <p>
 * Log in with: andrej@fliqa.io
 */
public final class TestHelper {

    private TestHelper() {
        // hide constructor
    }

    public static String CLIENT_WALLET_ADDRESS = "https://ilp.interledger-test.dev/andrejfliqatestwallet";

    // this is a test private key so it can be included in source control / otherwise this is not good practice
    private static String CLIENT_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MC4CAQAwBQYDK2VwBCIEIMoPnUbc3RNGyN1tiB9gEVcjrq4kCi1CAnFwqhl9GsZ+
            -----END PRIVATE KEY-----""";

    public static String CLIENT_KEY_ID = "89675b1d-53f3-4fb6-b8ea-33a56e576cef";

    public static String SENDER_WALLET_ADDRESS = "https://ilp.interledger-test.dev/sender";
    public static String RECEIVER_WALLET_ADDRESS = "https://ilp.interledger-test.dev/receiver";

    public static PrivateKey getPrivateKey() throws Exception {
        // Remove PEM headers and decode Base64
        String privateKeyBase64 = CLIENT_PRIVATE_KEY
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }
}
