package io.fliqa.client.interledger.model;

import java.net.*;

public class WalletAddress {

    public final URI paymentPointer;

    public WalletAddress(String address) {
        this.paymentPointer = URI.create(address);
    }
}
