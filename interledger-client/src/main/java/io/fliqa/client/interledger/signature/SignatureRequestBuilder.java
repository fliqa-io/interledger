package io.fliqa.client.interledger.signature;

import io.fliqa.client.interledger.InterledgerClientOptions;
import io.fliqa.client.interledger.InterledgerObjectMapper;
import io.fliqa.client.interledger.exception.InterledgerClientException;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

public class SignatureRequestBuilder {

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
    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String ACCEPT_HEADER = "Accept";

    static final String APPLICATION_JSON = "application/json";

    static final String DEFAULT_SIGNATURE_ID = "sig1";

    /*
     * Digest algorithm
     */
    static final String DIGEST_ALGORITHM = "SHA-512";
    static final String SIGNATURE_ALGORITHM = "Ed25519";

    static final Set<String> ALLOWED_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "HEAD");

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

    public SignatureRequestBuilder(PrivateKey privateKey,
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

    public SignatureRequestBuilder(PrivateKey privateKey,
                                   String keyId) {

        this(privateKey, keyId, null);
    }

    public SignatureRequestBuilder method(String value) {
        if (value == null || value.isBlank() || !ALLOWED_METHODS.contains(value)) {
            throw new IllegalArgumentException(String.format("Method '%s' is not allowed. Allowed methods are: %s", value, String.join(", ", ALLOWED_METHODS)));
        }
        parameters.put(METHOD, value.toUpperCase());
        return this;
    }

    public SignatureRequestBuilder POST(Object body) {
        checkMethod();
        return method("POST").json(body);
    }

    public SignatureRequestBuilder PUT(Object body) {
        checkMethod();
        return method("PUT").json(body);
    }

    public SignatureRequestBuilder GET() {
        checkMethod();
        return method("GET");
    }

    public SignatureRequestBuilder DELETE() {
        checkMethod();
        return method("DELETE");
    }

    public String getMethod() {
        checkHasParameter(METHOD);
        return parameters.get(METHOD).toString();
    }

    public SignatureRequestBuilder target(String value) {
        return target(URI.create(value));
    }

    public SignatureRequestBuilder target(URI value) {
        parameters.put(TARGET, prepareTarget(value));
        return this;
    }

    public SignatureRequestBuilder json(Object object) {

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
    public SignatureRequestBuilder json(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("JSON must not be null or empty!");
        }
        body = json;
        digest(json);
        length(json);
        parameters.put(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        return this;
    }

    public SignatureRequestBuilder accessToken(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be null or empty!");
        }

        parameters.put(AUTHORIZATION_HEADER, prepareToken(token));
        return this;
    }

    private String prepareToken(String token) {
        return String.format("GNAP %s", token);
    }

    private void length(String content) {
        int contentLength = content.getBytes(StandardCharsets.UTF_8).length;
        parameters.put(CONTENT_LENGTH_HEADER, contentLength);
    }

    private void digest(String value) {
        try {
            String digest = digestContentSha512(value);
            String digestHeader = String.format("%s=:%s:", DIGEST_ALGORITHM.toLowerCase(), digest);
            parameters.put(CONTENT_DIGEST_HEADER, digestHeader);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to calculate %s digest for: '%s'.", DIGEST_ALGORITHM, value), e);
        }
    }

    protected void setSignatureParams() {

        String signatureParams = parameters.keySet().stream()
                .map(item -> "\"" + item.toLowerCase() + "\"")  // Quote each item, need to be lower case
                .collect(Collectors.joining(" "));

        String params = String.format("(%s);keyid=\"%s\";created=%d", signatureParams, keyId, created);
        parameters.put(SIGNATURE_PARAMS, params);
    }

    /**
     * Build the signature should be called last before creating the request
     *
     * @return self
     */
    public SignatureRequestBuilder build() {
        return build(Instant.now().getEpochSecond());
    }

    /**
     * Build the signature for a specific time stamp should be called last before creating the request
     *
     * @param created time stamp in seconds, must be > 0
     * @return self
     */
    public SignatureRequestBuilder build(long created) {
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

    public URI getTarget() {
        checkHasParameter(TARGET);
        return URI.create(parameters.get(TARGET).toString());
    }

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

    private void checkHasParameter(String key) {
        if (parameters.get(key) == null) {
            throw new IllegalArgumentException(String.format("Parameter '%s' must be set before continuing!", key));
        }
    }

    private void checkMethod() {
        if (parameters.containsKey(METHOD)) {
            throw new IllegalArgumentException(String.format("Method '%s' already set!", parameters.get(METHOD)));
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
                    .append(entry.getKey().toLowerCase())   // headers nee to be lower case
                    .append("\": ")
                    .append(entry.getValue());

            if (iterator.hasNext()) {
                signatureBase.append(System.lineSeparator());
            }
        }

        return signatureBase.toString();
    }

    /**
     * Content digest calculated with SHA-512
     *
     * @param content to create digest for
     * @return content digest
     * @throws NoSuchAlgorithmException in case digest can't be calculated
     */
    protected static String digestContentSha512(String content) throws NoSuchAlgorithmException {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content must not be null or empty!");
        }

        MessageDigest digest = MessageDigest.getInstance(DIGEST_ALGORITHM.toUpperCase());
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
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(getSignatureBase().getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            throw new IllegalStateException("Failed to generate signature!", ex);
        }
    }

    protected String getSignatureHeader() {
        return String.format("%s=:%s:", DEFAULT_SIGNATURE_ID, getSignature());
    }

    public LinkedHashMap<String, String> getHeaders() {
        checkIsBuild();

        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put(ACCEPT_HEADER, APPLICATION_JSON);

        if (parameters.containsKey(CONTENT_LENGTH_HEADER)) {
            headers.put(CONTENT_TYPE_HEADER, parameters.get(CONTENT_TYPE_HEADER).toString());
        }

        if (parameters.containsKey(CONTENT_DIGEST_HEADER)) {
            headers.put(CONTENT_DIGEST_HEADER, parameters.get(CONTENT_DIGEST_HEADER).toString());
        }

        if (parameters.containsKey(AUTHORIZATION_HEADER)) {
            headers.put(AUTHORIZATION_HEADER, parameters.get(AUTHORIZATION_HEADER).toString());
        }

        headers.put(SIGNATURE_INPUT_HEADER, getSignatureParamsHeader());
        headers.put(SIGNATURE_HEADER, getSignatureHeader());
        return headers;
    }

    public String getBody() {
        return body;
    }

    /**
     * Builds up signed request from all input data
     *
     * @param options client options (aka timeouts)
     * @return request
     */
    public HttpRequest getRequest(InterledgerClientOptions options) {
        return getBuilder(options).build();
    }

    public HttpRequest.Builder getBuilder(InterledgerClientOptions options) {
        // build if not already
        if (parameters.get(SIGNATURE_PARAMS) == null) {
            build();
        }

        checkIsBuild();

        HttpRequest.Builder builder = HttpRequest.newBuilder(getTarget());
        for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        switch (getMethod()) {
            case "POST":
                if (body == null || body.isBlank()) {
                    builder.POST(HttpRequest.BodyPublishers.noBody());
                } else {
                    builder.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
                }
                break;
            case "PUT":
                if (body == null || body.isBlank()) {
                    builder.PUT(HttpRequest.BodyPublishers.noBody());
                } else {
                    builder.PUT(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
                }
                break;
            default:
                builder.method(getMethod(), HttpRequest.BodyPublishers.noBody());
                break;
        }

        builder.timeout(Duration.of(options.timeOutInSeconds, SECONDS));
        return builder;
    }
}