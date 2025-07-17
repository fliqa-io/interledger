package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fliqa.client.interledger.serializer.WalletAddressSerializer;

import java.net.URI;

@JsonSerialize(using = WalletAddressSerializer.class)
public class WalletAddress {

    public final URI paymentPointer;

    public WalletAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null.");
        }
        this.paymentPointer = URI.create(address);
    }

    public WalletAddress(URI address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null.");
        }
        this.paymentPointer = address;
    }

    @Override
    public String toString() {
        return "WalletAddress{" +
                "paymentPointer=" + paymentPointer +
                '}';
    }
}
