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
package io.fliqa.client.interledger.exception;

import io.fliqa.client.interledger.model.ApiError;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

/**
 * Exception thrown when errors occur during Interledger API operations.
 *
 * <p>This exception encapsulates both network-level errors (timeouts, connection failures)
 * and application-level errors (invalid requests, authentication failures, server errors).
 * It provides access to HTTP status codes, response headers, and response bodies when
 * available to help with debugging and error handling.
 *
 * <h3>Error Categories</h3>
 * <ul>
 *   <li><strong>Network Errors</strong> - Connection timeouts, DNS failures, network unreachable</li>
 *   <li><strong>Client Errors (4xx)</strong> - Invalid requests, authentication failures, not found</li>
 *   <li><strong>Server Errors (5xx)</strong> - Interledger server internal errors, service unavailable</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <p>When catching this exception, applications can inspect the HTTP status code to determine
 * the appropriate response:
 * <pre>{@code
 * try {
 *     PaymentPointer wallet = client.getWallet(address);
 * } catch (InterledgerClientException e) {
 *     if (e.getCode() == 404) {
 *         // Wallet not found
 *     } else if (e.getCode() >= 500) {
 *         // Server error - retry may be appropriate
 *     }
 * }
 * }</pre>
 *
 * @author Fliqa
 * @version 1.0
 * @see ApiError
 * @since 1.0
 */
public class InterledgerClientException extends Exception {

    private static final long serialVersionUID = 1L;

    private int code = 0;
    private HttpHeaders responseHeaders = null;
    private String responseBody = null;

    /**
     * Creates a new exception wrapping an underlying cause.
     *
     * @param throwable the underlying cause of this exception
     */
    public InterledgerClientException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param message the error message describing what went wrong
     */
    public InterledgerClientException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with full HTTP response details and an underlying cause.
     *
     * @param message         the error message
     * @param throwable       the underlying cause
     * @param code            the HTTP status code
     * @param responseHeaders the HTTP response headers
     * @param responseBody    the HTTP response body
     */
    public InterledgerClientException(String message, Throwable throwable, int code, HttpHeaders responseHeaders, String responseBody) {
        super(message, throwable);
        this.code = code;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    /**
     * Creates a new exception with HTTP response details.
     *
     * @param message         the error message
     * @param code            the HTTP status code
     * @param responseHeaders the HTTP response headers
     * @param responseBody    the HTTP response body
     */
    public InterledgerClientException(String message, int code, HttpHeaders responseHeaders, String responseBody) {
        this(message, (Throwable) null, code, responseHeaders, responseBody);
    }

    public InterledgerClientException(String message, Throwable throwable, int code, HttpHeaders responseHeaders) {
        this(message, throwable, code, responseHeaders, null);
    }

    public InterledgerClientException(int code, HttpHeaders responseHeaders, String responseBody) {
        this((String) null, (Throwable) null, code, responseHeaders, responseBody);
    }

    public InterledgerClientException(int code, String message) {
        super(message);
        this.code = code;
    }

    public InterledgerClientException(int code, String message, HttpHeaders responseHeaders, String responseBody) {
        this(code, message);
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    /**
     * Get the HTTP status code.
     *
     * @return HTTP status code
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the HTTP response headers.
     *
     * @return Headers as an HttpHeaders object
     */
    public HttpHeaders getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Get the HTTP response body.
     *
     * @return Response body in the form of string
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Creates an exception from an Interledger API error response.
     *
     * <p>This factory method constructs a properly formatted exception from an API error
     * response, including the HTTP status code, headers, and structured error information.
     *
     * @param error    the parsed API error from the response body
     * @param response the HTTP response containing error details
     * @return a new exception with formatted error message and response details
     */
    public static InterledgerClientException getApiException(ApiError error, HttpResponse<String> response) {
        String message = formatExceptionMessage(error, response.statusCode());
        String body = response.body();
        if (response.body() == null) {
            body = "[no body]";
        }

        return new InterledgerClientException(response.statusCode(), message, response.headers(), body);
    }

    /**
     * Formats an exception message from API error details.
     *
     * @param error      the API error containing code and description
     * @param statusCode the HTTP status code
     * @return formatted error message in the format "[statusCode] (errorCode) description"
     */
    private static String formatExceptionMessage(ApiError error, int statusCode) {

        String code = error.code == null || error.code.isBlank() ? ">no error code<" : error.code;
        String description = error.description == null || error.description.isBlank() ? ">no error description<" : error.description;

        return "[" + statusCode + "] (" + code + ") " + description;
    }
}
