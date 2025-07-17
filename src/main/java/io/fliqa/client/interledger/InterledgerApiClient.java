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

import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.*;

import java.math.BigDecimal;
import java.net.URI;

/**
 * Client interface for communicating with Interledger Open Payments protocol servers.
 * 
 * <p>This client implements a subset of the Interledger Open Payments specification
 * optimized for Fliqa's payment facilitation use cases. It does not support all
 * Interledger flows but focuses on the core payment workflows where Fliqa acts
 * as an intermediary between senders and receivers.
 * 
 * <h3>Configuration Requirements</h3>
 * <ul>
 *   <li>Client wallet address (payment facilitator/initiator)</li>
 *   <li>Ed25519 private key for request signing</li>
 *   <li>Key ID corresponding to the private key</li>
 * </ul>
 * 
 * <h3>Supported Payment Flow</h3>
 * <p>The client supports a 7-step payment flow where Fliqa facilitates payments
 * between tenants (receivers) and users (senders):
 * <ol>
 *   <li><strong>Wallet Discovery</strong> - Get wallet information for receiver and sender</li>
 *   <li><strong>Receiver Grant</strong> - Create access grant for incoming payments</li>
 *   <li><strong>Incoming Payment</strong> - Create payment request with amount</li>
 *   <li><strong>Sender Quote</strong> - Calculate transaction costs and fees</li>
 *   <li><strong>User Interaction</strong> - Redirect user to approve payment</li>
 *   <li><strong>Payment Finalization</strong> - Complete payment after approval</li>
 *   <li><strong>Status Monitoring</strong> - Track payment completion</li>
 * </ol>
 * 
 * <h3>Security</h3>
 * <p>All requests are cryptographically signed using Ed25519 signatures following
 * the HTTP Message Signatures specification. This ensures request authenticity
 * and integrity when communicating with Interledger servers.
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see InterledgerApiClientImpl
 */
public interface InterledgerApiClient {

    int INTERNAL_SERVER_ERROR = 500;

    /**
     * Retrieves wallet information from an Interledger payment pointer.
     * 
     * <p>This is typically the first step in any payment flow, used to discover
     * the wallet's supported assets, authorization server, and resource server endpoints.
     * The wallet information is essential for subsequent grant requests and payment operations.
     * 
     * @param address the wallet address that facilitates, sends, or receives payments
     * @return wallet information including asset details, authorization server, and resource server URLs
     * @throws InterledgerClientException if the wallet cannot be found or accessed
     * @see PaymentPointer
     * @see WalletAddress
     */
    PaymentPointer getWallet(WalletAddress address) throws InterledgerClientException;

    /**
     * Creates a pending grant for a receiving wallet to enable incoming payment creation.
     * 
     * <p>This grant provides Fliqa with permission to create incoming payment requests
     * on behalf of the receiver. The grant includes access tokens that authorize
     * operations like creating, reading, and completing incoming payments.
     * 
     * <p><strong>Step 1</strong> in the payment flow (Receiver side).
     * 
     * @param receiver the wallet that will receive the payment
     * @return access grant containing tokens and permissions for incoming payments
     * @throws InterledgerClientException if the grant cannot be created or the wallet rejects the request
     * @see AccessGrant
     * @see PaymentPointer
     */
    AccessGrant createPendingGrant(PaymentPointer receiver) throws InterledgerClientException;

    /**
     * Creates an incoming payment request on the receiver's wallet.
     * 
     * <p>This establishes a payment destination with a specific amount that senders
     * can pay to. The incoming payment serves as the target for the outgoing payment
     * from the sender's wallet.
     * 
     * <p><strong>Step 2</strong> in the payment flow (Receiver side).
     * 
     * @param receiver the wallet that will receive the payment
     * @param pendingGrant access grant obtained from {@link #createPendingGrant(PaymentPointer)}
     * @param amount the payment amount with two decimal places precision
     * @return incoming payment request that can be referenced by sender wallets
     * @throws InterledgerClientException if the payment request cannot be created
     * @see IncomingPayment
     * @see AccessGrant
     */
    IncomingPayment createIncomingPayment(PaymentPointer receiver, AccessGrant pendingGrant, BigDecimal amount) throws InterledgerClientException;

