package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InteractRef {

    @JsonProperty("interact_ref")
    String interactRef;

    public static InteractRef build(String interactRef) {
        InteractRef ref = new InteractRef();
        ref.interactRef = interactRef;
        return ref;
    }
}
