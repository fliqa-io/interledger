package io.fliqa.client.interledger.exception;

public class InterledgerClientException extends Exception {

    public InterledgerClientException(String message) {
        super(message);
    }

    public InterledgerClientException(String message, Exception e) {
        super(message, e);
    }
}
