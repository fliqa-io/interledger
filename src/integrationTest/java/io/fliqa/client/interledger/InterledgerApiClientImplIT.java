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
        WalletAddress initiatorWalletAddress = new WalletAddress(TestHelper.getClientWalletAddress());

        InterledgerClientOptions options = new InterledgerClientOptions(20, 20, 120);

        client = new InterledgerApiClientImpl(initiatorWalletAddress,
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
        Assertions.assertEquals(URI.create("https://auth.interledger-test.dev/f537937b-7016-481b-b655-9f0d1014822c"), wallet.authServer);
        Assertions.assertEquals(URI.create("https://ilp.interledger-test.dev/f537937b-7016-481b-b655-9f0d1014822c"), wallet.resourceServer);
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
        Assertions.assertEquals(URI.create("https://auth.interledger-test.dev/f537937b-7016-481b-b655-9f0d1014822c"), wallet.authServer);
        Assertions.assertEquals(URI.create("https://ilp.interledger-test.dev/f537937b-7016-481b-b655-9f0d1014822c"), wallet.resourceServer);
    }

    @Test
    public void getSenderWallet() throws InterledgerClientException {

        PaymentPointer wallet = client.getWallet(new WalletAddress(TestHelper.getSenderWalletAddress()));
        assertNotNull(wallet);

        Assertions.assertEquals(URI.create(TestHelper.getSenderWalletAddress()), wallet.address);
        Assertions.assertEquals("Fliqa sender", wallet.publicName);
        Assertions.assertEquals("EUR", wallet.assetCode);
        Assertions.assertEquals(2, wallet.assetScale);
        Assertions.assertEquals(URI.create("https://auth.interledger-test.dev/f537937b-7016-481b-b655-9f0d1014822c"), wallet.authServer);
        Assertions.assertEquals(URI.create("https://ilp.interledger-test.dev/f537937b-7016-481b-b655-9f0d1014822c"), wallet.resourceServer);
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
        //IncomingPayment incomingPayment = client.createIncomingPayment(receiverWallet, grantRequest, BigDecimal.valueOf(12.34));
        IncomingPayment incomingPayment = client.createIncomingPayment(receiverWallet, grantRequest, BigDecimal.valueOf(1234.56)); // not enough balance
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
        OutgoingPayment continueInteract = client.continueGrant(senderWallet, quote,
                URI.create("https://demo.fliqa.io/interledger/payment.html?paymentId=1234"),
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

        // Hoisted so they remain accessible in STEP 8 below, where we explore which
        // status-check calls still work once the incoming payment has been completed.
        AccessGrant finalized = null;
        Payment finalizedPayment = null;

        if (interactReference != null && !interactReference.isBlank()) {
            log.info("User approved payment - finalizing with reference: " + interactReference);
            try {
                // STEP 6A: Finalize the grant using the interact reference
                // This confirms the user's authorization and provides final access token
                finalized = client.finalizeGrant(continueInteract, interactReference);
                assertNotNull(finalized);
                log.info("Grant finalized successfully");
            } catch (InterledgerClientException e) {
                logGrantFinalizationError(e, false);
            }
        } else {
            // STEP 6A (no interact_ref): rather than assuming denial just because the callback
            // came back empty, poll the continuation endpoint directly. GNAP's
            // continuation-request schema makes interact_ref optional, so the client may still
            // call POST /continue/{id} with no body - but note that not every auth server
            // supports this (see logGrantFinalizationError's javadoc for what we observed
            // against Rafiki/interledger-test.dev).
            log.info("********");
            log.info("No interact_ref on callback - polling continuation endpoint instead of assuming denial");
            try {
                AccessGrant polled = client.pollGrant(continueInteract);
                if (polled.access != null && polled.access.token != null) {
                    finalized = polled;
                    log.info("Grant approved (discovered via poll, without an interact_ref)");
                } else {
                    Integer wait = continueInteract.paymentContinue.wait;
                    log.info("Grant still pending user interaction" + (wait != null ? " (retry after " + wait + "s)" : ""));
                }
            } catch (InterledgerClientException e) {
                logGrantFinalizationError(e, true);
            }
        }

        if (finalized != null && finalized.access.token != null) {
            // STEP 6B: Execute the actual payment using the finalized grant
            // This transfers the funds from sender to receiver
            finalizedPayment = client.finalizePayment(finalized, senderWallet, quote);
            assertNotNull(finalizedPayment);
            assertFalse(finalizedPayment.failed);
            log.info("Payment executed successfully: " + finalizedPayment.id);
        } else {
            log.info("********");
            log.error("Cannot execute payment - grant was not finalized: " + continueInteract.interact.redirect);
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

        // STEP 8: EXPLORE STATUS-CHECK OPTIONS AFTER PAYMENT COMPLETION
        // Once the incoming payment is marked completed, the receiver-side grant used by
        // getIncomingPayment(...) may no longer be usable for polling - some wallets restrict
        // or revoke read access once an incoming payment is completed. Here we probe a few
        // different calls to see which ones remain usable after completion:
        //   A) re-check the incoming payment with the original receiver-side grant
        //   B) check the outgoing payment with the sender-side finalized grant
        // Each call is isolated in its own try/catch so a failure in one does not hide the
        // result of the others - we're only interested in observing what works.
        //
        // NOTE on DENIED payments: none of these calls will ever distinguish "denied" from
        // "still pending" - if the user denies consent, no outgoing payment is ever created,
        // so getIncomingPayment simply stays completed=false/receivedAmount=0 forever (until
        // the incoming payment expires), and getOutgoingPayment has no resource to fetch.
        // Denial is only observable earlier, in STEP 6A, as a 401 "request_denied" GNAP error
        // from finalizeGrant(...) or pollGrant(...) - see logGrantFinalizationError(...) below.
        log.info("********");
        log.info("STEP 8: Explore payment status checks after completion");

        // Option A: re-check the incoming payment (receiver-side grant from STEP 1)
        try {
            IncomingPayment recheck = client.getIncomingPayment(incomingPayment, grantRequest);
            log.info("getIncomingPayment after completion: OK - completed=" + recheck.completed +
                    ", receivedAmount=" + recheck.receivedAmount.amount + " " + recheck.receivedAmount.assetCode);
        } catch (InterledgerClientException e) {
            log.warn("getIncomingPayment after completion FAILED: " + e.getMessage());
        }

        // Option B: check the outgoing payment (sender-side grant from STEP 6A/6B)
        if (finalizedPayment != null && finalized != null) {
            try {
                Payment outgoingStatus = client.getOutgoingPayment(finalizedPayment.id, finalized);
                assertNotNull(outgoingStatus);
                log.info("getOutgoingPayment: OK - failed=" + outgoingStatus.failed +
                        ", sentAmount=" + outgoingStatus.sentAmount.amount + " " + outgoingStatus.sentAmount.assetCode +
                        ", receivedAmount=" + outgoingStatus.receivedAmount.amount + " " + outgoingStatus.receivedAmount.assetCode +
                        ", metadata=" + outgoingStatus.metadata);
                if (Boolean.TRUE.equals(outgoingStatus.failed)) {
                    // metadata is free-form per the Open Payments spec - Rafiki reports a
                    // failure reason here, e.g. {"cancellationReason": "Insufficient funds"}
                    log.warn("Outgoing payment FAILED - metadata: " + outgoingStatus.metadata);
                }
            } catch (InterledgerClientException e) {
                log.warn("getOutgoingPayment FAILED: " + e.getMessage());
            }
        } else {
            log.info("Skipping getOutgoingPayment check - payment was not finalized (denied, abandoned, or failed - see STEP 6 log above)");
        }
    }

    /**
     * Logs the outcome of a failed grant finalization/poll attempt, distinguishing an explicit
     * GNAP {@code request_denied} rejection (HTTP 401) from any other failure.
     *
     * <p>The meaning of {@code request_denied} depends on how it was triggered. When it comes
     * back from {@link InterledgerApiClient#finalizeGrant(OutgoingPayment, String)} - i.e. an
     * {@code interact_ref} was actually presented - it's an authoritative signal that the user
     * denied the grant. When it comes back from {@link InterledgerApiClient#pollGrant(OutgoingPayment)}
     * - i.e. no {@code interact_ref} was available - it's ambiguous: as observed against the
     * Rafiki reference implementation ({@code interledger-test.dev}), that auth server returns
     * this exact code with description "grant cannot be polled" for <em>any</em> continuation
     * attempt made without an {@code interact_ref}, regardless of whether the user approved or
     * denied. So a poll-triggered {@code request_denied} should be treated as inconclusive.
     *
     * @param e       the exception thrown by {@code finalizeGrant}/{@code pollGrant}
     * @param viaPoll {@code true} if {@code e} came from {@code pollGrant} (no interact_ref
     *                presented), {@code false} if it came from {@code finalizeGrant} (an
     *                interact_ref was presented and explicitly rejected)
     */
    private static void logGrantFinalizationError(InterledgerClientException e, boolean viaPoll) {
        boolean requestDenied = e.getCode() == 401 && e.getMessage() != null && e.getMessage().contains("(request_denied)");

        if (requestDenied && viaPoll) {
            log.warn("Grant continuation rejected (request_denied) while polling without an interact_ref: " +
                    e.getMessage() + " - this MAY mean the user denied consent, or simply that this auth " +
                    "server does not support polling without an interact_ref (observed with Rafiki/" +
                    "interledger-test.dev). Treat as inconclusive, not a confirmed denial.");
        } else if (requestDenied) {
            log.error("Payment DENIED by user during wallet interaction: " + e.getMessage());
        } else {
            log.error("Failed to finalize grant: " + e.getMessage());
        }
    }
}