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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fliqa.client.interledger.serializer.WalletAddressSerializer;

import java.net.URI;

/**
 * Represents a wallet address for Interledger Open Payments operations.
 * 
 * <p>A wallet address is a URL that identifies a specific wallet or account
 * within the Interledger network. It serves as the primary identifier for
 * sending and receiving payments through the Open Payments protocol.
 * 
 * <p>Wallet addresses are typically hosted by Rafiki instances and follow
 * the URI format. They are used to discover wallet capabilities, create
 * payment resources, and identify parties in payment transactions.
 * 
 * <p>Example wallet addresses:
 * <ul>
 *   <li>"https://ilp.interledger-test.dev/alice"</li>
 *   <li>"https://ilp.interledger-test.dev/bob"</li>
 *   <li>"https://wallet.example.com/user123"</li>
 * </ul>
 * 
 * <p>This class is immutable and thread-safe. The wallet address is validated
 * during construction to ensure it's a valid URI.
 * 
 * @see PaymentPointer
 * @see WalletAddressSerializer
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 */
@JsonSerialize(using = WalletAddressSerializer.class)
public class WalletAddress {

    /**
     * The URI representing the wallet address.
     * 
     * <p>This is the actual wallet address URI that identifies the wallet
     * within the Interledger network. It's used for payment operations,
     * resource discovery, and wallet identification.
     * 
     * <p>The URI must be valid and typically follows the HTTPS scheme
     * for security purposes.
     */
    public final URI paymentPointer;

    /**
     * Constructs a new WalletAddress from a string URI.
     * 
     * <p>This constructor parses the provided address string into a URI
     * and validates that it's not null. The string should be a valid
     * URI format.
     * 
     * @param address the wallet address as a string (e.g., "https://ilp.interledger-test.dev/alice")
     * @throws IllegalArgumentException if the address is null
     * @throws IllegalArgumentException if the address is not a valid URI format
     */
    public WalletAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null.");
        }
        this.paymentPointer = URI.create(address);
    }

    /**
     * Constructs a new WalletAddress from a URI object.
     * 
     * <p>This constructor accepts a pre-parsed URI object and validates
     * that it's not null. This is useful when you already have a URI
     * object from other operations.
     * 
     * @param address the wallet address as a URI object
     * @throws IllegalArgumentException if the address is null
     */
    public WalletAddress(URI address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null.");
        }
        this.paymentPointer = address;
    }

    @Override
    public String toString() {
        return "WalletAddress{" +
                "paymentPointer=" + paymentPointer +
                '}';
    }
}
