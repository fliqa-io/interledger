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

public class OutgoingPayment {

    @JsonProperty(value = "continue", required = true)
    public AccessContinue paymentContinue;

    // NOTE: one property must be given either interact or access_token
    @JsonProperty(value = "interact")
    public InteractContinue interact;

    @JsonProperty(value = "access_token")
    public String token;
}
