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

public class InterledgerClientOptions {

    // HTTP connection
    public final int connectTimeOutInSeconds;
    public final int timeOutInSeconds;

    /**
     * Expiration time in seconds for pending transaction (i.e. 5minutes ... )
     */
    public final int transactionExpirationInSeconds;

    /**
     * @param connectTimeoutInSeconds        Timeout for establishing connections, in seconds.
     * @param timeoutInSeconds               General timeout for operations, in seconds.
     * @param transactionExpirationInSeconds Transaction expiration time in seconds
     */
    public InterledgerClientOptions(int connectTimeoutInSeconds,
                                    int timeoutInSeconds,
                                    int transactionExpirationInSeconds) {
        this.connectTimeOutInSeconds = connectTimeoutInSeconds;
        this.timeOutInSeconds = timeoutInSeconds;
        this.transactionExpirationInSeconds = transactionExpirationInSeconds;
    }

    public static final InterledgerClientOptions DEFAULT =
            new InterledgerClientOptions(10, 10, 10 * 60);
}