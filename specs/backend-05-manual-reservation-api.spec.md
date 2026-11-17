# Spec: Expose manual confirmed reservation creation

> Issue: [#28](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/28) | Branch: `feature/28-manual-reservation-api`
> Status: in-progress
> Last updated: 2026-11-17
> Contract: approved by mentor

---

## Problem

RN-07 requires staff to create a reservation manually, but the backend currently has no approved HTTP creation contract. A separate frontend flow must not bypass the shared reservation rules.

## Proposed solution

Expose a Spring MVC write endpoint that accepts the required reservation fields and delegates to the channel-agnostic confirmed-reservation service in #7. Propose the exact route, request/response types, and error mapping in this spec for mentor approval before implementation.

## Scope

### In scope

- [x] Define and approve the POST route, request shape, success response, validation errors, and insufficient-capacity response.
- [ ] Map first and last name, phone, date, arrival time, and party size to the #7 application command without accepting a client-supplied service or capacity.
- [ ] Return a distinguishable outcome for successful persistence, invalid data, and insufficient capacity.
- [ ] Add controller and PostgreSQL-backed integration tests; document the implemented contract in generated OpenAPI.

### Out of scope

- [ ] Reimplementing validation or transactional capacity checks in the controller.
- [ ] Frontend form, Voice webhooks, customer account, business-duplicate policy, cancellation, or modification.
- [ ] Adding authentication to the controlled demo, or implementing manual SMS triggering in this published endpoint issue. The product policy is approved; its wiring needs separately reviewed scope after the telephone journey.

## Acceptance criteria

- [ ] Given valid fictional details, when staff submit a manual request, exactly one confirmed reservation is persisted through #7 and the endpoint returns its approved success response.
- [ ] Given invalid fields, a past date, or an invalid arrival time, the endpoint returns a documented client error and persists nothing.
- [ ] Given insufficient capacity, the endpoint reports that outcome without persisting a reservation.
- [ ] Concurrent requests cannot oversell a service because they use the same #7 persistence path.
- [ ] The OpenAPI description and tests match the approved request, success, and error contracts.

## Business rules and decisions

- Business rules: [RN-01](../docs/business-rules.md#rn-01-capacity-by-service), [RN-03](../docs/business-rules.md#rn-03-reservation-arrival-windows), [RN-07](../docs/business-rules.md#rn-07-staff-dashboard-and-manual-reservations).
- Related ADRs: [ADR-006](../docs/adr/006-add-generated-openapi-documentation.md).
- Open questions: none for the approved HTTP contract. RN-08 phone-number validation is a separate shared-service follow-up. RN-06 to RN-08 approve SMS for manual bookings when the contact number can receive it, but this issue does not implement that later notification wiring.

## Technical design

### Backend

Use the #7 service as the single creation path. Fill in the contract before implementation:

| Method | Route or event | Description |
| --- | --- | --- |
| POST | `/api/reservations` | Create a confirmed manual reservation. |

#### Mentor-approved contract

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
| `phoneNumber` | string | Non-blank | #7 currently checks only that the value is present and not blank. The RN-08 Spanish-number restriction is not yet enforced by #7, and this adapter does not add it because that would duplicate business validation. RN-08 validation is a separate shared-service follow-up; this endpoint inherits it from `ReservationCreationService` once merged. |
| `reservationDate` | string | Strict ISO-8601 calendar date, `YYYY-MM-DD` | Must not be in the past. |
| `arrivalTime` | string | Strict 24-hour `HH:mm` | Must fall within an RN-03 arrival window; on the current date it must be later than now. |
| `partySize` | integer | Whole number | Must be within the RN-01 range. |

The client does not send a dining session or capacity value. The backend derives the session from `arrivalTime`, and #7 calculates capacity. Unknown properties are ignored, so a client-supplied `diningSession` has no effect.

**Format enforcement:** the HTTP adapter explicitly enforces the `reservationDate`, `arrivalTime` and `partySize` formats; it does not rely on lenient parser defaults. A value that does not match the documented format exactly is rejected as `MALFORMED` for its field, and #7 is not invoked. Rejected examples include:

| Field | Rejected value | Why |
|---|---|---|
| `reservationDate` | `"14/05/2027"` | Not `YYYY-MM-DD`. |
| `reservationDate` | `"2027-5-14"` | Month and day must have two digits. |
| `reservationDate` | `"2027-02-30"` | Not an existing calendar date. |
| `reservationDate` | `"2027-05-14T21:00"` | Date-time instead of a date. |
| `arrivalTime` | `"21:00:00"` | Seconds are not accepted. |
| `arrivalTime` | `"9:00"` | Hour must have two digits. |
| `reservationDate`, `arrivalTime` | Arrays or numbers, such as `[2027, 5, 14]` | Only JSON strings are accepted. |
| `partySize` | `"4"` | Only JSON integers are accepted. |
| `partySize` | `4.5` | Not a whole number. |

HTTP tests cover each rejected example.

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

Error vocabulary:

- `field` always uses the #7 `ReservationField` vocabulary. The HTTP adapter does not define its own field names.
- Every reason with Origin #7 is the existing #7 `InvalidData.Reason` value, returned unchanged. The adapter does not rename, merge, or add business reasons.
- `MALFORMED` is the only adapter-specific reason. It exists because parsing fails before #7 is invoked, so no #7 result is available. It is not added to the #7 enum.

When a value cannot be parsed (see **Format enforcement**), the response uses `reason: "MALFORMED"` and the `field` resolved from the JSON property:

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
2. A missing or `null` `partySize` returns `PARTY_SIZE` / `MISSING` without invoking #7. The #7 command uses a primitive `int`, so a missing value cannot be passed through. The outcome reuses an existing #7 reason. The mentor approved this check as an adapter necessity, not duplicated business validation.

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

None in this published endpoint issue. A later, separately reviewed workflow will connect eligible manual bookings to the shared post-persistence notification flow after the telephone journey is implemented and validated.

## Security and data handling

The dashboard is a controlled fictional demo without login under RN-07. Use fictional data in tests and documentation; do not log full request bodies, names, or phone numbers. Deployment exposure is a separate decision.

## Accessibility and UX

Return stable, understandable error categories so the form can associate errors with fields and show capacity outcomes in text.

## Testing

### Automated

- [ ] Controller tests cover request validation and each approved response shape.
- [ ] HTTP tests reject every example in **Format enforcement** as `MALFORMED` for its field, without invoking #7.
- [ ] Testcontainers tests cover persisted success, rejection without persistence, and shared capacity behaviour.
- [ ] Reuse #7 concurrency coverage; add endpoint-level concurrency coverage only if the adapter introduces a distinct risk.

### Manual verification

- [ ] Inspect generated OpenAPI and exercise fictional success, validation, and capacity cases locally.

## Dependencies and environment

- [x] Blocked by [#7](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/7) being merged.
- [x] Mentor approves the HTTP contract in this spec before implementation starts.

## Notes

No business-duplicate restriction or separate customer entity applies to the MVP. RN-08 limits contact numbers to Spanish numbers; exact normalization remains an implementation detail. Staff should read back the booking details and obtain customer confirmation before submitting, but the backend cannot verify that conversation.
