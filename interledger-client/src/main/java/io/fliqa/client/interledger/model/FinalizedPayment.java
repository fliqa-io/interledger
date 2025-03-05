package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.time.Instant;

public class FinalizedPayment {

    @JsonProperty(value = "id", required = true)
    public URI id;

    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;

    @JsonProperty(value = "quoteId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public URI quoteId;

    @JsonProperty("failed")
    public boolean failed;

    @JsonProperty(value = "receiver", required = true)
    public URI receiver;

    @JsonProperty(value = "receiveAmount", required = true)
    public InterledgerAmount receivedAmount;

    @JsonProperty(value = "debitAmount", required = true)
    public InterledgerAmount debitAmount;

    @JsonProperty(value = "sentAmount", required = true)
    public InterledgerAmount sentAmount;

    @JsonProperty(value = "grantSpentDebitAmount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InterledgerAmount grantSpentDebitAmount;

    @JsonProperty(value = "grantSpentReceiveAmount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InterledgerAmount grantSpentReceiveAmount;

    @JsonProperty(value = "createdAt", required = true)
    public Instant createdAt;

    @JsonProperty(value = "updatedAt", required = true)
    public Instant updatedAt;

    @JsonProperty("metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public MetaData metadata;
}
