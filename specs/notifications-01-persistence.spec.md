# Spec: Persist reservation notification state

> Issue: pending creation | Branch: pending issue assignment
> Status: ready
> Last updated: 2026-11-15

---

## Problem

RN-06 requires the application to retain what it knows about a confirmation SMS, including failures, while keeping the reservation confirmed. The initial reservation table has no notification record or status. Provider states and identifiers still need verification.

## Proposed solution

After [Vonage contract research](vonage-01-contract-verification.spec.md) is approved, define a small persistent notification model tied to a confirmed reservation. Record the request's known state, provider correlation identifier when available, and a safe failure reason. Add a coordinated Flyway migration and repository tests. Sending SMS and handling callbacks belong to later issues.

## Scope

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

## Business rules and decisions

- Business rules: [RN-06](../docs/business-rules.md#rn-06-confirmation-messages).
- Related ADRs: [ADR-004](../docs/adr/004-adopt-flyway-migration-coordination-policy.md).
- Open questions: final provider status mapping, request/callback correlation, and whether manual bookings receive SMS. These must be resolved or explicitly scoped before implementation.

## Technical design

### Backend

Provide persistence operations for notification records and lookup by verified provider identifier. Exact fields and transition rules are pending the Vonage research and mentor review.

### Frontend

No UI change.

### Data and migrations

Add a later versioned migration; reserve its version and proposed filename only when implementation starts after updating from `develop`. Do not alter merged `V1__create_reservations.sql`. Define indexes and constraints from the approved correlation model, not from assumptions.

### Vonage integration

The [research spec](vonage-01-contract-verification.spec.md) supplies documented message identifiers, lifecycle states, and callback behaviour. This persistence issue makes no external request.

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

The issue can be published as planned work now. Implementation must wait for provider evidence and the approved state mapping.
