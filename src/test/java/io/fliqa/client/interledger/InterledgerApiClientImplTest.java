package io.fliqa.client.interledger;

import io.fliqa.client.interledger.model.WalletAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterledgerApiClientImplTest {

    private InterledgerApiClientImpl client;

    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException {
        // Create test client instance for accessing protected methods
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Ed25519");
        KeyPair keyPair = keyGen.generateKeyPair();
        WalletAddress walletAddress = new WalletAddress("$example.com/alice");

        client = new InterledgerApiClientImpl(walletAddress, keyPair.getPrivate(), "test-key-id");
    }

    @Test
    public void buildResourceUrl_withLeadingSlash() {
        URI baseUri = URI.create("https://example.com");
        URI result = client.buildResourceUrl(baseUri, "/incoming-payments");

        assertEquals("https://example.com/incoming-payments", result.toString());
    }

    @Test
    public void buildResourceUrl_withLongRoot() {
        URI baseUri = URI.create("https://ilp.interledger-test.dev/f537937b-7016-481b-b655-9f0d1014822c");
        URI result = client.buildResourceUrl(baseUri, "/incoming-payments");

        assertEquals("https://ilp.interledger-test.dev/f537937b-7016-481b-b655-9f0d1014822c/incoming-payments", result.toString());
    }

    @Test
    public void buildResourceUrl_withLongRoot_withSlash() {
        URI baseUri = URI.create("https://ilp.interledger-test.dev/f537937b-7016-481b-b655-9f0d1014822c/");
        URI result = client.buildResourceUrl(baseUri, "/incoming-payments");

        assertEquals("https://ilp.interledger-test.dev/f537937b-7016-481b-b655-9f0d1014822c/incoming-payments", result.toString());
    }

    @Test
    public void buildResourceUrl_withoutLeadingSlash() {
        URI baseUri = URI.create("https://example.com");
        URI result = client.buildResourceUrl(baseUri, "incoming-payments");

        assertEquals("https://example.com/incoming-payments", result.toString());
    }

    @Test
    public void buildResourceUrl_baseUriWithPath() {
        URI baseUri = URI.create("https://example.com/api/v1");
        URI result = client.buildResourceUrl(baseUri, "/quotes");

        assertEquals("https://example.com/api/v1/quotes", result.toString());
    }

    @Test
    public void buildResourceUrl_baseUriWithTrailingSlash() {
        URI baseUri = URI.create("https://example.com/");
        URI result = client.buildResourceUrl(baseUri, "/outgoing-payments");

        assertEquals("https://example.com/outgoing-payments", result.toString());
    }

    @Test
    public void buildResourceUrl_baseUriWithPort() {
        URI baseUri = URI.create("https://example.com:8443");
        URI result = client.buildResourceUrl(baseUri, "/incoming-payments");

        assertEquals("https://example.com:8443/incoming-payments", result.toString());
    }

    @Test
    public void buildResourceUrl_complexPath() {
        URI baseUri = URI.create("https://example.com");
        URI result = client.buildResourceUrl(baseUri, "/api/v1/payments");

        assertEquals("https://example.com/api/v1/payments", result.toString());
    }

    @Test
    public void buildResourceUrl_emptyPath() {
        URI baseUri = URI.create("https://example.com");
        URI result = client.buildResourceUrl(baseUri, "");

        assertEquals("https://example.com/", result.toString());
    }

    @Test
    public void buildResourceUrl_nullBaseUri() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            client.buildResourceUrl(null, "/incoming-payments");
        });

        assertEquals("Base URI cannot be null", exception.getMessage());
    }

    @Test
    public void buildResourceUrl_nullPath() {
        URI baseUri = URI.create("https://example.com");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            client.buildResourceUrl(baseUri, null);
        });

        assertEquals("Path cannot be null", exception.getMessage());
    }

    @Test
    public void buildResourceUrl_pathWithQueryParams() {
        URI baseUri = URI.create("https://example.com");
        URI result = client.buildResourceUrl(baseUri, "/payments?status=pending");

        assertEquals("https://example.com/payments?status=pending", result.toString());
    }
}