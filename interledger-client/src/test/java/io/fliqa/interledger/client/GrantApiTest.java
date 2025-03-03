package io.fliqa.interledger.client;

import io.fliqa.interledger.*;
import io.fliqa.interledger.client.model.*;
import org.junit.jupiter.api.*;

import java.net.*;
import java.nio.charset.*;
import java.security.*;
import java.security.spec.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

class GrantApiTest {

    ApiClient apiClient = new ApiClient()
                              .setBasePath("https://ilp.interledger-test.dev");

    GrantApi grantApi = new GrantApi(apiClient);

    // TODO: we can get the key information by checking the wallet via GET: {{ _.root }}/{{wallet}}/jwks.json
    // do we need this and if so what types of keys are possible
    public static PrivateKey loadPrivateKey(String base64Key) throws Exception {
        // Remove PEM headers and decode Base64
        String privateKeyBase64 = base64Key
                                      .replace("-----BEGIN PRIVATE KEY-----", "")
                                      .replace("-----END PRIVATE KEY-----", "")
                                      .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(keySpec);



        /*PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        // Ed25519 KeyFactory (Java 15+)
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        return keyFactory.generatePrivate(keySpec);*/

        /*public static PrivateKey loadEd25519PrivateKey(String base64Key) throws Exception {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return keyFactory.generatePrivate(keySpec);
        }*/
    }

   /* public static String computeDigest(String body) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(body.getBytes(StandardCharsets.UTF_8));
        return "SHA-256=" + Base64.getEncoder().encodeToString(hash);
    }

    public static String generateSignature(String keyId,
                                           PrivateKey privateKey,
                                           String requestTarget,
                                           String date,
                                           String digest) throws Exception {
        // Create the signing string
        String signingString = "(request-target): " + requestTarget + "\n" +
                                   "date: " + date + "\n" +
                                   "digest: " + digest;

        // Sign the string using the private key
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(signingString.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();

        // Encode the signature in Base64
        String encodedSignature = Base64.getEncoder().encodeToString(signatureBytes);

        // Construct the final Signature header
        return String.format("keyId=\"%s\", algorithm=\"hs2019\", headers=\"(request-target) date digest\", signature=\"%s\"",
                             keyId, encodedSignature);
    }*/

    private static final Logger log = Logger.getLogger(GrantApiTest.class.getName());

    @Test
    public void createIncomingPaymentGrant() throws Exception {

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

        // Step 1 - Create grant from Sender -> Receiver (must be a signed request)
        /*
        const incomingPaymentGrant = await client.grant.request(
        {
          url: receivingWalletAddress.authServer,
        },
        {
          access_token: {
            access: [
              {
                type: "incoming-payment",
                actions: ["read", "complete", "create"],
              },
            ],
          },
        });
        */
        SignedApiClient incomingClient = new SignedApiClient(TestHelper.CLIENT_KEY_ID,
                                                             loadPrivateKey(TestHelper.CLIENT_PRIVATE_KEY));
        incomingClient.updateBaseUri(receiverWallet.getAuthServer().toString());

        // Call the API
        AccessIncoming incomingPayment = new AccessIncoming();
        incomingPayment.setType(AccessIncoming.TypeEnum.INCOMING_PAYMENT);
        incomingPayment.setActions(Set.of(AccessIncoming.ActionsEnum.CREATE, AccessIncoming.ActionsEnum.COMPLETE, AccessIncoming.ActionsEnum.READ));

        AccessItem incoming = new AccessItem(incomingPayment);
        Set<AccessItem> access = Set.of(incoming);

        PostRequestRequestAccessToken token = new PostRequestRequestAccessToken();
        token.access(access);

        PostRequestRequest request = new PostRequestRequest();
        request.setClient(TestHelper.CLIENT_KEY_ID); // we need to set the Fliqa initiator client
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