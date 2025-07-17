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
     * ISO8601 repeating interval
     * (TODO: not used at the moment, to be properly implemented if repeating transactions are used)
     */
    @JsonProperty("interval")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String interval;
}
