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

    /*public static Consumer<HttpRequest.Builder> signRequest(String token) {
        return builder -> {
            builder.header("Authorization", "Bearer " + token);
            builder.header("X-Custom-Header", "CustomValue");
        };
    }*/

    @Override
    public PaymentPointer getWallet(WalletAddress address) throws InterledgerClientException {

        var request = HttpRequest.newBuilder(address.paymentPointer)
                .GET()
                .timeout(Duration.of(options.timeOutInSeconds, SECONDS))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // TODO also check response code and return appropriate exception
            return mapper.readValue(response.body(), PaymentPointer.class);
        } catch (Exception e) {
            log.warning(String.format("Failed to read wallet: %s", address.paymentPointer));
            throw new InterledgerClientException(e.getMessage(), e);
        }
    }

    @Override
    public PendingGrant createPendingGrant(PaymentPointer receiver) throws InterledgerClientException {

        GrantRequest grantRequest = GrantRequest.build(clientWallet,
                AccessItemType.incomingPayment,
                Set.of(AccessAction.read, AccessAction.complete, AccessAction.create));

        HttpRequest request = new SignatureRequestBuilder(privateKey, keyId, mapper)
                .method("POST")
                .target(receiver.authServer)
                .json(grantRequest)
                .build()
                .getRequest(options);

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response.body(), PendingGrant.class);
            // TODO: check response code before deserialization
        } catch (Exception e) {
            log.warning(String.format("Failed create pending grant: %s", e));
            throw new InterledgerClientException(e.getMessage(), e);
        }
    }

    @Override
    public IncomingPayment createIncomingPayment(String grantToken, PaymentPointer receiver, BigDecimal amount) throws InterledgerClientException {
        return null;
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
}
