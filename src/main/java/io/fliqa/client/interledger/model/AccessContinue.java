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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

/**
 * Represents the response from a GNAP access continuation request.
 * 
 * <p>This class contains the information returned by the authorization server
 * when a client continues an access request that was previously initiated.
 * It includes the access token, the continuation URI, and optionally a wait
 * time for polling-based interactions.
 * 
 * <p>Access continuation is used in the Grant Negotiation and Authorization
 * Protocol (GNAP) to allow clients to continue a previously started access
 * request without having to restart the entire authorization flow.
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see AccessToken
 * @see AccessGrant
 */
public class AccessContinue {

    /**
     * The access token granted by the authorization server.
     * 
     * <p>This token contains the actual access credentials that the client
     * can use to access protected resources. It includes the token value,
     * expiration information, and the scope of access granted.
     * 
     * @see AccessToken
     */
    @JsonProperty(value = "access_token", required = true)
    public AccessToken access;

    /**
     * The URI for continuing the access request.
     * 
     * <p>This URI is used by the client to continue the access request
     * in future interactions. It may be used for token refresh, access
     * continuation, or other follow-up operations as defined by the
     * authorization server.
     * 
     * <p>Example: "https://ilp.interledger-test.dev/gnap/continue/12345"
     */
    @JsonProperty(value = "uri", required = true)
    public URI uri;

    /**
     * Optional wait time in seconds for polling-based interactions.
     * 
     * <p>When present, this field indicates the recommended wait time
     * in seconds before the client should poll the continuation URI
     * again. This is used to prevent excessive polling and reduce
     * server load in polling-based authorization flows.
     * 
     * <p>If not present, the client should use a reasonable default
     * wait time or follow other interaction methods.
     */
    @JsonProperty(value = "wait")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer wait;
}
