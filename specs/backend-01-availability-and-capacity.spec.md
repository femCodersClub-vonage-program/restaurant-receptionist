# Spec: Implement service availability and capacity calculation

> Issue: pending creation | Branch: `feature/<issue-number>-service-availability-capacity`
> Status: ready
> Last updated: 2026-11-03

---

## Problem

Once reservations are persisted, the application needs one backend source of truth for lunch and dinner capacity. Telephone and manual channels must never calculate availability independently.

## Proposed solution

Implement a backend application service that derives the service from a valid arrival time, calculates total, occupied, and available guests for a date and service, and validates whether a proposed party can fit. Expose no public endpoint in this issue; the result is consumed by later reservation-creation and dashboard API work.

## Scope

### In scope

- [ ] Service classification for valid lunch and dinner arrival times.
- [ ] Calculation of total capacity, occupied guests, and available guests from confirmed reservations.
- [ ] Validation that party size is a whole number from 1 to 30.
- [ ] Unit and PostgreSQL-backed integration tests for capacity calculation.
- [ ] Domain errors or result types that allow later callers to distinguish invalid time from insufficient capacity.

### Out of scope

- [ ] Persistence of a new reservation, HTTP endpoints, dashboard data fetching, or Vonage integration.
- [ ] Alternative-service conversation flow, date searching, cancellation, modification, or table allocation.
- [ ] Concurrent final persistence handling; that belongs to the reservation-creation issue.

## Acceptance criteria

- [ ] Given confirmed lunch reservations for one date, when availability is calculated for that lunch service, then occupied and available capacity equal the sum of their party sizes and `30 - occupied`.
- [ ] Given confirmed reservations for another date or dinner service, when lunch availability is calculated, then they do not affect the result.
- [ ] Given a party size below 1 or above 30, when fit validation is requested, then the result is invalid.
- [ ] Given a party that exceeds remaining capacity, when fit validation is requested, then the result reports insufficient capacity.
- [ ] Given an arrival time from 13:00 through 16:00 inclusive, when its service is derived, then it is classified as lunch.
- [ ] Given an arrival time from 20:00 through 23:30 inclusive, when its service is derived, then it is classified as dinner.
- [ ] Given an arrival time outside those inclusive windows, when its service is derived, then it is rejected as invalid.

## Business rules and decisions

- Business rules: [RN-01](../docs/business-rules.md#rn-01-capacity-by-service), [RN-03](../docs/business-rules.md#rn-03-reservation-arrival-windows)
- Related ADRs: [ADR-003](../docs/adr/003-separate-local-database-and-integration-test-environments.md)
- Open questions or assumptions:
  - Arrival times use minute precision. The service is derived by the backend from the arrival time; callers must not supply a separate service value.
  - Reservation date and time validity, including the `Europe/Madrid` time zone and past-date handling, are enforced by `reservations-01`; this availability service focuses on service classification and capacity.
  - Maximum booking horizon is not part of this calculation yet.

## Technical design

### Backend

Create an application-level availability service. Its public result must include the service, total capacity, occupied capacity, and available capacity. It must obtain occupied capacity from the reservation persistence model rather than receiving a client-provided total. The 30-place capacity for each service must be defined in one central application location rather than repeated as literals across classes.

| Input | Result |
| --- | --- |
| reservation date and service | capacity summary for that date and service |
| arrival time | derived service or invalid-time result |
| date, arrival time, and party size | validation result for a proposed reservation |

### Frontend

Not applicable.

### Data and migrations

Uses the `reservations` table created by `data-01`. No migration is expected.

### Vonage integration

Not applicable.

## Security and data handling

The service must not log reservation names or telephone numbers when calculating aggregate capacity.

## Accessibility and UX

Not applicable.

## Testing

### Automated

- [ ] Unit tests cover valid service classification and invalid times.
- [ ] Unit tests cover zero, partial, and full capacity.
- [ ] PostgreSQL-backed integration test verifies that capacity is calculated from persisted confirmed reservations only.

### Manual verification

- [ ] Run the backend test suite locally and confirm all capacity scenarios pass.

## Dependencies and environment

- [ ] Depends on `data-01` being merged.

## Notes

This service is deliberately channel-agnostic. Both telephone and manual dashboard reservations will use it later.
