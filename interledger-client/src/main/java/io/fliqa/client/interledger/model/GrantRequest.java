package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.*;

public class GrantRequest {

    @JsonProperty("client")
    public WalletAddress client;

    @JsonProperty("access_token")
    public AccessToken accessToken;

    public GrantRequest(WalletAddress client) {
        this.client = client;
    }

    /*
    * {
    "access_token": {
        "access": [
            {
                "actions": [
                    "read",
                    "complete",
                    "create"
                ],
                "type": "incoming-payment"
            }
        ]
    },
    "client": "https://ilp.interledger-test.dev/andrejfliqatestwallet"
}
    * */
}
