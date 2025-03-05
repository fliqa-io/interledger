package io.fliqa.client.interledger.signature;

import io.fliqa.client.interledger.InterledgerObjectMapper;
import io.fliqa.client.interledger.exception.InterledgerClientException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SignatureBuilder {

    /**
     * Signature base parameters
     */
    static final String METHOD = "@method";
    static final String TARGET = "@target-uri";
    static final String SIGNATURE_PARAMS = "@signature-params";

    /**
     * Headers and input for signature base
     */
    static final String CONTENT_TYPE_HEADER = "Content-Type";
    static final String CONTENT_DIGEST_HEADER = "Content-Digest";
    static final String CONTENT_LENGTH_HEADER = "Content-Length";
    static final String SIGNATURE_INPUT_HEADER = "Signature-Input";
    static final String SIGNATURE_HEADER = "Signature";

    static final String APPLICATION_JSON = "application/json";

    static final String DEFAULT_SIGNATURE_ID = "sig1";
    public static final String ACCEPT_HEADER = "Accept";
    /**
     * Signature signing
     **/
    final PrivateKey privateKey;
    final String keyId;
    private long created = 0;

    /**
     * Signature input
     **/
    final LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
    private final InterledgerObjectMapper mapper;
    private String body;

    public SignatureBuilder(PrivateKey privateKey,
                            String keyId,
                            InterledgerObjectMapper mapper) {

        assert privateKey != null;
        assert keyId != null && !keyId.isBlank();

        this.privateKey = privateKey;
        this.keyId = keyId;

        if (mapper == null) {
            mapper = new InterledgerObjectMapper();
        }

        this.mapper = mapper;
    }

    public SignatureBuilder(PrivateKey privateKey,
                            String keyId) {

        this(privateKey, keyId, null);
    }

    public SignatureBuilder method(String value) {
        if (value == null || value.isBlank() || (!value.equalsIgnoreCase("GET") && !value.equalsIgnoreCase("POST"))) {
            throw new IllegalArgumentException("Method must not be null or empty! Expected: POST or GET.");
        }
        parameters.put(METHOD, value.toUpperCase());
        return this;
    }

    public SignatureBuilder target(String value) {
        return target(URI.create(value));
    }

    public SignatureBuilder target(URI value) {
        parameters.put(TARGET, prepareTarget(value));
        return this;
    }

    public SignatureBuilder json(Object object) {

        try {
            String value = mapper.writeValueAsString(object);
            return json(value);
        } catch (InterledgerClientException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Sets body, cotent-type: application/json, calculates content-length and content-digest
     *
     * @param json to set as body
     * @return self
     */
    public SignatureBuilder json(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("JSON must not be null or empty!");
        }
        body = json;
        digest(json);
        length(json);
        parameters.put(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        return this;
    }

    private void length(String content) {
        int contentLength = content.getBytes(StandardCharsets.UTF_8).length;
        parameters.put(CONTENT_LENGTH_HEADER, contentLength);
    }

    private void digest(String value) {
        try {
            String digest = digestContentSha512(value);
            String digestHeader = String.format("sha-512=:%s:", digest);
            parameters.put(CONTENT_DIGEST_HEADER, digestHeader);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to calculate digest for: '%s'", value), e);
        }
    }

    protected void setSignatureParams() {

        String signatureParams = parameters.keySet().stream()
                .map(item -> "\"" + item.toLowerCase() + "\"")  // Quote each item
                .collect(Collectors.joining(" "));

        String params = String.format("(%s);keyid=\"%s\";created=%d", signatureParams, keyId, created);
        parameters.put(SIGNATURE_PARAMS, params);
    }

    public SignatureBuilder build() {
        return build(Instant.now().getEpochSecond());
    }

    public SignatureBuilder build(long created) {
        this.created = created;
        setSignatureParams();
        return this;
    }

    /**
     * URL must end with "/" in case no query parameters are present
     *
     * @param value URL
     * @return URL that ends with '/'
     */
    private String prepareTarget(URI value) {
        if (value == null) {
            throw new IllegalArgumentException("Target URI must not be null!");
        }

        String out = value.toString();    // TODO: fix target so it conforms to expectations
        if (!out.endsWith("/") && value.getQuery() == null) {
            return out + "/";
        }

        return out;
    }

   /* public String getContentDigestHeader() {
        String digest = parameters.get(CONTENT_DIGEST_HEADER).toString();
        if (digest == null) {
            throw new IllegalStateException("Content digest must be set!");
        }
        return String.format("sha-512=:%s:", digest);
    }*/

    public String getSignatureParamsHeader() {

        checkIsBuild();
        String signatureParams = parameters.get(SIGNATURE_PARAMS).toString();

        return String.format("%s=%s", DEFAULT_SIGNATURE_ID, signatureParams);
    }

    private void checkIsBuild() {
        if (parameters.get(SIGNATURE_PARAMS) == null) {
            throw new IllegalStateException("Signature must be build before retrieval of signature params header!");
        }
    }

    /**
     * Provide base for signature calculation
     *
     * @return signature base input
     */
    protected String getSignatureBase() {

        checkIsBuild();

        StringBuilder signatureBase = new StringBuilder();

        Iterator<Map.Entry<String, Object>> iterator = parameters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            signatureBase.append("\"")
                    .append(entry.getKey().toLowerCase())
                    .append("\": ")
                    .append(entry.getValue());

            if (iterator.hasNext()) {
                signatureBase.append(System.lineSeparator());
            }
        }

        return signatureBase.toString();
    }

    protected static String digestContentSha512(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] bodyBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] hashedBytes = digest.digest(bodyBytes);
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    /**
     * Returns signature Base64 encoded
     * Signature is generated from a signature base consisting of the header names and values of the request
     *
     * @return signature of request to be added as request header
     */
    protected String getSignature() {
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);
            signature.update(getSignatureBase().getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            throw new IllegalStateException("Failed to generate signature!", ex);
        }
    }

    protected String getSigatureHeader() {
        return String.format("%s=:%s:", DEFAULT_SIGNATURE_ID, getSignature());
    }

    public LinkedHashMap<String, String> getHeaders() {
        checkIsBuild();

        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put(ACCEPT_HEADER, APPLICATION_JSON);

        if (parameters.get(CONTENT_LENGTH_HEADER) != null) {
            headers.put(CONTENT_TYPE_HEADER, parameters.get(CONTENT_TYPE_HEADER).toString());
        }

        if (parameters.get(CONTENT_DIGEST_HEADER) != null) {
            headers.put(CONTENT_DIGEST_HEADER, parameters.get(CONTENT_DIGEST_HEADER).toString());
        }

        // TODO: add token header option

        headers.put(SIGNATURE_INPUT_HEADER, getSignatureParamsHeader());
        headers.put(SIGNATURE_HEADER, getSigatureHeader());
        return headers;
    }
/*

    public static String[] mapToStringArray(LinkedHashMap<String, String> map) {
        List<String> list = new ArrayList<>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            list.add(entry.getKey() + ": " + entry.getValue());
        }

        return list.toArray(new String[0]);
    }
*/

    public String getBody() {
        return body;
    }
}
