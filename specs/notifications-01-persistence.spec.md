# Spec: Reservation confirmation notification journey

> Related issues: [#30](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/30), [#33](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/33), [#34](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/34)
> Branch (#30): `feature/30-notification-persistence`
> Status: #30 in progress; contract approved by mentor in [PR #53](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/pull/53). #33 and #34 planned and blocked
> Last updated: 2026-11-20

---

## Problem

RN-06 and RN-08 require an SMS request after a confirmed reservation is persisted only when the customer confirms its contact number can receive SMS. A reservation remains confirmed when the number cannot receive SMS or its capability is unknown, and records why no SMS was requested. Request acceptance is distinct from delivery. The initial reservation table has no notification record or status. Provider states and identifiers come from the completed [Vonage contract research](vonage-01-contract-verification.spec.md).

## Proposed solution

Build the journey in bounded slices: persist notification state, request an eligible SMS after a confirmed telephone reservation, and process delivery-status callbacks without duplicate effects. The approved product policy also covers manual reservations, but wiring their SMS trigger follows the telephone journey and requires separately reviewed scope. Use the approved findings from [Vonage contract research](vonage-01-contract-verification.spec.md) to define the exact provider contracts and state transitions. A notification failure never cancels a confirmed reservation.

## Delivery map

| Issue | Deliverable | Readiness and dependencies |
| --- | --- | --- |
| [#30](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/30) | Persist notification state, both no-request reasons, and correlation data. | In progress; [#26](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/26) is complete and the schema and state mapping below are mentor-approved. |
| [#33](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/33) | Request one confirmation SMS after an eligible telephone reservation is confirmed and record the provider outcome. Eligibility and no-request handling need issue review before implementation; manual triggering remains later work. | Planned and blocked; depends on [#26](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/26), [#7](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/7), [#30](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/30), verified Messages request contract, and approval of the send/correlation design. |
| [#34](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/34) | Accept verified Messages status events and update the matching notification idempotently. | Planned and blocked; depends on #26, #30, #33, verified callback contract and authentication, and approved state transitions. |

Each issue links back to this spec. The later slices do not expand #30.

## Scope

The scope and acceptance criteria in this section describe the published #30 persistence issue, whose text already includes the approved no-request policy. The later slices are specified separately below.

### In scope

- [x] Propose and approve the notification entity/table fields, allowed state transitions, and relationship to a reservation using the verified Messages contract.
- [ ] Persist a known request state, provider identifier when returned, and failure category or safe reason when available.
- [ ] Keep a confirmed reservation intact when a notification is failed or undeliverable.
- [x] Incorporate the approved distinction among no SMS requested because the number cannot receive it, no SMS requested because capability is unknown, and a failed request or delivery into the reviewed #30 state mapping. Represented by the two `NOT_REQUESTED_*` states, separate from `REQUEST_FAILED`, `REJECTED`, and `UNDELIVERABLE`.
- [ ] Add a new Flyway migration, JPA mapping/repository, and PostgreSQL-backed tests for constraints and retrieval by reservation/provider identifier.

### Out of scope

- [ ] Sending SMS, callback endpoint or signature verification, automated retries, dashboard notification display, RCS, or cancellation.
- [ ] Finalizing provider-specific statuses before the contract research is complete.
- [ ] Storing full SMS content, credentials, raw webhook payloads, or sensitive provider errors.

## Acceptance criteria

- [ ] The approved schema associates notification state with its confirmed reservation without changing the reservation status on notification failure.
- [ ] The stored states distinguish request acceptance from recipient delivery according to verified Messages behaviour.
- [ ] The provider identifier and failure information needed by later callback handling can be stored and retrieved without duplicate or ambiguous correlation.
- [ ] Migration and repository behaviour pass PostgreSQL-backed Testcontainers tests, including relevant constraints.
- [ ] The issue and implementation PR name the migration file and confirm no version collision with `develop`.

## Later slices: SMS request and status callbacks

### SMS request after confirmation

- Trigger only after a telephone reservation has been persisted as confirmed through #7 and the customer confirms that the contact number can receive SMS. Record the no-request reason when the number cannot receive SMS or its capability is unknown. Do not request a message after rejected, abandoned, or unconfirmed attempts.
- Use the approved persistence operations from #30 to record a request attempt and its known outcome. Distinguish provider acceptance from recipient delivery.
- Keep the reservation confirmed if the provider request fails, times out, or has an ambiguous outcome. Record `PENDING` before the request, then `ACCEPTED`, `REQUEST_FAILED` only when the request is known not to have been accepted, or `REQUEST_OUTCOME_UNKNOWN` when the outcome is ambiguous. Do not retry an ambiguous request.
- Send the notification `id` as `client_ref` (mentor-approved in [PR #53](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/pull/53)).
- Define the exact Messages request, authentication, response mapping, and correlation from the completed #26 research before implementation. Manual reservations follow the same approved product policy, but their SMS trigger is later work after the telephone journey and needs reviewed issue scope.
- Validate with provider test doubles, including accepted, rejected, failed, and ambiguous request outcomes. No automated test may spend credits by default.

### Delivery-status callbacks

- Match a callback by `message_uuid`, falling back to `client_ref` (the notification `id`) when no stored `provider_message_id` matches (mentor-approved in [PR #53](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/pull/53)). Unknown or ambiguous correlations must not mutate another reservation or notification.
- Verify callback authentication and required response behaviour from the official contract before implementing an endpoint. Do not assume a signature format, payload, or retry schedule.
- Apply the approved state transitions idempotently: a repeated callback is a no-op, a callback never regresses a final state, and a callback may resolve `REQUEST_OUTCOME_UNKNOWN` to the corresponding provider state.
- Retain only safe status and failure information; do not store raw webhook bodies or sensitive provider errors.
- Validate authenticated/invalid, duplicate, out-of-order, unknown-correlation, and failure cases with test doubles and PostgreSQL-backed tests where state transitions or constraints are involved.

These later slices are planning boundaries, not approved provider contracts or permission to implement. Their precise acceptance criteria and blockers must be finalized in their issues after #26 and mentor review.

## Business rules and decisions

- Business rules: [RN-06](../docs/business-rules.md#rn-06-confirmation-messages) and [RN-08](../docs/business-rules.md#rn-08-contact-phone-and-sms-eligibility).
- Related ADRs: [ADR-004](../docs/adr/004-adopt-flyway-migration-coordination-policy.md).
- Open questions: callback authentication and the reviewed implementation scope for manual SMS. The provider status mapping, request/callback correlation, and ambiguous request outcomes were resolved in [PR #53](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/pull/53). The manual product policy is approved; resolve questions affecting each slice before that slice starts.

## Technical design

### Backend

Add a `notifications` package with the `ReservationNotification` entity, the `NotificationStatus` and `NotificationFailureReason` enums, and a repository that looks up a notification by reservation and by provider message identifier. The entity enforces the proposed transitions below. Issue [#30](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/30) adds no service, controller, scheduler, or Vonage client.

### Frontend

No UI change.

### Data and migrations

Add the versioned migration described below and recheck its version against `develop` before requesting review ([ADR-004](../docs/adr/004-adopt-flyway-migration-coordination-policy.md)). Do not alter merged `V1__create_reservations.sql`. Indexes and constraints follow the correlation model below; the two `UNIQUE` constraints also provide the lookup indexes.

#### Schema

The schema, states, transitions, and correlation in these sections were approved by the mentor in [PR #53](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/pull/53) on 2026-11-20.

Migration: `V2__create_reservation_notifications.sql`. Checked against `develop` and all remote branches on 2026-11-19; no other migration besides `V1__create_reservations.sql` exists.

Table `reservation_notifications`:

| Column | Type | Rules |
| --- | --- | --- |
| `id` | `UUID` | Primary key, `DEFAULT gen_random_uuid()`. |
| `reservation_id` | `UUID` | `NOT NULL`, `UNIQUE`, foreign key to `reservations(id)`. One notification per reservation; automated retries are out of scope. |
| `status` | `VARCHAR(40)` | `NOT NULL`, restricted by `CHECK` to the states below. |
| `provider_message_id` | `TEXT` | Nullable, `UNIQUE`. Stores the Messages `message_uuid` returned with `202 Accepted`. `TEXT` avoids encoding an unverified identifier format. |
| `failure_reason` | `VARCHAR(40)` | Nullable, restricted by `CHECK` to a closed internal list. No free text or provider error body. |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT CURRENT_TIMESTAMP`. |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT CURRENT_TIMESTAMP`; updated by the application on each state change. |

Structural constraints:

- `provider_message_id` is present if and only if `status` is `ACCEPTED`, `SUBMITTED`, `DELIVERED`, `REJECTED`, or `UNDELIVERABLE`.
- `failure_reason` is required when `status` is `REQUEST_FAILED` (`PROVIDER_REQUEST_REJECTED` or `PROVIDER_UNAVAILABLE`) or `REQUEST_OUTCOME_UNKNOWN` (`PROVIDER_OUTCOME_UNKNOWN` only), and absent for every other state, including `REJECTED` and `UNDELIVERABLE`. Provider error codes for those states are not verified; [#34](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/34) may add them with a new migration.
- The table stores no SMS content, contact number, raw request or callback payload, or credential. The reservation's `status` column is never written by notification code.

#### States and transitions

| State | Source | Meaning | Final |
| --- | --- | --- | --- |
| `NOT_REQUESTED_CANNOT_RECEIVE` | RN-08 | The customer said the confirmed contact number cannot receive SMS. No request is made. | Yes |
| `NOT_REQUESTED_CAPABILITY_UNKNOWN` | RN-08 | The customer did not know whether the number can receive SMS. No request is made. | Yes |
| `PENDING` | Application | Recorded immediately before the Messages request, so that a crash or timeout leaves a trace. | No |
| `REQUEST_FAILED` | Messages request | The request is known not to have been accepted (refused by Vonage, or no connection was established). Requires a `failure_reason`. | Yes |
| `REQUEST_OUTCOME_UNKNOWN` | Messages request | The outcome is ambiguous (timeout, interrupted response, or server error): Vonage may still have accepted the request. Requires `PROVIDER_OUTCOME_UNKNOWN`. A later status callback can resolve it. | No |
| `ACCEPTED` | Messages `202 Accepted` | Vonage accepted the request and returned a `message_uuid`. This is not delivery. | No |
| `SUBMITTED` | Status callback `submitted` | Passed to a messaging provider. This is not delivery. | No |
| `DELIVERED` | Status callback `delivered` | Delivered to the recipient. | Yes |
| `REJECTED` | Status callback `rejected` | Rejected by the provider. | Yes |
| `UNDELIVERABLE` | Status callback `undeliverable` | Could not be delivered. | Yes |

Allowed transitions:

- `PENDING` → `ACCEPTED` (with `provider_message_id`), `REQUEST_FAILED` (with `failure_reason`), or `REQUEST_OUTCOME_UNKNOWN` (with `PROVIDER_OUTCOME_UNKNOWN`).
- `ACCEPTED` or `REQUEST_OUTCOME_UNKNOWN` → `SUBMITTED`, `DELIVERED`, `REJECTED`, or `UNDELIVERABLE`.
- `SUBMITTED` → `DELIVERED`, `REJECTED`, or `UNDELIVERABLE`.
- Resolving `REQUEST_OUTCOME_UNKNOWN` stores the callback's `message_uuid` as `provider_message_id` and clears `failure_reason`.
- A final state has no outgoing transition. Both no-request states are created directly as final.

Rules for provider status updates (applied by [#34](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/34)):

- A repeated status equal to the current one is a no-op.
- A status received for a notification already in a final state is ignored rather than rejected, because the reviewed documentation does not guarantee callback ordering or exactly-once delivery.
- A request-side transition that is invalid for the current state (for example, marking a no-request notification as accepted) is a programming error and fails explicitly.
- `failure_reason` values: `PROVIDER_REQUEST_REJECTED` (Vonage refused the request), `PROVIDER_UNAVAILABLE` (no connection was established, so the request never left the application), and `PROVIDER_OUTCOME_UNKNOWN` (timeout, interrupted response, or server error; the SMS may or may not have been sent and is not retried as if idempotent). The exact mapping from HTTP outcomes is defined by [#33](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/33) against the verified contract.
- These rules are enforced by the notification entity and covered by unit tests in [#30](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/30); [#33](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/33) and [#34](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/34) call them rather than redefining them.

#### Correlation

- Lookup by reservation: `reservation_id` is unique, so it returns at most one notification.
- Lookup by provider message: `provider_message_id` is unique and set once Vonage returns `202 Accepted`, or when a callback resolves `REQUEST_OUTCOME_UNKNOWN`, so a status callback matches at most one notification by `message_uuid`.
- Fallback by `client_ref`: [#33](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/33) sends the notification `id` as the Messages `client_ref`, which the documentation says is included in every status callback. When no stored `provider_message_id` matches, [#34](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/34) looks the notification up by that `id`. No additional column is needed. A callback that matches neither must not modify any notification.
- The entity stores the reservation identifier as a plain `UUID` without a JPA association; the database foreign key enforces the relationship. This keeps notification code from loading or modifying a `Reservation`.

### Vonage integration

The [research spec](vonage-01-contract-verification.spec.md) supplies documented message identifiers, lifecycle states, and callback behaviour. Issue #30 makes no external request. Later slices implement the request and callback adapters after their contracts are verified and approved.

## Security and data handling

Store only data needed for status and diagnostics. Avoid raw personal data in failure text, logs, fixtures, and migration examples. Use fictional reservation details in tests.

## Accessibility and UX

Not applicable; no UI change.

## Testing

### Automated

- [ ] Testcontainers tests verify migration, persistence, correlation lookup, constraints, and a failed notification leaving its reservation confirmed.
- [ ] Unit tests cover state-transition rules if those rules are implemented in this issue.

### Manual verification

- [x] Review the schema and state mapping against the completed official-contract evidence.
- [ ] Check the proposed migration version against current `develop` before implementation and review.

## Dependencies and environment

- [x] Completion and mentor review of [Vonage contract research](vonage-01-contract-verification.spec.md) ([#26](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/26), closed).
- [ ] Depends on the existing [#4](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/4) reservation model. Coordinate with [#7](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/7) for the post-persistence workflow.

## Notes

The [#30](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/30) contract is mentor-approved; implementation follows the schema, states, transitions, and correlation above. The later delivery-map entries remain proposals until their own issue scopes and prerequisites are reviewed.
