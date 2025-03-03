package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.*;

import java.net.*;

public class PaymentPointer {

    @JsonProperty("id")
    public URI id; // "https://ilp.interledger-test.dev/andrejfliqatestwallet",

    @JsonProperty("publicName")
    public String publicName;

    @JsonProperty("assetCode")
    public String assetCode;

    @JsonProperty("assetScale")
    public int assetScale;

    @JsonProperty("authServer")
    public URI authServer;

    @JsonProperty("resourceServer")
    public URI resourceServer;

    public PaymentPointer() {

    }
}
