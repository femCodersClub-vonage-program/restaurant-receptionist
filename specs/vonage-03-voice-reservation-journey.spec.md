# Spec: Guided telephone reservation journey

> Related issues: [#35](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/35), [#36](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/36), [#37](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/37)
> Status: #35 entry, routes, greeting, and PostgreSQL session design approved; implementation in progress. #36 and #37 remain planned.
> Last updated: 2026-11-27

---

## Problem

The guided journey must preserve capacity and require explicit confirmation before creating a reservation. [#26](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/26) records the provider research.

## Proposed solution

Connect a guided Voice flow to the existing reservation services. Capture and validate one field at a time, support bounded retries and one-field corrections, offer the other service on the same date when capacity is insufficient, and persist only after explicit final confirmation and atomic revalidation. Keep call and webhook processing safe against repeated events and abandonment. Fill in provider-specific contracts only from the completed, mentor-reviewed [Vonage research spec](vonage-01-contract-verification.spec.md).

## Delivery map

| Issue | Deliverable | Readiness and dependencies |
| --- | --- | --- |
| [#56](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/56) | Provide the shared signature verifier for incoming Vonage Voice webhooks. | #35 can use a verifier double during development and must integrate the real verifier before closure. |
| [#35](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/35) | Handle an incoming call, maintain the minimal call session, and deduplicate repeated events. | Approved: separate Voice controller, `POST` answer/event routes, greeting-only `es-ES` NCCO, PostgreSQL session keyed by call-leg `uuid` with `V3`, and 20-minute expiry. Start with a verifier double and integrate #56 before closure. |
| [#36](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/36) | Ask for the required fields, validate them, handle silence/invalid input, three attempts per field, and one-field correction. | Depends on #35. Agree the validated handoff to #37 with its owner. |
| [#37](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/37) | Read back details, require affirmative confirmation, call #7 for atomic creation, and use #19 for the same-date alternative when needed. | Start after the #35 session model and the validated data and state from #36 are agreed and recorded. Close after integration with the merged #36 flow and verification that a confirmation creates at most one reservation. |

The delivery map distinguishes prerequisites for starting each slice from integration requirements for closing it.

## Scope

### In scope

- [ ] Handle the approved incoming-call journey using verified Voice API contracts.
- [ ] Capture first and last name, a caller-confirmed Spanish contact number and its SMS eligibility, reservation date, arrival time, and party size through guided prompts. Ask for the contact number even when caller ID is present.
- [ ] Interpret "today" and "tomorrow" using `Europe/Madrid`; request a concrete, unambiguous date for other expressions, asking for the year if necessary. Accept today or a future date without a maximum booking horizon and read back the full interpreted date.
- [ ] Validate date, time, service, and capacity using the shared backend rules; do not make the Voice channel a second source of truth.
- [ ] Allow one initial question and up to two retries per field. Permit at most two successful one-field correction rounds after the initial summary, each followed by revalidation and another summary; end without persistence on a third correction request.
- [ ] Offer the other service on the same date when the requested service cannot fit the party; allow at most two caller-proposed alternative dates after the initial date. A same-date service offer does not consume an alternative date.
- [ ] Require explicit affirmative confirmation before calling the #7 confirmed-reservation service. Handle final capacity loss without false confirmation.
- [ ] Avoid duplicate creation from repeated webhooks or confirmations and avoid creation after pre-persistence abandonment.
- [ ] After successful persistence, hand off to the separately specified notification journey without treating SMS failure as reservation failure.

### Out of scope

- [ ] Conversational AI, DTMF, automatic date search, complex natural-language date handling, and post-MVP cancellation or modification.
- [ ] Reimplementing the capacity or transactional persistence logic owned by #6 and #7.
- [ ] SMS request or status-callback implementation, which belong to the [notification journey](notifications-01-persistence.spec.md).
- [ ] Inventing a fallback for unsupported Spanish speech before mentor review of #26 findings. Caller-ID and contact-number policy is approved in RN-08.

## Acceptance scenarios to refine after #26

- [ ] A caller supplies valid details, confirms the readback, and receives an accurate result only after #7 persists one confirmed reservation.
- [ ] A full requested service leads to the RN-02 same-date alternative through #19; final confirmation still revalidates capacity.
- [ ] An invalid field, silence, or unrecognised input consumes only that field's allowed attempts; the third failed attempt ends the call without persistence.
- [ ] A single-field correction updates the summary and revalidates affected rules before another explicit confirmation.
- [ ] Two correction rounds are allowed; a third request ends without persistence. Two alternative dates are allowed after the initial date; no accepted option ends without persistence.
- [ ] A hidden or incorrect caller ID does not block a reservation when the caller supplies and confirms a valid contact number. An unknown or negative SMS-capability answer does not block confirmation or trigger a message; correcting the number requires a new capability answer.
- [ ] A repeated webhook or confirmation does not create a second reservation; a hang-up before persistence creates none.
- [ ] Backend or provider failure is distinguished from invalid caller input and never produces a false confirmation.

## Business rules and decisions

- Business rules: [RN-01 to RN-06 and RN-08](../docs/business-rules.md), especially RN-02 to RN-05 and RN-08.
- Related services: [#6](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/6), [#7](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/7), and [#19](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/19).
- The mentor has approved no maximum booking horizon for the MVP, bounded date expressions, contact-number and SMS eligibility policy, two correction rounds, two alternative dates, and no business-duplicate restriction. These are separate from provider-contract verification and do not make a blocked slice ready for implementation.
- Provider behaviour is taken from the completed [contract verification spec](vonage-01-contract-verification.spec.md) and the current official references cited there. Documented behaviour, locally tested behaviour, and unverified hypotheses stay distinct.

### Confirmed #35 entry and session decisions

The mentor confirmed the following for #35 on 2026-11-23:

1. A separate Voice `CallController` owns `POST /api/webhooks/vonage/voice/answer` and `POST /api/webhooks/vonage/voice/event`. The Answer webhook must be configured as `POST` in the Vonage Dashboard; the #51 controlled test used the `GET` default.
2. The minimal call session is stored in PostgreSQL, keyed by the call-leg `uuid`, with `conversation_uuid` kept for correlation. The migration is `V3__create_voice_call_sessions.sql`; [#53](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/53) owns `V2`.
3. Signature verification uses the shared verifier owned by [#56](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/56). #35 develops against a verifier double and must integrate the real verifier before closure; the JWT library is selected in #56.
4. Every publicly exposed Voice endpoint requires a valid signature, including the demo. A missing signature returns `503` so Vonage can retry with a signed request; an invalid signature returns `401`. Neither case mutates session state. When `application_id` is present, it must match the configured application.
5. Session creation uses a unique constraint on `uuid` plus optimistic locking with `@Version`. Two simultaneous `answer` requests create one session, and a repeated `answer` preserves the current step. A `completed` that arrives before a delayed `answer` must prevent the later request from opening an active session.
6. Inactivity expiry defaults to 20 minutes and is configurable. Terminal-session retention, cleanup, and late-callback handling are proposed below for PR review.
7. #35 returns a short `es-ES` greeting NCCO and omits `bargeIn`. #36 adds the first question and the working `/input` callback, with `speech.language` set to `es-ES`.
8. A hang-up before reservation persistence ends the call without a reservation; a hang-up after a confirmed reservation is persisted preserves it. `disconnected_by` is diagnostic only. #37 connects call state to the reservation outcome.

## Technical design

### Backend

#### Controller, routes, and methods

Add a `CallController` for the Voice webhooks, separate from `ReservationCreationController` (dashboard consumer). The consumers, lifecycle, and authentication differ, so keeping them apart avoids coupling the two integrations.

| Method | Route | Purpose |
| --- | --- | --- |
| `POST` | `/api/webhooks/vonage/voice/answer` | Answer webhook; creates the call session and returns the NCCO that starts the call. |
| `POST` | `/api/webhooks/vonage/voice/event` | Call event webhook; updates the session idempotently. |

- `answer_method=POST` must be configured in the Vonage Dashboard. The event webhook already defaults to `POST`.
- `/api/webhooks/vonage/voice/input` belongs to #36 and is not implemented in #35.
- Both handlers are idempotent and must not depend on callback ordering.

#### Answer NCCO in #35

Return a short `es-ES` greeting and omit `bargeIn`:

```json
[
  {
    "action": "talk",
    "text": "Le damos la bienvenida a Restaurant Receptionist. Este servicio le permite hacer una reserva por teléfono.",
    "language": "es-ES"
  }
]
```

#36 adds the first question and the `input` action with its `/input` callback, and sets `speech.language` to `es-ES`. #35 does not return an `input` action.

#### Signature verification

- Apply the shared verifier from #56 to both routes. The verifier owns JWT-library choice and claim parsing.
- Both routes read the raw body and the `Authorization` header, verify them with `WebhookSignatureVerifier` before parsing, and return `503` (missing) or `401` (invalid) without parsing the body or changing state. The controller slice test replaces the verifier with a `@MockitoBean`; the integration tests enable real verification with locally signed fictional fixtures.
- Verification runs only when enabled in the configuration. Otherwise the verifier fails closed and treats every request as unsigned, so the endpoints return `503` even for a validly signed request.
- A missing signature (no `Authorization` header, no `Bearer` scheme, or no JWT) returns `503` so Vonage can attempt a signed retry; session state is unchanged.
- An invalid signature (HMAC mismatch, unsupported `alg`/`typ`, wrong `iss`, or `application_id` mismatch when the claim is present) returns `401`; session state is unchanged.
- The documented rare unsigned callback is treated as a missing signature: return `503`, do not process it, and allow the retry.
- Do not add a fail-open switch for public endpoints. Signature enforcement applies to the demo as well.

#### Call event handling

| Event | #35 handling |
| --- | --- |
| `started`, `ringing` | Acknowledge and log; no session change. |
| `answered` | If a session exists, confirm it; otherwise acknowledge and log without creating one. The `answer` webhook is authoritative for creation. |
| `completed` | Mark an active session terminal as `ABANDONED` unless #37 has already set `COMPLETED`; record `disconnected_by` as a diagnostic. If no session exists, persist a minimal terminal tombstone so a delayed `answer` cannot open an active session. |
| `rejected`, `failed` | Same terminal handling. Record the `detail` diagnostic (`invalid_number`, `restricted`, `declined`, `throttled`, `cannot_route`, `number_out_of_service`, `internal_error`) as `end_reason`. If `uuid` is absent (documented for `rejected`), acknowledge and log only. |
| `input` | Not in #35; implemented in #36. |
| `transfer` | Acknowledge; not applicable. |
| Error webhook (`reason`) | Log safely as a provider error; do not count it as a caller attempt and do not create a reservation. Acknowledge and log only — an error webhook reports a provider-side error and does not signal that the call has ended. |
| Other documented statuses (`busy`, `cancelled`, `unanswered`, `reconnecting`, `reconnected`, `timeout`, `record`, `human`/`machine`) | Acknowledge and log; mutate state only through a terminal event above. |

#### State transitions and idempotency

| Situation | Existing session | Result |
| --- | --- | --- |
| Valid answer, new `uuid` | none | Create `ACTIVE`, set the initial `current_step`, and return the greeting NCCO. |
| Two simultaneous answers | one inserted | One insert wins; the other re-reads it; both return the current-step NCCO. |
| Repeated answer | `ACTIVE` | No change; preserve `current_step`; return the current-step NCCO. |
| `answered` | `ACTIVE` | No change. |
| `completed` / `rejected` / `failed` | `ACTIVE` | Set terminal `ABANDONED`, `terminal_at`, `end_reason`, and `disconnected_by` when present. Do not downgrade `COMPLETED`. |
| Terminal event with `uuid` | none | Create a minimal terminal tombstone (`ABANDONED`) so a later answer cannot open an active session; acknowledge and log. |
| Delayed answer | terminal | Do not open an active session or change the terminal row; acknowledge without reopening. |
| Duplicate or out-of-order event | any | No regression; acknowledge. |
| No update for 20 minutes | `ACTIVE` | Set `EXPIRED` and `terminal_at`. |
| Event on terminal state | `ABANDONED` / `COMPLETED` / `EXPIRED` | No change; acknowledge. |

- Rely on the unique `uuid` constraint and catch `DataIntegrityViolationException` (or use `INSERT ... ON CONFLICT`) so a concurrent answer re-reads the winning row.
- Use `@Version` for updates. On an optimistic-lock conflict, re-read and re-evaluate; apply only a non-regressing transition.
- Session creation and event updates run in transactions.

#### HTTP responses

| Code | Condition |
| --- | --- |
| `200` with NCCO | Valid answer, including a repeated answer on an active session; return the current-step NCCO. |
| `200`/`204` | Acknowledged event: duplicate, non-applicable status, unknown or absent `uuid` that can only be logged, or delayed answer on a terminal tombstone. No state change. |
| `503` | Missing signature (no header, no `Bearer` scheme, or no JWT), including the documented unsigned-callback edge case. No state change; Vonage may retry. |
| `401` | Invalid signature or claims, or an `application_id` mismatch. No state change. |
| `400` | Malformed callback payload, such as an answer without required documented fields. No state change. |
| `5xx` | Database or unexpected failure, so Vonage retries. |

An event with an unknown or absent `uuid` is acknowledged rather than rejected, so it neither triggers retries nor creates application state. The terminal-tombstone rule above is the only exception.

#### Call outcome

- #35 records call-session state only; it does not write a reservation and does not call `ReservationCreationService`.
- A hang-up before persistence terminates the session without a reservation. A later hang-up after #37 has persisted a confirmed reservation preserves it; terminal states never regress.
- `disconnected_by` is diagnostic: `user` and `platform` do not encode whether a reservation was persisted.
- #37 connects the recorded call state to the actual reservation outcome and sets `COMPLETED` when a reservation is persisted.

#### Expiry, retention, and cleanup (proposal for PR review)

- The 20-minute inactivity default is approved. Keep the timeout configurable and use the injected `Clock` for deterministic tests.
- Proposal: retain terminal sessions for 30 days (configurable) to absorb late callbacks and support diagnostics. The provider retry window is not verified, so this is a safety margin rather than a derived value.
- Proposal: a scheduled maintenance job marks stale `ACTIVE` sessions `EXPIRED` and deletes terminal sessions older than the retention period in bounded batches.
- Proposal: late callbacks after cleanup are acknowledged (`2xx`) and logged without creating a session. A late `answer` whose tombstone has been deleted cannot be distinguished from a genuine new call; document this residual risk until the retry window is verified.

### Data and migrations

- Add `V3__create_voice_call_sessions.sql`. Do not modify `V1__create_reservations.sql`; `V2` belongs to #53.
- Minimal `voice_call_sessions` table:

| Column | Type | Rule |
| --- | --- | --- |
| `id` | `UUID` | Primary key, `DEFAULT gen_random_uuid()`. |
| `uuid` | `TEXT` | `NOT NULL`, unique call-leg identifier. |
| `conversation_uuid` | `TEXT` | Nullable; stored for correlation when present. |
| `state` | `VARCHAR(20)` | `ACTIVE`, `ABANDONED`, `COMPLETED`, or `EXPIRED`. |
| `current_step` | `VARCHAR(64)` | Nullable; preserved across repeated answers. |
| `created_at` | `TIMESTAMPTZ` | `NOT NULL`, default `CURRENT_TIMESTAMP`. |
| `updated_at` | `TIMESTAMPTZ` | `NOT NULL`, default `CURRENT_TIMESTAMP`. |
| `terminal_at` | `TIMESTAMPTZ` | Nullable; set on terminal transition from the event `timestamp`, or the server clock when that field is absent. |
| `end_reason` | `TEXT` | Nullable; safe diagnostic only. Holds `detail` for `rejected`/`failed`; null for `completed`. |
| `disconnected_by` | `VARCHAR(20)` | Nullable; diagnostic only. |
| `version` | `BIGINT` | `NOT NULL DEFAULT 0` for optimistic locking. |

- Add a checked `state` constraint, a unique constraint/index on `uuid`, an index on `conversation_uuid`, and an index on `terminal_at` for cleanup.
- Store only safe session metadata. Do not store raw webhook payloads, names, phone numbers, or tokens.

### Vonage integration

- The Answer webhook must return an NCCO JSON array. It defaults to `GET` and is configurable to `POST`; #35 requires `POST` in the Dashboard configuration.
- The event webhook defaults to `POST` and is expected to be acknowledged with a `2xx`.
- Answer and event callbacks expose the call-leg `uuid` and `conversation_uuid`. Event callbacks also include a `timestamp` in ISO 8601 format. `completed` includes `disconnected_by` (`user` or `platform`). `rejected` may arrive without a `uuid` before a call resource exists. #35 uses the event `timestamp` to populate `terminal_at` and falls back to the server clock when the field is absent.
- Signed webhooks use an `Authorization: Bearer` JWT with `alg: HS256`, `iss: "Vonage"`, `api_key`, optional `application_id`, and an optional `payload_hash`. The exact verification implementation belongs to #56.
- Official references: [Voice webhook reference](https://developer.vonage.com/en/voice/voice-api/webhook-reference), [NCCO reference](https://developer.vonage.com/en/voice/voice-api/ncco-reference), and [signed webhooks](https://developer.vonage.com/en/getting-started/concepts/webhooks).
- Still unverified locally: practical Spanish recognition accuracy, the exact retry and duplicate-delivery ordering, and whether the JWT `jti` is retry-stable. Do not use `jti` as an idempotency key.

### Frontend

No dashboard change is included in this feature.

## Security and data handling

Every publicly exposed Voice endpoint requires the signed JWT enforced by the shared #56 verifier: missing signatures return `503` and invalid signatures return `401`, without state changes. Keep the API key, signature secret, application ID, and private keys in local configuration only. Use fictional caller data in tests and documentation. Do not log tokens, full webhook payloads, names, or phone numbers. Store only safe session metadata.

## Testing and validation

### Automated

- [ ] For #36/#37, test the guided state transitions, attempt counts, corrections, alternatives, final confirmation, abandonment, and repeated events with provider doubles.
- [ ] Verify a valid answer creates one `ACTIVE` session with the initial `current_step` and returns the `es-ES` greeting NCCO without `bargeIn` or `input`.
- [ ] Verify two simultaneous answers against PostgreSQL create one session and both return the current-step NCCO.
- [ ] Verify a repeated answer preserves `current_step` and does not create a second session.
- [ ] Verify `completed` before a delayed answer persists a terminal tombstone and the later answer does not open an active session.
- [ ] Verify `completed`, `rejected`, and `failed` terminate an active session, duplicate or out-of-order events do not regress state, and unknown or absent `uuid` is acknowledged and logged; only terminal events create a tombstone.
- [ ] Verify a missing signature returns `503`, an invalid signature or claims and an `application_id` mismatch return `401`, and valid signed requests are processed; rejected requests leave state unchanged.
- [ ] Verify the 20-minute inactivity expiry with the injected `Clock`, and the retention/cleanup rule if implemented.
- [ ] Verify a database failure returns `5xx` so Vonage can retry.
- [ ] Use PostgreSQL-backed Testcontainers tests for persisted state, constraints, and concurrency, and for the final handoff to #7.
- [ ] Use a verifier double in #35 and re-run the signature scenarios against the #56 verifier before closure.

### Manual verification

- [ ] Configure `answer_method=POST` and `event_method=POST` in the Vonage Dashboard.
- [ ] Validate a controlled call manually only after the required account, number, consent, and test access are approved; confirm the greeting and record what was actually tested with redacted evidence.

## Dependencies and notes

- [x] The completed #26 research is the provider baseline.
- [ ] #56 provides the shared signature verifier; integrate it before closing #35.
- [ ] #53 owns migration `V2`; confirm `V3` does not collide with `develop` before merge.
- [ ] #37 connects the call session to the persisted reservation outcome and sets `COMPLETED`.
- Open questions outside #35: the stable idempotency key (`jti` is not retry-stable) and the exact retry/duplicate-delivery schedule.
