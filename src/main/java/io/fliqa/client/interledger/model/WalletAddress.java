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

@JsonSerialize(using = WalletAddressSerializer.class)
public class WalletAddress {

    public final URI paymentPointer;

    public WalletAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null.");
        }
        this.paymentPointer = URI.create(address);
    }

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
