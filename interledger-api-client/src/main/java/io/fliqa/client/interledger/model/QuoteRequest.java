package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class QuoteRequest {

    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;

    @JsonProperty(value = "receiver", required = true)
    public String receiver;

    @JsonProperty(value = "method", required = true)
    public String method;

    public static QuoteRequest build(URI walletAddress, String receiver, String method) {
        QuoteRequest quoteRequest = new QuoteRequest();
        quoteRequest.walletAddress = walletAddress;
        quoteRequest.receiver = receiver;
        quoteRequest.method = method;
        return quoteRequest;
    }
}
