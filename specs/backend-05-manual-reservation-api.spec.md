# Spec: Expose manual confirmed reservation creation

> Issue: pending creation | Branch: pending issue assignment
> Status: ready
> Last updated: 2026-11-15

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
| POST | Pending mentor approval | Create a confirmed manual reservation. |

Document the required fields, response examples, validation format, and capacity-conflict status here when approved. Do not expose persistence entities directly.

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
