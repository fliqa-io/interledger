# Interledger Open payments API client

Run:
> ./gradlew clean build

To run integration tests only:
> ./gradlew integrationTest

Publish locally
> ./gradlew clean publishToMavenLocal -Prelease.version=<VERSION>

## Interledger Open payment protocol

Following section explains how Fliqa uses the Open payment protocol.
We have three "players":

- Fliqa (the payment **initiator**)
- Tenant (the payment **receiver**)
- User (the payment **sender**)

### Interledger lingo explained

- **asset type** = currency, currently supported:
    - EUR (Euro),
    - GBP (Pound Sterling),
    - USD (US Dollar),
    - SGD (Singaporean Dollar)
    - ZAR (South African Rand)
    - MXN (Mexican Peso)
- **payment pointer** = account number / wallet address (aka where the assets are deposited/withdrawn)
    - this is typically a URL address
- **account** = holds one or multiple payment pointers and is locked to an asset type
- **quote** = transaction fee

### 1. Fliqa initiates payment

Using Fliqa's **private key** and **key id** and the _receiver_ **payment pointer** to

- get a grand request
- create an incoming payment to the _recevier_ wallet (aka payment pointer)

> **NOTE:** The receiver payment pointer is already entered in the Tenant's Point of sale (same as IBAN).

### 2. Fliqa creates a quote request

A quote is the cost of the transaction (aka fee) from the sender to the receiver payment pointer (wallet).  
In order to do this we need the sender payment pointer.

> **NOTE:** This is the point where the sender enters his payment pointer / same as entering IBAN

The quote is made for the incoming payment on the _sender_ **payment pointer**

### 3. Redirect user to confirm the payment

Once we have the quote we can create a redirect link for the sender to confirm the payment.
  
> **NOTE:** User is redirected to his wallet where he confirms / denies the payment

### 4. Finalization of payment

If user has confirmed the payment Fliqa needs to finalized it with an additional call to the sender wallet.

### Final notes

Most of the payment steps are a two-step operation where:

- first step is a grant action request (do we have privileges to do this)
- second step is the execution of the action