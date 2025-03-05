package io.fliqa.client.interledger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fliqa.client.interledger.model.InterledgerAmount;
import io.fliqa.client.interledger.model.MetaData;

import java.net.URI;

public class OutgoingPaymentRequest {

    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;

    @JsonProperty("quoteId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public URI quoteId;

    @JsonProperty("metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public MetaData metadata;

    @JsonProperty("incomingPayment")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public URI incomingPayment;

    @JsonProperty("debitAmount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InterledgerAmount debitAmount;
}
