package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fliqa.interledger.client.model.ContinueAccessToken;

import java.net.URI;

public class AccessContinue {

    @JsonProperty(value = "access_token", required = true)
    public ContinueAccessToken accessToken;

    @JsonProperty(value = "uri", required = true)
    public URI uri;

    @JsonProperty(value = "wait")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer wait;
}
