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

public class PaymentPointer {

    @JsonProperty(value = "id", required = true)
    public URI address; // "https://ilp.interledger-test.dev/andrejfliqatestwallet",

    @JsonProperty(value = "publicName", required = true)
    public String publicName;

    @JsonProperty(value = "assetCode", required = true)
    public String assetCode;

    @JsonProperty(value = "assetScale", required = true)
    public int assetScale;

    @JsonProperty(value = "authServer", required = true)
    public URI authServer;

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
