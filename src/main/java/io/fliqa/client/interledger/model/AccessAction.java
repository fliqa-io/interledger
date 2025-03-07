package io.fliqa.client.interledger.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccessAction {

    read("read"),
    complete("complete"),
    create("create"),
    read_all("read-all"),
    list("list"),
    list_all("list-all");

    public final String value;

    AccessAction(String name) {
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
    public static AccessAction fromValue(String value) {
        for (AccessAction item : AccessAction.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown AccessAction: '" + value + "'.");
    }
}
