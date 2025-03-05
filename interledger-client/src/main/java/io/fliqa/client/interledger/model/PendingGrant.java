package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PendingGrant {

    @JsonProperty("access_token")
    public AccessToken accessToken;

    @JsonProperty("continue")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AccessContinue accessContinue;
}
