package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.HashSet;

public class PaymentRequest {

    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;

    @JsonProperty(value = "incomingAmount", required = true)
    public InterledgerAmount incomingAmount;

    @JsonProperty("expiresAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Instant expiresAt;

    @JsonProperty("metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public MetaData metadata;

    public static PaymentRequest build(PaymentPointer receiver, BigDecimal amount, int expiresInSeconds) {

        if (receiver == null) {
            throw new NullPointerException("Missing receiver address.");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        if (expiresInSeconds <= 0) {
            throw new IllegalArgumentException("expiresInSeconds must be greater than 0.");
        }

        PaymentRequest request = new PaymentRequest();
        request.walletAddress = receiver.address;
        request.incomingAmount = InterledgerAmount.build(amount, receiver.assetCode, receiver.assetScale);

        request.expiresAt = Instant.now().plusSeconds(expiresInSeconds);

        // TODO: testing remove later
        MetaData meta = new MetaData();
        meta.externalId = "external_reference_id";
        meta.value = new HashSet<>();

        MetaDataItem item = new MetaDataItem();
        item.key = "key";
        item.value = "value";
        meta.value.add(item);

        request.metadata = meta;
        // TODO: end

        return request;
    }
}
