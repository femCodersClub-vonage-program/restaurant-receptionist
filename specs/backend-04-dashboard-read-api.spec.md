# Spec: Expose dashboard reservation and capacity data

> Issue: [#20](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/20) | Branch: `feature/20-dashboard-read-api`
> Status: in-progress
> Last updated: 2026-11-16
> Contract: approved by mentor

---

## Problem

Frontend issue #11 needs an approved backend read contract for reservations and capacity on a selected date. The database query from #4 and capacity calculation from #6 are now available; both issues have been merged.

## Proposed solution

Define and implement a read-only Spring MVC contract for a selected date that returns lunch and dinner reservations in arrival order and backend-calculated capacity for both sessions.

## Scope

### In scope

- [x] Mentor approved the route, query parameter, response shape, and validation/error responses before implementation.
- [x] Return reservation fields required by RN-07 and a capacity summary for each session.
- [x] Document the implemented contract in the generated OpenAPI document.
- [x] Add controller and PostgreSQL-backed integration tests for populated, empty, and invalid-date cases.

### Out of scope

- [ ] Create or modify reservations, calculate capacity in the browser, or add authentication.
- [ ] Add pagination, notification details, realtime updates, or Vonage integration.

## Acceptance criteria

- [x] A valid selected date returns both lunch and dinner, including empty lists and total, occupied, and available capacity.
- [x] Reservations belong only to the selected date and are ordered by arrival time within each session.
- [x] The response includes name, phone, date, time, party size, and reservation status required by RN-07.
- [x] A missing or invalid date receives `400 Bad Request` using RFC 9457 Problem Details.
- [x] Capacity comes from #6 and matches persisted confirmed reservations.
- [x] Each reservation includes its stable UUID as a frontend list key.

## Business rules and decisions

- Business rules: [RN-01](../docs/business-rules.md#rn-01-capacity-by-service), [RN-07](../docs/business-rules.md#rn-07-staff-dashboard-and-manual-reservations).
- Related ADRs: None required for this read contract.
- Open questions or assumptions: None for the approved read contract.

## Technical design

### Backend

Use the repository query from #4 and capacity service from #6. Implement the approved GET contract consistently and keep a single backend source of truth for capacity.

| Method | Route or event | Description |
| --- | --- | --- |
| GET | `/api/reservations?date=YYYY-MM-DD` | Read both sessions for one selected date. |

#### Mentor-approved contract

**Route:** `GET /api/reservations?date=YYYY-MM-DD`

The path is `/api/reservations`; `date` is a required query parameter after `?`. For example:

```http
GET /api/reservations?date=2026-11-12
```

This is a read projection for HTTP consumers. The telephone flow should call backend application services directly, not call this endpoint.

The route filters the reservation resource and is not tied to the dashboard or telephone channel. The response includes capacity because RN-07 requires it.

**Other routes considered:**

- `/api/dashboard?date=...` describes the combined dashboard view but ties the path to one consumer.
- `/api/dashboard/reservations?date=...` also ties the path to the dashboard, while the response contains capacity as well.

**Request parameter:**

| Parameter | Required | Format | Behaviour |
| --- | --- | --- | --- |
| `date` | Yes | ISO-8601 calendar date, `YYYY-MM-DD` | Missing or invalid values return `400 Bad Request`. Any valid past, current, or future date is readable. Reservation-creation date and time rules do not apply to this read-only request. |

**Successful response:**

Return `200 OK` with both sessions, even when either or both have no reservations. Reservations and capacity are nested under each session so the frontend can render the groups without calculating capacity or grouping a flat list.

For historical dates, including a service whose arrival window has passed today, return the persisted reservations and the RN-01 capacity formula. For example, 18 confirmed lunch guests for a past date means `occupiedCapacity: 18` and `availableCapacity: 12`. `availableCapacity` is the unoccupied-place count for that date and service; it does not mean that a new reservation can still be created for an elapsed arrival window. Reservation creation remains subject to the applicable date/time and capacity rules in RN-01 and RN-03; invalid input attempts are handled as defined in RN-05.
```json
{
  "date": "2026-11-12",
  "sessions": {
    "LUNCH": {
      "reservations": [
        {
          "id": "00000000-0000-0000-0000-000000000001",
          "customerFirstName": "Fictional",
          "customerLastName": "Guest",
          "phoneNumber": "fictional-test-contact",
          "reservationDate": "2026-11-12",
          "arrivalTime": "13:30",
          "partySize": 2,
          "status": "CONFIRMED"
        }
      ],
      "capacity": {
        "totalCapacity": 30,
        "occupiedCapacity": 2,
        "availableCapacity": 28
      }
    },
    "DINNER": {
      "reservations": [],
      "capacity": {
        "totalCapacity": 30,
        "occupiedCapacity": 0,
        "availableCapacity": 30
      }
    }
  }
}
```

Reservation items expose the RN-07 fields and the reservation UUID as a stable frontend list key. Do not expose the persistence entity or `createdAt`. Format `arrivalTime` as `HH:mm`, with no seconds, and order each session's list ascending by that value. Capacity is calculated separately for each session by `AvailabilityService` and counts confirmed reservations only.

**Approved error response:**

For a missing or invalid `date`, return `400 Bad Request` using RFC 9457 Problem Details (`application/problem+json`):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Query parameter 'date' must be a valid calendar date in YYYY-MM-DD format."
}
```

For a missing parameter, `detail` is `"Query parameter 'date' is required."`. Do not include reservation or contact data in error responses or logs.

Problem Details is approved for this endpoint only. This does not establish a shared error policy for other endpoints or add a global exception handler.

**Contract decisions and read consistency:**

- The nested response by service, reading any valid past, current, or future date, and using Problem Details for this endpoint are approved contract decisions.
- The approved contract does not require the reservation lists and capacity summaries to reflect one database snapshot. A read-only application transaction is appropriate; PostgreSQL `REPEATABLE READ` remains an optional implementation choice, not an acceptance criterion.

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

- [x] Controller tests cover date validation and response shape.
- [x] Testcontainers integration tests cover both services, ordering, isolation by date, and calculated capacity.

### Manual verification

- [x] Run backend tests and inspect the documented endpoint locally.

## Dependencies and environment

- [x] #4 and #6 are merged. #7 is not required.
- [ ] Coordinate with [frontend issue #11](frontend-02-dashboard-reservation-data.spec.md) before approving the response contract.
- [x] Mentor approval of the route, response, and error format is complete.

## Notes

The OpenAPI foundation was integrated in [PR #23](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/pull/23). This endpoint must appear in the generated document when implemented.
