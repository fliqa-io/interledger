# Interledger Open Payments API Resources

This directory contains API definitions and resources for the Interledger Open Payments protocol implementation.

## API Definition Strategy

### Custom Model Implementation

The client library uses **hand-crafted model classes** rather than generated code to ensure:

- ✅ **Optimal Performance**: Models are specifically tailored for Fliqa's payment workflows
- ✅ **Type Safety**: Strong typing with proper validation and error handling
- ✅ **Maintainability**: Clean, readable code that's easy to understand and modify
- ✅ **Flexibility**: Custom implementations can adapt to specific business requirements
- ✅ **Security**: Models include proper input validation and secure serialization

### Design Philosophy

**Quality over Automation**: While code generation tools like OpenAPI Generator can be useful,
the current Interledger Open Payments specification and available generators have compatibility issues that prevent
reliable code generation.
Rather than working around these limitations, we chose to implement hand-crafted models that provide better:

- **Control**: Precise control over serialization, validation, and behavior
- **Documentation**: Clear JavaDoc and inline documentation
- **Testing**: Comprehensive test coverage with meaningful assertions
- **Debugging**: Easier troubleshooting and maintenance
- **Reliability**: No dependency on external tool compatibility for core functionality

### Current Model Coverage

The models in `io.fliqa.client.interledger.model` cover the **core Interledger Open Payments workflows**:

- **Payment Pointers**: Wallet address resolution and metadata
- **Access Grants**: Authorization and permission management
- **Payment Flows**: Incoming and outgoing payment processing
- **Quotes**: Transaction cost calculation and fee handling
- **Error Handling**: Comprehensive exception and error response models

### Contributing to Model Development

If you need additional models or modifications:

1. **Check existing models** in `src/main/java/io/fliqa/client/interledger/model/`
2. **Review the OpenAPI specification** for the Interledger protocol
3. **Follow existing patterns** for consistency and maintainability
4. **Include comprehensive JavaDoc** for all new models
5. **Add unit tests** for serialization and validation logic

### References

- [Interledger Open Payments Specification](https://openpayments.guide/)
- [Official OpenAPI Definitions](https://github.com/interledger/open-payments)
- [Project Documentation](../../../README.md)
- **Model Classes**: Located in `src/main/java/io/fliqa/client/interledger/model/`

