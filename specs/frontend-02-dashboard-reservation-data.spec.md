# Spec: Connect the dashboard to reservation data

> Issue: [#11](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/11) | Branch: `feature/11-dashboard-reservation-data`
> Status: ready
> Last updated: 2026-11-10

---

## Problem

The dashboard shell provides structure but cannot yet help staff view reservations or capacity. It needs to consume an approved backend read contract and present real, fictional-demo data clearly.

## Proposed solution

Connect the staff dashboard to backend endpoints that return reservations and calculated capacity for a selected date. Render lunch and dinner summaries, ordered reservation lists, and explicit loading, empty, and error states.

## Scope

### In scope

- [ ] An approved read API contract for reservations and capacity by selected date.
- [ ] Frontend data fetching and rendering for both services.
- [ ] Date selection and manual refresh.
- [ ] Accessible loading, empty, and error states.
- [ ] Display of the fields required by RN-07.

### Out of scope

- [ ] Manual reservation submission, real-time updates, authentication, pagination, filtering by customer, or notification status details.
- [ ] Client-side capacity calculation.
- [ ] Any Vonage integration.

## Acceptance criteria

- [ ] Given a selected date with reservations, when the dashboard loads data, then it shows lunch and dinner reservations ordered by arrival time.
- [ ] Given a selected date, when the dashboard loads data, then it shows total, occupied, and available capacity calculated by the backend for each service.
- [ ] Given no reservations for the selected date, when data loads successfully, then the dashboard shows an explicit empty state.
- [ ] Given a backend request fails, when the dashboard renders, then it shows an understandable error state and a way to retry.
- [ ] Given a user changes the date or requests a refresh, when the request succeeds, then the displayed data reflects the selected date.

## Business rules and decisions

- Business rules: [RN-01](../docs/business-rules.md#rn-01-capacity-by-service), [RN-07](../docs/business-rules.md#rn-07-staff-dashboard-and-manual-reservations)
- Related ADRs: [ADR-002](../docs/adr/002-establish-initial-repository-layout.md)
- Open questions or assumptions:
  - The backend read route and response contract must be approved before this issue moves to **Ready**.
  - The backend, not the browser, remains the source of truth for capacity.

## Technical design

### Backend

Provide a read contract for a selected date that returns reservations grouped or groupable by service and a capacity summary for each service. The exact route is pending contract approval.

| Method | Route or event | Description |
| --- | --- | --- |
| GET | Pending contract approval | Returns reservations and backend-calculated capacity for one selected date. |

### Frontend

Extend the dashboard shell with a data-access boundary, typed response handling, and presentational service sections. Keep request errors and empty results distinct.

### Data and migrations

Uses the existing reservations model. No migration is expected.

### Vonage integration

Not applicable.

## Security and data handling

Reservation names and phone numbers must be treated as demo personal data. Do not log API responses in the browser console and do not add real values to fixtures or screenshots.

## Accessibility and UX

- Announce loading and refreshed content appropriately.
- Keep date controls labelled and keyboard accessible.
- Present errors in text, not colour alone.
- Make each service summary understandable without relying on visual grouping alone.

## Testing

### Automated

- [ ] Frontend lint and production build pass.
- [ ] Add focused component or integration tests if the approved frontend test tooling is introduced in a separate decision.

### Manual verification

- [ ] Verify populated, empty, loading, and failed-request states with fictional data.
- [ ] Verify date selection and refresh with keyboard navigation.

## Dependencies and environment

- [ ] Depends on `ui-01`, `ui-02`, `ui-03`, `frontend-01`, `data-01`, `backend-01`, and the approved backend read contract.

## Notes

Manual reservation creation is deliberately separate so displaying existing reservations can be delivered and reviewed independently.
