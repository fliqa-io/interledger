package io.fliqa.client.interledger.signature;

import io.fliqa.client.interledger.model.AccessAction;
import io.fliqa.client.interledger.model.AccessItemType;
import io.fliqa.client.interledger.model.GrantRequest;
import io.fliqa.client.interledger.model.WalletAddress;
import io.fliqa.interledger.TestHelper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Set;

import static io.fliqa.client.interledger.signature.SignatureRequestBuilder.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SignatureRequestBuilderTest {

    @Test
    public void generateSignature() throws Exception {

        long createdTime = 1741002284L;
        SignatureRequestBuilder builder = new SignatureRequestBuilder(TestHelper.getPrivateKey(), TestHelper.CLIENT_KEY_ID)
                .method("POST")
                .target(URI.create("https://auth.interledger-test.dev"))
                .json("{\"access_token\":{\"access\":[{\"type\":\"incoming-payment\",\"actions\":[\"read\",\"complete\",\"create\"]}]},\"client\":\"https://ilp.interledger-test.dev/andrejfliqatestwallet\"}")
                .build(createdTime);

        // check signature base
        String base = builder.getSignatureBase();
        assertEquals(
                "\"@method\": POST\n" +
                        "\"@target-uri\": https://auth.interledger-test.dev/\n" +
                        "\"content-digest\": sha-512=:v2baXKn2bRWwis7fZwF4sB8B7I7izwCA5kybiVdCVb8nhD2kd0qf07hgK+p1Jaa00wQiEmOXKzlS6gurYKdBHA==:\n" +
                        "\"content-length\": 162\n" +
                        "\"content-type\": application/json\n" +
                        "\"@signature-params\": (\"@method\" \"@target-uri\" \"content-digest\" \"content-length\" \"content-type\");keyid=\"89675b1d-53f3-4fb6-b8ea-33a56e576cef\";created=" + createdTime,
                base);

        String signature = builder.getSignature();
        assertEquals("Muxo74zrmuf2gvAt5/mMd/BSKxMU4G80jhOHyCpzFocBQ0cnkRIej4NYYqS9fWkhfxAZD3T1mItYVRIh3gQ8Ag==", signature);

        // check headers
        LinkedHashMap<String, String> headers = builder.getHeaders();
        assertEquals("sig1=:Muxo74zrmuf2gvAt5/mMd/BSKxMU4G80jhOHyCpzFocBQ0cnkRIej4NYYqS9fWkhfxAZD3T1mItYVRIh3gQ8Ag==:", headers.get(SIGNATURE_HEADER));
        assertEquals("sha-512=:v2baXKn2bRWwis7fZwF4sB8B7I7izwCA5kybiVdCVb8nhD2kd0qf07hgK+p1Jaa00wQiEmOXKzlS6gurYKdBHA==:", headers.get(CONTENT_DIGEST_HEADER));
        assertEquals("application/json", headers.get(CONTENT_TYPE_HEADER));
        assertEquals("application/json", headers.get(ACCEPT_HEADER));
    }

    @Test
    public void generateSignatureWithGrantRequest() throws Exception {

        GrantRequest grantRequest = GrantRequest.build(new WalletAddress(TestHelper.CLIENT_WALLET_ADDRESS),
                AccessItemType.incomingPayment,
                Set.of(AccessAction.read, AccessAction.complete, AccessAction.create));

        SignatureRequestBuilder builder = new SignatureRequestBuilder(TestHelper.getPrivateKey(), TestHelper.CLIENT_KEY_ID)
                .method("POST")
                .target(URI.create("https://auth.interledger-test.dev"))
                .json(grantRequest)
                .build(1741002284L);

        String signature = builder.getSignature();
        assertEquals("eJRoTpNxgTK88ujbipcn0/8TDf0oUhbqTgOoKcOFnUslyNYYBk7Ar/5Mw8HvBubrQMbGt+AcjONb/6sKC8IXAQ==", signature);
    }
}