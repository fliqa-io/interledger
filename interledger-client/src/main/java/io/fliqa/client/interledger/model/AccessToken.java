package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Set;

public class AccessToken {

    @JsonProperty(value = "value", required = true)
    public String value;

    @JsonProperty(value = "manage", required = true)
    public URI manage;

    @JsonProperty(value = "expires_in")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer expiresIn;

    @JsonProperty(value = "access", required = true)
    public Set<AccessItem> access;
}
