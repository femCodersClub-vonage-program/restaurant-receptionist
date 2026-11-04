# Spec: Create reusable base UI components

> Issue: pending creation | Branch: `feature/<issue-number>-base-ui-components`
> Status: ready
> Last updated: 2026-11-04

---

## Problem

After visual tokens exist, dashboard features need a small common component set. Rebuilding buttons, inputs, status indicators, and panels per screen would duplicate style and accessibility work.

## Proposed solution

Create typed, reusable base components that use only the approved design tokens: `Button`, `Input`, `Select`, `Badge`, `Card`, and `Alert`. Add `lucide-react` as the project's single approved icon library.

## Scope

### In scope

- [ ] Typed `Button`, `Input`, `Select`, `Badge`, `Card`, and `Alert` components.
- [ ] Applicable default, hover, focus, disabled, loading, error, and success states.
- [ ] `lucide-react` installed and used as the sole icon library.
- [ ] A small component demonstration route or view for manual verification.
- [ ] `docs/design-system.md` updated with the implemented component variants and usage rules.

### Out of scope

- [ ] Dashboard screens, backend integration, product-specific forms, or custom SVG icon sets.
- [ ] A third-party component library or complex animation system.

## Acceptance criteria

- [ ] Given a feature needs a button, input, select, badge, card, or alert, when it uses the component, then it does not need to recreate the approved base styles.
- [ ] Given an applicable component state, when it is displayed, then its state is understandable through text or iconography as well as colour.
- [ ] Given a keyboard user, when they operate interactive base components, then focus is visible and native keyboard behaviour is preserved.
- [ ] Given an icon is required, when it is used, then it comes from `lucide-react` and is labelled or decorative as appropriate.
- [ ] Given the component demonstration view, when it is checked manually, then every base component and applicable state is visible.
- [ ] Given the component documentation, when it is read, then it matches the implemented base-component API and variants.

## Business rules and decisions

- Business rules: not applicable
- Related ADRs: [ADR-001](../docs/adr/001-initial-technology-stack.md)
- Open questions or assumptions: The final restaurant name and logo remain independent of the reusable component API.

## Technical design

### Backend

Not applicable.

### Frontend

Components must be TypeScript typed, use the semantic tokens from `ui-01`, and remain separate from product feature data. The author may choose the exact component-folder arrangement provided it is consistent, documented, and does not duplicate styles.

### Data and migrations

Not applicable.

### Vonage integration

Not applicable.

## Security and data handling

Component examples must not include real personal data or credentials.

## Accessibility and UX

- Use native controls where a native control is sufficient.
- Give form controls visible labels through their later `FormField` wrapper or an equivalent accessible association.
- Preserve visible focus and keyboard interaction.
- Use icons decoratively only when adjacent text already conveys the action; otherwise provide an accessible name.

## Testing

### Automated

- [ ] `npm run lint` passes.
- [ ] `npm run build` passes.

### Manual verification

- [ ] Verify each component's visible focus and disabled state with keyboard navigation.
- [ ] Verify button loading, alert, error, and success states are understandable without relying only on colour.

## Dependencies and environment

- [ ] Depends on `ui-01` being merged.
- [ ] Adds the approved `lucide-react` dependency through npm and commits the lockfile update.

## Notes

The exact components intentionally stay small. New generic components require an identified product need rather than speculative expansion.
