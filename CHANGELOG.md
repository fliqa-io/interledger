# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-09-22

### Changed

- **BREAKING:** `InterledgerApiClient.finalizeGrant(OutgoingPayment, String)` now requires three
  additional parameters: `finalizeGrant(OutgoingPayment outgoingPayment, String interactRef, String hash, String clientNonce, URI grantEndpoint)`.
  Before the `interact_ref` returned from the user's wallet is used to finalize the grant, the
  implementation now verifies the `hash` query parameter returned alongside it on the interaction
  callback, per the GNAP interaction hash check
  ([draft-ietf-gnap-core-protocol §4.2.3](https://datatracker.ietf.org/doc/html/draft-ietf-gnap-core-protocol#section-4.2.3)).
  This prevents a forged callback from being used to hijack a payment grant.

  **Migration:** callers must now also pass the `hash` query parameter from the callback URL, the
  `clientNonce` originally passed to `continueGrant(...)`, and the sender's `authServer` URI
  (`grantEndpoint`) used for the original grant request:

  ```diff
  - client.finalizeGrant(outgoingPayment, interactRef);
  + client.finalizeGrant(outgoingPayment, interactRef, hash, clientNonce, sender.authServer);
  ```

  If the computed hash doesn't match, `finalizeGrant` now throws `InterledgerClientException`
  instead of proceeding with an unverified `interact_ref`.

### Added

- `GrantAccessRequest.verifyInteractionHash(String clientNonce, String asNonce, String interactRef, String hash, URI grantEndpoint)` -
  static helper implementing the GNAP interaction hash check, usable independently of `finalizeGrant`.

## [1.0.3]

### Added

- Outgoing payment request retrieval (`getOutgoingPaymentRequest`).
- Token rotation support for outgoing payment status checks.

### Changed

- Integration test now verifies grant confirmation/denial via the outgoing payment resource
  instead of the (now removed) `/outgoing-payment-grant` endpoint, matching the updated
  Open Payments resource server spec.
- Updated the handcrafted OpenAPI spec (`src/main/resources/api`) to reflect changes in the
  upstream Open Payments spec (`updatedAt` fields on incoming/outgoing payments, grant spend
  amounts folded into the outgoing payment representation).

## [1.0.2]

### Fixed

- Fixed resource URL building logic.
- Fixed artifact id and version references in `README.md`.

## [1.0.1]

### Added

- Additional input validation (`Assert`) across client methods.

### Fixed

- Fixed a typo.
- Removed leftover testing code.

## [1.0.0]

- Initial release.
