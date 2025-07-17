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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.time.Instant;

public class Quote {

    @JsonProperty(value = "createdAt", required = true)
    public Instant createdAt;

    @JsonProperty("expiresAt")
    public Instant expiresAt;

    @JsonProperty(value = "debitAmount", required = true)
    public InterledgerAmount debitAmount;

    @JsonProperty(value = "id", required = true)
    public URI id;

    @JsonProperty(value = "method", required = true)
    public String method;

    @JsonProperty(value = "receiveAmount", required = true)
    public InterledgerAmount receiveAmount;

    @JsonProperty(value = "receiver", required = true)
    public URI receiver;

    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;

    @Override
    public String toString() {
        return "Quote{" +
                "createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", debitAmount=" + debitAmount +
                ", id=" + id +
                ", method='" + method + '\'' +
                ", receiveAmount=" + receiveAmount +
                ", receiver=" + receiver +
                ", walletAddress=" + walletAddress +
                '}';
    }
}
