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
 * Represents the response from creating an outgoing payment request.
 * 
 * <p>This class contains the information returned by the authorization server
 * when a client requests permission to create an outgoing payment. It includes
 * continuation information for the access grant process and either interaction
 * details or an access token, depending on the authorization flow.
 * 
 * <p>The outgoing payment response is used in the Interledger Open Payments
 * protocol to manage the authorization flow for sending payments. It provides
 * the necessary information for the client to complete the payment authorization
 * and proceed with the actual payment execution.
 * 
 * <p>The response will contain either:
 * <ul>
 *   <li>Interaction information if user consent is required</li>
 *   <li>An access token if authorization is granted immediately</li>
 * </ul>
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see AccessContinue
 * @see InteractContinue
 * @see OutgoingPaymentRequest
 */
public class OutgoingPayment {

    /**
     * The continuation information for the access grant process.
     * 
     * <p>This field contains the information needed to continue the access
     * grant process after the initial request. It includes the access token
     * and continuation URI that the client can use to manage the grant.
     * 
     * <p>The payment continuation is always provided and allows the client
     * to track and manage the authorization status of the payment request.
     * 
     * @see AccessContinue
     */
    @JsonProperty(value = "continue", required = true)
    public AccessContinue paymentContinue;

    /**
     * Optional interaction information if user consent is required.
     * 
     * <p>This field is present when the authorization server requires user
     * interaction to complete the payment authorization. It contains the
     * information needed to continue the grant process after the user has
     * completed the required interaction.
     * 
     * <p>Note: Either this field or the access token field will be present,
     * but not both. This field is used when user consent is required.
     * 
     * @see InteractContinue
     */
    @JsonProperty(value = "interact")
    public InteractContinue interact;

    /**
     * Optional access token if authorization is granted immediately.
     * 
     * <p>This field contains the access token that can be used immediately
     * to access the payment resources. It is present when the authorization
     * server can grant access without requiring user interaction.
     * 
     * <p>Note: Either this field or the interact field will be present,
     * but not both. This field is used when immediate authorization is granted.
     * 
     * <p>Example: "OS9M2PMHKUR64TB8N6BW7OZB8CDFONP219RP1LT0"
     */
    @JsonProperty(value = "access_token")
    public String token;
}
