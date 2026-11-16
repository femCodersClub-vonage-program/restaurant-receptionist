# Spec: Determine the alternative service on the same date

> Issue: [#19](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/19) | Branch: `feature/19-same-date-service-alternative`
> Status: done
> Last updated: 2026-11-16

---

## Problem

RN-02 requires the other service on the requested date to be considered when the requested service cannot fit a party. This decision must use the same backend capacity calculation as other channels.

## Proposed solution

Add a channel-agnostic application operation that checks the other dining session for the same date and party size, returning whether it can fit. A later voice flow decides how to present and accept the offer.

## Scope

### In scope

- [x] Resolve the opposite dining session and evaluate its capacity through #6.
- [x] Return a distinguishable available or unavailable result with the relevant capacity summary.
- [x] Test both directions, full services, and party-size boundaries.

### Out of scope

- [ ] Choose a new arrival time, automatically search dates, or create a reservation.
- [ ] Implement voice prompts, HTTP endpoints, retry counts, or customer acceptance.

## Acceptance criteria

- [x] When dinner cannot fit a valid party, lunch on the same date is evaluated; the reverse also works.
- [x] When the other service has enough places, the result identifies it as an option without persisting a reservation.
- [x] When neither service fits, the result reports no same-date alternative and persists nothing.
- [x] Invalid party sizes are not treated as an availability failure.

## Business rules and decisions

- Business rules: [RN-01](../docs/business-rules.md#rn-01-capacity-by-service), [RN-02](../docs/business-rules.md#rn-02-alternatives-when-a-service-is-full).
- Related ADRs: None required.
- Open questions or assumptions: A later caller must validate that an offered arrival time is valid for the date, especially for same-day requests. This operation does not select a time.

## Technical design

### Backend

Reuse the availability service from #6. Take a date, requested `DiningSession`, and party size. Return the opposite session and a fit outcome. Keep the operation independent of #7's final transactional creation.

### Frontend

Not applicable.

### Data and migrations

Use existing reservations; no migration.

### Vonage integration

Not applicable.

## Security and data handling

The operation needs no customer names or phone numbers. Do not log reservation contact data.

## Accessibility and UX

A later voice flow owns the phrasing and acceptance of the alternative.

## Testing

### Automated

- [x] Unit tests cover lunch-to-dinner and dinner-to-lunch, enough and insufficient places, and invalid party sizes.
- [x] PostgreSQL-backed test verifies capacity comes from confirmed reservations if #6's integration coverage does not already establish this path.

### Manual verification

- [x] Run backend tests.

## Dependencies and environment

- [x] #6 must be merged. This issue is independent of #7.

## Notes

The later Voice adapter implements the RN-02 conversation and must revalidate before final persistence.
