package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccessItemType {

    incomingPayment("incoming-payment"),
    outgoingPayment("outgoing-payment"),
    quote("quote");

    public final String value;

    AccessItemType(String name) {
        this.value = name;
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AccessItemType fromValue(String value) {
        for (AccessItemType item : AccessItemType.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown AccessItemType: '" + value + "'");
    }
}
