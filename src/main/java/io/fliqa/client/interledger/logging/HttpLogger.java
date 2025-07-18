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
package io.fliqa.client.interledger.logging;

import io.fliqa.client.interledger.utils.Assert;
import org.slf4j.Logger;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * Specialized HTTP request and response logger for debugging and monitoring API communications.
 *
 * <p>This logger is essential for debugging Interledger API interactions, especially when dealing with
 * cryptographically signed requests and complex payment flows. It provides detailed logging of HTTP
 * requests and responses at different log levels to support both development debugging and production
 * monitoring.
 *
 * <h2>Why This Logger is Needed</h2>
 * <ul>
 *   <li><strong>Payment Flow Debugging</strong> - Track Interledger payment process with detailed request/response logs</li>
 *   <li><strong>Signature Verification</strong> - Log signed request headers to verify Ed25519 signature generation</li>
 *   <li><strong>API Error Diagnosis</strong> - Capture full error responses for troubleshooting authentication and payment failures</li>
 *   <li><strong>Integration Testing</strong> - Monitor request/response patterns during integration with Interledger servers</li>
 *   <li><strong>Security Auditing</strong> - Log HTTP interactions for security analysis and compliance</li>
 *   <li><strong>Performance Monitoring</strong> - Track API response times and identify bottlenecks</li>
 * </ul>
 *
 * <h3>Log Levels</h3>
 * <ul>
 *   <li><strong>DEBUG</strong> - Basic request/response information (method, URI, status code)</li>
 *   <li><strong>TRACE</strong> - Full request/response details including headers and body content</li>
 * </ul>
 *
 * <h3>Security Considerations</h3>
 * <p><strong>Warning:</strong> TRACE level logging will log request and response bodies, which may contain
 * sensitive information such as:
 * <ul>
 *   <li>Access tokens and authorization headers</li>
 *   <li>Payment amounts and wallet addresses</li>
 *   <li>Cryptographic signatures and key identifiers</li>
 * </ul>
 * <p>Use TRACE logging only in development and testing environments. In production, use DEBUG level
 * to log essential information without exposing sensitive data.
 *
 * <h3>Usage in Payment Flows</h3>
 * <p>This logger is particularly valuable for debugging:
 * <ul>
 *   <li>Grant request and response cycles</li>
 *   <li>Quote generation and validation</li>
 *   <li>Payment creation and completion status</li>
 *   <li>Error responses from Interledger servers</li>
 *   <li>Signature header verification</li>
 * </ul>
 *
 * <h3>Request Body Handling</h3>
 * <p>Unlike standard HTTP loggers, this implementation uses a custom subscriber to capture
 * request bodies from the Java 11+ HTTP client's reactive streams API. This is necessary
 * because request bodies are consumed as they're sent, making them unavailable for
 * standard logging approaches.
 *
 * @author Fliqa
 * @version 1.0
 * @see java.net.http.HttpRequest
 * @see java.net.http.HttpResponse
 * @see org.slf4j.Logger
 * @since 1.0
 */
public class HttpLogger {

    private static final String LOG_SPACE = "    ";
    private static final String LOG_BODY = "body: ";
    private static final String LOG_NO_BODY = "<no body>";

    /**
     * SLF4J logger instance for outputting HTTP request/response information.
     */
    private final Logger LOGGER;

    /**
     * Creates a new HTTP logger with the specified SLF4J logger.
     *
     * @param logger SLF4J logger instance to use for output
     * @throws IllegalArgumentException if logger is null
     */
    public HttpLogger(Logger logger) {
        Assert.notNull(logger, "Logger must not be null");
        this.LOGGER = logger;
    }

    /**
     * Logs an HTTP request with appropriate detail level based on logger configuration.
     *
     * <p>Logging behavior:
     * <ul>
     *   <li><strong>DEBUG level:</strong> Logs method, URI, and basic request information</li>
     *   <li><strong>TRACE level:</strong> Logs complete request including headers and body</li>
     * </ul>
     *
     * <p>For requests with bodies, this method uses a custom subscriber to capture
     * the request body content as it's being sent, since the Java HTTP client
     * consumes the body stream during transmission.
     *
     * @param req HTTP request to log
     */
    public void logRequest(HttpRequest req) {

        Assert.notNull(req, "HTTP request must not be null");
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("HTTP Request:  ").append(req.method()).append(" ").append(req.uri());

        if (LOGGER.isTraceEnabled()) {  // log request details only if trace level is enabled
            logHeaders(req.headers(), logMessage);
            // define a consumer for how you want to log
            if (req.bodyPublisher().isPresent()) {
                Consumer<String> bodyConsumer = body -> {
                    if (body != null && !body.isBlank()) {
                        logMessage.append(System.lineSeparator()).append(LOG_SPACE).append(LOG_BODY).append(body);
                    }

                    LOGGER.trace(logMessage.toString());
                };

                req.bodyPublisher().get().subscribe(new HttpBodySubscriber(bodyConsumer));
            } else {
                logMessage.append(System.lineSeparator()).append(LOG_SPACE).append(LOG_NO_BODY);
                LOGGER.trace(logMessage.toString());
            }
        } else {
            LOGGER.debug(logMessage.toString());
        }
    }

