package io.fliqa.client.interledger;

public class InterledgerClientOptions {

    public final int connectTimeOutInSeconds;
    public final int timeOutInSeconds;

    /**
     * @param connectTimeout Timeout for establishing connections, in seconds.
     * @param timeout        General timeout for operations, in seconds.
     */
    public InterledgerClientOptions(int connectTimeout, int timeout) {
        this.connectTimeOutInSeconds = connectTimeout;
        this.timeOutInSeconds = timeout;
    }

    public static final InterledgerClientOptions DEFAULT = new InterledgerClientOptions(10, 10);
}