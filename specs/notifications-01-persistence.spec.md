# Spec: Reservation confirmation notification journey

> Related issues: [#30](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/30); later slices pending creation
> Status: #30 ready for planning but blocked for implementation; later slices draft
> Last updated: 2026-11-15

---

## Problem

RN-06 requires a confirmation SMS request after a telephone reservation is persisted, with its known state retained even if sending or delivery fails. Request acceptance is distinct from delivery. The initial reservation table has no notification record or status. Provider states and identifiers still need verification.

## Proposed solution

Build the journey in bounded slices: persist notification state, request an SMS after a confirmed telephone reservation, and process delivery-status callbacks without duplicate effects. Use the approved findings from [Vonage contract research](vonage-01-contract-verification.spec.md) to define the exact provider contracts and state transitions. A notification failure never cancels a confirmed reservation.

## Delivery map

| Issue | Deliverable | Readiness and dependencies |
| --- | --- | --- |
| [#30](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/30) | Persist notification state and correlation data. The existing scope and acceptance criteria below apply only to #30. | Published and ready for planning; implementation blocked by mentor-reviewed #26 findings and approved state mapping. |
| Pending creation: SMS request | Request one confirmation SMS after a telephone reservation is confirmed and record the provider outcome. | Draft; depends on #7, #30, verified Messages request contract, and approval of the send/correlation design. |
| Pending creation: status callbacks | Accept verified Messages status events and update the matching notification idempotently. | Draft; depends on #30, verified callback contract and authentication, and approved state transitions. |

Future issue numbers and links belong in this map after publication. Each issue must link back to this spec. The later slices do not expand #30.

## Scope

The scope and acceptance criteria in this section preserve the already published #30 persistence issue. The later slices are specified separately below.

### In scope

- [ ] Propose and approve the notification entity/table fields, allowed state transitions, and relationship to a reservation using the verified Messages contract.
- [ ] Persist a known request state, provider identifier when returned, and failure category or safe reason when available.
- [ ] Keep a confirmed reservation intact when a notification is failed or undeliverable.
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

- Trigger only after a telephone reservation has been persisted as confirmed through #7. Do not request a message after rejected, abandoned, or unconfirmed attempts.
- Use the approved persistence operations from #30 to record a request attempt and its known outcome. Distinguish provider acceptance from recipient delivery.
- Keep the reservation confirmed if the provider request fails, times out, or has an ambiguous outcome. Record a safe diagnostic state without assuming a retry policy.
- Define the exact Messages request, authentication, response mapping, and correlation from the completed #26 research before implementation. Do not infer that manual reservations receive SMS; that remains a mentor decision.
- Validate with provider test doubles, including accepted, rejected, failed, and ambiguous request outcomes. No automated test may spend credits by default.

### Delivery-status callbacks

- Match a callback to a known notification using only identifiers and semantics confirmed by #26. Unknown or ambiguous correlations must not mutate another reservation or notification.
- Verify callback authentication and required response behaviour from the official contract before implementing an endpoint. Do not assume a signature format, payload, or retry schedule.
- Apply approved state transitions idempotently: repeated or out-of-order callbacks must not duplicate effects or regress a final state without an explicitly approved rule.
- Retain only safe status and failure information; do not store raw webhook bodies or sensitive provider errors.
- Validate authenticated/invalid, duplicate, out-of-order, unknown-correlation, and failure cases with test doubles and PostgreSQL-backed tests where state transitions or constraints are involved.

These later slices are planning boundaries, not approved provider contracts or permission to implement. Their precise acceptance criteria and blockers must be finalized in their issues after #26 and mentor review.

## Business rules and decisions

- Business rules: [RN-06](../docs/business-rules.md#rn-06-confirmation-messages).
- Related ADRs: [ADR-004](../docs/adr/004-adopt-flyway-migration-coordination-policy.md).
- Open questions: final provider status mapping, request/callback correlation and authentication, ambiguous request outcomes, and whether manual bookings receive SMS. Resolve questions affecting each slice before that slice starts; do not apply an unapproved policy to manual bookings.

## Technical design

### Backend

Provide persistence operations for notification records and lookup by verified provider identifier. Exact fields and transition rules are pending the Vonage research and mentor review.

### Frontend

No UI change.

### Data and migrations

Add a later versioned migration; reserve its version and proposed filename only when implementation starts after updating from `develop`. Do not alter merged `V1__create_reservations.sql`. Define indexes and constraints from the approved correlation model, not from assumptions.

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

- [ ] Review the schema and state mapping against the completed official-contract evidence.
- [ ] Check the proposed migration version against current `develop` before implementation and review.

## Dependencies and environment

- [ ] Blocked by completion and mentor review of [Vonage contract research](vonage-01-contract-verification.spec.md).
- [ ] Depends on the existing [#4](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/4) reservation model. Coordinate with [#7](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/7) for the post-persistence workflow.

## Notes

Issue #30 is already published as planned work. Its implementation must wait for provider evidence and the approved state mapping. The later delivery-map entries are proposals until their own issue scopes and prerequisites are reviewed.
