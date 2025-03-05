package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class Limits {

    @JsonProperty("receiver")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public URI receiver;

    @JsonProperty("receiveAmount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InterledgerAmount receiveAmount;

    @JsonProperty("debitAmount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InterledgerAmount debitAmount;

    /**
     * ISO8601 repeating interval (TODO: to be properly implemented if repeating transactions are used)
     */
    @JsonProperty("interval")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String interval;
}
