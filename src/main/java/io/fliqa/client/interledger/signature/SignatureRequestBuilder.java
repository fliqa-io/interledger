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
package io.fliqa.client.interledger.signature;

import io.fliqa.client.interledger.InterledgerClientOptions;
import io.fliqa.client.interledger.InterledgerObjectMapper;
import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.utils.Assert;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Builder for creating cryptographically signed HTTP requests for Interledger API communication.
 *
 * <p>This class implements the HTTP Message Signatures specification (RFC 9421) to create
 * signed HTTP requests that ensure authenticity and integrity when communicating with
 * Interledger Open Payments servers. All requests are signed using Ed25519 cryptographic
 * signatures.
 *
 * <h3>Signature Process</h3>
 * <p>The signing process involves several steps:
 * <ol>
 *   <li><strong>Request Building</strong> - Set HTTP method, target URI, and request body</li>
 *   <li><strong>Header Generation</strong> - Calculate content digest, length, and authorization</li>
 *   <li><strong>Signature Base</strong> - Create signature base from headers and metadata</li>
 *   <li><strong>Signing</strong> - Generate Ed25519 signature of the signature base</li>
 *   <li><strong>Header Assembly</strong> - Add signature and signature input headers</li>
 * </ol>
 *
 * <h3>Supported Features</h3>
 * <ul>
 *   <li>Ed25519 digital signatures for request authentication</li>
 *   <li>SHA-512 content digest calculation for request integrity</li>
 *   <li>Support for GET, POST, PUT, DELETE, and HEAD methods</li>
 *   <li>JSON request body serialization and content-type handling</li>
 *   <li>Bearer token authorization with GNAP format</li>
 *   <li>Configurable request timeouts</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * SignatureRequestBuilder builder = new SignatureRequestBuilder(privateKey, keyId)
 *     .POST(requestBody)
 *     .target("https://api.example.com/payments")
 *     .accessToken("example-access-token")
 *     .build();
 *
 * HttpRequest signedRequest = builder.getRequest(clientOptions);
 * }</pre>
 *
 * <h3>Security Considerations</h3>
 * <ul>
 *   <li>Private keys should be securely stored and not logged</li>
 *   <li>Signatures include timestamps to prevent replay attacks</li>
 *   <li>Content digests ensure request body integrity</li>
 *   <li>All signature parameters are included in the signature calculation</li>
 * </ul>
 *
 * @author Fliqa
 * @version 1.0
 * @see java.security.PrivateKey
 * @see java.net.http.HttpRequest
 * @since 1.0
 */
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
    public static final String ACCEPT_HEADER = "Accept";

    public static final String APPLICATION_JSON = "application/json";

    static final String DEFAULT_SIGNATURE_ID = "sig1";

    /*
     * Digest algorithm
     */
    static final String DIGEST_ALGORITHM = "SHA-512";
    static final String SIGNATURE_ALGORITHM = "Ed25519";

    static final Set<String> ALLOWED_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "HEAD");

    /**
     * Ed25519 private key used for signing requests.
     */
    final PrivateKey privateKey;

    /**
     * Identifier for the private key, included in signature headers.
     */
    final String keyId;

    /**
     * Unix timestamp when the signature was created (for replay protection).
     */
    private long created = 0;

    /**
     * Ordered map of signature parameters and headers.
     * Order is important for signature base calculation.
     */
    final LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

    /**
     * JSON mapper for serializing request bodies.
     */
    private final InterledgerObjectMapper mapper;

    /**
     * Request body content (typically JSON).
     */
    private String body;

    /**
     * Creates a new signature request builder with custom JSON mapper.
     *
     * @param privateKey Ed25519 private key for signing requests (must not be null)
     * @param keyId      identifier for the private key (must not be null or blank)
     * @param mapper     JSON mapper for serializing objects, uses default if null
     * @throws AssertionError if privateKey is null or keyId is null/blank
     */
    public SignatureRequestBuilder(PrivateKey privateKey,
                                   String keyId,
                                   InterledgerObjectMapper mapper) {

        Assert.notNull(privateKey, "PrivateKey cannot be null");
        Assert.notNullOrEmpty(keyId, "KeyId cannot be null or empty");

        this.privateKey = privateKey;
        this.keyId = keyId;

        if (mapper == null) {
            mapper = new InterledgerObjectMapper();
        }

        this.mapper = mapper;
    }

    /**
     * Creates a new signature request builder with default JSON mapper.
     *
     * @param privateKey Ed25519 private key for signing requests (must not be null)
     * @param keyId      identifier for the private key (must not be null or blank)
     * @throws AssertionError if privateKey is null or keyId is null/blank
     */
    public SignatureRequestBuilder(PrivateKey privateKey,
                                   String keyId) {

        this(privateKey, keyId, null);
    }

    /**
     * Sets the HTTP method for the request.
     *
     * @param value HTTP method (GET, POST, PUT, DELETE, HEAD)
     * @return this builder for method chaining
     * @throws IllegalArgumentException if method is null, blank, or not allowed
     */
    public SignatureRequestBuilder method(String value) {
        Assert.notNullOrEmpty(value, "Method cannot be null or empty!");
        Assert.isTrue(ALLOWED_METHODS.contains(value), String.format("Method '%s' is not allowed. Allowed methods are: %s", value, String.join(", ", ALLOWED_METHODS)));

        parameters.put(METHOD, value.toUpperCase());
        return this;
    }

    /**
     * Sets the request method to POST and serializes the body as JSON.
     *
     * @param body object to serialize as JSON request body
     * @return this builder for method chaining
     * @throws IllegalArgumentException if method is already set or body serialization fails
     */
    public SignatureRequestBuilder POST(Object body) {
        checkMethod();
        return method("POST").json(body);
    }

    public SignatureRequestBuilder POST() {
        checkMethod();
        return method("POST");
    }

    public SignatureRequestBuilder PUT(Object body) {
        checkMethod();
        return method("PUT").json(body);
    }

    /**
     * Sets the request method to GET.
     *
     * @return this builder for method chaining
     * @throws IllegalArgumentException if method is already set
     */
    public SignatureRequestBuilder GET() {
        checkMethod();
        return method("GET");
    }

    public SignatureRequestBuilder DELETE() {
        checkMethod();
        return method("DELETE");
    }

    /**
     * Gets the configured HTTP method.
     * 
     * @return the HTTP method (GET, POST, PUT, DELETE, HEAD)
     * @throws IllegalArgumentException if method has not been set
     */
    public String getMethod() {
        checkHasParameter(METHOD);
        return parameters.get(METHOD).toString();
    }

    public SignatureRequestBuilder target(String value) {
        return target(URI.create(value));
    }

    /**
     * Sets the target URI for the request.
     *
     * <p>The URI is normalized to ensure it ends with '/' when no query parameters
     * are present, as required by the signature specification.
     *
     * @param value target URI for the HTTP request
     * @return this builder for method chaining
     * @throws IllegalArgumentException if URI is null
     */
    public SignatureRequestBuilder target(URI value) {
        parameters.put(TARGET, prepareTarget(value));
        return this;
    }

    /**
     * Sets the request body by serializing an object to JSON.
     * 
     * <p>This method automatically serializes the provided object to JSON using
     * the configured ObjectMapper and then calls {@link #json(String)} to set
     * all required headers.
     * 
     * @param object object to serialize as JSON request body
     * @return this builder for method chaining
     * @throws IllegalArgumentException if object serialization fails
     */
    public SignatureRequestBuilder json(Object object) {

        try {
            String value = mapper.writeValueAsString(object);
            return json(value);
        } catch (InterledgerClientException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Sets the request body as JSON and calculates required headers.
     *
     * <p>This method automatically:
     * <ul>
     *   <li>Sets Content-Type to application/json</li>
     *   <li>Calculates Content-Length header</li>
     *   <li>Generates SHA-512 Content-Digest header</li>
     * </ul>
     *
     * @param json JSON string to set as request body
     * @return this builder for method chaining
     * @throws IllegalArgumentException if JSON is null or blank
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

    /**
     * Sets the access token for authorization.
     *
     * <p>The token is formatted as a GNAP (Grant Negotiation and Authorization Protocol)
     * bearer token in the Authorization header.
     *
     * @param token access token for API authorization
     * @return this builder for method chaining
     * @throws IllegalArgumentException if token is null or blank
     */
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
     * Builds the signature using the current timestamp.
     *
     * <p>This method should be called after all request parameters have been set
     * and before retrieving headers or creating the HTTP request. It generates
     * the signature parameters and prepares the builder for signature calculation.
     *
     * @return this builder for method chaining
     */
    public SignatureRequestBuilder build() {
        return build(Instant.now().getEpochSecond());
    }

    /**
     * Builds the signature using a specific timestamp.
     *
     * <p>This method allows setting a custom timestamp for signature creation,
     * which is useful for testing or when you need precise control over the
     * signature timestamp for replay attack prevention.
     *
     * @param created Unix timestamp in seconds when the signature was created
     * @return this builder for method chaining
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

        // We must fix target so it conforms to expectations
        String out = value.toString();
        if (!out.endsWith("/") && value.getQuery() == null) {
            return out + "/";
        }

        return out;
    }

    /**
     * Gets the configured target URI.
     * 
     * @return the target URI for the HTTP request
     * @throws IllegalArgumentException if target has not been set
     */
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
     * Calculates SHA-512 content digest for request body integrity.
     * 
     * <p>This method generates a SHA-512 hash of the content and returns it
     * as a Base64-encoded string. The content digest is used to ensure that
     * the request body has not been tampered with during transmission.
     * 
     * @param content request body content to create digest for
     * @return Base64-encoded SHA-512 digest of the content
     * @throws IllegalArgumentException if content is null or empty
     * @throws NoSuchAlgorithmException if SHA-512 algorithm is not available
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

    /**
     * Gets all HTTP headers for the signed request.
     * 
     * <p>This method returns all headers required for the signed request including:
     * <ul>
     *   <li>Accept header (application/json)</li>
     *   <li>Content-Type header (if body is present)</li>
     *   <li>Content-Digest header (if body is present)</li>
     *   <li>Authorization header (if access token is set)</li>
     *   <li>Signature-Input header with signature parameters</li>
     *   <li>Signature header with the actual signature</li>
     * </ul>
     * 
     * @return ordered map of HTTP headers for the request
     * @throws IllegalStateException if signature has not been built yet
     */
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
     * Creates a fully signed HTTP request from all configured parameters.
     *
     * <p>This method combines all the configured parameters, headers, and signature
     * information to create a complete HTTP request ready for execution. If the
     * signature has not been built yet, it will be built automatically.
     *
     * @param options client configuration including request timeouts
     * @return signed HTTP request ready for execution
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

        if (body == null || body.isBlank()) {
            builder.method(getMethod(), HttpRequest.BodyPublishers.noBody());
        } else {
            builder.method(getMethod(), HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }

        builder.timeout(Duration.of(options.timeOutInSeconds, SECONDS));
        return builder;
    }
}