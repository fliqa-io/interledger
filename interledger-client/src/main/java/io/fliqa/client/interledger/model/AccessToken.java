package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class AccessToken {

    @JsonProperty("access")
    public Set<AccessItem> access;
}
