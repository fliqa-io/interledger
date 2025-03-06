package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class MetaData {

    @JsonProperty(value = "externalId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String externalId;

    @JsonProperty(value = "value")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Set<MetaDataItem> value;
}
