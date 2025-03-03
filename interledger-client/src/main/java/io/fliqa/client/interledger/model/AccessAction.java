package io.fliqa.client.interledger.model;

public enum AccessAction {

    read("read"),
    complete("complete"),
    create("create");

    public final String name;

    AccessAction(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
