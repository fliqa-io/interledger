package io.fliqa.client.interledger.model;

public enum AccessItemType {

    incomingPayment("incoming-payment"),
    outgoingPayment("outgoing-payment"),
    quote("quote");

    public final String name;

    AccessItemType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }


}
