package io.fliqa.client.interledger;

import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.*;

import java.math.BigDecimal;

/**
 * Client that communicates with Interledger backend
 * - must be configured with clientWallet, privateKey, keyId
 * - client is the third party that only acts as a facilitator between the sender and receiver of the payment
 */
public interface InterledgerApiClient {

    /**
     * Step 0
     *
     * @param address of wallet address that facilitates, sends or receives the payment
     * @return wallet info including asset (aka currency), authorization and asset addresses
     * @throws InterledgerClientException
     */
    PaymentPointer getWallet(WalletAddress address) throws InterledgerClientException;

    /**
     * Step 1 (RECEIVER)
     * Create a token for the receiving wallet to be able to create a payment request (to receive funds)
     *
     * @param receiver the wallet that will receive the payment
     * @return the consent that a grant request can be created on the wallet (aka access)
     * @throws InterledgerClientException
     */
    PendingGrant createPendingGrant(PaymentPointer receiver) throws InterledgerClientException;

    /**
     * Step 2 (RECEIVER)
     * Create an incoming payment request (from some wallet to the receiving wallet)
     *
     * @param receiver     wallet
     * @param pendingGrant grant request holding token
     * @param amount       amount with two decimal spaces
     * @return incoming payment request that will be used on sender wallet
     * @throws InterledgerClientException
     */
    IncomingPayment createIncomingPayment(PaymentPointer receiver, PendingGrant pendingGrant, BigDecimal amount) throws InterledgerClientException;

    /**
     * Step 3 (SENDER)
     * Use wallet address to get a token in order to produce a quote (cost estimation) of the transaction to take place
     *
     * @param sender wallet
     * @param amount to be sent from sender to receiver in two decimal places
     * @return pending quote
     * @throws InterledgerClientException
     */
    PendingQuote createQuoteRequest(PaymentPointer sender, BigDecimal amount) throws InterledgerClientException;

    /**
     * Step 4 (SENDER)
     * Create quote for transaction / how much the sender will need to spend in order to send the amount from his wallet to the client
     * (the payment and the fee)
     *
     * @param quoteToken
     * @param sender
     * @param incomingPayment
     * @return
     * @throws InterledgerClientException
     */
    Quote createQuote(String quoteToken, PaymentPointer sender, IncomingPayment incomingPayment) throws InterledgerClientException;

    /**
     * Step 5 (SENDER)
     * Finally we create a pending payment that needs to be confirmed by the sender (via redirect-url)
     *
     * @param sender wallet
     * @param quote  created quote
     * @return payment grant to be confirmed / canceled
     * @throws InterledgerClientException
     */
    OutgoingPayment continueGrant(PaymentPointer sender, Quote quote) throws InterledgerClientException;

    /**
     * Step 6 (CLIENT)
     * Payment was confirmed by the sender we can finalize it
     *
     * @param outgoingPayment to be finalized
     * @return finalized payment
     * @throws InterledgerClientException
     */
    FinalizedPayment finalizePayment(OutgoingPayment outgoingPayment) throws InterledgerClientException;
}
