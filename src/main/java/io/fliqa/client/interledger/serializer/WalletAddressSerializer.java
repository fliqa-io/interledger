package io.fliqa.client.interledger.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.fliqa.client.interledger.model.WalletAddress;

import java.io.IOException;

public class WalletAddressSerializer extends JsonSerializer<WalletAddress> {

    @Override
    public void serialize(WalletAddress address, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(address.paymentPointer.toString());
    }
}
