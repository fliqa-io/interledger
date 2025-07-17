# Interledger Open Payments API Client

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://github.com/fliqa-io/interledger/workflows/CI/badge.svg)](https://github.com/fliqa-io/interledger/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.fliqa/interledger-client.svg)](https://search.maven.org/artifact/io.fliqa/interledger-client)

A Java client library for the [Interledger Open Payments](https://openpayments.guide/) protocol, providing secure and
efficient payment facilitation capabilities.

## Features

- **🔐 Cryptographic Security**: Ed25519 signature-based request authentication
- **🏦 Payment Facilitation**: Complete 7-step Interledger payment workflow
- **⚡ High Performance**: Built on Java 11+ HTTP client with connection pooling
- **🛡️ Comprehensive Error Handling**: Detailed exception handling with HTTP status codes
- **📝 Extensive Documentation**: JavaDoc documentation for all public APIs
- **🧪 Well Tested**: Comprehensive unit and integration test coverage

## Quick Start

### Dependencies

Add the following dependency to your project:

**Maven:**

```xml

<dependency>
    <groupId>io.fliqa</groupId>
    <artifactId>interledger-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**

```kotlin
implementation("io.fliqa:interledger-client:1.0.0")
```

### Basic Usage

```java
// Initialize client with Ed25519 private key
InterledgerApiClient client = new InterledgerApiClientImpl(
                new WalletAddress("https://your-wallet.example.com/wallet"),
                privateKey,
                "your-key-id"
        );

// Get wallet information
PaymentPointer wallet = client.getWallet(receiverWallet);

// Create payment flow
AccessGrant grant = client.createPendingGrant(wallet);
IncomingPayment payment = client.createIncomingPayment(wallet, grant, amount);
```

## Building from Source

### Prerequisites

- Java 21 or higher
- Gradle 8.0+ (or use included wrapper)

### Build Commands

```bash
# Clean build
./gradlew clean build

# Run unit tests only
./gradlew test

# Run integration tests only
./gradlew integrationTest

# Publish to local Maven repository
./gradlew publishToMavenLocal -Prelease.version=1.0.0
```

## Interledger Open payment protocol

The following section explains how Fliqa uses the Open payment protocol.
We have three "players":

- Fliqa (the payment **initiator**)
- Tenant (the payment **receiver**)
- User (the payment **sender**)

### Interledger lingo explained

The Interledger Protocol uses terminology that differs from traditional banking and financial applications.
This is because Interledger was designed for decentralized, internet-native payments rather than traditional banking
infrastructure.
Understanding these terms is crucial for developers working with the protocol:

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

> **NOTE:** The receiver payment pointer is already known to us, we know where the payment will be deposited.

### 2. Fliqa creates a quote request

A quote is the cost of the transaction (aka fee) from the sender to the receiver payment pointer (wallet).  
In order to do this we need the sender payment pointer.

> **NOTE:** This is the point where the sender enters his payment pointer

In our case the quote is made for the incoming payment on the _sender_ **payment pointer**. Sender covers the
transaction fees.

### 3. Redirect user to confirm the payment

Once we have the quote, we can create a redirect link for the sender to confirm the payment.

> **NOTE:** User is redirected to his wallet where he confirms / denies the payment

### 4. Finalization of payment

If user has confirmed the payment Fliqa needs to finalized it with an additional call to the sender wallet.

### Final notes

Most of the payment steps are a two-step operation where:

- first step is a grant action request (do we have privileges to do this)
- second step is the execution of the action

## Contributing

We welcome contributions from the community! Whether you're fixing bugs, adding features, improving documentation, or
sharing feedback, your contributions help make this project better for everyone.

### Ways to Contribute

- **🐛 Bug Reports**: Found a
  bug? [Open an issue](https://github.com/fliqa-io/interledger/issues/new?template=bug_report.md)
- **💡 Feature Requests**: Have an
  idea? [Request a feature](https://github.com/fliqa-io/interledger/issues/new?template=feature_request.md)
- **📖 Documentation**: Help improve our documentation
- **🔧 Code Contributions**: Submit pull requests for bug fixes or new features
- **🧪 Testing**: Help expand test coverage or test in different environments
- **💬 Community Support**: Help answer questions in discussions and issues

### Development Setup

1. **Fork and Clone**
   ```bash
   git clone https://github.com/YOUR_USERNAME/interledger.git
   cd interledger
   ```

2. **Set Up Development Environment**
   ```bash
   # Ensure Java 21+ is installed
   java -version
   
   # Run tests to verify setup
   ./gradlew test
   ```

3. **Integration Test Configuration**
   ```bash
   # Copy configuration template
   cp src/integrationTest/resources/test-config.properties.template \
      src/integrationTest/resources/test-config.properties
   
   # Edit with your test values
   # See src/integrationTest/resources/README.md for details
   ```

### Code Contribution Guidelines

1. **Fork the Repository**: Create your own fork on GitHub
2. **Create a Feature Branch**: Use descriptive branch names
   ```bash
   git checkout -b feature/add-payment-validation
   git checkout -b fix/signature-encoding-issue
   ```
3. **Write Quality Code**:
    - Follow existing code style and conventions
    - Add JavaDoc documentation for public APIs
    - Include unit tests for new functionality
    - Ensure integration tests pass
4. **Commit with Clear Messages**:
   ```bash
   git commit -m "Add payment amount validation
   
   - Validates positive amounts before processing
   - Adds comprehensive error messages
   - Includes unit tests for edge cases"
   ```
5. **Push and Create Pull Request**:
   ```bash
   git push origin feature/add-payment-validation
   ```
   Then create a pull request with:
    - Clear description of changes
    - Reference to related issues
    - Test plan and validation steps

### Code Style and Standards

- **Java Code Style**: Follow standard Java conventions
- **Documentation**: All public APIs must have JavaDoc
- **Testing**: Maintain high test coverage (aim for >80%)
- **Security**: Never commit secrets, keys, or sensitive data
- **Dependencies**: Minimize external dependencies

### Review Process

1. **Automated Checks**: All PRs run automated tests and quality checks
2. **Code Review**: Maintainers will review code for quality and security
3. **Testing**: Changes are tested in multiple environments
4. **Documentation**: Ensure documentation is updated for user-facing changes

## Community and Support

### Getting Help

- **📖 Documentation**: Check the [JavaDoc documentation](https://fliqa-io.github.io/interledger/)
- **💬 Discussions**: Join [GitHub Discussions](https://github.com/fliqa-io/interledger/discussions)
- **🐛 Issues**: Report bugs in [GitHub Issues](https://github.com/fliqa-io/interledger/issues)
- **📧 Email**: For security issues, email [security@fliqa.io](mailto:security@fliqa.io)

### Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you agree to
uphold this code. Please report unacceptable behavior to [conduct@fliqa.io](mailto:conduct@fliqa.io).

### Community Guidelines

- **Be Respectful**: Treat all community members with respect and kindness
- **Be Collaborative**: Work together to solve problems and improve the project
- **Be Patient**: Remember that people have different experience levels
- **Be Constructive**: Provide helpful feedback and suggestions
- **Follow Guidelines**: Adhere to project conventions and guidelines

## Security

### Reporting Security Vulnerabilities

🔒 **Do NOT report security vulnerabilities through public GitHub issues.**

To report a security vulnerability:

1. Email [security@fliqa.io](mailto:security@fliqa.io)
2. Include a detailed description of the vulnerability
3. Provide steps to reproduce if possible
4. We'll respond within 48 hours

### Security Best Practices

- **Private Keys**: Never commit private keys or sensitive credentials
- **Dependencies**: Keep dependencies updated and scan for vulnerabilities
- **Code Review**: All security-related changes require thorough review
- **Testing**: Security features must have comprehensive test coverage

## License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](LICENSE) file for details.

### What this means:

- ✅ **Commercial Use**: You can use this library in commercial applications
- ✅ **Modification**: You can modify the source code
- ✅ **Distribution**: You can distribute the original or modified versions
- ✅ **Patent Use**: Express grant of patent rights from contributors
- ⚠️ **License Notice**: Must include copyright and license notice
- ⚠️ **State Changes**: Must indicate significant changes made

## Acknowledgments

- **Interledger Foundation**: For developing the Open Payments specification
- **Contributors**: Thanks to all who have contributed to this project
- **Community**: Special thanks to the open source community for feedback and support

## Roadmap

### Current Version (1.0.x)

- ✅ Core Interledger Open Payments implementation
- ✅ Ed25519 signature support
- ✅ Comprehensive error handling
- ✅ Integration test suite

### Future Releases

- **1.1.x**: Enhanced logging and monitoring
- **1.2.x**: Additional currency support
- **2.0.x**: Async/reactive API support

See our [GitHub Milestones](https://github.com/fliqa-io/interledger/milestones) for detailed planning.

---

**Made with ❤️ by [Fliqa](https://fliqa.io) and the open source community**