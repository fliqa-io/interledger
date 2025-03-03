package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fliqa.client.interledger.InterledgerObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrantRequestSerializationTest {

    final static ObjectMapper MAPPER = InterledgerObjectMapper.get();

    @Test
    public void testSerialize() throws Exception {

        GrantRequest grantRequest = new GrantRequest(new WalletAddress("https://ilp.interledger-test.dev/andrejfliqatestwallet"));
        grantRequest.accessToken = new AccessToken();

        AccessItem incomingPayment = new AccessItem();
        incomingPayment.accessType = AccessItemType.incomingPayment;
        incomingPayment.actions = Set.of(AccessAction.read, AccessAction.complete, AccessAction.create);

        grantRequest.accessToken.access = Set.of(incomingPayment);

        String json = MAPPER.writeValueAsString(grantRequest);

        assertEquals("{\"client\":\"https://ilp.interledger-test.dev/andrejfliqatestwallet\",\"access_token\":{\"access\":[{\"type\":\"incoming-payment\",\"actions\":[\"complete\",\"create\",\"read\"]}]}}",
                json);
    }
}