package io.fliqa.interledger;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * References to test wallet setup on
 * https://wallet.interledger-test.dev/account/29c7f1fe-22a2-4d90-9ba7-ec99aad7f318
 */
public final class TestHelper {

    private TestHelper() {
        // hide constructor
    }

    public static String CLIENT_WALLET_ADDRESS = "https://ilp.interledger-test.dev/fliqa-initiator";

    // this is a test private key so it can be included in source control / otherwise this is not good practice
    private static String CLIENT_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MC4CAQAwBQYDK2VwBCIEIEfIalZwF5+mHSSxN9+v9h601DjAIa4CvBVD4Pb3I/AV
            -----END PRIVATE KEY-----
            """;

    public static String CLIENT_KEY_ID = "761fbd9c-16a6-4e19-a4cf-0f4076d78469";

    public static String SENDER_WALLET_ADDRESS = "https://ilp.interledger-test.dev/fliqa-sender";
    public static String RECEIVER_WALLET_ADDRESS = "https://ilp.interledger-test.dev/fliqa-receiver";

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
