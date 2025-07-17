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
package io.fliqa.client.interledger;

import io.fliqa.client.TestHelper;
import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import javax.swing.*;
import java.math.BigDecimal;
import java.net.URI;
import java.security.PrivateKey;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Integration test for the Interledger API client
 */
class InterledgerApiClientImplIT {

    private static final Logger log = getLogger(InterledgerApiClientImplIT.class);
    private InterledgerApiClientImpl client;

    @BeforeEach
    public void setUp() throws Exception {
        PrivateKey privateKey = TestHelper.getPrivateKey();
        WalletAddress clientWallet = new WalletAddress(TestHelper.getClientWalletAddress());

        InterledgerClientOptions options = new InterledgerClientOptions(10, 10, 120);

        client = new InterledgerApiClientImpl(clientWallet,
                privateKey,
                TestHelper.getClientKeyId(),
                options);
    }

    @Test
    public void getClientWallet() throws InterledgerClientException {

        PaymentPointer wallet = client.getWallet(new WalletAddress(TestHelper.getClientWalletAddress()));
        assertNotNull(wallet);

        Assertions.assertEquals(URI.create(TestHelper.getClientWalletAddress()), wallet.address);
        Assertions.assertEquals("Fliqa payment initiator", wallet.publicName);
        Assertions.assertEquals("EUR", wallet.assetCode);
        Assertions.assertEquals(2, wallet.assetScale);
        Assertions.assertEquals(URI.create("https://auth.interledger-test.dev"), wallet.authServer);
        Assertions.assertEquals(URI.create("https://ilp.interledger-test.dev"), wallet.resourceServer);
    }

    // Get receiver wallet data
    @Test
    public void getReceiverWallet() throws InterledgerClientException {

        PaymentPointer wallet = client.getWallet(new WalletAddress(TestHelper.getReceiverWalletAddress()));
        assertNotNull(wallet);

        Assertions.assertEquals(URI.create(TestHelper.getReceiverWalletAddress()), wallet.address);
        Assertions.assertEquals("Fliqa receiver", wallet.publicName);
        Assertions.assertEquals("EUR", wallet.assetCode);
        Assertions.assertEquals(2, wallet.assetScale);
        Assertions.assertEquals(URI.create("https://auth.interledger-test.dev"), wallet.authServer);
        Assertions.assertEquals(URI.create("https://ilp.interledger-test.dev"), wallet.resourceServer);
    }

    @Test
    public void getSenderWallet() throws InterledgerClientException {

        PaymentPointer wallet = client.getWallet(new WalletAddress(TestHelper.getSenderWalletAddress()));
        assertNotNull(wallet);

        Assertions.assertEquals(URI.create(TestHelper.getSenderWalletAddress()), wallet.address);
        Assertions.assertEquals("Fliqa sender", wallet.publicName);
        Assertions.assertEquals("EUR", wallet.assetCode);
        Assertions.assertEquals(2, wallet.assetScale);
        Assertions.assertEquals(URI.create("https://auth.interledger-test.dev"), wallet.authServer);
        Assertions.assertEquals(URI.create("https://ilp.interledger-test.dev"), wallet.resourceServer);
    }

