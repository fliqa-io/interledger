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
import io.fliqa.client.interledger.logging.HttpLogger;
import io.fliqa.client.interledger.model.*;
import io.fliqa.client.interledger.signature.SignatureRequestBuilder;
import io.fliqa.client.interledger.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.Set;

import static io.fliqa.client.interledger.exception.InterledgerClientException.getApiException;
import static io.fliqa.client.interledger.signature.SignatureRequestBuilder.ACCEPT_HEADER;
import static io.fliqa.client.interledger.signature.SignatureRequestBuilder.APPLICATION_JSON;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Default implementation of the {@link InterledgerApiClient} interface.
 *
 * <p>This implementation provides a complete HTTP-based client for the Interledger
 * Open Payments protocol, with cryptographic request signing using Ed25519 keys.
 * The client is designed for Fliqa's payment facilitation use cases where Fliqa
 * acts as an intermediary between payment senders and receivers.
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Cryptographic request signing using Ed25519 private keys</li>
 *   <li>HTTP/2 client with configurable timeouts and connection limits</li>
 *   <li>Comprehensive error handling with structured exception types</li>
 *   <li>Request/response logging for debugging and auditing</li>
 *   <li>Input validation for all public methods</li>
 * </ul>
 *
 * <h3>Thread Safety</h3>
 * <p>This implementation is thread-safe and can be used concurrently from multiple threads.
 * The underlying HTTP client and cryptographic operations are thread-safe.
 *
 * <h3>Security</h3>
 * <p>All requests to Interledger servers are signed using Ed25519 signatures following
 * the HTTP Message Signatures specification. The client validates that all required
 * parameters are provided and non-null before making requests.
 *
 * <h3>Error Handling</h3>
 * <p>The client distinguishes between different types of HTTP errors:
 * <ul>
 *   <li>4xx errors - Client errors (invalid requests, authentication failures)</li>
 *   <li>5xx errors - Server errors (Interledger server issues)</li>
 *   <li>Network errors - Connection timeouts, DNS failures, etc.</li>
 * </ul>
 *
 * @author Fliqa
 * @version 1.0
 * @see InterledgerApiClient
 * @see InterledgerClientOptions
 * @since 1.0
 */
