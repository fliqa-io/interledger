package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.time.Instant;

public class Quote {

    @JsonProperty(value = "createdAt", required = true)
    public Instant createdAt;

    @JsonProperty("expiresAt")
    public Instant expiresAt;

    @JsonProperty(value = "debitAmount", required = true)
    public InterledgerAmount debitAmount;

    @JsonProperty(value = "id", required = true)
    public URI id;

    @JsonProperty(value = "method", required = true)
    public String method;

    @JsonProperty(value = "receiveAmount", required = true)
    public InterledgerAmount receiveAmount;

    @JsonProperty(value = "receiver", required = true)
    public URI receiver;

    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;
}
