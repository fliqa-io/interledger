package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashSet;
import java.util.Set;

public class GrantRequest {

    @JsonProperty("client")
    public WalletAddress client;

    @JsonProperty("access_token")
    public AccessToken accessToken;

    /*
        currently not utilized
        @JsonProperty("interact")
        public Object interact;
    */

    public GrantRequest(WalletAddress client) {
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
    public static GrantRequest build(WalletAddress clientWallet,
                                     AccessItemType accessType,
                                     Set<AccessAction> accessActions) {
        GrantRequest request = new GrantRequest(clientWallet);
        request.accessToken = new AccessToken();

        AccessItem accessItem = new AccessItem();
        accessItem.accessType = accessType;
        accessItem.actions = accessActions;

        request.accessToken.access = new LinkedHashSet<>();
        request.accessToken.access.add(accessItem);

        return request;
    }
}
