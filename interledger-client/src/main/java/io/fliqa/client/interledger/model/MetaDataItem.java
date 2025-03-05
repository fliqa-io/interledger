package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MetaDataItem {

    @JsonProperty(value = "key", required = true)
    public String key;

    @JsonProperty(value = "value")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String value;
}
