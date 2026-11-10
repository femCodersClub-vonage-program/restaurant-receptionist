# Spec: Create the initial reservation persistence model

> Issue: [#4](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/4) | Branch: `feature/4-initial-persistence`
> Status: done
> Last updated: 2026-11-10

---

## Problem

The application has no persistent representation of a confirmed reservation. Subsequent capacity, manual-reservation, telephone, notification, and dashboard work needs one small, reliable database foundation without prematurely introducing customer accounts or unapproved booking behaviour.

## Proposed solution

Create the first Flyway migration and the minimal Spring Data JPA persistence model for a confirmed reservation. The model stores the reservation details defined by the MVP and supports querying reservations by date and service.

## Scope

### In scope

- [ ] A versioned Flyway migration named `V1__create_reservations.sql`.
- [ ] A `reservations` table for confirmed reservations.
- [ ] A JPA entity and repository for the table.
- [ ] PostgreSQL-backed integration tests using Testcontainers.
- [ ] An index that supports the dashboard query by reservation date, service, and arrival time.

### Out of scope

- [ ] A `customer` table, customer accounts, authentication, or recurring-customer behaviour.
- [ ] Reservation creation endpoints, capacity calculation, telephone flow, SMS notifications, cancellation, or modification.
- [ ] A business-duplicate rule or database uniqueness rule beyond the primary key.
- [ ] Seed data or real personal data.

## Acceptance criteria

- [ ] Given an empty PostgreSQL database, when Flyway runs, then it creates the `reservations` table successfully.
- [ ] Given a confirmed reservation with the required MVP fields, when it is persisted and retrieved, then its values are preserved.
- [ ] Given reservations for different dates or services, when the repository queries one date and service, then it returns only the matching reservations ordered by arrival time.
- [ ] Given an invalid party size of zero or a value above the total service capacity, when the database constraint is evaluated, then persistence is rejected.
- [ ] Given the committed migration, when the integration test suite runs with Testcontainers, then the migration is validated against PostgreSQL 17.

## Business rules and decisions

- Business rules: [RN-01](../docs/business-rules.md#rn-01-capacity-by-service), [RN-07](../docs/business-rules.md#rn-07-staff-dashboard-and-manual-reservations)
- Related ADRs: [ADR-003](../docs/adr/003-separate-local-database-and-integration-test-environments.md), [ADR-004](../docs/adr/004-adopt-flyway-migration-coordination-policy.md)
- Open questions or assumptions:
  - This work stores only confirmed reservations. The first supported reservation status is `CONFIRMED`.
  - Customer name and phone number belong to the reservation record for this MVP; they do not define a customer identity.
  - Telephone normalization, hidden caller IDs, and business-duplicate behaviour remain open product decisions.

## Technical design

### Backend

Create a reservation persistence model under the existing backend package structure. It must be limited to persistence responsibilities; service-hour validation and capacity decisions belong to later work.

| Component | Description |
| --- | --- |
| `Reservation` entity | Maps the confirmed-reservation record. |
| `ReservationRepository` | Provides a date-and-service query ordered by arrival time. |
| persistence integration test | Verifies migration and repository behaviour against PostgreSQL. |

### Frontend

Not applicable.

### Data and migrations

`reservations` must contain, at minimum:

| Column | Purpose |
| --- | --- |
| `id` | Stable primary key. |
| `customer_first_name` | Required reservation contact first name. |
| `customer_last_name` | Required reservation contact last name. |
| `phone_number` | Required reservation contact phone number, stored as text. |
| `reservation_date` | Requested calendar date. |
| `arrival_time` | Requested arrival time. |
| `service` | Lunch or dinner, derived by later validation logic. |
| `party_size` | Positive number of guests, limited to the service capacity of 30. |
| `status` | Initially `CONFIRMED`. |
| `created_at` | Timestamp of persistence. |

All reservation business fields must be `NOT NULL`. The migration must add a primary key, a check for party size from 1 to 30, checks that limit `service` to `LUNCH` or `DINNER` and `status` to `CONFIRMED`, and an index for date, service, and arrival time. It must not encode the still-open arrival-window endpoint decision as a database constraint.

### Vonage integration

Not applicable.

## Security and data handling

Names and phone numbers are personal data even in a fictional demo. Tests and fixtures must use clearly fictional values. Do not add real phone numbers, screenshots containing personal data, or credentials to the repository.

## Accessibility and UX

Not applicable.

## Testing

### Automated

- [ ] Testcontainers integration test applies `V1__create_reservations.sql` successfully.
- [ ] Integration test persists and retrieves a valid reservation.
- [ ] Integration test verifies filtering and ordering by date and service.
- [ ] Integration test verifies the party-size constraints.

### Manual verification

- [ ] Start the local PostgreSQL service, run the backend, and confirm Flyway applies the migration without error.

## Dependencies and environment

- [ ] Depends on the completed initial technical setup.
- [ ] Uses the existing PostgreSQL, Flyway, JPA, and Testcontainers configuration.
- [ ] Uses migration version `V1`; the contributor must follow the migration coordination policy in `CONTRIBUTING.md`.

## Notes

This is intentionally a reservation-only foundation. Any later customer model must be introduced through a separate approved spec and migration.
