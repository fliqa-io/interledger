/*
 * Copyright 2025 Fliqa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
