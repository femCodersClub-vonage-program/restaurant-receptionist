# Spec: Expose manual confirmed reservation creation

> Issue: [#28](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/28) | Branch: `feature/28-manual-reservation-api`
> Status: in-progress
> Last updated: 2026-11-16
> Contract: pending mentor approval

---

## Problem

RN-07 requires staff to create a reservation manually, but the backend currently has no approved HTTP creation contract. A separate frontend flow must not bypass the shared reservation rules.

## Proposed solution

Expose a Spring MVC write endpoint that accepts the required reservation fields and delegates to the channel-agnostic confirmed-reservation service in #7. Propose the exact route, request/response types, and error mapping in this spec for mentor approval before implementation.

## Scope

### In scope

- [ ] Define and approve the POST route, request shape, success response, validation errors, and insufficient-capacity response.
- [ ] Map first and last name, phone, date, arrival time, and party size to the #7 application command without accepting a client-supplied service or capacity.
- [ ] Return a distinguishable outcome for successful persistence, invalid data, and insufficient capacity.
- [ ] Add controller and PostgreSQL-backed integration tests; document the implemented contract in generated OpenAPI.

### Out of scope

- [ ] Reimplementing validation or transactional capacity checks in the controller.
- [ ] Frontend form, Voice webhooks, customer account, business-duplicate policy, cancellation, or modification.
- [ ] Adding authentication to the controlled demo, or deciding how a manual booking triggers SMS before that product policy is approved.

## Acceptance criteria

- [ ] Given valid fictional details, when staff submit a manual request, exactly one confirmed reservation is persisted through #7 and the endpoint returns its approved success response.
- [ ] Given invalid fields, a past date, or an invalid arrival time, the endpoint returns a documented client error and persists nothing.
- [ ] Given insufficient capacity, the endpoint reports that outcome without persisting a reservation.
- [ ] Concurrent requests cannot oversell a service because they use the same #7 persistence path.
- [ ] The OpenAPI description and tests match the approved request, success, and error contracts.

## Business rules and decisions

- Business rules: [RN-01](../docs/business-rules.md#rn-01-capacity-by-service), [RN-03](../docs/business-rules.md#rn-03-reservation-arrival-windows), [RN-07](../docs/business-rules.md#rn-07-staff-dashboard-and-manual-reservations).
- Related ADRs: [ADR-006](../docs/adr/006-add-generated-openapi-documentation.md).
- Open questions: exact HTTP contract requires mentor approval. The MVP does not yet decide whether manual bookings request SMS; this issue neither assumes nor implements that behaviour.

## Technical design

### Backend

Use the #7 service as the single creation path. Fill in the contract before implementation:

| Method | Route or event | Description |
| --- | --- | --- |
| POST | `/api/reservations` | Create a confirmed manual reservation. |

#### Proposed contract (pending mentor approval)

**Route:** `POST /api/reservations` with an `application/json` body.

The route creates a reservation resource and shares its path with the approved read contract (`GET /api/reservations?date=...`). It is not tied to the dashboard. The telephone flow calls `ReservationCreationService` directly and must not call this endpoint.

**Request body:**

```json
{
    "customerFirstName": "Fictional",
    "customerLastName": "Guest",
    "phoneNumber": "fictional-test-contact",
    "reservationDate": "2027-05-14",
    "arrivalTime": "21:00",
    "partySize": 4
}
```

| Field | Type | Format | Notes |
|---|---|---|---|
| `customerFirstName` | string | Non-blank | Leading and trailing whitespace is stripped by #7. |
| `customerLastName` | string | Non-blank | Leading and trailing whitespace is stripped by #7. |
| `phoneNumber` | string | Non-blank | No normalization or format policy is introduced. |
| `reservationDate` | string | ISO-8601 date, `YYYY-MM-DD` | Must not be in the past. |
| `arrivalTime` | string | Strict `HH:mm` | Must fall within an RN-03 arrival window; on the current date it must be later than now. |
| `partySize` | integer | Whole number | Must be within the RN-01 range. |

The client does not send a dining session or capacity value. The backend derives the session from `arrivalTime`, and #7 calculates capacity. Unknown properties are ignored, so a client-supplied `diningSession` has no effect.

**Successful response:** `201 Created`

```json
{
    "id": "00000000-0000-0000-0000-000000000001",
    "diningSession": "DINNER"
}
```

There is no `Location` header, because the MVP has no read-by-id endpoint. The frontend refreshes the dashboard through `GET /api/reservations?date=...`.

**Validation error:** `400 Bad Request` using RFC 9457 Problem Details (`application/problem+json`), with the `field` and `reason` extensions:

```json
{
    "type": "about:blank",
    "title": "Bad Request",
    "status": 400,
    "detail": "The reservation request contains invalid data.",
    "field": "PARTY_SIZE",
    "reason": "OUT_OF_RANGE"
}
```

| `field` | Request property |
|---|---|
| `FIRST_NAME` | `customerFirstName` |
| `LAST_NAME` | `customerLastName` |
| `PHONE_NUMBER` | `phoneNumber` |
| `RESERVATION_DATE` | `reservationDate` |
| `ARRIVAL_TIME` | `arrivalTime` |
| `PARTY_SIZE` | `partySize` |

| `reason` | Origin | Meaning |
|---|---|---|
| `MISSING` | #7 | The field is absent or blank. |
| `PAST_DATE` | #7 | `reservationDate` is before today. |
| `TIME_NOT_IN_FUTURE` | #7 | On today's date, `arrivalTime` is not later than now. |
| `OUTSIDE_ARRIVAL_WINDOW` | #7 | `arrivalTime` is outside the RN-03 lunch and dinner windows. |
| `OUT_OF_RANGE` | #7 | `partySize` is outside the RN-01 range. |
| `MALFORMED` | HTTP adapter | The value cannot be parsed into the documented type or format. |

`field` and `reason` reuse the #7 `ReservationField` and `InvalidData.Reason` values, so validation is not duplicated. When a value cannot be parsed (for example, `"reservationDate": "14/05/2027"`, `"arrivalTime": "21:00:00"`, or `"partySize": "four"`), #7 is never invoked. In that case the response uses `reason: "MALFORMED"` and the field resolved from the JSON property:

```json
{
    "type": "about:blank",
    "title": "Bad Request",
    "status": 400,
    "detail": "The reservation request contains invalid data.",
    "field": "ARRIVAL_TIME",
    "reason": "MALFORMED"
}
```

If the body is not readable JSON, or the failing property cannot be identified, the response is `400 Bad Request` with `detail: "The request body is not readable."` and no `field` or `reason`.

**Adapter-side checks:** the controller adds no business rules. It has only two checks of its own:

1. Parsing errors produce `MALFORMED`, because they happen before #7 is reached.
2. A missing or `null` `partySize` returns `PARTY_SIZE` / `MISSING` without invoking #7. The #7 command uses a primitive `int`, so a missing value cannot be passed through. The outcome reuses an existing #7 reason.

**Insufficient capacity:** `409 Conflict` using Problem Details, with capacity extensions:

```json
{
    "type": "about:blank",
    "title": "Conflict",
    "status": 409,
    "detail": "The dining session does not have enough available capacity for the requested party size.",
    "diningSession": "DINNER",
    "availableCapacity": 3,
    "requestedPartySize": 4
}
```

Nothing is persisted on a `400` or `409` response. Capacity is checked and locked by #7 in the same transaction as persistence, so concurrent requests cannot oversell a service.

**Data handling:** error responses and logs must not include names, phone numbers, or request bodies. Problem Details is proposed for this endpoint only and does not add a global exception handler.

### Frontend

The separate [manual form spec](frontend-03-manual-reservation-form.spec.md) consumes the approved contract.

### Data and migrations

Use the existing reservation model and #7. No migration is assumed; a required schema change must be proposed and approved before implementation.

### Vonage integration

None in this endpoint. A later notification workflow must resolve whether manually created bookings receive SMS.

## Security and data handling

The dashboard is a controlled fictional demo without login under RN-07. Use fictional data in tests and documentation; do not log full request bodies, names, or phone numbers. Deployment exposure is a separate decision.

## Accessibility and UX

Return stable, understandable error categories so the form can associate errors with fields and show capacity outcomes in text.

## Testing

### Automated

- [ ] Controller tests cover request validation and each approved response shape.
- [ ] Testcontainers tests cover persisted success, rejection without persistence, and shared capacity behaviour.
- [ ] Reuse #7 concurrency coverage; add endpoint-level concurrency coverage only if the adapter introduces a distinct risk.

### Manual verification

- [ ] Inspect generated OpenAPI and exercise fictional success, validation, and capacity cases locally.

## Dependencies and environment

- [ ] Blocked by [#7](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/7) being merged.
- [ ] Mentor approves the HTTP contract in this spec before implementation starts.

## Notes

No business-duplicate or phone-normalization policy is introduced. Those remain separate product decisions.
