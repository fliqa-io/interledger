package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.*;
import io.fliqa.interledger.client.model.*;

import java.util.*;

public class AccessToken {

    @JsonProperty("access")
    public Set<AccessItem> access = new LinkedHashSet<>();
}
