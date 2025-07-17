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
import java.time.Instant;
import java.util.Set;

/**
 * Represents an incoming payment request on a receiver's wallet.
 * 
 * <p>An incoming payment is created to establish a payment destination with a specific
 * amount that senders can pay to. It serves as the target for outgoing payments and
 * tracks the completion status and received amount.
 * 
 * <p>This is created in step 2 of the payment flow on the receiver's wallet and
 * referenced by senders when creating quotes and executing payments.
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see PaymentPointer
 * @see InterledgerAmount
 */
public class IncomingPayment {

    @JsonProperty(value = "id", required = true)
    public URI id;

    @JsonProperty(value = "completed", required = true)
    public boolean completed;

    @JsonProperty(value = "createdAt", required = true)
    public Instant createdAt;

    @JsonProperty(value = "updatedAt", required = true)
    public Instant updatedAt;

    @JsonProperty(value = "expiresAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Instant expiresAt;

    @JsonProperty(value = "incomingAmount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InterledgerAmount incomingAmount;

    @JsonProperty(value = "methods", required = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Set<InterledgerMethod> methods;

    @JsonProperty(value = "receivedAmount", required = true)
    public InterledgerAmount receivedAmount;

    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;

    @JsonProperty("metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public MetaData metadata;
}
