package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("error")
public class ApiError {

    /**
     * Error description
     */
    @JsonProperty("description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String description;

    /**
     * Unique error code
     */
    @JsonProperty(value = "code")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String code;
}