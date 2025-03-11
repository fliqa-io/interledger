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