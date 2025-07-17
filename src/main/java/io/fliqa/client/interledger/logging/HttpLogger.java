package io.fliqa.client.interledger.logging;

import org.slf4j.Logger;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * We need a special logger in order to capture request body
 * NOTE: same class is also used in Billing / LagoClient API module
 */
public class HttpLogger {

    private static final String LOG_SPACE = "    ";
    private static final String LOG_BODY = "body: ";
    private static final String LOG_NO_BODY = "<no body>";

    private final Logger LOGGER;

    public HttpLogger(Logger logger) {
        this.LOGGER = logger;
    }

    public void logRequest(HttpRequest req) {

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

    private static void logHeaders(HttpHeaders headers, StringBuilder logMessage) {
        headers.map().forEach((name, values) -> {
            if (!":status".equalsIgnoreCase(name)) { // ignore :status header (we already log this)
                logMessage.append(System.lineSeparator())
                        .append(LOG_SPACE).append(name).append(": ").append(String.join(", ", values));
            }
        });
    }

    public void logResponse(HttpResponse<String> res) {
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

    protected static class HttpBodySubscriber implements Flow.Subscriber<ByteBuffer> {

        private final Consumer<String> consumer;

        public HttpBodySubscriber(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            consumer.accept(new String(item.array(), StandardCharsets.UTF_8));
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onComplete() {
        }
    }
}
