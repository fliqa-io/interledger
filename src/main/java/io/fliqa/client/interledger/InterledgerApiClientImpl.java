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

public class InterledgerApiClientImpl implements InterledgerApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterledgerApiClientImpl.class);
    private static final String ILP_METHOD = "ilp";

    private final WalletAddress clientWallet;
    private final PrivateKey privateKey;
    private final String keyId;

    private final HttpClient client;
    private final InterledgerClientOptions options;
    private final InterledgerObjectMapper mapper = new InterledgerObjectMapper();
    private final HttpLogger httpLogger;

    public InterledgerApiClientImpl(WalletAddress clientWallet,
                                    PrivateKey privateKey,
                                    String keyId,
                                    InterledgerClientOptions options) {

        Assert.notNull(clientWallet, "clientWallet cannot be null");
        Assert.notNull(privateKey, "privateKey cannot be null");
        Assert.notNullOrEmpty(keyId, "keyId cannot be null or empty");
        Assert.notNull(options, "client options cannot be null");
        
        this.clientWallet = clientWallet;
        this.privateKey = privateKey;
        this.keyId = keyId;
        this.options = options;

        client = createDefaultHttpClient(options);
        httpLogger = new HttpLogger(LOGGER);
    }

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

        var request = HttpRequest.newBuilder(address.paymentPointer)
                .GET()
                .header(ACCEPT_HEADER.toLowerCase(), APPLICATION_JSON)
                .timeout(Duration.of(options.timeOutInSeconds, SECONDS))
                .build();

        return send(request, PaymentPointer.class);
    }

    @Override
    public AccessGrant createPendingGrant(PaymentPointer receiver) throws InterledgerClientException {

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

        PaymentRequest paymentRequest = PaymentRequest.build(receiver, amount, options.transactionExpirationInSeconds);

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(paymentRequest)
                .target(receiver.resourceServer + "/incoming-payments")
                .accessToken(pendingGrant.access.token)
                .getRequest(options);

        return send(request, IncomingPayment.class);
    }

    @Override
    public AccessGrant createQuoteRequest(PaymentPointer sender) throws InterledgerClientException {

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

        QuoteRequest quoteRequest = QuoteRequest.build(sender.address,
                incomingPayment.id.toString(),
                ILP_METHOD);

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(quoteRequest)
                .target(sender.resourceServer + "/quotes")
                .accessToken(quoteToken)
                .getRequest(options);

        return send(request, Quote.class);
    }

    @Override
    public OutgoingPayment continueGrant(PaymentPointer sender, Quote quote, URI returnUrl, String nonce) throws InterledgerClientException {

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

        InteractRef ref = InteractRef.build(interactRef);

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(ref)
                .target(outgoingPayment.paymentContinue.uri)
                .accessToken(outgoingPayment.paymentContinue.access.token)
                .getRequest(options);

        return send(request, AccessGrant.class);
    }

    @Override
    public Payment finalizePayment(AccessGrant finalizedGrant, PaymentPointer senderWallet, Quote quote) throws InterledgerClientException {

        OutgoingPaymentRequest outgoingPayment = new OutgoingPaymentRequest();
        outgoingPayment.quoteId = quote.id;
        outgoingPayment.walletAddress = senderWallet.address;

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(outgoingPayment)
                .target(senderWallet.resourceServer + "/outgoing-payments")
                .accessToken(finalizedGrant.access.token)
                .getRequest(options);

        return send(request, Payment.class);
    }

    @Override
    public IncomingPayment getIncomingPayment(IncomingPayment payment, AccessGrant grant) throws InterledgerClientException {

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .GET()
                .target(payment.id)
                .accessToken(grant.access.token)
                .getRequest(options);

        return send(request, IncomingPayment.class);
    }

    public <T> T send(HttpRequest request, Class<T> responseType) throws InterledgerClientException {
        try {
            httpLogger.logRequest(request);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            httpLogger.logResponse(response);

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                // deserialize error and throw exception
                ApiError error = mapper.readError(response.body(), response.statusCode());
                throw getApiException(error, response);
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
}
