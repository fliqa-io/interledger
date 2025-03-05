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

    public GrantAccessRequest redirectInteract() {
        interact = new AccessInteract();
        interact.start = List.of("redirect");
        return this;
    }
}
