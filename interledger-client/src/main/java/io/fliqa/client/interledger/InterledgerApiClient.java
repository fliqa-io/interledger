package io.fliqa.client.interledger;

import io.fliqa.client.interledger.model.*;

import java.io.*;
import java.security.*;

public interface InterledgerApiClient {

    PaymentPointer getWallet(WalletAddress address) throws IOException, InterruptedException;

    PendingGrant createPendingGrant(PaymentPointer receiver) throws IOException, InterruptedException, NoSuchAlgorithmException, SignatureException, InvalidKeyException;
}
