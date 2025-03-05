package io.fliqa.client.interledger;

import io.fliqa.client.interledger.exception.InterledgerClientException;
import io.fliqa.client.interledger.model.PaymentPointer;
import io.fliqa.client.interledger.model.PendingGrant;
import io.fliqa.client.interledger.model.WalletAddress;

public interface InterledgerApiClient {

    PaymentPointer getWallet(WalletAddress address) throws InterledgerClientException;

    PendingGrant createPendingGrant(PaymentPointer receiver) throws InterledgerClientException;
}
