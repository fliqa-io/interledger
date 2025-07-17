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

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.HashSet;

public class PaymentRequest {

    @JsonProperty(value = "walletAddress", required = true)
    public URI walletAddress;

    @JsonProperty(value = "incomingAmount", required = true)
    public InterledgerAmount incomingAmount;

    @JsonProperty("expiresAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Instant expiresAt;

    @JsonProperty("metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public MetaData metadata;

    public static PaymentRequest build(PaymentPointer receiver, BigDecimal amount, int expiresInSeconds) {

        if (receiver == null) {
            throw new NullPointerException("Missing receiver address.");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        if (expiresInSeconds <= 0) {
            throw new IllegalArgumentException("expiresInSeconds must be greater than 0.");
        }

        PaymentRequest request = new PaymentRequest();
        request.walletAddress = receiver.address;
        request.incomingAmount = InterledgerAmount.build(amount, receiver.assetCode, receiver.assetScale);

        request.expiresAt = Instant.now().plusSeconds(expiresInSeconds);

        // TODO: testing remove later
        MetaData meta = new MetaData();
        meta.externalId = "external_reference_id";
        meta.value = new HashSet<>();

        MetaDataItem item = new MetaDataItem();
        item.key = "key";
        item.value = "value";
        meta.value.add(item);

        request.metadata = meta;
        // TODO: end

        return request;
    }
}
