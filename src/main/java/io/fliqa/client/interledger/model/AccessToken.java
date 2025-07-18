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
import java.util.Set;

/**
 * Represents an access token granted by the authorization server.
 * 
 * <p>This class contains the access token credentials that a client can use
 * to access protected resources in the Interledger Open Payments ecosystem.
 * The token includes the actual token value, management information, expiration
 * details, and the specific access rights that have been granted.
 * 
 * <p>Access tokens are used to authenticate and authorize API requests to
 * protected resources. They are typically included in the Authorization header
 * of HTTP requests using the GNAP (Grant Negotiation and Authorization Protocol)
 * bearer token format.
 * 
 * <p>Example usage:
 * <pre>
 * Authorization: GNAP access_token_value
 * </pre>
 * 
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 * @see AccessItem
 * @see AccessGrant
 * @see AccessContinue
 */
public class AccessToken {

    /**
     * The access token value that should be used in API requests.
     * 
     * <p>This is the actual token string that the client must include in
     * the Authorization header when making requests to protected resources.
     * The token value is opaque to the client and should be treated as
     * a credential.
     * 
     * <p>Example: "OS9M2PMHKUR64TB8N6BW7OZB8CDFONP219RP1LT0"
     */
    @JsonProperty(value = "value", required = true)
    public String token;

    /**
     * The URI for managing this access token.
     * 
     * <p>This URI can be used by the client to perform management operations
     * on the token such as refreshing, revoking, or checking the token status.
     * The exact operations available depend on the authorization server's
     * implementation.
     * 
     * <p>Example: "https://ilp.interledger-test.dev/gnap/token/12345"
     */
    @JsonProperty(value = "manage", required = true)
    public URI manage;

    /**
     * Optional expiration time for the access token in seconds.
     * 
     * <p>This field indicates the lifetime of the token in seconds from
     * the time of issuance. After this time, the token will no longer be
     * valid and the client should obtain a new token.
     * 
     * <p>If not present, the token may have an indefinite lifetime or
     * use the authorization server's default expiration policy.
     * 
     * <p>Example: 3600 (1 hour)
     */
    @JsonProperty(value = "expires_in")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer expiresIn;

    /**
     * The specific access rights granted by this token.
     * 
     * <p>This set contains the detailed permissions that this token provides.
     * Each access item specifies the type of resource, the actions that can
     * be performed, and any limits or constraints on the access.
     * 
     * <p>The access rights determine what operations the client can perform
     * with this token, such as creating payments, reading account information,
     * or managing resources.
     * 
     * @see AccessItem
     */
    @JsonProperty(value = "access", required = true)
    public Set<AccessItem> access;
}
