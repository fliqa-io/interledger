package io.fliqa.client.interledger;

import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.*;
import io.fliqa.client.interledger.signature.SignatureRequestBuilder;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.Set;
import java.util.logging.Logger;

import static java.time.temporal.ChronoUnit.SECONDS;

public class InterledgerApiClientImpl implements InterledgerApiClient {

    private final Logger log = Logger.getLogger(InterledgerApiClientImpl.class.getName());

    private final WalletAddress clientWallet;
    private final PrivateKey privateKey;
    private final String keyId;
    private final InterledgerClientOptions options;
    private final InterledgerObjectMapper mapper = new InterledgerObjectMapper();

    private final HttpClient client;

    public InterledgerApiClientImpl(WalletAddress clientWallet,
                                    PrivateKey privateKey,
                                    String keyId,
                                    InterledgerClientOptions options) {

        this.clientWallet = clientWallet;
        this.privateKey = privateKey;
        this.keyId = keyId;
        this.options = options;

        client = createDefaultHttpClient(options);
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

        PaymentRequest paymentRequest = PaymentRequest.build(receiver, amount);

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
                incomingPayment.id,
                "ilp"); // not sure why "ilp" is needed

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(quoteRequest)
                .target(sender.resourceServer + "/quotes")
                .accessToken(quoteToken)
                .getRequest(options);

        return send(request, Quote.class);
    }

    @Override
    public OutgoingPayment continueGrant(PaymentPointer sender, Quote quote) throws InterledgerClientException {

        GrantAccessRequest accessRequest = GrantAccessRequest.outgoing(clientWallet,
                        AccessItemType.outgoingPayment,
                        Set.of(AccessAction.read, AccessAction.create),
                        sender.address, quote.debitAmount)
                .redirectInteract();

        log.info(String.format("POST: %s", mapper.writeValueAsString(accessRequest)));

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(accessRequest)
                .target(sender.authServer)
                .getRequest(options);

        return send(request, OutgoingPayment.class);
    }

    @Override
    public AccessGrant finalizeGrant(OutgoingPayment outgoingPayment) throws InterledgerClientException {

        log.info(String.format("POST: %s", mapper.writeValueAsString(outgoingPayment)));
        log.info(String.format("token: %s", outgoingPayment.paymentContinue.access.token));

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST()
                .target(outgoingPayment.paymentContinue.uri)
                .accessToken(outgoingPayment.paymentContinue.access.token)
                .getRequest(options);
        return send(request, AccessGrant.class);
    }

    @Override
    public FinalizedPayment finalizePayment(AccessGrant finalizedGrant, PaymentPointer senderWallet, Quote quote) throws InterledgerClientException {

        OutgoingPaymentRequest outgoingPayment = new OutgoingPaymentRequest();
        outgoingPayment.quoteId = quote.id;
        outgoingPayment.walletAddress = senderWallet.address;

        log.info(String.format("POST: %s", mapper.writeValueAsString(outgoingPayment)));
        log.info(String.format("token: %s", finalizedGrant.access.token));

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(outgoingPayment)
                .target(senderWallet.resourceServer + "/outgoing-payments")
                .accessToken(finalizedGrant.access.token)
                .getRequest(options);

        log.info(String.format("Outgoing payment request: %s", mapper.writeValueAsString(outgoingPayment)));

        return send(request, FinalizedPayment.class);
    }

    public <T> T send(HttpRequest request, Class<T> responseType) throws InterledgerClientException {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info(String.format("[%d]: %s", response.statusCode(), response.body()));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new InterledgerClientException(response.body());
            }

            return mapper.readValue(response.body(), responseType);
            // TODO: check response code before deserialization
            // in case of ERROR log and deserialize error and throw appropriate exception
        } catch (Exception e) {
            log.warning(String.format("Request failed: %s.", e));
            throw new InterledgerClientException(e.getMessage(), e);
        }
    }
}
