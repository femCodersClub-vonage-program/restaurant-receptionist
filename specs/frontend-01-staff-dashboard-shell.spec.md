# Spec: Create the staff dashboard shell

> Issue: [#10](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/10) | Branch: `feature/10-staff-dashboard-shell`
> Status: ready
> Last updated: 2026-11-10

---

## Problem

The frontend currently contains the default Next.js page. The team needs an accessible, purposeful starting point for the staff dashboard without pretending that reservation data or backend endpoints already exist.

## Proposed solution

Replace the generated landing page with the initial staff-dashboard route and layout. Build the shell from the approved design-system tokens and reusable components. The first version communicates the future dashboard structure and its loading, empty, and error states; it does not call the backend or use mock reservation data.

## Scope

### In scope

- [ ] A staff dashboard route at `/dashboard` and a clear entry route from `/`.
- [ ] Page structure for selected date, lunch and dinner service summaries, and reservation lists.
- [ ] Explicit loading, empty, and error states as reusable presentational components or patterns.
- [ ] Semantic HTML, labelled controls, keyboard-accessible navigation, and responsive layout.
- [ ] Removal of the generated Next.js starter content.

### Out of scope

- [ ] Fetching data, submitting forms, mock reservations, or a frontend API client.
- [ ] Authentication, real-time updates, filtering beyond the selected-date control, or final visual branding.
- [ ] Design-system tokens or generic components already covered by `ui-01`, `ui-02`, and `ui-03`.
- [ ] Implementing manual reservation creation behaviour.

## Acceptance criteria

- [ ] Given a user opens `/`, when the page renders, then they can reach the staff dashboard through a clear action.
- [ ] Given a user opens `/dashboard`, when the page renders, then it identifies the page as the staff reservation dashboard and presents the planned lunch and dinner sections.
- [ ] Given the dashboard has no connected data source, when its empty state is shown, then it does not claim that reservations exist or show fabricated guest data.
- [ ] Given a keyboard-only user, when they navigate the dashboard, then all interactive elements are reachable and have visible focus.
- [ ] Given the frontend checks run, when lint and production build complete, then they pass.

## Business rules and decisions

- Business rules: [RN-07](../docs/business-rules.md#rn-07-staff-dashboard-and-manual-reservations)
- Related ADRs: [ADR-001](../docs/adr/001-initial-technology-stack.md), [ADR-002](../docs/adr/002-establish-initial-repository-layout.md)
- Open questions or assumptions:
  - The dashboard is for a controlled fictional demo and does not implement login.
  - The final data contract is defined by later backend work.
  - The initial wording remains in English because repository-facing product content is written in English.

## Technical design

### Backend

Not applicable. No backend request is introduced by this work.

### Frontend

Use the existing Next.js App Router, Tailwind CSS setup, and approved UI components. The dashboard must separate presentational sections so later work can supply real reservations and calculated capacity without replacing the page structure.

The initial page includes:

- a page heading and concise purpose;
- a selected-date control with a label;
- one section per service with placeholders for total, occupied, and available capacity;
- a reservation-list area with an explicit empty state;
- an error-state pattern that later data-fetching work can use.

### Data and migrations

Not applicable.

### Vonage integration

Not applicable.

## Security and data handling

Do not hard-code real names, phone numbers, API URLs, credentials, or environment values. This work must not add client-side access to Vonage credentials.

## Accessibility and UX

- Use native semantic landmarks and headings.
- Associate labels with controls.
- Ensure keyboard reachability and visible focus.
- Do not use colour as the only means of communicating service or state.
- Make loading, empty, error, and unavailable states understandable in text.

## Testing

### Automated

- [ ] `npm run lint` passes.
- [ ] `npm run build` passes.

### Manual verification

- [ ] Run `npm run dev` and verify `/` and `/dashboard` in a browser.
- [ ] Navigate the page and controls with a keyboard.
- [ ] Check a narrow viewport and a desktop viewport for usable layout.

## Dependencies and environment

- [ ] Depends on `ui-01`, `ui-02`, and `ui-03` being merged.
- [ ] Does not depend on an API endpoint or database migration.

## Notes

This issue creates the dashboard structure, not a simulated product flow. It follows the design-system foundation rather than introducing feature-specific visual styles.
