package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fliqa.client.interledger.serializer.OrderedSetSerializer;

import java.net.URI;
import java.util.Set;

public class AccessItem {

    @JsonProperty(value = "type", required = true)
    public AccessItemType accessType;

    @JsonProperty(value = "actions", required = true)
    @JsonSerialize(using = OrderedSetSerializer.class)
    public Set<AccessAction> actions;

    @JsonProperty("identifier")
    @JsonInclude(JsonInclude.Include.NON_NULL)  // is only required on accessType access-outgoing
    public URI identifier;

    @JsonProperty("limits")
    public Limits limits;

    public AccessItem accessOutgoing(URI identifier, InterledgerAmount debitAmount) {
        this.identifier = identifier;
        limits = new Limits();
        limits.debitAmount = debitAmount;
        return this;
    }

}
