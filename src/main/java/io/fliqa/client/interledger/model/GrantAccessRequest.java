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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GrantAccessRequest {

    @JsonProperty("client")
    public WalletAddress client;

    @JsonProperty("access_token")
    public AccessToken accessToken;

    @JsonProperty("interact")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public AccessInteract interact;

    private static final String REDIRECT = "redirect";

    public GrantAccessRequest(WalletAddress client) {
        this.client = client;
    }

    /**
     * Helper method to quickly assemble request
     *
     * @param clientWallet  client wallet address
     * @param accessType    type of access (incoming / outgoing)
     * @param accessActions actions to be performed
     * @return request
     */
    public static GrantAccessRequest build(WalletAddress clientWallet,
                                           AccessItemType accessType,
                                           Set<AccessAction> accessActions) {
        GrantAccessRequest request = new GrantAccessRequest(clientWallet);
        request.accessToken = new AccessToken();

        AccessItem accessItem = new AccessItem();
        accessItem.accessType = accessType;
        accessItem.actions = accessActions;

        request.accessToken.access = new LinkedHashSet<>();
        request.accessToken.access.add(accessItem);

        return request;
    }

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
     * @param returnUrl url to return to when interaction is finished or null to omit
     * @return grant request access
     */
    public GrantAccessRequest redirectInteract(URI returnUrl, String nonce) {
        interact = new AccessInteract();
        interact.start = List.of(REDIRECT);

        if (returnUrl != null) {
            interact.finish = new InteractFinish();
            interact.finish.method = REDIRECT;
            interact.finish.uri = returnUrl;
            interact.finish.nonce = nonce;
        }
        return this;
    }
}