    /**
     * Complete Interledger Payment Flow Integration Test
     * <p>
     * This test demonstrates the full 7-step Interledger Open Payments protocol flow
     * as implemented by Fliqa, where Fliqa acts as a payment facilitator between
     * a tenant (receiver) and a user (sender).
     * <p>
     * Flow Overview:
     * 1. Discovery: Get wallet information for both receiver and sender
     * 2. Authorization: Create access grants for receiving and sending
     * 3. Payment Setup: Create an incoming payment request with amount
     * 4. Quote Generation: Calculate transaction costs and fees
     * 5. User Interaction: Redirect user to approve payment in their wallet
     * 6. Payment Finalization: Complete the payment after user approval
     * 7. Status Monitoring: Track payment completion status
     * <p>
     * Note: This test requires manual interaction (clicking the redirect link and entering
     * the interact_ref parameter) to simulate real user wallet interaction.
     */
    @Test
    public void getGrantRequest() throws InterledgerClientException, InterruptedException {

        // STEP 0: RECEIVER WALLET DISCOVERY
        // Get the receiver's wallet information including supported currencies,
        // authorization server, and resource server endpoints
        log.info("********");
        log.info("STEP 0: Get receiver wallet information");
        PaymentPointer receiverWallet = client.getWallet(new WalletAddress(TestHelper.getReceiverWalletAddress()));
        assertNotNull(receiverWallet);
        log.info("Receiver wallet discovered: " + receiverWallet.publicName + " (" + receiverWallet.assetCode + ")");

        // STEP 1: RECEIVER GRANT REQUEST
        // Create an access grant that allows Fliqa to create incoming payment requests
        // on behalf of the receiver. This grant provides permissions to read, complete, and create
        // incoming payments on the receiver's wallet.
        log.info("********");
        log.info("STEP 1: Create pending grant for receiver");
        AccessGrant grantRequest = client.createPendingGrant(receiverWallet);
        assertNotNull(grantRequest);
        log.info("Receiver grant created with token: " + grantRequest.access.token.substring(0, 10) + "...");

        // STEP 2: INCOMING PAYMENT CREATION
        // Create an incoming payment request for €12.34 on the receiver's wallet.
        // This establishes the payment destination and amount that the sender will pay to.
        log.info("********");
        log.info("STEP 2: Create incoming payment request");
        IncomingPayment incomingPayment = client.createIncomingPayment(receiverWallet, grantRequest, BigDecimal.valueOf(12.34));
        assertNotNull(incomingPayment);
        log.info("Incoming payment created: " + incomingPayment.id + " for " +
                incomingPayment.incomingAmount.amount + " " + incomingPayment.incomingAmount.assetCode);

        // STEP 2.5: SENDER WALLET DISCOVERY  
        // In a real scenario, the user would enter their wallet address at this point.
        // We simulate this by getting the sender's wallet information.
        log.info("********");
        log.info("STEP 2.5: Get sender wallet information (user enters their wallet)");
        PaymentPointer senderWallet = client.getWallet(new WalletAddress(TestHelper.getSenderWalletAddress()));
        assertNotNull(senderWallet);
        log.info("Sender wallet discovered: " + senderWallet.publicName + " (" + senderWallet.assetCode + ")");

        // STEP 3: SENDER QUOTE REQUEST GRANT
        // Create an access grant that allows Fliqa to request quotes on the sender's wallet.
        // This is needed to calculate transaction fees and exchange rates.
        log.info("********");
        log.info("STEP 3: Create quote request grant for sender");
        AccessGrant quoteRequest = client.createQuoteRequest(senderWallet);
        assertNotNull(quoteRequest);
        log.info("Quote request grant created for sender wallet");

        // STEP 4: QUOTE GENERATION
        // Generate a quote that calculates the exact amount the sender needs to pay
        // including any transaction fees. The quote links the sender to the incoming payment.
        log.info("********");
        log.info("STEP 4: Generate payment quote");
        Quote quote = client.createQuote(quoteRequest.access.token, senderWallet, incomingPayment);
        assertNotNull(quote);
        log.info("Quote generated - Sender pays: " + quote.debitAmount.amount + " " + quote.debitAmount.assetCode +
                ", Receiver gets: " + quote.receiveAmount.amount + " " + quote.receiveAmount.assetCode);

        // STEP 5: PAYMENT CONTINUATION & USER INTERACTION
        // Create a redirect URL that the user must visit to authorize the payment in their wallet.
        // This implements the interactive authorization flow required by Open Payments.
        log.info("********");
        log.info("STEP 5: Create user interaction redirect");
        OutgoingPayment continueInteract = client.continueGrant(senderWallet, quote, URI.create("https://demo.fliqa.io/interledger?paymentId=1234"),
                "test");

        // The redirect URL will contain query parameters when the user returns:
        // Example: https://demo.fliqa.io/interledger?hash=...&interact_ref=bd046f2e-656b-499e-af36-8fd495e083fb

        log.info("********");
        log.info("USER INTERACTION REQUIRED:");
        log.info(String.format("CLICK ON LINK: %s", continueInteract.interact.redirect));
        System.out.printf("CLICK ON LINK: %s%n", continueInteract.interact.redirect);
        log.info("After clicking, copy the 'interact_ref' parameter from the return URL");
        log.info("********");

        // MANUAL STEP: User clicks redirect link and authorizes payment in their wallet
        // The wallet redirects back with an interact_ref parameter that we need to capture
        String interactReference = JOptionPane.showInputDialog("Enter interact_ref query parameter from return URL:");
        System.out.println("You entered: " + interactReference);

        // STEP 5.5: CHECK PAYMENT STATUS BEFORE FINALIZATION
        // Verify the payment state before attempting to finalize it
        log.info("********");
        log.info("STEP 5.5: Check incoming payment status before finalization");
        IncomingPayment payment = client.getIncomingPayment(incomingPayment, grantRequest);
        assertNotNull(payment);
        assertFalse(payment.completed); // Should not be completed yet
        log.info("Payment status before finalization: completed=" + payment.completed);

        // STEP 6: PAYMENT FINALIZATION
        // If user approved the payment (provided interact_ref), finalize the transaction
        log.info("********");
        log.info("STEP 6: Finalize payment transaction");

        if (interactReference != null && !interactReference.isBlank()) {
            log.info("User approved payment - finalizing with reference: " + interactReference);
            AccessGrant finalized = null;
            try {
                // STEP 6A: Finalize the grant using the interact reference
                // This confirms the user's authorization and provides final access token
                finalized = client.finalizeGrant(continueInteract, interactReference);
                assertNotNull(finalized);
                log.info("Grant finalized successfully");
            } catch (InterledgerClientException e) {
                log.error("Failed to finalize grant: " + e.getMessage());
            }

            if (finalized != null && finalized.access.token != null) {
                // STEP 6B: Execute the actual payment using the finalized grant
                // This transfers the funds from sender to receiver
                Payment finalizedPayment = client.finalizePayment(finalized, senderWallet, quote);
                assertNotNull(finalizedPayment);
                assertFalse(finalizedPayment.failed);
                log.info("Payment executed successfully: " + finalizedPayment.id);
            } else {
                log.info("********");
                log.error("Cannot execute payment - grant finalization failed: " + continueInteract.interact.redirect);
            }
        } else {
            log.info("********");
            log.error("Payment DECLINED - user did not provide interact_ref");
        }

        // STEP 7: PAYMENT STATUS MONITORING
        // Poll the payment status until completion or timeout (10 attempts)
        log.info("********");
        log.info("STEP 7: Monitor payment completion status");
        int count = 0;
        while (!payment.completed && count < 10) { // wait at least 10s ...
            count++;

            payment = client.getIncomingPayment(incomingPayment, grantRequest);
            assertNotNull(payment);
            log.info("********");
            log.info("Payment status check #" + count + " - Completed: " + payment.completed);

            if (!payment.completed) {
                sleep(1000); // Wait 1 second before the next check
            }
        }

        if (payment.completed) {
            log.info("SUCCESS: Payment completed successfully!");
            log.info("Final payment amount: " + payment.receivedAmount.amount + " " + payment.receivedAmount.assetCode);
        } else {
            log.warn("Payment did not complete within timeout period");
        }
    }
}