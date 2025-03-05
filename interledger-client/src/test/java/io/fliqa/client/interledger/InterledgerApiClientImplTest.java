package io.fliqa.client.interledger;

import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.IncomingPayment;
import io.fliqa.client.interledger.model.PaymentPointer;
import io.fliqa.client.interledger.model.PendingGrant;
import io.fliqa.client.interledger.model.WalletAddress;
import io.fliqa.interledger.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InterledgerApiClientImplTest {

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

        // 0. get reciever wallet
        PaymentPointer wallet = client.getWallet(new WalletAddress(TestHelper.RECEIVER_WALLET_ADDRESS));
        assertNotNull(wallet);

        // 1. create grant request
        PendingGrant grantRequest = client.createPendingGrant(wallet);
        assertNotNull(grantRequest);

        // 2. create incoming payment request
        IncomingPayment incomingPayment = client.createIncomingPayment(wallet, grantRequest, BigDecimal.valueOf(12.34));
        assertNotNull(incomingPayment);
    }
}