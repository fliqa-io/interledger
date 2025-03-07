package io.fliqa.client.interledger;

import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.AccessGrant;
import io.fliqa.client.interledger.model.PaymentPointer;
import io.fliqa.client.interledger.model.WalletAddress;
import io.fliqa.interledger.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for Interledger API client
 */
class InterledgerApiClientImplIT {

    private InterledgerApiClientImpl client;

    @BeforeEach
    public void setUp() throws Exception {
        PrivateKey privateKey = TestHelper.getPrivateKey();
        WalletAddress clientWallet = new WalletAddress(TestHelper.CLIENT_WALLET_ADDRESS);

        client = new InterledgerApiClientImpl(clientWallet,
                privateKey,
                TestHelper.CLIENT_KEY_ID);
    }

    @Test
    public void getClientWallet() throws InterledgerClientException {

        PaymentPointer wallet = client.getWallet(new WalletAddress(TestHelper.CLIENT_WALLET_ADDRESS));
        assertNotNull(wallet);

        Assertions.assertEquals(URI.create(TestHelper.CLIENT_WALLET_ADDRESS), wallet.address);
        Assertions.assertEquals("Fliqa payment initiator", wallet.publicName);
        Assertions.assertEquals("EUR", wallet.assetCode);
        Assertions.assertEquals(2, wallet.assetScale);
        Assertions.assertEquals(URI.create("https://auth.interledger-test.dev"), wallet.authServer);
        Assertions.assertEquals(URI.create("https://ilp.interledger-test.dev"), wallet.resourceServer);
    }

    // Get receiver wallet data
    @Test
    public void getReceiverWallet() throws InterledgerClientException {

        PaymentPointer wallet = client.getWallet(new WalletAddress(TestHelper.RECEIVER_WALLET_ADDRESS));
        assertNotNull(wallet);

        Assertions.assertEquals(URI.create(TestHelper.RECEIVER_WALLET_ADDRESS), wallet.address);
        Assertions.assertEquals("Fliqa receiver", wallet.publicName);
        Assertions.assertEquals("EUR", wallet.assetCode);
        Assertions.assertEquals(2, wallet.assetScale);
        Assertions.assertEquals(URI.create("https://auth.interledger-test.dev"), wallet.authServer);
        Assertions.assertEquals(URI.create("https://ilp.interledger-test.dev"), wallet.resourceServer);
    }

    @Test
    public void getSenderWallet() throws InterledgerClientException {

        PaymentPointer wallet = client.getWallet(new WalletAddress(TestHelper.SENDER_WALLET_ADDRESS));
        assertNotNull(wallet);

        Assertions.assertEquals(URI.create(TestHelper.SENDER_WALLET_ADDRESS), wallet.address);
        Assertions.assertEquals("Fliqa sender", wallet.publicName);
        Assertions.assertEquals("EUR", wallet.assetCode);
        Assertions.assertEquals(2, wallet.assetScale);
        Assertions.assertEquals(URI.create("https://auth.interledger-test.dev"), wallet.authServer);
        Assertions.assertEquals(URI.create("https://ilp.interledger-test.dev"), wallet.resourceServer);
    }

    // Step 1: Get a grant for the incoming payment, so we can create the incoming payment on the receiving wallet address
    @Test
    public void getGrantRequest() throws InterledgerClientException {

        // 0. get receiver receiverWallet
        PaymentPointer receiverWallet = client.getWallet(new WalletAddress(TestHelper.RECEIVER_WALLET_ADDRESS));
        assertNotNull(receiverWallet);

        // 1. create grant request
        AccessGrant grantRequest = client.createPendingGrant(receiverWallet);
        assertNotNull(grantRequest);

/*        // 2. create incoming payment request
        IncomingPayment incomingPayment = client.createIncomingPayment(receiverWallet, grantRequest, BigDecimal.valueOf(12.34));
        assertNotNull(incomingPayment);

        // get sender wallet (at this point the user has to enter his wallet address)
        PaymentPointer senderWallet = client.getWallet(new WalletAddress(TestHelper.SENDER_WALLET_ADDRESS));
        assertNotNull(senderWallet);

        // 3. create a quote request
        AccessGrant quoteRequest = client.createQuoteRequest(senderWallet);
        assertNotNull(quoteRequest);

        // 4. get quote
        Quote quote = client.createQuote(quoteRequest.access.token, senderWallet, incomingPayment);
        assertNotNull(quote);

        // 5. continue / get redirect interact
        OutgoingPayment continueInteract = client.continueGrant(senderWallet, quote);

        log.info("********");
        log.info(String.format("CLICK ON LINK: %s", continueInteract.interact.redirect));
        log.info("********");

        // Wait for the user to press a button before proceeding
        JOptionPane.showMessageDialog(null,
                "Click on the provided link,\n" +
                        "then press OK once you've completed the action at the provided link.");

        // 6. finish payment
        AccessGrant finalized = client.finalizeGrant(continueInteract);
        assertNotNull(finalized);

        FinalizedPayment finalizedPayment = client.finalizePayment(finalized,
                senderWallet, quote);
        assertNotNull(finalizedPayment);*/
    }
}