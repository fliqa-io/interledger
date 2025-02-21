package io.fliqa.interledger.client;

import io.fliqa.interledger.*;
import io.fliqa.interledger.client.model.*;
import org.junit.jupiter.api.*;

import java.net.*;
import java.util.*;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

class GrantApiTest {

    ApiClient apiClient = new ApiClient()
                              .setBasePath("https://ilp.interledger-test.dev");

    GrantApi grantApi = new GrantApi(apiClient);

    private static final Logger log = Logger.getLogger(GrantApiTest.class.getName());

    @Test
    public void createIncomingPaymentGrant() throws ApiException {

        ApiClient walletClient = new ApiClient();
        walletClient.updateBaseUri(TestHelper.RECEIVER_WALLET_ADDRESS);
        log.info("Wallet client base path: " + walletClient.getBaseUri());

        WalletAddressApi walletApi = new WalletAddressApi(walletClient);

        WalletAddress receiverWallet = null;
        try {
            receiverWallet = walletApi.getWalletAddress();

            log.info("Found receiver wallet");
            log.info(receiverWallet.toString());
        }
        catch (ApiException ex) {
            log.severe("Failed to get receiver wallet: " + ex.getMessage());
        }
        assertNotNull(receiverWallet);

        // Step 1 - Create grant from Sender -> Receiver
        ApiClient incomingClient = new ApiClient();
        incomingClient.updateBaseUri(receiverWallet.getAuthServer().toString());

        AccessIncoming incomingPayment = new AccessIncoming();
        incomingPayment.setType(AccessIncoming.TypeEnum.INCOMING_PAYMENT);
        incomingPayment.setActions(Set.of(AccessIncoming.ActionsEnum.CREATE, AccessIncoming.ActionsEnum.COMPLETE, AccessIncoming.ActionsEnum.READ));

        AccessItem incoming = new AccessItem(incomingPayment);
        Set<AccessItem> access = Set.of(incoming);


        PostRequestRequestAccessToken token = new PostRequestRequestAccessToken();
        token.access(access);


        PostRequestRequest request = new PostRequestRequest();
        request.setClient(TestHelper.CLIENT_WALLET_ADDRESS); // we need to set the Fliqa initiator client

        request.accessToken(token);

        GrantApi incomingPaymentGrantApi = new GrantApi(incomingClient);

        PostRequest200Response incomingRequest = null;
        try {
            incomingRequest = incomingPaymentGrantApi.postRequest(request);

            log.info("Created incoming payment request");
            log.info(incomingRequest.toString());
        }
        catch (ApiException ex) {
            log.severe("Failed to get receiver wallet: " + ex.getMessage());
        }
        assertNotNull(incomingRequest);
    }
}