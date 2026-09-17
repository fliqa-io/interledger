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
import io.fliqa.client.interledger.utils.Assert;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;

/**
 * Represents a request to create an incoming payment on a receiver's wallet.
 *
 * <p>This is the payload sent in step 2 of the payment flow to establish a payment
 * destination with a specific expected amount and expiration.
 *
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see IncomingPayment
 */
public class PaymentRequest {

    /**
     * Creates a new, empty {@code PaymentRequest} instance.
     */
    public PaymentRequest() {
    }

    /**
     * The wallet address on which the incoming payment will be created.
     */
    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;

    /**
     * The expected amount to be received for this payment.
     */
    @JsonProperty(value = "incomingAmount", required = true)
    public InterledgerAmount incomingAmount;

    /**
     * The timestamp after which this incoming payment will no longer accept funds.
     */
    @JsonProperty("expiresAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Instant expiresAt;

    /**
     * Optional metadata associated with this payment.
     */
    @JsonProperty("metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public MetaData metadata;

    /**
     * Builds a {@code PaymentRequest} for the given receiver, amount, and expiration.
     *
     * @param receiver         the wallet that will receive the payment
     * @param amount           the payment amount, must not be negative
     * @param expiresInSeconds the number of seconds until the payment expires, must be greater than zero
     * @return a new {@code PaymentRequest} instance
     * @throws IllegalArgumentException if receiver is null, amount is negative, or expiresInSeconds is not positive
     */
    public static PaymentRequest build(PaymentPointer receiver, BigDecimal amount, int expiresInSeconds) {

        Assert.notNull(receiver, "receiver cannot be null.");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must be greater than zero.");
        }

        if (expiresInSeconds <= 0) {
            throw new IllegalArgumentException("expiresInSeconds must be greater than zero.");
        }

        PaymentRequest request = new PaymentRequest();
        request.walletAddress = receiver.address;
        request.incomingAmount = InterledgerAmount.build(amount, receiver.assetCode, receiver.assetScale);

        request.expiresAt = Instant.now().plusSeconds(expiresInSeconds);
        return request;
    }
}
