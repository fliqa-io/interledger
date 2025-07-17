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

/**
 * Represents a request to create an outgoing payment.
 * 
 * <p>This class encapsulates the data required to initiate an outgoing payment
 * from a sender's wallet to a receiver's incoming payment. It contains references
 * to the quote, wallet address, and payment destination.
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see OutgoingPayment
 * @see InterledgerAmount
 */
public class OutgoingPaymentRequest {

    /**
     * The wallet address from which the payment will be sent.
     */
    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;

    /**
     * Reference to the quote that determines payment amounts and fees.
     */
    @JsonProperty("quoteId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public URI quoteId;

    /**
     * Additional metadata for the payment.
     */
    @JsonProperty("metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public MetaData metadata;

    /**
     * Reference to the incoming payment that will receive the funds.
     */
    @JsonProperty("incomingPayment")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public URI incomingPayment;

    /**
     * The amount to be debited from the sender's wallet.
     */
    @JsonProperty("debitAmount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public InterledgerAmount debitAmount;
}