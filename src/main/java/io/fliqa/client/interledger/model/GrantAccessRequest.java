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
import io.fliqa.client.interledger.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Represents a request to grant access to resources using the GNAP protocol.
 *
 * <p>This class is used to initiate an access request to the authorization server
 * in the Grant Negotiation and Authorization Protocol (GNAP). It contains the
 * client identifier, the access token request specifying what permissions are
 * being requested, and optionally the interaction methods for obtaining user
 * consent.
 *
 * <p>The grant access request is typically the first step in the GNAP flow where
 * the client requests permission to access protected resources on behalf of a
 * resource owner. The request includes:
 * <ul>
 *   <li>Client identification (wallet address)</li>
 *   <li>Access token request with desired permissions</li>
 *   <li>Optional interaction methods for user consent</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * GrantAccessRequest request = GrantAccessRequest.outgoing(
 *     clientWallet,
 *     AccessItemType.outgoingPayment,
 *     Set.of(AccessAction.create),
 *     quoteId,
 *     debitAmount
 * );
 * request.redirectInteract(returnUrl, nonce);
 * </pre>
 *
 * @author Fliqa
 * @version 1.0
 * @see AccessToken
 * @see AccessInteract
 * @see WalletAddress
 * @see AccessGrant
 * @since 1.0
 */
