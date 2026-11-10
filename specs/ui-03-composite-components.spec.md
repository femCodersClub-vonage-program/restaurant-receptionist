# Spec: Create reusable composite UI components

> Issue: [#9](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/9) | Branch: `feature/9-composite-ui-components`
> Status: ready
> Last updated: 2026-11-10

---

## Problem

The dashboard needs recurring structures composed from base components: a page header, labelled form fields, reservation lists, and service-capacity indicators. These should be shared rather than rebuilt on each future screen.

## Proposed solution

Create reusable `Header`, `FormField`, `Table`, and `CapacityIndicator` components from the approved base component set and tokens.

## Scope

### In scope

- [ ] A reusable page `Header`.
- [ ] A `FormField` that supports visible label, help text, required state, and linked error message.
- [ ] An accessible `Table` pattern with headers, rows, and explicit empty state.
- [ ] A `CapacityIndicator` that communicates total, occupied, and available places with text as well as visual status.
- [ ] Responsive behaviour for the four composite components.
- [ ] `docs/design-system.md` updated with the implemented composite-component usage rules.

### Out of scope

- [ ] Reservation data fetching, form submission, sorting, pagination, authentication, or real-time updates.
- [ ] Product-specific dashboard layout beyond what is needed to demonstrate these reusable components.

## Acceptance criteria

- [ ] Given a form needs a field, when it uses `FormField`, then label, help text, required state, and error message are associated accessibly.
- [ ] Given a reservation list has no entries, when `Table` is rendered, then it communicates an explicit empty state.
- [ ] Given capacity is shown, when a user reads the `CapacityIndicator`, then total, occupied, and available values are available in text and not only through colour or a progress visual.
- [ ] Given a narrow viewport, when the composite components render, then content remains usable without clipped essential information.
- [ ] Given the frontend checks run, when lint and production build complete, then they pass.
- [ ] Given the component documentation, when it is read, then it matches the implemented composite components.

## Business rules and decisions

- Business rules: [RN-07](../docs/business-rules.md#rn-07-staff-dashboard-and-manual-reservations)
- Related ADRs: not applicable
- Open questions or assumptions: Components remain presentational and do not calculate capacity or implement form submission.

## Technical design

### Backend

Not applicable.

### Frontend

Compose these components from `ui-02` base components and approved design tokens. Keep product-specific data and feature logic outside the component primitives.

### Data and migrations

Not applicable.

### Vonage integration

Not applicable.

## Security and data handling

Use fictional content only in examples and component demonstrations.

## Accessibility and UX

- Use semantic heading, form, table, and status elements when available.
- Keep keyboard use and focus behaviour intact.
- Link error messages to the relevant field.
- Do not communicate capacity state only through colour.

## Testing

### Automated

- [ ] `npm run lint` passes.
- [ ] `npm run build` passes.

### Manual verification

- [ ] Verify labels, error messages, and empty states with keyboard navigation.
- [ ] Verify responsive table/list behaviour on a narrow viewport.

## Dependencies and environment

- [ ] Depends on `ui-02` being merged.

## Notes

These components provide the building blocks for the dashboard and later optional manual-reservation form.
