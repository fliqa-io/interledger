package io.fliqa.client.interledger;

import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.PaymentPointer;
import io.fliqa.client.interledger.model.WalletAddress;
import io.fliqa.interledger.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InterledgerApiClientImplTest {

    private static final String BASE_PATH = "https://ilp.interledger-test.dev";
    private InterledgerApiClientImpl client;


    @BeforeEach
    public void setUp() throws Exception {
        PrivateKey privateKey = TestHelper.getPrivateKey();
        WalletAddress clientWallet = new WalletAddress(TestHelper.CLIENT_WALLET_ADDRESS);
        client = new InterledgerApiClientImpl(clientWallet,
                privateKey,
                TestHelper.CLIENT_KEY_ID);
    }

    // Get receiver wallet data
    @Test
    public void getWallet() throws InterledgerClientException {

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
    public void getGrantRequest() throws InterledgerClientException {

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

   /* @Test
    public void digestContentTest() throws NoSuchAlgorithmException {
        System.out.println("----");
        System.out.println(requestBody);
        System.out.println("----");

        String out = InterledgerApiClientImpl.digestContentSha512(requestBody);
        assertEquals("v2baXKn2bRWwis7fZwF4sB8B7I7izwCA5kybiVdCVb8nhD2kd0qf07hgK+p1Jaa00wQiEmOXKzlS6gurYKdBHA==", out);
    }
*/


}