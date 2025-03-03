package io.fliqa.interledger;

import com.fasterxml.jackson.databind.*;

import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import net.visma.autopay.http.signature.*;

public class SignedApiClient extends ApiClient {

    private final String keyId;
    private final PrivateKey privateKey;

    public SignedApiClient(String keyId, PrivateKey privateKey) {
        super();
        this.keyId = keyId;
        this.privateKey = privateKey;

        // Set interceptor to automatically sign requests
        super.setRequestInterceptor(this::createSignedRequest);
    }

    public SignedApiClient(HttpClient.Builder builder, ObjectMapper mapper, String baseUri, String keyId, PrivateKey privateKey) {
        super(builder, mapper, baseUri);
        this.keyId = keyId;
        this.privateKey = privateKey;

        // Set interceptor to automatically sign requests
        super.setRequestInterceptor(this::createSignedRequest);
    }

    public HttpRequest.Builder createSignedRequest(HttpRequest.Builder builder) {

       // SignatureKey signatureKey = SignatureKey.from(KeyId.of(keyId), privateKey, SignatureAlgorithm.ED25519);




        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("Host", "example.org");
        headers.put("Date", "Tue, 07 Jun 2014 20:51:35 GMT");
        headers.put("Content-Type", "application/json");
        headers.put("Digest", "SHA-256=X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=");
        headers.put("Accept", "*/*");
        headers.put("Content-Length", "18");




        try {
            String requestTarget = extractRequestTarget(builder);
            String date = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
            String digest = computeDigest(builder);
//            String signatureHeader = generateSignature(requestTarget, date, digest);

            builder.header("Date", date);
            builder.header("Digest", digest);
      //      builder.header("Signature", signatureHeader);
        } catch (Exception e) {
            throw new RuntimeException("Error signing request", e);
        }
        return builder;
    }

    private String extractRequestTarget(HttpRequest.Builder builder) {
        return "post " + super.getBaseUri();
    }

    private String computeDigest(HttpRequest.Builder builder) throws Exception {
        String body = ""; // Extract the actual request body if necessary
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(body.getBytes(StandardCharsets.UTF_8));
        return "SHA-256=" + Base64.getEncoder().encodeToString(hash);
    }
/*

    private String generateSignature(String requestTarget, String date, String digest) throws Exception {
        String signingString = "(request-target): " + requestTarget + "\n" +
                                   "date: " + date + "\n" +
                                   "digest: " + digest;

        Signature signature = Signature.getInstance("Ed25519"); // TODO: do wee need this dynamically??
        signature.initSign(privateKey);
        signature.update(signingString.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();
        String encodedSignature = Base64.getEncoder().encodeToString(signatureBytes);

        return String.format("keyId=\"%s\", algorithm=\"hs2019\", headers=\"(request-target) date digest\", signature=\"%s\"", keyId, encodedSignature);
    }
*/

    /**
     * "content-type": application/json
     * "content-digest": sha-512=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:
     * "content-length": 18
     * "authorization": GNAP 123454321
     * "@method": POST
     * "@target-uri": https://example.com/
     * "@signature-params": ("content-type" "content-digest" "content-length" "authorization" "@method" "@target-uri");alg="ed25519";keyid="eddsa_key_1";created=1704722601
     */
    public static String getSignatureBase(String contentType,
                                          String contentDigest,
                                          Integer contentLenght,
                                          String authorization,
                                          String method,
                                          String targetUri,
                                          String signatureParams) {
        StringBuilder builder = new StringBuilder();
        builder.append("\"content-type\": ").append(contentType).append(System.lineSeparator());
        builder.append("\"content-digest\": ").append(contentDigest).append(System.lineSeparator());
        builder.append("\"content-length\": ").append(contentLenght).append(System.lineSeparator());
        builder.append("\"authorization\": ").append(contentLenght).append(System.lineSeparator());
        return "";
    }


    public static String generateSignature(String signatureBase, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("Ed25519"); // TODO: do wee need this dynamically??
        signature.initSign(privateKey);
        signature.update(signatureBase.getBytes(StandardCharsets.UTF_8));
        byte[] signedBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signedBytes);
    }

}