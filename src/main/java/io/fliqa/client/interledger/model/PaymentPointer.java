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

/**
 * Represents a payment pointer and its associated wallet information.
 * 
 * <p>A payment pointer is an Interledger Open Payments identifier that resolves to
 * wallet information including supported assets, authorization endpoints, and
 * resource server URLs. This class contains the resolved wallet metadata retrieved
 * from the payment pointer's well-known endpoint.
 * 
 * <h2>Asset Information</h2>
 * <p>The asset code and scale define the currency and precision supported by this wallet:
 * <ul>
 *   <li><strong>Asset Code</strong> - Currency code (e.g., "EUR", "USD")</li>
 *   <li><strong>Asset Scale</strong> - Decimal precision (e.g., 2 for cents, 3 for mills)</li>
 * </ul>
 * 
 * <h3>Server Endpoints</h3>
 * <ul>
 *   <li><strong>Auth Server</strong> - Authorization server for obtaining grants and tokens</li>
 *   <li><strong>Resource Server</strong> - API server for payments, quotes, and wallet operations</li>
 * </ul>
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see WalletAddress
 */
public class PaymentPointer {

    /**
     * The unique identifier URL for this payment pointer.
     * This is the canonical address that can be used to reference this wallet.
     */
    @JsonProperty(value = "id", required = true)
    public URI address;

    /**
     * The human-readable public name for this wallet.
     * This name may be displayed to users during payment flows.
     */
    @JsonProperty(value = "publicName", required = true)
    public String publicName;

    /**
     * The currency or asset code supported by this wallet (e.g., "EUR", "USD").
     */
    @JsonProperty(value = "assetCode", required = true)
    public String assetCode;

    /**
     * The number of decimal places for the asset.
     * For example, 2 for currencies like EUR (cents), 3 for mills.
     */
    @JsonProperty(value = "assetScale", required = true)
    public int assetScale;

    /**
     * The authorization server URL for obtaining grants and access tokens.
     * Used in the OAuth-like flow for payment authorization.
     */
    @JsonProperty(value = "authServer", required = true)
    public URI authServer;

    /**
     * The resource server URL for payment operations.
     * This is where payment requests, quotes, and wallet operations are performed.
     */
    @JsonProperty(value = "resourceServer", required = true)
    public URI resourceServer;

    @Override
    public String toString() {
        return "PaymentPointer{" +
                "address=" + address +
                ", publicName='" + publicName + '\'' +
                ", assetCode='" + assetCode + '\'' +
                ", assetScale=" + assetScale +
                ", authServer=" + authServer +
                ", resourceServer=" + resourceServer +
                '}';
    }
}
