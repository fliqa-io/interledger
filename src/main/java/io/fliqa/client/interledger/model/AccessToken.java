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
import java.util.Set;

public class AccessToken {

    @JsonProperty(value = "value", required = true)
    public String token;

    @JsonProperty(value = "manage", required = true)
    public URI manage;

    @JsonProperty(value = "expires_in")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer expiresIn;

    @JsonProperty(value = "access", required = true)
    public Set<AccessItem> access;
}