public class GrantAccessRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrantAccessRequest.class);

    /**
     * The client identifier for this access request.
     *
     * <p>This identifies the client application that is requesting access
     * to protected resources. In the context of Interledger Open Payments,
     * this is typically the wallet address of the client application.
     *
     * @see WalletAddress
     */
    @JsonProperty("client")
    public WalletAddress client;

    /**
     * The access token request specifying what permissions are being requested.
     *
     * <p>This field contains the detailed specification of what access the
     * client is requesting. It includes the types of resources, the actions
     * that can be performed, and any limits or constraints on the access.
     *
     * @see AccessToken
     */
    @JsonProperty("access_token")
    public AccessToken accessToken;

    /**
     * Optional interaction methods for obtaining user consent.
     *
     * <p>This field specifies how the client can interact with the authorization
     * server to obtain consent from the resource owner. If not present, the
     * authorization server may use its default interaction methods or deny
     * the request if interaction is required.
     *
     * @see AccessInteract
     */
    @JsonProperty("interact")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AccessInteract interact;

    private static final String REDIRECT = "redirect";

    /**
     * Constructs a new grant access request for the specified client.
     *
     * @param clientWalletAddress the client wallet address making the access request
     * @throws IllegalArgumentException if client is null
     */
    public GrantAccessRequest(WalletAddress clientWalletAddress) {
        Assert.notNull(clientWalletAddress, "clientWalletAddress cannot be null.");
        this.client = clientWalletAddress;
    }

    /**
     * Helper method to quickly assemble a basic access request.
     *
     * <p>This method creates a grant access request with the specified client
     * wallet address, access type, and actions. It automatically creates the
     * necessary access token and access item structures.
     *
     * @param clientWallet  the client wallet address making the request
     * @param accessType    the type of access being requested (incoming/outgoing)
     * @param accessActions the set of actions to be performed
     * @return a new GrantAccessRequest instance configured with the specified parameters
     */
    public static GrantAccessRequest build(WalletAddress clientWallet,
                                           AccessItemType accessType,
                                           Set<AccessAction> accessActions) {

        Assert.notNull(clientWallet, "clientWallet cannot be null.");
        Assert.notNull(accessType, "accessType cannot be null.");
        Assert.notNullOrEmpty(accessActions, "accessActions cannot be null or empty.");

        GrantAccessRequest request = new GrantAccessRequest(clientWallet);
        request.accessToken = new AccessToken();

        AccessItem accessItem = new AccessItem();
        accessItem.accessType = accessType;
        accessItem.actions = accessActions;

        request.accessToken.access = new LinkedHashSet<>();
        request.accessToken.access.add(accessItem);

        return request;
    }

    /**
     * Creates a grant access request for outgoing payment operations.
     *
     * <p>This method creates a specialized access request for outgoing payments,
     * including the specific identifier and debit amount limits. It's commonly
     * used when requesting permission to create outgoing payments with specific
     * constraints.
     *
     * @param clientWallet  the client wallet address making the request
     * @param accessType    the type of access being requested (typically outgoingPayment)
     * @param accessActions the set of actions to be performed (typically create)
     * @param identifier    the URI identifier for the specific resource (e.g., quote ID)
     * @param debitAmount   the maximum amount that can be debited
     * @return a new GrantAccessRequest configured for outgoing payments
     */
    public static GrantAccessRequest outgoing(WalletAddress clientWallet,
                                              AccessItemType accessType,
                                              Set<AccessAction> accessActions,
                                              URI identifier,
                                              InterledgerAmount debitAmount) {

        GrantAccessRequest request = build(clientWallet, accessType, accessActions);

        Optional<AccessItem> found = request.accessToken.access.stream().findFirst();
        found.ifPresent(accessItem -> accessItem.accessOutgoing(identifier, debitAmount));

        return request;
    }

    /**
     * Configures this request to use redirect-based interaction.
     *
     * <p>This method sets up the request to use browser-based redirect interaction
     * for obtaining user consent. The authorization server will redirect the user
     * to the specified return URL after the interaction is complete.
     *
     * @param returnUrl the URL to return to when interaction is finished, or null to omit
     * @param nonce     a unique value to prevent replay attacks during the interaction
     * @return this GrantAccessRequest instance for method chaining
     */
    public GrantAccessRequest redirectInteract(URI returnUrl, String nonce) {

        interact = new AccessInteract();
        interact.start = List.of(REDIRECT);

        if (returnUrl != null) {
            Assert.notNullOrEmpty(nonce, "nonce cannot be null or empty.");

            interact.finish = new InteractFinish();
            interact.finish.method = REDIRECT;
            interact.finish.uri = returnUrl;
            interact.finish.nonce = nonce;
        }
        return this;
    }

    /**
     * Verifies the {@code hash} query parameter that the authorization server appends to the
     * interaction callback (redirect) URI, as defined by the GNAP interaction finish hash
     * (<a href="https://datatracker.ietf.org/doc/html/draft-ietf-gnap-core-protocol#section-4.2.3">
     * draft-ietf-gnap-core-protocol &sect;4.2.3</a>).
     *
     * <p>The hash is calculated as {@code BASE64URL(SHA-256(clientNonce + "\n" + asNonce + "\n" +
     * interactRef + "\n" + grantEndpoint))} and must be verified before the {@code interactRef}
     * returned on the callback is trusted and used to call {@code finalizeGrant()} - otherwise an
     * attacker could forge a callback and hijack the grant.
     *
     * @param clientNonce   the nonce this client generated and sent as {@code interact.finish.nonce}
     *                      in the original grant request (the value passed to {@link #redirectInteract(URI, String)})
     * @param asNonce       the authorization server's nonce, returned as {@code interact.finish} in the
     *                      initial grant response (see {@link InteractContinue#token})
     * @param interactRef   the {@code interact_ref} query parameter received on the callback
     * @param hash          the {@code hash} query parameter received on the callback
     * @param grantEndpoint the URI the initial grant request was sent to (the sender's auth server)
     * @return {@code true} if the received hash matches the computed hash, {@code false} otherwise
     */
    public static boolean verifyInteractionHash(String clientNonce,
                                                 String asNonce,
                                                 String interactRef,
                                                 String hash,
                                                 URI grantEndpoint) {
        Assert.notNullOrEmpty(clientNonce, "clientNonce cannot be null or empty.");
        Assert.notNullOrEmpty(asNonce, "asNonce cannot be null or empty.");
        Assert.notNullOrEmpty(interactRef, "interactRef cannot be null or empty.");
        Assert.notNullOrEmpty(hash, "hash cannot be null or empty.");
        Assert.notNull(grantEndpoint, "grantEndpoint cannot be null.");

        String input = clientNonce + "\n" + asNonce + "\n" + interactRef + "\n" + grantEndpoint;

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available.", e);
        }

        byte[] computedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        String computedHashString = Base64.getUrlEncoder().withoutPadding().encodeToString(computedHash);

        byte[] receivedHash;
        try {
            receivedHash = decodeBase64Tolerant(hash);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Interaction hash verification FAILED - received hash: '{}' is not valid base64/base64url: {}",
                    hash, e.getMessage());
            return false;
        }

        boolean matches = MessageDigest.isEqual(computedHash, receivedHash);

        if (!matches) {
            LOGGER.warn("Interaction hash verification FAILED - clientNonce: '{}', asNonce: '{}', interactRef: '{}', " +
                            "grantEndpoint: '{}', received hash: '{}', computed hash: '{}'",
                    clientNonce, asNonce, interactRef, grantEndpoint, hash, computedHashString);
        } else {
            LOGGER.debug("Interaction hash verification succeeded - interactRef: '{}', hash: '{}'", interactRef, hash);
        }

        return matches;
    }

    /**
     * Decodes a base64-encoded value received from a third party (the GNAP spec requires
     * base64url without padding, but not every authorization server implementation follows that
     * exactly) - accepts either the standard ({@code +}, {@code /}) or URL-safe ({@code -}, {@code _})
     * alphabet, and with or without {@code =} padding.
     */
    private static byte[] decodeBase64Tolerant(String value) {
        String normalized = value.replace('-', '+').replace('_', '/');

        int remainder = normalized.length() % 4;
        if (remainder != 0) {
            normalized = normalized + "=".repeat(4 - remainder);
        }

        return Base64.getDecoder().decode(normalized);
    }
}
