package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fliqa.client.interledger.serializer.WalletAddressSerializer;

import java.net.URI;

@JsonSerialize(using = WalletAddressSerializer.class)
public class WalletAddress {

    public final URI paymentPointer;

    public WalletAddress(String address) {
        this.paymentPointer = URI.create(address);
    }
}
