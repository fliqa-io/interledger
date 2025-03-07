package io.fliqa.client.interledger.exception;

import io.fliqa.client.interledger.model.ApiError;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

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

    public static InterledgerClientException getApiException(ApiError error, HttpResponse<String> response) throws IOException {
        String message = formatExceptionMessage(error, response.statusCode());
        String body = response.body();
        if (response.body() == null) {
            body = "[no body]";
        }

        return new InterledgerClientException(response.statusCode(), message, response.headers(), body);
    }

    private static String formatExceptionMessage(ApiError error, int statusCode) {

        String code = error.code == null || error.code.isBlank() ? ">no error code<" : error.code;
        String description = error.description == null || error.description.isBlank() ? ">no error description<" : error.description;

        return "[" + statusCode + "](" + code + ") " + description;
    }
}
