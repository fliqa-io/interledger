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
    public PendingGrant createPendingGrant(PaymentPointer receiver) throws InterledgerClientException {

        GrantRequest grantRequest = GrantRequest.build(clientWallet,
                AccessItemType.incomingPayment,
                Set.of(AccessAction.read, AccessAction.complete, AccessAction.create));

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(grantRequest)
                .target(receiver.authServer)
                .getRequest(options);

        return send(request, PendingGrant.class);
    }

    @Override
    public IncomingPayment createIncomingPayment(PaymentPointer receiver, PendingGrant pendingGrant, BigDecimal amount) throws InterledgerClientException {

        PaymentRequest paymentRequest = PaymentRequest.build(receiver, amount);

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .POST(paymentRequest)
                .target(receiver.resourceServer + "/incoming-payments")
                .accessToken(pendingGrant.accessToken.value)
                .getRequest(options);

        return send(request, IncomingPayment.class);
    }

    @Override
    public PendingQuote createQuoteRequest(PaymentPointer sender, BigDecimal amount) throws InterledgerClientException {
        return null;
    }

    @Override
    public Quote createQuote(String quoteToken, PaymentPointer sender, IncomingPayment incomingPayment) throws InterledgerClientException {
        return null;
    }

    @Override
    public OutgoingPayment continueGrant(PaymentPointer sender, Quote quote) throws InterledgerClientException {
        return null;
    }

    @Override
    public FinalizedPayment finalizePayment(OutgoingPayment outgoingPayment) throws InterledgerClientException {
        return null;
    }

    public <T> T send(HttpRequest request, Class<T> responseType) throws InterledgerClientException {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info(String.format("Response: %s", response.body()));
            return mapper.readValue(response.body(), responseType);
            // TODO: check response code before deserialization
        } catch (Exception e) {
            log.warning(String.format("Failed create pending grant: %s.", e));
            throw new InterledgerClientException(e.getMessage(), e);
        }
    }
}
