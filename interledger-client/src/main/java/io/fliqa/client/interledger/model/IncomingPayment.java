package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

public class IncomingPayment {

    @JsonProperty(value = "completed", required = true)
    public boolean completed;

    @JsonProperty(value = "createdAt", required = true)
    public Instant createdAt;

    @JsonProperty(value = "updatedAt", required = true)
    public Instant updatedAt;

    @JsonProperty(value = "expiresAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Instant expiresAt;

    @JsonProperty(value = "id", required = true)
    public String id;

    @JsonProperty(value = "incomingAmount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InterledgerAmount incomingAmount;

    @JsonProperty(value = "methods", required = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Set<InterledgerMethod> methods;

    @JsonProperty(value = "receivedAmount", required = true)
    public InterledgerAmount receivedAmount;

    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;

    @JsonProperty("metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public MetaData metadata;
}
