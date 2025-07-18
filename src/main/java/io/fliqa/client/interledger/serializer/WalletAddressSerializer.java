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
package io.fliqa.client.interledger.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.fliqa.client.interledger.model.WalletAddress;

import java.io.IOException;

/**
 * A custom serializer for the {@link WalletAddress} class.
 * <p>
 * This serializer is used to serialize a {@link WalletAddress} object into its
 * JSON representation. Specifically, it writes the string value of the wallet's
 * payment pointer URI to the JSON output. The serialization process ensures
 * that only the payment pointer is included as a string in the resulting JSON.
 * <p>
 * This serializer is useful for applications that need to interact with
 * JSON-based APIs where wallet addresses are exchanged in a lightweight
 * string format instead of a structured object.
 * <p>
 * This class extends {@link JsonSerializer} and overrides the {@code serialize}
 * method to define the custom serialization logic for {@link WalletAddress}.
 *
 * @see WalletAddress
 * @see JsonSerializer
 */
public class WalletAddressSerializer extends JsonSerializer<WalletAddress> {

    @Override
    public void serialize(WalletAddress address, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(address.paymentPointer.toString());
    }
}
