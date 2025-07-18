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

/**
 * Represents an Interledger payment method for establishing STREAM connections.
 * 
 * <p>This class defines the ILP (Interledger Protocol) payment method used for
 * making payments through the Interledger network. It contains the necessary
 * information to establish a secure STREAM connection between the sender and
 * receiver for transferring value.
 * 
 * <p>The payment method includes the ILP address for routing payments and a
 * shared secret for encrypting and authenticating the payment stream.
 * 
 * @see <a href="https://interledger.org/rfcs/0029-stream/">STREAM Protocol Specification</a>
 * @see <a href="https://openpayments.guide/">Interledger Open Payments Guide</a>
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 */
public class InterledgerMethod {

    /**
     * The type of payment method.
     * 
     * <p>For Interledger payments, this is always "ilp" indicating the
     * use of the Interledger Protocol for payment delivery.
     * 
     * @see <a href="https://openpayments.guide/">Open Payments specification</a>
     */
    @JsonProperty(value = "type", required = true)
    public String type;

    /**
     * The ILP address to use when establishing a STREAM connection.
     * 
     * <p>This is a hierarchical address that follows the ILP address format
     * (e.g., "g.ilp.example.alice"). The address is used to route payments
     * through the Interledger network to the intended recipient.
     * 
     * <p>Valid ILP addresses must:
     * <ul>
     *   <li>Start with a scheme: g, private, example, peer, self, test[1-3], or local</li>
     *   <li>Be followed by segments separated by dots</li>
     *   <li>Contain only alphanumeric characters, underscores, tildes, and hyphens</li>
     *   <li>Be at most 1023 characters long</li>
     * </ul>
     * 
     * @see <a href="https://interledger.org/rfcs/0015-ilp-addresses/">ILP Address Format</a>
     */
    @JsonProperty(value = "ilpAddress", required = true)
    public String ilpAddress;

    /**
     * The shared secret used for STREAM connection encryption and authentication.
     * 
     * <p>This is a base64url-encoded secret that is shared between the sender and
     * receiver to establish a secure STREAM connection. The shared secret is used
     * to encrypt packet contents and authenticate the connection.
     * 
     * <p>The shared secret is typically 32 bytes (256 bits) of cryptographically
     * secure random data, encoded as a base64url string without padding.
     * 
     * @see <a href="https://interledger.org/rfcs/0029-stream/">STREAM Protocol Security</a>
     */
    @JsonProperty(value = "sharedSecret", required = true)
    public String sharedSecret;
}
