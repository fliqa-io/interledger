package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AccessInteract {

    @JsonProperty(value = "start", required = true)
    public List<String> start;

    @JsonProperty("finish")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InteractFinish finish;
}
