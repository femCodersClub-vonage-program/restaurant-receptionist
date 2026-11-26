# Vonage integration

Restaurant Receptionist uses Vonage Voice API for reservation calls and Vonage Messages API for SMS confirmations. This document records the intended product role of each integration and the backend's webhook verification setup.

## Voice API

The MVP uses Voice API to support a guided telephone reservation journey:

1. Receive an incoming call.
2. Ask for and capture the reservation details.
3. Report validation or capacity outcomes.
4. Read the final summary and obtain explicit confirmation.
5. Confirm a persisted reservation by voice.

Spanish speech recognition, text-to-speech behaviour, webhook flow, authentication, payload formats, error handling, caller-number availability, test numbers, and credit requirements must be verified against current official Vonage documentation before implementation.

## Messages API

The MVP uses Messages API to request an SMS reservation confirmation after the reservation has been persisted.

- SMS is the agreed MVP channel.
- RCS is out of scope for the MVP.
- A request accepted by Vonage is not evidence that the message reached the recipient.
- The backend must record known delivery states and errors after their behaviour and webhooks have been verified.
- A failed notification must not cancel a confirmed reservation.

## Inbound webhook verification

The shared `com.restaurantreceptionist.webhooks.WebhookSignatureVerifier` verifies incoming Voice and Messages callbacks. The Vonage Java Server SDK checks the HS256 JWT signature using the account's signature secret. The backend checks the signed `api_key` and `iss` claims and compares `application_id` with the configured application when the claim is present. When `payload_hash` is present, the backend compares it with the SHA-256 digest of the original UTF-8 body. See the [Vonage signed webhook reference](https://developer.vonage.com/en/getting-started/concepts/webhooks#decoding-signed-webhooks).

Set these variables in the receiving backend's environment. See the [local setup guide](setup.md) for loading a local ignored `.env` file.

| Variable | Purpose |
| --- | --- |
| `VONAGE_WEBHOOKS_ENABLED` | Set to `true` to enable verification with the SDK. Defaults to `false`. |
| `VONAGE_API_KEY` | API key for the expected Vonage account. |
| `VONAGE_SIGNATURE_SECRET` | Signature secret associated with the expected API key. |
| `VONAGE_APPLICATION_ID` | ID of the expected Vonage application. |

When enabled, the backend fails at startup if any of the three settings is missing or blank. When disabled, the backend can start without Vonage credentials and the verifier always returns `MISSING_SIGNATURE`, including for signed requests. Application private keys and the API secret are used for outbound authentication where applicable.

Pass the received `Authorization` header and the original UTF-8 request body to `verify` before JSON parsing or any state change. Use an empty string for a request without a body. The controller processes only `VERIFIED` and maps `MISSING_SIGNATURE` to HTTP `503` and `INVALID_SIGNATURE` to HTTP `401`.

Consumer tests can inject `StubWebhookSignatureVerifier` with a fixed result. For integration tests, `VonageWebhookFixtures.authorizationFor(rawBody)` generates a locally signed header for the supplied body. The fixtures and their configuration values are fictional and make no requests to Vonage. Consumers handle callback idempotency separately from signature verification.

## Integration principles

- The backend owns all Vonage credentials and API requests.
- No secret, API key, private key, test number configuration, or real personal data may be committed.
- Controlled integration or demo testing may use a consenting team member's phone number or a Vonage test number. Keep those numbers in local configuration only; never include them in repository files, logs, seed data, or public screenshots.
- Automated tests must use test doubles or verified local test mechanisms rather than consume real credits by default.
- Documented behaviour, tested behaviour, and unverified hypotheses must remain clearly distinguished.

## Outside the MVP

- Verify API and Silent Authentication.
- RCS and RCS-to-SMS fallback.
- Conversational AI as a replacement for the guided flow.
