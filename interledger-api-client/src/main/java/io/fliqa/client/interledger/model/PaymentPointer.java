package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class PaymentPointer {

    @JsonProperty(value = "id", required = true)
    public URI address; // "https://ilp.interledger-test.dev/andrejfliqatestwallet",

    @JsonProperty(value = "publicName", required = true)
    public String publicName;

    @JsonProperty(value = "assetCode", required = true)
    public String assetCode;

    @JsonProperty(value = "assetScale", required = true)
    public int assetScale;

    @JsonProperty(value = "authServer", required = true)
    public URI authServer;

    @JsonProperty(value = "resourceServer", required = true)
    public URI resourceServer;
}
