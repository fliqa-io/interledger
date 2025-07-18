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
package io.fliqa.client.interledger;

/**
 * Configuration options for the Interledger API client.
 *
 * <p>This class encapsulates HTTP client configuration including connection timeouts,
 * request timeouts, and transaction expiration settings. These options control the
 * behavior of the underlying HTTP client and payment processing timeouts.
 *
 * <h2>Timeout Settings</h2>
 * <ul>
 *   <li><strong>Connect Timeout</strong> - Maximum time to establish HTTP connections</li>
 *   <li><strong>Request Timeout</strong> - Maximum time for individual HTTP requests</li>
 *   <li><strong>Transaction Expiration</strong> - How long pending payments remain valid</li>
 * </ul>
 *
 * <h3>Default Configuration</h3>
 * <p>The default configuration provides reasonable timeouts for most use cases:
 * <ul>
 *   <li>10 second connection timeout</li>
 *   <li>10 seconds request timeout</li>
 *   <li>10 minutes transaction expiration</li>
 * </ul>
 *
 * @author Fliqa
 * @version 1.0
 * @see InterledgerApiClientImpl
 * @since 1.0
 */
public class InterledgerClientOptions {

    /**
     * Timeout for establishing HTTP connections, in seconds.
     */
    public final int connectTimeOutInSeconds;

    /**
     * General timeout for HTTP operations, in seconds.
     */
    public final int timeOutInSeconds;

    /**
     * Expiration time in seconds for pending transactions.
     * After this time, pending payments will be considered expired.
     */
    public final int transactionExpirationInSeconds;

    /**
     * Creates a new configuration with custom timeout settings.
     *
     * @param connectTimeoutInSeconds        timeout for establishing HTTP connections, in seconds
     * @param timeoutInSeconds               general timeout for HTTP operations, in seconds
     * @param transactionExpirationInSeconds expiration time for pending transactions, in seconds
     */
    public InterledgerClientOptions(int connectTimeoutInSeconds,
                                    int timeoutInSeconds,
                                    int transactionExpirationInSeconds) {
        this.connectTimeOutInSeconds = connectTimeoutInSeconds;
        this.timeOutInSeconds = timeoutInSeconds;
        this.transactionExpirationInSeconds = transactionExpirationInSeconds;
    }

    /**
     * Default configuration with reasonable timeout values.
     * Uses 10 second connection and request timeouts, and 10 minute transaction expiration.
     */
    public static final InterledgerClientOptions DEFAULT =
            new InterledgerClientOptions(10, 10, 10 * 60);
}