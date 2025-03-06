package io.fliqa.client.interledger.signature;

import io.fliqa.client.interledger.model.AccessAction;
import io.fliqa.client.interledger.model.AccessItemType;
import io.fliqa.client.interledger.model.GrantAccessRequest;
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
                        "\"@signature-params\": (\"@method\" \"@target-uri\" \"content-digest\" \"content-length\" \"content-type\");keyid=\"761fbd9c-16a6-4e19-a4cf-0f4076d78469\";created=" + createdTime,
                base);

        String signature = builder.getSignature();
        assertEquals("LZyYeDxkEkKOYV1Xu8+G2RsjpTj/M3nJa6EBSS/SUOHPeeJlLzMdjSkYvwFvAul/0lMoOVEPsKBes30QkRqxAQ==", signature);

        // check headers
        LinkedHashMap<String, String> headers = builder.getHeaders();
        assertEquals("sig1=:LZyYeDxkEkKOYV1Xu8+G2RsjpTj/M3nJa6EBSS/SUOHPeeJlLzMdjSkYvwFvAul/0lMoOVEPsKBes30QkRqxAQ==:", headers.get(SIGNATURE_HEADER));
        assertEquals("sha-512=:v2baXKn2bRWwis7fZwF4sB8B7I7izwCA5kybiVdCVb8nhD2kd0qf07hgK+p1Jaa00wQiEmOXKzlS6gurYKdBHA==:", headers.get(CONTENT_DIGEST_HEADER));
        assertEquals("application/json", headers.get(CONTENT_TYPE_HEADER));
        assertEquals("application/json", headers.get(ACCEPT_HEADER));
    }

    @Test
    public void generateSignatureWithGrantRequest() throws Exception {

        GrantAccessRequest grantRequest = GrantAccessRequest.build(new WalletAddress(TestHelper.CLIENT_WALLET_ADDRESS),
                AccessItemType.incomingPayment,
                Set.of(AccessAction.read, AccessAction.complete, AccessAction.create));

        SignatureRequestBuilder builder = new SignatureRequestBuilder(TestHelper.getPrivateKey(), TestHelper.CLIENT_KEY_ID)
                .method("POST")
                .target(URI.create("https://auth.interledger-test.dev"))
                .json(grantRequest)
                .build(1741002284L);

        String signature = builder.getSignature();
        assertEquals("7NHDvPz0p3lVPCb+T4z+AK/V6x0jrO8KZu7W/F+t0LbahVnfBi/8YDXSt6EjpXoZp2pIZe2hc2tm9GGUvCH5AA==", signature);
    }
}