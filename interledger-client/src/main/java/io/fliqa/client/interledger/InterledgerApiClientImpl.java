package io.fliqa.client.interledger;

import com.fasterxml.jackson.databind.*;
import io.fliqa.client.interledger.model.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.*;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.logging.*;

import static java.time.temporal.ChronoUnit.*;

public class InterledgerApiClientImpl implements InterledgerApiClient {

    private Logger log = Logger.getLogger(InterledgerApiClientImpl.class.getName());

    private final WalletAddress clientWallet;
    private final PrivateKey privateKey;
    private final String keyId;
    private HttpClient client;
    private ObjectMapper mapper = new ObjectMapper();

    public InterledgerApiClientImpl(WalletAddress clientWallet,
                                    PrivateKey privateKey,
                                    String keyId) {

        this.clientWallet = clientWallet;
        this.privateKey = privateKey;
        this.keyId = keyId;

        client = HttpClient.newHttpClient();
    }

    @Override
    public PaymentPointer getWallet(WalletAddress address) throws IOException, InterruptedException {

        var request = HttpRequest.newBuilder(address.paymentPointer)
                          .GET()
                          .timeout(Duration.of(10, SECONDS))
                          .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response.body(), PaymentPointer.class);
        } catch (Exception e) {
            log.warning(String.format("Failed read wallet: %s", address.paymentPointer));
            throw e;
        }
    }

    /**
     * POST to reciever.athServer with JSON
     * <p>
     * {
     * "access_token": {
     * "access": [
     * {
     * "actions": [
     * "read",
     * "complete",
     * "create"
     * ],
     * "type": "incoming-payment"
     * }
     * ]
     * },
     * "client": "https://ilp.interledger-test.dev/andrejfliqatestwallet" // initiator clientWallet
     * }
     *
     * @param receiver
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public PendingGrant createPendingGrant(PaymentPointer receiver) throws IOException, InterruptedException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        GrantRequest grantRequest = new GrantRequest(clientWallet);
        grantRequest.accessToken = new AccessToken();

        AccessItem incomingPayment = new AccessItem();
        incomingPayment.accessType = AccessItemType.incomingPayment;
        incomingPayment.actions = Set.of(AccessAction.read, AccessAction.complete, AccessAction.create);
        grantRequest.accessToken.access.add(incomingPayment);

        // Content-Digest: sha-512=:v2baXKn2bRWwis7fZwF4sB8B7I7izwCA5kybiVdCVb8nhD2kd0qf07hgK+p1Jaa00wQiEmOXKzlS6gurYKdBHA==:
        // Signature: sig1=:fa4PDxIKz7+jHcKgkPO1IwAds/VHqPJDCdUs35SFZHZS77x3OWVONWZwhh6T93XSoqFgRgBZkcMxzSZvrVGVDA==:
        // Signature-Input: sig1=("@method" "@target-uri" "content-digest" "content-length" "content-type");keyid="89675b1d-53f3-4fb6-b8ea-33a56e576cef";created=1740757701
        // content-length: 162

        long createdTime = Instant.now().getEpochSecond();
        //createdTime = 1741002284L;

        String signatureParams = String.format("(\"@method\" \"@target-uri\" \"content-digest\" \"content-length\" \"content-type\");keyid=\"%s\";created=%d",
                                               keyId,
                                               createdTime);
        String signatureInputHeader = String.format("sig1=%s", signatureParams);


        String json = "{\"access_token\":{\"access\":[{\"type\":\"incoming-payment\",\"actions\":[\"read\",\"complete\",\"create\"]}]},\"client\":\"https://ilp.interledger-test.dev/andrejfliqatestwallet\"}";
        // mapper.writeValueAsString(grantRequest); TODO: fix deserialization
        int contentLength = json.getBytes(StandardCharsets.UTF_8).length;

        String digest = digestContentSha512(json);
        String contentDigest = String.format("sha-512=:%s:", digest);

        String signatureInput = new StringBuilder()
                                    .append("\"@method\": POST").append(System.lineSeparator())
                                    .append("\"@target-uri\": ").append(receiver.authServer).append("/").append(System.lineSeparator())
                                    .append("\"content-digest\": ").append(contentDigest).append(System.lineSeparator())
                                    .append("\"content-length\": ").append(contentLength).append(System.lineSeparator())
                                    .append("\"content-type\": application/json").append(System.lineSeparator())
                                    .append("\"@signature-params\": ").append(signatureParams)
                                    .toString();

        assert signatureInput.equals("\"@method\": POST\n" +
                             "\"@target-uri\": https://auth.interledger-test.dev/\n" +
                             "\"content-digest\": sha-512=:v2baXKn2bRWwis7fZwF4sB8B7I7izwCA5kybiVdCVb8nhD2kd0qf07hgK+p1Jaa00wQiEmOXKzlS6gurYKdBHA==:\n" +
                             "\"content-length\": 162\n" +
                             "\"content-type\": application/json\n" +
                             "\"@signature-params\": (\"@method\" \"@target-uri\" \"content-digest\" \"content-length\" \"content-type\");keyid=\"89675b1d-53f3-4fb6-b8ea-33a56e576cef\";created=" + createdTime);


        // Example headers (typically Date, Content-Digest, etc.)
      /*  Map<String, String> headers = Map.of(
                                          "Content-Type", "application/json", // Set content type
                                          "Accept", "application/json", // Optional: Define accepted response type
                                          "Content-Digest", contentDigest,
                                          "Signature-Input", signatureInputHeader,
                                          "Content-Lenght", Integer.toString(contentLength));*/

       // String bodyDigest = "sha-256=:sZtF9LVzH1aP5EC/p7gzMlfLO3pys3CQUXv2FB0UOHg=:";

        // Construct the signature base string
        //String signatureBase = constructSignatureBase("POST", receiver.authServer, headers, contentDigest);

        log.info(String.format("Singature-Base: %s", signatureInput));


        String signature = generateSignature(privateKey, signatureInput);
        log.info(String.format("Calculated signature: %s", signature));

        String signatureHeader = String.format("sig1=:%s:", signature);


        // MANUAL
        assert contentDigest.equals("sha-512=:v2baXKn2bRWwis7fZwF4sB8B7I7izwCA5kybiVdCVb8nhD2kd0qf07hgK+p1Jaa00wQiEmOXKzlS6gurYKdBHA==:");
        assert signatureInputHeader.equals("sig1=(\"@method\" \"@target-uri\" \"content-digest\" \"content-length\" \"content-type\");keyid=\"89675b1d-53f3-4fb6-b8ea-33a56e576cef\";created=" + createdTime);
     //   assert signatureHeader.equals("sig1=:Muxo74zrmuf2gvAt5/mMd/BSKxMU4G80jhOHyCpzFocBQ0cnkRIej4NYYqS9fWkhfxAZD3T1mItYVRIh3gQ8Ag==:");

        // URI proxy = URI.create("http://localhost:8090");
        var request = HttpRequest.newBuilder(receiver.authServer)
                          .header("Content-Type", "application/json") // Set content type
                          .header("Accept", "application/json") // Optional: Define accepted response type
                          .header("Content-Digest", contentDigest)
                          .header("Signature-Input", signatureInputHeader)
                          .header("Signature", signatureHeader)
                       //  .header("Content-Length", Integer.toString(contentLength))
                          .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)) // Attach JSON body
                          .timeout(Duration.of(10, SECONDS))
                          .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response.body(), PendingGrant.class);
        } catch (Exception e) {
            log.warning(String.format("Failed create pending grant: %s", e));
            throw e;
        }
    }

    public static String digestContentSha512(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] bodyBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] hashedBytes = digest.digest(bodyBytes);
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

   /* public static String createSignatureBase() {
        String contentType = "application/json";
        String contentDigest = "sha-512=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:";
        String contentLength = "18";
        String authorization = "GNAP 123454321";
        String method = "POST";
        String targetUri = "https://example.com/";
        String signatureParams = "(\"content-type\" \"content-digest\" \"content-length\" \"authorization\" \"@method\" \"@target-uri\");alg=\"ed25519\";keyid=\"eddsa_key_1\";created=1704722601";

        return "content-type: " + contentType + "\n" +
                   "content-digest: " + contentDigest + "\n" +
                   "content-length: " + contentLength + "\n" +
                   "authorization: " + authorization + "\n" +
                   "@method: " + method + "\n" +
                   "@target-uri: " + targetUri + "\n" +
                   "@signature-params: " + signatureParams;
    }*/

 /*   public static String hashSignatureBase(String signatureBase) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] hashedBytes = digest.digest(signatureBase.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    public static String signDigest(String digest, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(privateKey);
        signature.update(Base64.getDecoder().decode(digest));
        byte[] signedBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signedBytes);
    }*/

    public static String generateSignature(PrivateKey privateKey, String input) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("Ed25519"); // TODO: do wee need this dynamically??
        signature.initSign(privateKey);
        signature.update(input.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }
}
