# Spec: Create confirmed reservation service

> Issue: [#7](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/7) | Branch: `feature/7-create-confirmed-reservation-service`
> Status: ready
> Last updated: 2026-11-10

---

## Problem

The MVP needs one safe way to create a confirmed reservation after a caller has completed the voice flow. A later manual staff flow must reuse the same rules rather than implement its own capacity or persistence logic.

## Proposed solution

Create a channel-agnostic backend application service that receives already confirmed reservation details, validates them through the shared availability service, performs final capacity revalidation, and persists the reservation atomically.

## Scope

### In scope

- [ ] An application command containing first name, last name, phone number, reservation date, arrival time, and party size.
- [ ] Shared validation of reservation data, service hours, date and time validity, and capacity.
- [ ] Derivation of lunch or dinner from the arrival time; callers do not provide a separate service value.
- [ ] Transactional final capacity revalidation and persistence of one confirmed reservation.
- [ ] Clear application-level outcomes for invalid data and insufficient capacity.
- [ ] Unit and PostgreSQL-backed integration tests, including concurrent attempts for the last available places.

### Out of scope

- [ ] HTTP controllers, request/response DTOs, frontend forms, Voice webhooks, or SMS notifications.
- [ ] Capturing caller details or asking the caller for confirmation; this service receives details only after confirmation has already happened.
- [ ] Customer accounts, business-duplicate rules, reservation cancellation, or modification.

## Acceptance criteria

- [ ] Given valid, confirmed details and sufficient capacity, when the service creates a reservation, then exactly one `CONFIRMED` reservation is persisted.
- [ ] Given invalid required data, an invalid arrival window, a past date, or a same-day arrival time that is current or already passed in `Europe/Madrid`, when the service is called, then no reservation is persisted.
- [ ] Given a party that exceeds the available capacity, when the service is called, then no reservation is persisted and the result reports insufficient capacity.
- [ ] Given concurrent confirmed-reservation attempts for the last available places, when they complete, then the total confirmed party size never exceeds 30 for that date and service.
- [ ] Given the same confirmed details supplied by different future channels, when the service is called, then it applies the same validation and persistence rules without channel-specific behaviour.

## Business rules and decisions

- Business rules: [RN-01](../docs/business-rules.md#rn-01-capacity-by-service), [RN-03](../docs/business-rules.md#rn-03-reservation-arrival-windows), [RN-04](../docs/business-rules.md#rn-04-confirmation-and-corrections), [RN-05](../docs/business-rules.md#rn-05-errors-attempts-and-abandonment)
- Related ADRs: [ADR-003](../docs/adr/003-separate-local-database-and-integration-test-environments.md), [ADR-004](../docs/adr/004-adopt-flyway-migration-coordination-policy.md)
- Open questions or assumptions:
  - The restaurant time zone is `Europe/Madrid`.
  - A same-day arrival time must be strictly later than the current local time; no implicit grace period applies.
  - A phone number is required and non-blank. International normalization and business-duplicate handling remain later decisions.

## Technical design

### Backend

Create an application-level use case, separate from transport adapters. Voice and future manual-reservation adapters pass the same confirmed-reservation command to it.

| Input | Result |
| --- | --- |
| Confirmed reservation command | Persisted confirmed reservation, invalid-data result, or insufficient-capacity result. |

The implementation must use a testable clock and the `Europe/Madrid` zone when checking date and time validity. It must use a documented transactional PostgreSQL approach that prevents overselling under concurrent requests. If that approach needs additional schema or a significant architectural decision, document it before implementation continues.

### Frontend

Not applicable.

### Data and migrations

Uses the `reservations` table from `data-01`. No additional migration is assumed; any required schema change must be explicitly approved and introduced through a coordinated Flyway migration.

### Vonage integration

Not applicable. A later Voice adapter calls this use case after verified call-flow confirmation.

## Security and data handling

Validate all input before persistence. Do not log raw reservation commands or full telephone numbers. Tests and fixtures must use clearly fictional contact data.

## Accessibility and UX

Not applicable to this backend service. Its outcomes must allow future channel adapters to provide understandable validation and capacity messages.

## Testing

### Automated

- [ ] Unit tests cover data validation, service derivation, and temporal validation using a fixed clock.
- [ ] PostgreSQL-backed integration tests cover successful persistence and insufficient-capacity rejection.
- [ ] Concurrency integration test proves that concurrent creation attempts cannot oversell a service.

### Manual verification

- [ ] Run the backend test suite locally against the Testcontainers configuration.

## Dependencies and environment

- [ ] Depends on `data-01` and `backend-01` being merged.
- [ ] Uses the existing PostgreSQL, Flyway, JPA, and Testcontainers setup.

## Notes

The voice flow is the primary MVP consumer. A future manual-reservation API must delegate to this service instead of reimplementing its rules.
