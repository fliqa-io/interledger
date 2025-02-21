package io.fliqa.interledger;

public class TestHelper {

    public static String CLIENT_WALLET_ADDRESS = "https://ilp.interledger-test.dev/andrejfliqatestwallet";
    public static String CLIENT_PRIVATE_KEY = """
        -----BEGIN PRIVATE KEY-----
        MC4CAQAwBQYDK2VwBCIEIMoPnUbc3RNGyN1tiB9gEVcjrq4kCi1CAnFwqhl9GsZ+
        -----END PRIVATE KEY-----""";
    // TODO: move key to file that is not included in source control ... OK for now

    public static String CLIENT_KEY_ID = "89675b1d-53f3-4fb6-b8ea-33a56e576cef";

    public static String SENDER_WALLET_ADDRESS = "https://ilp.interledger-test.dev/sender";
    public static String RECEIVER_WALLET_ADDRESS = "https://ilp.interledger-test.dev/reciever";

}
