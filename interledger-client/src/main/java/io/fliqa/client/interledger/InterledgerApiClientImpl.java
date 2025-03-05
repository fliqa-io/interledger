package io.fliqa.client.interledger;

import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.*;
import io.fliqa.client.interledger.signature.SignatureBuilder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.Map;
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

        SignatureBuilder builder = new SignatureBuilder(privateKey, keyId, mapper);
        builder.method("POST")
                .target(receiver.authServer)
                .json(grantRequest)
                .build();

        var request = HttpRequest.newBuilder(receiver.authServer);

        for (Map.Entry<String, String> entry : builder.getHeaders().entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }

        request.POST(HttpRequest.BodyPublishers.ofString(builder.getBody(), StandardCharsets.UTF_8)) // Attach JSON body
                .timeout(Duration.of(options.timeOutInSeconds, SECONDS));

        try {
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response.body(), PendingGrant.class);
            // TODO: check response code before deserialization
        } catch (Exception e) {
            log.warning(String.format("Failed create pending grant: %s", e));
            throw new InterledgerClientException(e.getMessage(), e);
        }
    }
}