    /**
     * Creates a quote request grant for the sender's wallet.
     * 
     * <p>This grant provides permission to request quotes from the sender's wallet,
     * which is necessary to calculate transaction fees and exchange rates before
     * creating the actual payment.
     * 
     * <p><strong>Step 3</strong> in the payment flow (Sender side).
     * 
     * @param sender the wallet that will send the payment
     * @return access grant for creating quotes on the sender's wallet
     * @throws InterledgerClientException if the quote request grant cannot be created
     * @see AccessGrant
     * @see PaymentPointer
     */
    AccessGrant createQuoteRequest(PaymentPointer sender) throws InterledgerClientException;

    /**
     * Creates a quote that calculates the exact cost for the sender to complete the payment.
     * 
     * <p>The quote includes the debit amount (what the sender pays) and receive amount
     * (what the receiver gets), accounting for any transaction fees and currency
     * conversions. This quote links the sender's wallet to the specific incoming payment.
     * 
     * <p><strong>Step 4</strong> in the payment flow (Sender side).
     * 
     * @param quoteToken access token from the quote request grant
     * @param sender the wallet that will send the payment
     * @param incomingPayment the target payment request created on the receiver's wallet
     * @return quote containing debit amount, receive amount, and payment details
     * @throws InterledgerClientException if the quote cannot be generated
     * @see Quote
     * @see IncomingPayment
     */
    Quote createQuote(String quoteToken, PaymentPointer sender, IncomingPayment incomingPayment) throws InterledgerClientException;

    /**
     * Creates a pending outgoing payment that requires user interaction for authorization.
     * 
     * <p>This step initiates the interactive authorization flow where the user must
     * visit a redirect URL to approve the payment in their wallet. The user will be
     * redirected back to the return URL with an interaction reference upon completion.
     * 
     * <p><strong>Step 5</strong> in the payment flow (Sender side).
     * 
     * @param sender the wallet that will send the payment
     * @param quote the quote generated for this payment
     * @param returnUrl URI where the user will be redirected after payment authorization
     * @param nonce unique identifier to prevent replay attacks and maintain state
     * @return outgoing payment with interaction details including the redirect URL
     * @throws InterledgerClientException if the payment cannot be created
     * @see OutgoingPayment
     * @see Quote
     */
    OutgoingPayment continueGrant(PaymentPointer sender, Quote quote, URI returnUrl, String nonce) throws InterledgerClientException;

    /**
     * Finalizes the grant after the user has approved the payment.
     * 
     * <p>Once the user completes the interactive authorization flow, they are redirected
     * back with an interaction reference. This method uses that reference to finalize
     * the grant and obtain the final access token needed to execute the payment.
     * 
     * <p><strong>Step 6</strong> in the payment flow (Client side).
     * 
     * @param outgoingPayment the pending payment to be finalized
     * @param interactRef interaction reference returned from the user's wallet after authorization
     * @return finalized access grant with tokens to execute the payment
     * @throws InterledgerClientException if the grant cannot be finalized or the interaction reference is invalid
     * @see AccessGrant
     * @see OutgoingPayment
     */
    AccessGrant finalizeGrant(OutgoingPayment outgoingPayment, String interactRef) throws InterledgerClientException;

    /**
     * Executes the final payment using the finalized grant.
     * 
     * <p>This method transfers the funds from the sender's wallet to the receiver's wallet
     * using the finalized access grant. The payment amount and details are based on
     * the previously generated quote.
     * 
     * <p><strong>Step 7</strong> in the payment flow (Client side).
     * 
     * @param finalized the finalized access grant obtained from {@link #finalizeGrant(OutgoingPayment, String)}
     * @param senderWallet the wallet that will send the payment
     * @param quote the quote that determines payment amounts and fees
     * @return completed payment details including transaction ID and status
     * @throws InterledgerClientException if the payment execution fails
     * @see Payment
     * @see AccessGrant
     * @see Quote
     */
    Payment finalizePayment(AccessGrant finalized, PaymentPointer senderWallet, Quote quote) throws InterledgerClientException;

    /**
     * Retrieves the current status of an incoming payment.
     * 
     * <p>This method is used to monitor payment completion and track the received amount.
     * It can be called multiple times to poll for payment status updates until the
     * payment is marked as completed.
     * 
     * @param incomingPayment the incoming payment to check
     * @param grantRequest the access grant that provides permission to read the payment
     * @return current payment status including completion state and received amount
     * @throws InterledgerClientException if the payment cannot be found or access is denied
     * @see IncomingPayment
     * @see AccessGrant
     */
    IncomingPayment getIncomingPayment(IncomingPayment incomingPayment, AccessGrant grantRequest) throws InterledgerClientException;
}