public class InterledgerApiClientImpl implements InterledgerApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterledgerApiClientImpl.class);
    private static final String ILP_METHOD = "ilp"; // NOTE: this is currently hardcoded (might be an argument / not sure)

    private final WalletAddress clientWallet;
    private final PrivateKey privateKey;
    private final String keyId;

    private final HttpClient client;
    private final InterledgerClientOptions options;
    private final InterledgerObjectMapper mapper = new InterledgerObjectMapper();
    private final HttpLogger httpLogger;

    /**
     * Creates a new Interledger API client with custom configuration options.
     *
     * <p>This constructor allows full customization of the HTTP client behavior including
     * connection timeouts, request timeouts, and maximum concurrent connections.
     *
     * @param clientWallet the wallet address of the payment facilitator (Fliqa)
     * @param privateKey   Ed25519 private key for signing requests
     * @param keyId        identifier for the private key, used in signature headers
     * @param options      HTTP client configuration including timeouts and connection limits
     * @throws IllegalArgumentException if any parameter is null or keyId is empty
     * @see InterledgerClientOptions
     */
    public InterledgerApiClientImpl(WalletAddress clientWallet,
                                    PrivateKey privateKey,
                                    String keyId,
                                    InterledgerClientOptions options) {

        Assert.notNull(clientWallet, "WalletAddress cannot be null");
        Assert.notNull(privateKey, "PrivateKey cannot be null");
        Assert.notNullOrEmpty(keyId, "KeyId cannot be null or empty");
        Assert.notNull(options, "InterledgerClientOptions cannot be null");

        this.clientWallet = clientWallet;
        this.privateKey = privateKey;
        this.keyId = keyId;
        this.options = options;

        client = createDefaultHttpClient(options);
        httpLogger = new HttpLogger(LOGGER);
    }

    /**
     * Creates a new Interledger API client with default configuration options.
     *
     * <p>This convenience constructor uses default HTTP client settings, including
     * standard timeouts and connection limits suitable for most use cases.
     *
     * @param clientWallet the wallet address of the payment facilitator (Fliqa)
     * @param privateKey   Ed25519 private key for signing requests
     * @param keyId        identifier for the private key, used in signature headers
     * @throws IllegalArgumentException if any parameter is null or keyId is empty
     * @see #InterledgerApiClientImpl(WalletAddress, PrivateKey, String, InterledgerClientOptions)
     */
    public InterledgerApiClientImpl(WalletAddress clientWallet,
                                    PrivateKey privateKey,
                                    String keyId) {
        this(clientWallet, privateKey, keyId, InterledgerClientOptions.DEFAULT);
    }

    protected static HttpClient createDefaultHttpClient(InterledgerClientOptions options) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(options.connectTimeOutInSeconds))  // Connect timeout
                .build();
    }

    @Override
    public PaymentPointer getWallet(WalletAddress address) throws InterledgerClientException {
        Assert.notNull(address, "WalletAddress cannot be null");
        LOGGER.debug("getWallet: {}", address);

        var request = HttpRequest.newBuilder(address.paymentPointer)
                .GET()
                .header(ACCEPT_HEADER.toLowerCase(), APPLICATION_JSON)
                .timeout(Duration.of(options.timeOutInSeconds, SECONDS))
                .build();

        return send(request, PaymentPointer.class);
    }

    @Override
    public AccessGrant createPendingGrant(PaymentPointer receiver) throws InterledgerClientException {
        Assert.notNull(receiver, "PaymentPointer receiver cannot be null");
        LOGGER.debug("createPendingGrant: {}", receiver);

        GrantAccessRequest accessRequest = GrantAccessRequest.build(clientWallet,
                AccessItemType.incomingPayment,
                Set.of(AccessAction.read, AccessAction.complete, AccessAction.create));

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(accessRequest)
                .target(receiver.authServer)
                .getRequest(options);

        return send(request, AccessGrant.class);
    }

    @Override
    public IncomingPayment createIncomingPayment(PaymentPointer receiver, AccessGrant pendingGrant, BigDecimal amount) throws InterledgerClientException {
        Assert.notNull(receiver, "PaymentPointer receiver cannot be null");
        Assert.notNull(pendingGrant, "AccessGrant pendingGrant cannot be null");
        Assert.notNull(amount, "BigDecimal amount cannot be null");

        LOGGER.debug("createIncomingPayment: {} for: {}", receiver, amount);

        PaymentRequest paymentRequest = PaymentRequest.build(receiver, amount, options.transactionExpirationInSeconds);

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(paymentRequest)
                .target(buildResourceUrl(receiver.resourceServer, "/incoming-payments"))
                .accessToken(extractAccessToken(pendingGrant))
                .getRequest(options);

        return send(request, IncomingPayment.class);
    }

    @Override
    public AccessGrant createQuoteRequest(PaymentPointer sender) throws InterledgerClientException {
        Assert.notNull(sender, "PaymentPointer sender cannot be null");
        LOGGER.debug("createQuoteRequest: {}", sender);

        GrantAccessRequest accessRequest = GrantAccessRequest.build(clientWallet,
                AccessItemType.quote,
                Set.of(AccessAction.read, AccessAction.create));

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(accessRequest)
                .target(sender.authServer)
                .getRequest(options);

        return send(request, AccessGrant.class);
    }

    @Override
    public Quote createQuote(String quoteToken, PaymentPointer sender, IncomingPayment incomingPayment) throws InterledgerClientException {
        Assert.notNullOrEmpty(quoteToken, "Quote token cannot be null or empty");
        Assert.notNull(sender, "PaymentPointer sender cannot be null");
        Assert.notNull(incomingPayment, "IncomingPayment cannot be null");
        LOGGER.debug("createQuote: {} for: {}", incomingPayment, sender);

        QuoteRequest quoteRequest = QuoteRequest.build(sender.address,
                incomingPayment.id.toString(),
                ILP_METHOD);

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(quoteRequest)
                .target(buildResourceUrl(sender.resourceServer, "/quotes"))
                .accessToken(quoteToken)
                .getRequest(options);

        return send(request, Quote.class);
    }

    @Override
    public OutgoingPayment continueGrant(PaymentPointer sender, Quote quote, URI returnUrl, String nonce) throws InterledgerClientException {
        Assert.notNull(sender, "PaymentPointer sender cannot be null");
        Assert.notNull(quote, "Quote cannot be null");
        Assert.notNull(returnUrl, "Return URL cannot be null");
        Assert.notNullOrEmpty(nonce, "Nonce cannot be null or empty");
        LOGGER.debug("continueGrant: {} for: {}", quote, sender);

        GrantAccessRequest accessRequest = GrantAccessRequest.outgoing(clientWallet,
                        AccessItemType.outgoingPayment,
                        Set.of(AccessAction.read, AccessAction.create),
                        sender.address, quote.debitAmount)
                .redirectInteract(returnUrl, nonce);

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(accessRequest)
                .target(sender.authServer)
                .getRequest(options);

        return send(request, OutgoingPayment.class);
    }

    @Override
    public AccessGrant finalizeGrant(OutgoingPayment outgoingPayment, String interactRef) throws InterledgerClientException {
        Assert.notNull(outgoingPayment, "OutgoingPayment cannot be null");
        Assert.notNullOrEmpty(interactRef, "Interact reference cannot be null or empty");
        LOGGER.debug("finalizeGrant: {} for: {}", outgoingPayment, interactRef);

        InteractRef ref = InteractRef.build(interactRef);

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(ref)
                .target(outgoingPayment.paymentContinue.uri)
                .accessToken(extractContinueAccessToken(outgoingPayment))
                .getRequest(options);

        return send(request, AccessGrant.class);
    }

    @Override
    public Payment finalizePayment(AccessGrant finalizedGrant, PaymentPointer senderWallet, Quote quote) throws InterledgerClientException {
        Assert.notNull(finalizedGrant, "AccessGrant finalizedGrant cannot be null");
        Assert.notNull(senderWallet, "PaymentPointer senderWallet cannot be null");
        Assert.notNull(quote, "Quote cannot be null");
        LOGGER.debug("finalizePayment: {} for: {}, with: {}", finalizedGrant, senderWallet, quote);

        OutgoingPaymentRequest outgoingPayment = new OutgoingPaymentRequest();
        outgoingPayment.quoteId = quote.id;
        outgoingPayment.walletAddress = senderWallet.address;

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(outgoingPayment)
                .target(buildResourceUrl(senderWallet.resourceServer, "/outgoing-payments"))
                .accessToken(extractAccessToken(finalizedGrant))
                .getRequest(options);

        return send(request, Payment.class);
    }

    @Override
    public IncomingPayment getIncomingPayment(IncomingPayment payment, AccessGrant grant) throws InterledgerClientException {
        Assert.notNull(payment, "IncomingPayment cannot be null");
        Assert.notNull(grant, "AccessGrant cannot be null");
        LOGGER.debug("getIncomingPayment: {}", payment);

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .GET()
                .target(payment.id)
                .accessToken(extractAccessToken(grant))
                .getRequest(options);

        return send(request, IncomingPayment.class);
    }

    /**
     * Extracts access token from AccessGrant
     *
     * @param grant the access grant containing the token
     * @return the access token string
     * @throws IllegalArgumentException if grant, grant.access, or grant.access.token is null
     */
    private String extractAccessToken(AccessGrant grant) {
        Assert.notNull(grant, "AccessGrant cannot be null");
        Assert.notNull(grant.access, "AccessGrant.access cannot be null");
        Assert.notNull(grant.access.token, "AccessGrant.access.token cannot be null");
        return grant.access.token;
    }

    /**
     * Extracts access token from OutgoingPayment's continue access
     *
     * @param outgoingPayment the outgoing payment containing continue access
     * @return the access token string
     * @throws IllegalArgumentException if outgoingPayment, paymentContinue, access, or token is null
     */
    private String extractContinueAccessToken(OutgoingPayment outgoingPayment) {
        Assert.notNull(outgoingPayment, "OutgoingPayment cannot be null");
        Assert.notNull(outgoingPayment.paymentContinue, "OutgoingPayment.paymentContinue cannot be null");
        Assert.notNull(outgoingPayment.paymentContinue.access, "OutgoingPayment.paymentContinue.access cannot be null");
        Assert.notNull(outgoingPayment.paymentContinue.access.token, "OutgoingPayment.paymentContinue.access.token cannot be null");
        return outgoingPayment.paymentContinue.access.token;
    }

    /**
     * Safely builds a resource URL by appending a path to a base URI
     *
     * @param baseUri the base URI
     * @param path    the path to append (should start with / or will be prepended)
     * @return the constructed URI
     * @throws IllegalArgumentException if baseUri or path is null
     */
    protected URI buildResourceUrl(URI baseUri, String path) {
        Assert.notNull(baseUri, "Base URI cannot be null");
        Assert.notNull(path, "Path cannot be null");
        
        String rootPath = baseUri.toString();
        if (rootPath.endsWith("/")) {
            rootPath = rootPath.substring(0, rootPath.length() - 1);
        }

        String normalizedPath = path.startsWith("/") ? path : "/" + path;

        return URI.create(rootPath + normalizedPath);
    }

    /**
     * Sends an HTTP request and processes the response to return the desired type.
     *
     * @param <T>          represents the type of the response object to be returned after deserialization.
     * @param request      the HTTP request to be sent
     * @param responseType the class type of the response object to be returned
     * @return the deserialized response of type T
     * @throws InterledgerClientException if an error occurs during the request, response handling,
     *                                    or deserialization process
     */
    public <T> T send(HttpRequest request, Class<T> responseType) throws InterledgerClientException {
        try {
            httpLogger.logRequest(request);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            httpLogger.logResponse(response);

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                handleHttpError(response);
            }

            // deserialize
            return mapper.readValue(response.body(), responseType);

        } catch (IOException e) {
            throw new InterledgerClientException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterledgerClientException(e);
        }
    }

    /**
     * Handles HTTP error responses with different logging and error handling strategies
     * based on status code ranges
     *
     * @param response the HTTP response with error status code
     * @throws InterledgerClientException wrapping the appropriate error information
     */
    private void handleHttpError(HttpResponse<String> response) throws InterledgerClientException, IOException {
        int statusCode = response.statusCode();
        ApiError error = mapper.readError(response.body(), statusCode);

        if (statusCode >= 400 && statusCode < 500) {
            // 4xx - Client errors (bad request, unauthorized, forbidden, not found, etc.)
            LOGGER.warn("Client error [{}]: {}", statusCode, error.description);
        } else if (statusCode >= 500 && statusCode < 600) {
            // 5xx - Server errors (internal server error, bad gateway, service unavailable, etc.)
            LOGGER.error("Server error [{}]: {}", statusCode, error.description);
        } else {
            // Other non-2xx status codes (1xx, 3xx, or unexpected codes)
            LOGGER.warn("Unexpected HTTP status [{}]: {}", statusCode, error.description);
        }

        throw getApiException(error, response);
    }
}