    /**
     * Appends HTTP headers to the log message in a formatted manner.
     *
     * <p>Headers are formatted as "name: value1, value2" with proper indentation.
     * The ":status" pseudo-header is excluded since status codes are logged separately.
     *
     * @param headers    HTTP headers to log
     * @param logMessage StringBuilder to append formatted headers to
     */
    private static void logHeaders(HttpHeaders headers, StringBuilder logMessage) {
        headers.map().forEach((name, values) -> {
            if (!":status".equalsIgnoreCase(name)) { // ignore :status header (we already log this)
                logMessage.append(System.lineSeparator())
                        .append(LOG_SPACE).append(name).append(": ").append(String.join(", ", values));
            }
        });
    }

    /**
     * Logs an HTTP response with appropriate detail level based on logger configuration.
     *
     * <p>Logging behavior:
     * <ul>
     *   <li><strong>DEBUG level:</strong> Logs status code, URI, and basic response information</li>
     *   <li><strong>TRACE level:</strong> Logs complete response including headers and body</li>
     * </ul>
     *
     * <p>This is particularly useful for debugging API errors, monitoring payment
     * status responses, and verifying server responses during the Interledger
     * payment flow.
     *
     * @param res HTTP response to log
     */
    public void logResponse(HttpResponse<String> res) {
        Assert.notNull(res, "HTTP response must not be null");

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("HTTP Response: ").append(res.statusCode()).append(" ").append(res.uri());

        if (LOGGER.isTraceEnabled()) {
            logHeaders(res.headers(), logMessage);

            String body = res.body() != null ? res.body() : LOG_NO_BODY;
            logMessage.append(System.lineSeparator())
                    .append(LOG_SPACE)
                    .append(LOG_BODY)
                    .append(body);

            LOGGER.trace(logMessage.toString());
        } else {
            LOGGER.debug(logMessage.toString());
        }
    }

    /**
     * Custom subscriber for capturing HTTP request body content from reactive streams.
     *
     * <p>This subscriber is necessary because the Java 11+ HTTP client uses reactive
     * streams for request bodies, and the body content is consumed as it's sent to
     * the server. Standard logging approaches can't access the body content after
     * it has been consumed.
     *
     * <p>The subscriber converts ByteBuffer chunks to UTF-8 strings and passes them
     * to a consumer function for logging. This allows us to capture and log request
     * bodies without interfering with the actual HTTP request transmission.
     *
     * @see java.util.concurrent.Flow.Subscriber
     * @see java.nio.ByteBuffer
     */
    protected static class HttpBodySubscriber implements Flow.Subscriber<ByteBuffer> {

        /**
         * Consumer function that processes the captured body content.
         */
        private final Consumer<String> consumer;

        /**
         * Creates a new body subscriber with the specified consumer.
         *
         * @param consumer function to process captured body content
         */
        public HttpBodySubscriber(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        /**
         * Called when the subscription is established. Requests all available data.
         *
         * @param subscription the subscription for controlling data flow
         */
        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        /**
         * Called for each chunk of body data. Converts ByteBuffer to UTF-8 string
         * and passes it to the consumer for logging.
         *
         * @param item ByteBuffer containing body data chunk
         */
        @Override
        public void onNext(ByteBuffer item) {
            consumer.accept(new String(item.array(), StandardCharsets.UTF_8));
        }

        /**
         * Called when an error occurs during body streaming.
         * Currently, does not perform any error handling.
         *
         * @param throwable the error that occurred
         */
        @Override
        public void onError(Throwable throwable) {
        }

        /**
         * Called when the body streaming is complete.
         * Currently, does not perform any completion handling.
         */
        @Override
        public void onComplete() {
        }
    }
}
