# Spec: Add manual reservation creation to the dashboard

> Issue: pending creation | Branch: pending issue assignment
> Status: ready
> Last updated: 2026-11-15

---

## Problem

Staff can eventually inspect reservations in the dashboard, but RN-07 also requires them to enter a manual reservation. The form needs the shared backend creation rules and clear feedback for validation and capacity errors.

## Proposed solution

Add an accessible form to the staff dashboard that submits the approved manual-creation API contract, reports the result, and refreshes the affected reservation and capacity view after a successful save.

## Scope

### In scope

- [ ] Capture first and last name, phone number, date, arrival time, and party size with labelled controls.
- [ ] Submit to the approved manual-creation endpoint and render field validation, insufficient-capacity, network, loading, and success states.
- [ ] Prevent duplicate submission while a request is pending and refresh the selected date after confirmed success.
- [ ] Follow approved design-system components and responsive, keyboard-accessible patterns.

### Out of scope

- [ ] Browser-side source-of-truth capacity calculation, business-duplicate rules, SMS settings, Voice flow, cancellation, or modification.
- [ ] New authentication, real-time updates, or speculative form fields.

## Acceptance criteria

- [ ] Given valid fictional details, when the API confirms creation, the form reports success and the selected-date dashboard reflects the reservation and updated backend capacity.
- [ ] Given invalid input, the user receives a clear field-level or form-level message and can correct it without losing unrelated entries.
- [ ] Given insufficient capacity, the form explains the rejection and does not claim a reservation was made.
- [ ] Given a network or server failure, the form gives a retry path and does not claim success.
- [ ] All controls and feedback work with keyboard navigation and remain usable at narrow widths.

## Business rules and decisions

- Business rules: [RN-01](../docs/business-rules.md#rn-01-capacity-by-service), [RN-03](../docs/business-rules.md#rn-03-reservation-arrival-windows), [RN-07](../docs/business-rules.md#rn-07-staff-dashboard-and-manual-reservations).
- Related ADRs: [ADR-002](../docs/adr/002-establish-initial-repository-layout.md).
- Open questions: none for the form beyond the approved API contract. Any change to phone policy or manual SMS handling belongs to mentor-approved rules and follow-up work.

## Technical design

### Backend

Consume the approved [manual creation contract](backend-05-manual-reservation-api.spec.md); do not change the backend in this issue.

### Frontend

Extend the staff dashboard using the established UI components. Treat server validation and capacity results as authoritative. Keep the submitted date and the displayed selected date consistent after success.

### Data and migrations

No database change.

### Vonage integration

Not applicable.

## Security and data handling

Use fictional data only. Do not log names, phone numbers, or full API payloads in the browser. Do not persist form values in browser storage.

## Accessibility and UX

Use visible labels, required indicators, linked help/error text, visible focus, and text-based status messages. Manage focus after submission errors and success; announce asynchronous status changes appropriately. Verify WCAG 2.1 AA expectations in the controlled demo.

## Testing

### Automated

- [ ] Run `npm run lint` and `npm run build`.
- [ ] Add focused tests only if frontend test tooling has been approved by then; do not add a test dependency in this issue.

### Manual verification

- [ ] Verify keyboard and narrow-viewport entry, pending/duplicate-submit behaviour, success, validation, insufficient capacity, and network failure with fictional data.
- [ ] Verify that dashboard capacity changes only after the backend confirms persistence.

## Dependencies and environment

- [ ] Blocked by [#9](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/9), [#10](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/10), and the approved manual-creation API being merged.
- [ ] Coordinate the final placement and refresh behaviour with [#11](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/11).

## Notes

This issue adds the manual form after the dashboard shell and API exist; it does not duplicate their scope.
