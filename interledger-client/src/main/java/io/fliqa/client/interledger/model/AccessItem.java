package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fliqa.client.interledger.serializer.OrderedSetSerializer;

import java.util.Set;

public class AccessItem {

    @JsonProperty("type")
    public AccessItemType accessType;

    @JsonProperty("actions")
    @JsonSerialize(using = OrderedSetSerializer.class)
    public Set<AccessAction> actions;
}
