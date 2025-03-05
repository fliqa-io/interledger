package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InterledgerMethod {

    @JsonProperty(value = "type", required = true)
    public String type;

    @JsonProperty(value = "ilpAddress", required = true)
    public String ilpAddress;

    @JsonProperty(value = "sharedSecret", required = true)
    public String sharedSecret;
}
