# Spec: Expose dashboard reservation and capacity data

> Issue: [#20](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/20) | Branch: `feature/20-dashboard-read-api`
> Status: ready
> Last updated: 2026-11-10

---

## Problem

Frontend issue #11 needs an approved backend read contract for reservations and capacity on a selected date. The database query exists, while #6 will provide the capacity calculation.

## Proposed solution

Define and implement a read-only Spring MVC contract for a selected date that returns lunch and dinner reservations in arrival order and backend-calculated capacity for both sessions.

## Scope

### In scope

- [ ] Approve the route, query parameter, response shape, and validation/error responses before implementation.
- [ ] Return reservation fields required by RN-07 and a capacity summary for each session.
- [ ] Document the implemented contract with OpenAPI when that foundation is available.
- [ ] Add controller and PostgreSQL-backed integration tests for populated, empty, and invalid-date cases.

### Out of scope

- [ ] Create or modify reservations, calculate capacity in the browser, or add authentication.
- [ ] Add pagination, notification details, realtime updates, or Vonage integration.

## Acceptance criteria

- [ ] A valid selected date returns both lunch and dinner, including empty lists and total, occupied, and available capacity.
- [ ] Reservations belong only to the selected date and are ordered by arrival time within each session.
- [ ] The response includes name, phone, date, time, party size, and reservation status required by RN-07.
- [ ] An invalid or absent date receives a documented, understandable client error.
- [ ] Capacity comes from #6 and matches persisted confirmed reservations.

## Business rules and decisions

- Business rules: [RN-01](../docs/business-rules.md#rn-01-capacity-by-service), [RN-07](../docs/business-rules.md#rn-07-staff-dashboard-and-manual-reservations).
- Related ADRs: None required for this read contract.
- Open questions or assumptions: The endpoint author will propose the exact route, date parameter, response shape, and error format in this spec. The mentor confirms them before endpoint implementation; this decision is not needed to create the issue.

## Technical design

### Backend

Use the repository query from #4 and capacity service from #6. Record the proposed GET contract here for mentor review before implementation, then implement it consistently. Keep a single backend source of truth for capacity.

| Method | Route or event | Description |
| --- | --- | --- |
| GET | Pending contract approval | Read both sessions for one selected date. |

### Frontend

Issue #11 consumes this contract; frontend implementation is outside this issue.

### Data and migrations

Use V1 reservations; no migration expected.

### Vonage integration

Not applicable.

## Security and data handling

This controlled demo has no login under RN-07. Use fictional data and do not log contact fields or return internal entity details beyond the approved response.

## Accessibility and UX

Provide distinguishable empty and error outcomes so #11 can render them accessibly.

## Testing

### Automated

- [ ] Controller tests cover date validation and response shape.
- [ ] Testcontainers integration tests cover both services, ordering, isolation by date, and calculated capacity.

### Manual verification

- [ ] Run backend tests and inspect the documented endpoint locally.

## Dependencies and environment

- [ ] #4 and #6 must be merged. #7 is not required.
- [ ] Coordinate with [frontend issue #11](frontend-02-dashboard-reservation-data.spec.md) before approving the response contract.

## Notes

OpenAPI foundation may proceed in parallel; once merged, this endpoint must appear in the generated document.
