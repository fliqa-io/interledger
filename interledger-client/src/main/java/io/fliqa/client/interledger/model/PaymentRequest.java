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

    public static PaymentRequest build(PaymentPointer receiver, BigDecimal amount) {
        PaymentRequest request = new PaymentRequest();
        request.walletAddress = receiver.id;
        request.incomingAmount = InterledgerAmount.build(amount, receiver.assetCode, receiver.assetScale);

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
