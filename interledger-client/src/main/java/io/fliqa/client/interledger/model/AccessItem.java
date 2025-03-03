package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

public class AccessItem {

    @JsonProperty("type")
    public AccessItemType accessType;

    @JsonProperty("actions")
    public Set<AccessAction> actions;
}
