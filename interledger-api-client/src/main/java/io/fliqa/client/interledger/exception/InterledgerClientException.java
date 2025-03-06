package io.fliqa.client.interledger.exception;

import java.net.http.HttpHeaders;

public class InterledgerClientException extends Exception {

    private static final long serialVersionUID = 1L;

    private int code = 0;
    private HttpHeaders responseHeaders = null;
    private String responseBody = null;

    public InterledgerClientException(Throwable throwable) {
        super(throwable);
    }

    public InterledgerClientException(String message) {
        super(message);
    }

    public InterledgerClientException(String message, Throwable throwable, int code, HttpHeaders responseHeaders, String responseBody) {
        super(message, throwable);
        this.code = code;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

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
}
