# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java client library for the Interledger Open Payments protocol, developed by Fliqa. It implements client-side functionality for initiating cross-wallet payments using the Interledger protocol, focusing on Fliqa's specific use cases as a payment facilitator.

## Build System and Commands

This project uses **Gradle** with Kotlin DSL and Java 21.

### Core Commands
- `./gradlew clean build` - Clean and build the project
- `./gradlew test` - Run unit tests
- `./gradlew integrationTest` - Run integration tests only
- `./gradlew publishToMavenLocal -Prelease.version=<VERSION>` - Publish to local Maven repository
- `./gradlew publish` - Publish to GitHub Packages (requires authentication)

### Test Structure
- **Unit tests**: `src/test/java/` - Standard JUnit 5 tests
- **Integration tests**: `src/integrationTest/java/` - Separate source set for integration tests
- Both test suites use JUnit 5 platform and include structured logging

## Code Architecture

### Core Components

#### 1. Client Interface (`InterledgerApiClient.java`)
The main interface defining a 7-step payment flow:
1. **getWallet()** - Get wallet information
2. **createPendingGrant()** - Create token for receiving wallet
3. **createIncomingPayment()** - Create payment request
4. **createQuoteRequest()** - Get quote token for sender
5. **createQuote()** - Calculate transaction costs
6. **continueGrant()** - Create pending payment requiring confirmation
7. **finalizePayment()** - Complete confirmed payment

#### 2. Client Implementation (`InterledgerApiClientImpl.java`)
- Uses Java 11+ HttpClient for HTTP communication
- Implements cryptographic request signing via `SignatureRequestBuilder`
- Includes structured HTTP logging via `HttpLogger`
- Requires configuration: `clientWallet`, `privateKey`, `keyId`

#### 3. Model Layer (`src/main/java/io/fliqa/client/interledger/model/`)
Key data structures:
- **PaymentPointer** - Wallet address with metadata and server endpoints
- **IncomingPayment** - Payment request that can receive funds
- **OutgoingPayment** - Payment being sent with continuation tokens
- **Quote** - Payment quote with exchange rates and fees
- **InterledgerAmount** - Standardized monetary representation with precision handling
- **AccessGrant/AccessToken** - Authorization tokens for API access

#### 4. Supporting Components
- **InterledgerObjectMapper** - Jackson-based JSON serialization
- **SignatureRequestBuilder** - HTTP request signing for authentication
- **HttpLogger** - Structured logging for HTTP requests/responses
- **Custom serializers** - For specialized data types (Instant, WalletAddress, etc.)

### Payment Flow Architecture

The client implements a multi-step payment flow:
1. **Discovery** - Get wallet capabilities and server endpoints
2. **Authorization** - Create grants for both sender and receiver
3. **Quoting** - Calculate transaction costs and exchange rates
4. **Execution** - Create payment with user confirmation
5. **Finalization** - Complete the payment transaction

Each step involves cryptographically signed HTTP requests to different Interledger-compliant servers.

### Error Handling
- **InterledgerClientException** - Wraps all client-side errors
- **ApiError** - Structured error responses from servers
- HTTP status code handling with detailed error messages

## Key Libraries and Dependencies

- **Jackson** (2.17.1) - JSON serialization/deserialization
- **SLF4J** (2.0.17) - Logging facade
- **JUnit 5** (5.10.2) - Testing framework
- **Java 21** - Target runtime version
- **JSR305** - Null safety annotations

## Development Notes

- The client is designed specifically for Fliqa's payment facilitator use case
- All monetary amounts use `InterledgerAmount` for precision and consistency
- Request signing is mandatory for all API calls
- Integration tests require live Interledger server endpoints
- The project publishes to GitHub Packages repository

## API Documentation

API specifications are stored in `src/main/resources/api/`:
- `auth-server.yaml` - Authentication server endpoints
- `resource-server.yaml` - Resource server endpoints  
- `wallet-address-server.yaml` - Wallet discovery endpoints
- `schemas.yaml` - Data model definitions