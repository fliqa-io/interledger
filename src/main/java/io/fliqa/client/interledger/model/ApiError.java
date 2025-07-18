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
package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Represents an API error response from Interledger Open Payments servers.
 * 
 * <p>This class encapsulates error information returned by the Interledger API
 * when requests fail. It provides both a human-readable description and a
 * machine-readable error code for programmatic error handling.
 * 
 * <p>API errors are typically returned as JSON responses with HTTP status codes
 * indicating the type of error (4xx for client errors, 5xx for server errors).
 * The error object is wrapped in an "error" root element as specified by the
 * JSON serialization configuration.
 * 
 * <p>Example error response:
 * <pre>
 * {
 *   "error": {
 *     "code": "invalid_request",
 *     "description": "The request is missing required parameters"
 *   }
 * }
 * </pre>
 * 
 * @see io.fliqa.client.interledger.exception.InterledgerClientException
 * @author Fliqa
 * @version 1.0
 * @since 1.0
 */
@JsonRootName("error")
public class ApiError {

    /**
     * Human-readable error description.
     * 
     * <p>Provides a detailed explanation of what went wrong with the request.
     * This field is intended for developers and debugging purposes, offering
     * context about the error condition.
     * 
     * <p>Examples:
     * <ul>
     *   <li>"The request is missing required parameters"</li>
     *   <li>"Invalid authentication credentials"</li>
     *   <li>"The requested resource was not found"</li>
     *   <li>"Internal server error occurred"</li>
     * </ul>
     */
    @JsonProperty("description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String description;

    /**
     * Unique error code for programmatic error handling.
     * 
     * <p>Provides a machine-readable identifier for the specific error condition.
     * Error codes allow applications to handle different types of errors
     * programmatically without parsing error descriptions.
     * 
     * <p>Common error codes include:
     * <ul>
     *   <li>"invalid_request" - Malformed or missing required parameters</li>
     *   <li>"unauthorized" - Authentication required or invalid credentials</li>
     *   <li>"forbidden" - Access denied for the requested resource</li>
     *   <li>"not_found" - Requested resource does not exist</li>
     *   <li>"internal_error" - Server-side error occurred</li>
     * </ul>
     */
    @JsonProperty(value = "code")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String code;
}