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
 * Represents the continuation response from an interaction in the GNAP protocol.
 * 
 * <p>This class contains the information returned by the authorization server
 * after a user interaction has been completed. It includes the interaction
 * token that can be used to continue the grant process and the redirect URI
 * where the user was sent after completing the interaction.
 * 
 * <p>The interact continuation is used to bridge the gap between the user
 * interaction phase and the token grant phase in the GNAP flow. After the
 * user completes the required interaction (such as authentication or consent),
 * the authorization server provides this information to allow the client to
 * continue and complete the access grant process.
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see AccessInteract
 * @see InteractFinish
 * @see OutgoingPayment
 */
public class InteractContinue {

    /**
     * The interaction token provided by the authorization server.
     * 
     * <p>This token represents the completed user interaction and can be used
     * by the client to continue the grant process. The token is typically
     * provided by the authorization server after the user has successfully
     * completed the required interaction steps.
     * 
     * <p>The client should use this token when making subsequent requests
     * to the authorization server to complete the access grant process.
     * 
     * <p>Example: "K2BBQgDjAyvb92jhb3R43Wd2NTWoUQgB"
     */
    @JsonProperty(value = "finish", required = true)
    public String token;

    /**
     * The redirect URI where the user was sent after completing the interaction.
     * 
     * <p>This URI indicates where the user was redirected after completing
     * the required interaction with the authorization server. It typically
     * contains additional information or parameters that were appended by
     * the authorization server to communicate the result of the interaction.
     * 
     * <p>The client can use this information to understand the context of
     * the completed interaction and to continue the grant process accordingly.
     * 
     * <p>Example: "https://client.example.com/callback?state=12345&amp;interact_ref=abc123"
     */
    @JsonProperty(value = "redirect", required = true)
    public URI redirect;
}
