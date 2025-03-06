package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OutgoingPayment {

    @JsonProperty(value = "continue", required = true)
    public AccessContinue paymentContinue;

    // NOTE: one property must be given either interact or access_token
    @JsonProperty(value = "interact")
    public InteractContinue interact;

    @JsonProperty(value = "access_token")
    public String token;
}
