# ADR-008: Verify signed Vonage webhooks with the official Java SDK

## Status

Accepted. This record documents the decision approved in [#56](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/56) and the [Voice webhook decision in #35](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/35#issuecomment-7099986230).

## Date

2026-11-26

## Context

Public Voice callbacks can change call sessions, and public Messages status callbacks can change notifications. Both consumers need a consistent way to authenticate a request before it can change state. Vonage documents signed webhooks as HS256 bearer JWTs using the signature secret associated with the account.

## Decision

Use one backend `WebhookSignatureVerifier` for inbound Voice and Messages callbacks. Pin the official Vonage Java Server SDK at `com.vonage:server-sdk:9.15.1` in Maven, validated with Java 21. Use the SDK's supported helper to verify the JWT signature with the configured account signature secret.

After signature verification, check that `api_key` matches the configured account and `iss` is `Vonage`. If the JWT contains `application_id`, it must match the configured application. If it contains `payload_hash`, compare it with the SHA-256 digest of the original UTF-8 body. The controller must supply that body before parsing it.

The verifier returns `VERIFIED`, `MISSING_SIGNATURE`, or `INVALID_SIGNATURE`. Each controller owns its response and only processes a callback after `VERIFIED`. The agreed responses for missing and invalid signatures are HTTP `503` and `401`, respectively.

## Alternatives considered

- Verify independently in each controller. This duplicates provider and application checks and allows Voice and Messages to drift in how they reject requests.
- Use a general-purpose JWT library directly for signature verification. This would still require mapping Vonage's signing contract and maintaining the provider-specific verification path. The official SDK provides a supported helper for the signed webhook format.

## Consequences

- The backend gains a pinned runtime dependency. Review SDK upgrades and rerun the verifier fixtures and consumer integration tests when changing the version.
- The backend starts without Vonage credentials by default and rejects all callbacks through the verifier. Enabling verification requires the expected account API key, account signature secret, and application ID. Missing settings stop startup. Keep their values in backend environment configuration and keep tokens, secrets, and callback bodies out of logs.
- The shared verifier supports local tests with fictional signed fixtures or a test double. Consumer issues [#35](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/35) and [#34](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/34) own controller integration and state changes. Callback idempotency remains with the consumers because the JWT ID is not established as stable across retries.
- Outbound SMS authentication and live Vonage setup are handled separately from this inbound control.

## Related material

- [ADR-005: Pin the initial runtime and tooling versions](005-pin-initial-runtime-and-tooling-versions.md)
- [Vonage integration and webhook configuration](../vonage-integration.md#inbound-webhook-verification)
- [Vonage contract research](../../specs/vonage-01-contract-verification.spec.md)
- [Voice reservation journey](../../specs/vonage-03-voice-reservation-journey.spec.md)
- [Notification persistence and callbacks](../../specs/notifications-01-persistence.spec.md)
- [Official Vonage Java Server SDK](https://github.com/Vonage/vonage-java-sdk#installation)
- [Vonage signed webhook reference](https://developer.vonage.com/en/getting-started/concepts/webhooks#decoding-signed-webhooks)
