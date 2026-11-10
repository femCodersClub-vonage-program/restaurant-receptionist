# Spec: Define design system foundations

> Issue: [#5](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/5) | Branch: `feature/5-design-system-foundations`
> Status: done
> Last updated: 2026-11-10

---

## Problem

The frontend needs shared visual decisions before dashboard screens and components are implemented. Without central tokens, each feature risks introducing arbitrary, duplicated styles and inconsistent accessibility behaviour.

## Proposed solution

Implement the approved Clean UI design-system foundation documented in `docs/design-system.md`. Define the semantic visual tokens and global styles in the frontend; do not build product screens in this issue.

## Scope

### In scope

- [ ] Semantic colour tokens for primary, background, surface, text, border, selection, success, error, and warning states.
- [ ] System-font typography scale, fixed spacing scale, radii, borders, subtle shadows, and visible focus treatment.
- [ ] Tokens implemented centrally in the existing CSS and Tailwind CSS setup.
- [ ] Responsive and accessibility foundations for later components.

### Out of scope

- [ ] Product screens, dashboard data, forms, backend calls, or business logic.
- [ ] A large component library, complex animations, custom branding assets, or a separate design tool.
- [ ] New frontend dependencies.

## Acceptance criteria

- [ ] Given a frontend developer needs a standard visual value, when they consult the tokens, then primary, surface, text, border, and semantic state values are available centrally.
- [ ] Given an implementer uses the approved design-system document, when tokens are implemented, then spacing uses the approved 4, 8, 12, 16, 24, 32, and 48 px scale.
- [ ] Given an interactive component is later built with the foundation, when it receives keyboard focus, then a visible focus treatment is available without inventing a new style.
- [ ] Given the implemented tokens, when a feature uses them, then it does not need to repeat raw design values in feature-specific styles.
- [ ] Given the frontend checks run, when lint and production build complete, then they pass.

## Business rules and decisions

- Business rules: not applicable
- Related ADRs: [ADR-001](../docs/adr/001-initial-technology-stack.md)
- Approved design decisions:
  - Clean UI is the visual direction.
  - The primary colour is petrol blue `#075B60`.
  - Background `#F7F8FB`, surface `#FFFFFF`, primary text `#111827`, secondary text `#374151`, border `#E5E7EB`, and selection `#E8F3F3` form the initial neutral palette.
  - Success uses text `#14532D` on `#E7F6EC`; error uses text `#991B1B` on `#FEF2F2`; warning uses text `#713F12` on `#FEF9E7`.
  - Use the system font stack initially; a hosted custom font is not required for the MVP.
  - Use 8 or 12 px radii and subtle shadows only.

## Technical design

### Backend

Not applicable.

### Frontend

Keep tokens semantic rather than component-specific. For example, use names that express `primary`, `surface`, or `error` instead of duplicating raw colour values in feature components. The foundation must work with the project's existing Next.js and Tailwind CSS 4 setup.

### Data and migrations

Not applicable.

### Vonage integration

Not applicable.

## Security and data handling

Do not add API URLs, environment values, credentials, or personal data to styles, examples, or documentation.

## Accessibility and UX

- Meet the project's proportionate WCAG 2.1 AA objective for contrast, focus visibility, keyboard use, and understandable text states.
- Do not communicate state through colour alone.
- Keep the system responsive for desktop, tablet, and mobile use.

## Testing

### Automated

- [ ] `npm run lint` passes.
- [ ] `npm run build` passes.

### Manual verification

- [ ] Check focus visibility for an interactive sample control.
- [ ] Check text and state contrast against the documented backgrounds.
- [ ] Check the documented spacing and typography remain readable on a narrow viewport.

## Dependencies and environment

- [ ] Depends only on the completed frontend technical setup.

## Notes

This issue implements the approved design direction in `docs/design-system.md`, which becomes the visual reference for all subsequent frontend work.
