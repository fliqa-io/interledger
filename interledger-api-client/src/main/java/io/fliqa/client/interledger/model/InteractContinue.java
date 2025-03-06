package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class InteractContinue {

    @JsonProperty(value = "finish", required = true)
    public String token;

    @JsonProperty(value = "redirect", required = true)
    public URI redirect;
}
