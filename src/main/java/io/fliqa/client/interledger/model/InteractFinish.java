package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class InteractFinish {

    @JsonProperty(value = "method", required = true)
    public String method;

    @JsonProperty(value = "uri", required = true)
    public URI uri;

    @JsonProperty(value = "nonce", required = true)
    public String nonce;
}
