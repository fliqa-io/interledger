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

/**
 * Represents a complete access grant response from the authorization server.
 * 
 * <p>This class contains the result of a successful access grant request in
 * the Grant Negotiation and Authorization Protocol (GNAP). It includes the
 * access token that can be used to access protected resources, and optionally
 * continuation information for future interactions.
 * 
 * <p>An access grant is the successful outcome of an authorization flow where
 * the client has been granted permission to access specific resources on
 * behalf of a resource owner. The grant includes both immediate access
 * credentials and information for maintaining or extending that access.
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see AccessToken
 * @see AccessContinue
 * @see GrantAccessRequest
 */
public class AccessGrant {

    /**
     * The access token granted by the authorization server.
     * 
     * <p>This token contains the credentials that the client can use to
     * access protected resources. It includes the token value, expiration
     * information, and the scope of access that has been granted.
     * 
     * <p>The access token is typically used in the Authorization header
     * of HTTP requests to authenticate and authorize API calls.
     * 
     * @see AccessToken
     */
    @JsonProperty("access_token")
    public AccessToken access;

    /**
     * Optional continuation information for future interactions.
     * 
     * <p>When present, this field provides information that the client
     * can use to continue the access request in the future. This might
     * be used for token refresh, access extension, or other follow-up
     * operations as defined by the authorization server.
     * 
     * <p>If not present, the client should treat this as a final grant
     * with no continuation capabilities.
     * 
     * @see AccessContinue
     */
    @JsonProperty("continue")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AccessContinue accessContinue;
}
