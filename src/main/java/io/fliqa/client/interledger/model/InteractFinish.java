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
 * Represents the finish specification for interactions in the GNAP protocol.
 * 
 * <p>This class defines how the authorization server should communicate the
 * completion of a user interaction back to the client. It specifies the method
 * to use for notification, the URI where the client should be contacted or
 * redirected, and a nonce for security purposes.
 * 
 * <p>The interact finish mechanism is used to ensure that the client can
 * reliably receive notification when the user has completed the required
 * interaction with the authorization server. This is crucial for continuing
 * the grant process after user consent or authentication.
 * 
 * <p>Common finish methods include:
 * <ul>
 *   <li>"redirect" - Browser redirect to the client's callback URI</li>
 *   <li>"push" - Server-to-server notification to the client</li>
 * </ul>
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see AccessInteract
 * @see InteractContinue
 * @see GrantAccessRequest
 */
public class InteractFinish {

    /**
     * The method to use for notifying the client of interaction completion.
     * 
     * <p>This field specifies how the authorization server should communicate
     * the completion of the user interaction back to the client. The method
     * determines the mechanism used for the notification.
     * 
     * <p>Supported methods:
     * <ul>
     *   <li>"redirect" - Browser redirect to the client's callback URI</li>
     *   <li>"push" - Direct server-to-server notification</li>
     * </ul>
     * 
     * <p>Example: "redirect"
     */
    @JsonProperty(value = "method", required = true)
    public String method;

    /**
     * The URI where the client should be contacted or redirected after interaction.
     * 
     * <p>This URI serves as the callback endpoint where the authorization server
     * will send the interaction completion notification. For redirect methods,
     * this is where the user's browser will be redirected. For push methods,
     * this is the endpoint that will receive the server-to-server notification.
     * 
     * <p>The authorization server may append additional parameters to this URI
     * to communicate the result of the interaction.
     * 
     * <p>Example: "https://client.example.com/callback"
     */
    @JsonProperty(value = "uri", required = true)
    public URI uri;

    /**
     * A unique value to prevent replay attacks during the interaction.
     * 
     * <p>This nonce is used to ensure the security and integrity of the
     * interaction finish process. It helps prevent replay attacks and ensures
     * that the interaction completion notification is authentic and recent.
     * 
     * <p>The nonce should be a cryptographically random value that is unique
     * for each interaction. The client should verify this nonce when receiving
     * the interaction completion notification.
     * 
     * <p>Example: "VjVHaYojGUc3dJd4oQ8WwHm5YGxmE3"
     */
    @JsonProperty(value = "nonce", required = true)
    public String nonce;
}
