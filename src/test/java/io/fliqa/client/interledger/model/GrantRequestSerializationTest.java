/*
 * Copyright 2025 Fliqa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

        GrantAccessRequest grantRequest = new GrantAccessRequest(new WalletAddress("https://ilp.interledger-test.dev/andrejfliqatestwallet"));
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