package io.fliqa.client.interledger;

import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.*;
import io.fliqa.interledger.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import javax.swing.*;
import java.math.BigDecimal;
import java.net.URI;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Integration test for Interledger API client
 */
class InterledgerApiClientImplIT {

    private static final Logger log = getLogger(InterledgerApiClientImplIT.class);

    private static final int TRANSACTION_TIMEOUT = 120;
    private InterledgerApiClientImpl client;

    @BeforeEach
    public void setUp() throws Exception {
        PrivateKey privateKey = TestHelper.getPrivateKey();
        WalletAddress clientWallet = new WalletAddress(TestHelper.CLIENT_WALLET_ADDRESS);

        InterledgerClientOptions options = new InterledgerClientOptions(10, 10, TRANSACTION_TIMEOUT);

        client = new InterledgerApiClientImpl(clientWallet,
                privateKey,
                TestHelper.CLIENT_KEY_ID,
                options);
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
    public void getGrantRequest() throws InterledgerClientException, InterruptedException {

        // 0. get receiver receiverWallet
        log.info("********");
        log.info("Get receiver wallet:");
        PaymentPointer receiverWallet = client.getWallet(new WalletAddress(TestHelper.RECEIVER_WALLET_ADDRESS));
        assertNotNull(receiverWallet);

        // 1. create grant request
        log.info("********");
        log.info("Create pending grant:");
        AccessGrant grantRequest = client.createPendingGrant(receiverWallet);
        assertNotNull(grantRequest);

        // 2. create incoming payment request
        log.info("********");
        log.info("Create incoming payment grant:");
        IncomingPayment incomingPayment = client.createIncomingPayment(receiverWallet, grantRequest, BigDecimal.valueOf(12.34));
        assertNotNull(incomingPayment);

        // get sender wallet (at this point the user has to enter his wallet address)
        PaymentPointer senderWallet = client.getWallet(new WalletAddress(TestHelper.SENDER_WALLET_ADDRESS));
        assertNotNull(senderWallet);

        // 3. create a quote request
        log.info("********");
        log.info("Create quote request:");
        AccessGrant quoteRequest = client.createQuoteRequest(senderWallet);
        assertNotNull(quoteRequest);

        // 4. get quote
        log.info("********");
        log.info("Create quote:");
        Quote quote = client.createQuote(quoteRequest.access.token, senderWallet, incomingPayment);
        assertNotNull(quote);

        // 5. continue / get redirect interact
        log.info("Get redirect link / continue interact:");
        OutgoingPayment continueInteract = client.continueGrant(senderWallet, quote, URI.create("https://demo.fliqa.io?paymentId=1234"), "test");

        // return to https://demo.fliqa.io?hash=saVqe5FJo8CR3DeVShjitbEP973siqE3m0313Ne80uU%3D&interact_ref=bd046f2e-656b-499e-af36-8fd495e083fb

        log.info("********");
        log.info(String.format("CLICK ON LINK: %s", continueInteract.interact.redirect));
        log.info("********");

        String interactReference = JOptionPane.showInputDialog("Enter interact_ref query parameter:");
        System.out.println("You entered: " + interactReference);

        // Call to check state before continuing (we must wait until user finishes his action)
        log.info("********");
        log.info("Get incoming payment status:");
        IncomingPayment payment = client.getIncomingPayment(incomingPayment, grantRequest);
        assertNotNull(payment);
        assertFalse(payment.completed);

        // 6. finish payment
        // TODO: check if accepted then we can finalize it (otherwise this will fail)
        log.info("********");

        if (interactReference != null && !interactReference.isBlank()) {
            log.info("Finalizing payment with reference: " + interactReference);
            AccessGrant finalized = null;
            try {
                finalized = client.finalizeGrant(continueInteract, interactReference);
                assertNotNull(finalized);
            } catch (InterledgerClientException e) {
                log.error(e.getMessage());
            }

            if (finalized != null && finalized.access.token != null) {
                Payment finalizedPayment = client.finalizePayment(finalized,
                        senderWallet, quote);
                assertNotNull(finalizedPayment);
                assertFalse(finalizedPayment.failed);
            } else {
                log.info("********");
                log.error(String.format("Can't finalize: %s", continueInteract.interact.redirect));
            }
        } else {
            log.info("********");
            log.error("Payment DECLINED");
        }

        int count = 0;
        while (!payment.completed && count < 10) {
            count++;

            payment = client.getIncomingPayment(incomingPayment, grantRequest);
            assertNotNull(payment);
            log.info("********");
            log.info(String.format("Completed: %s", payment.completed));

            Thread.sleep(1000);
        }

    }

    @Test
    void getIncomingPaymentStatus() throws InterledgerClientException {
        PaymentPointer senderWallet = client.getWallet(new WalletAddress(TestHelper.SENDER_WALLET_ADDRESS));
        assertNotNull(senderWallet);

        // TODO: implement call to get payment status 
    }
}