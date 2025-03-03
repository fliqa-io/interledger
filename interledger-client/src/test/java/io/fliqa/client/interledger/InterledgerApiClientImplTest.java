package io.fliqa.client.interledger;

import io.fliqa.client.interledger.model.*;
import io.fliqa.interledger.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

import static io.fliqa.client.interledger.InterledgerApiClientImpl.generateSignature;
import static org.junit.jupiter.api.Assertions.*;

class InterledgerApiClientImplTest {

    private static final String BASE_PATH = "https://ilp.interledger-test.dev";
    private InterledgerApiClientImpl client;


    public static PrivateKey loadPrivateKey(String base64Key) throws Exception {
        // Remove PEM headers and decode Base64
        String privateKeyBase64 = base64Key
                                      .replace("-----BEGIN PRIVATE KEY-----", "")
                                      .replace("-----END PRIVATE KEY-----", "")
                                      .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);
    }

    @BeforeEach
    public void setUp() throws Exception {
        PrivateKey privateKey = loadPrivateKey(TestHelper.CLIENT_PRIVATE_KEY);
        WalletAddress clientWallet = new WalletAddress(TestHelper.CLIENT_WALLET_ADDRESS);
        client = new InterledgerApiClientImpl(clientWallet,
                                              privateKey,
                                              TestHelper.CLIENT_KEY_ID);
    }

    // Get receiver wallet data
    @Test
    public void getWallet() throws Exception {

        PaymentPointer wallet = client.getWallet(new WalletAddress(TestHelper.RECEIVER_WALLET_ADDRESS));
        assertNotNull(wallet);

        assertEquals(URI.create("https://ilp.interledger-test.dev/reciever"), wallet.id);
        assertEquals("reciever", wallet.publicName);
        assertEquals("EUR", wallet.assetCode);
        assertEquals(2, wallet.assetScale);
        assertEquals(URI.create("https://auth.interledger-test.dev"), wallet.authServer);
        assertEquals(URI.create("https://ilp.interledger-test.dev"), wallet.resourceServer);
    }

    // Step 1: Get a grant for the incoming payment, so we can create the incoming payment on the receiving wallet address
    @Test
    public void getGrantRequest() throws IOException, InterruptedException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        PaymentPointer wallet = client.getWallet(new WalletAddress(TestHelper.RECEIVER_WALLET_ADDRESS));
        client.createPendingGrant(wallet);
    }


    String requestBody = "{\n" +
                             "    \"access_token\": {\n" +
                             "        \"access\": [ \n" +
                             "            {\n" +
                             "                \"actions\": [\n" +
                             "                    \"read\", \n" +
                             "                    \"complete\", \n" +
                             "                    \"create\"\n" +
                             "                ], \n" +
                             "                \"type\": \"incoming-payment\"\n" +
                             "            }\n" +
                             "        ]\n" +
                             "    }, \n" +
                             "    \"client\": \"https://ilp.interledger-test.dev/andrejfliqatestwallet\"\n" +
                             "}";

    String json = "{    \"access_token\": {        \"access\": [            {                \"actions\": [                    \"read\",                    \"complete\",                    \"create\"                ],                \"type\": \"incoming-payment\"            }        ]    },    \"client\": \"https://ilp.interledger-test.dev/andrejfliqatestwallet\"}";

    @Test
    public void digestContentTest() throws NoSuchAlgorithmException {
        System.out.println("----");
        System.out.println(requestBody);
        System.out.println("----");

        String out = InterledgerApiClientImpl.digestContentSha512(requestBody);
        assertEquals("v2baXKn2bRWwis7fZwF4sB8B7I7izwCA5kybiVdCVb8nhD2kd0qf07hgK+p1Jaa00wQiEmOXKzlS6gurYKdBHA==", out);
    }


    @Test
    public void generateSignatureTest() throws Exception {

        /*

        "content-type": application/json
"content-digest": sha-512=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:
"content-length": 18
"authorization": GNAP 123454321
"@method": POST
"@target-uri": https://example.com/
"@signature-params": ("content-type" "content-digest" "content-length" "authorization" "@method" "@target-uri");alg="ed25519";keyid="eddsa_key_1";created=1704722601

        */

        String signatureInput = new StringBuilder()
                                    .append("\"@method\": POST").append(System.lineSeparator())
                                    .append("\"@target-uri\": ").append("https://auth.interledger-test.dev/").append(System.lineSeparator())
                                    .append("\"content-digest\": ").append("sha-512=:v2baXKn2bRWwis7fZwF4sB8B7I7izwCA5kybiVdCVb8nhD2kd0qf07hgK+p1Jaa00wQiEmOXKzlS6gurYKdBHA==:").append(System.lineSeparator())
                                    .append("\"content-length\": ").append(162).append(System.lineSeparator())
                                    .append("\"content-type\": application/json").append(System.lineSeparator())
                                    .append("\"@signature-params\": ").append("(\"@method\" \"@target-uri\" \"content-digest\" \"content-length\" \"content-type\");keyid=\"89675b1d-53f3-4fb6-b8ea-33a56e576cef\";created=1741002284")
                                    .toString();

        String signatureBase = "\"@method\": POST\n" +
                                   "\"@target-uri\": https://auth.interledger-test.dev/\n" +
                                   "\"content-digest\": sha-512=:v2baXKn2bRWwis7fZwF4sB8B7I7izwCA5kybiVdCVb8nhD2kd0qf07hgK+p1Jaa00wQiEmOXKzlS6gurYKdBHA==:\n" +
                                   "\"content-length\": 162\n" +
                                   "\"content-type\": application/json\n" +
                                   "\"@signature-params\": (\"@method\" \"@target-uri\" \"content-digest\" \"content-length\" \"content-type\");keyid=\"89675b1d-53f3-4fb6-b8ea-33a56e576cef\";created=1741002284";

        //System.out.println(signatureInput);
        assertEquals(signatureBase, signatureInput);

        PrivateKey privateKey = loadPrivateKey(TestHelper.CLIENT_PRIVATE_KEY);
        String signature = generateSignature(privateKey, signatureBase);
        String signature2 = generateSignature(privateKey, signatureInput);

        assertEquals("Muxo74zrmuf2gvAt5/mMd/BSKxMU4G80jhOHyCpzFocBQ0cnkRIej4NYYqS9fWkhfxAZD3T1mItYVRIh3gQ8Ag==", signature2);
        assertEquals("Muxo74zrmuf2gvAt5/mMd/BSKxMU4G80jhOHyCpzFocBQ0cnkRIej4NYYqS9fWkhfxAZD3T1mItYVRIh3gQ8Ag==", signature);

        //assertEquals("EeRtfPjISLqf7FM+E6EFa2nUXYUfbY6VvPvJLKwhbRByE5U3rujZQQyg2CjiNUWqB40PFdUhyQdNAzmA0GcjBA==", signature);
        //assertEquals("EeRtfPjISLqf7FM+E6EFa2nUXYUfbY6VvPvJLKwhbRByE5U3rujZQQyg2CjiNUWqB40PFdUhyQdNAzmA0GcjBA==", signature2);
    }




}