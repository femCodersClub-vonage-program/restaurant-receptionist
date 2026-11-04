# Restaurant Receptionist Design System

## Purpose

This document is the shared visual reference for Restaurant Receptionist. It defines the minimal Clean UI system used by the MVP so contributors reuse consistent tokens and components instead of introducing arbitrary styles.

The system is deliberately small. It supports the staff dashboard and future controlled demo flows; it is not a general-purpose component library.

## Design direction

Use a clean, calm, operational interface:

- Quiet light surfaces and clear hierarchy.
- One prominent primary action per context.
- Semantic states communicated with text or iconography as well as colour.
- Responsive layouts for desktop, tablet, and mobile.
- Native HTML controls and keyboard interaction whenever they are sufficient.

## Foundations

### Colour tokens

| Token purpose | Value | Use |
| --- | --- | --- |
| Primary | `#075B60` | Primary actions, active controls, and key interactive emphasis. |
| Background | `#F7F8FB` | Application background. |
| Surface | `#FFFFFF` | Cards, panels, menus, and form surfaces. |
| Primary text | `#111827` | Headings and essential content. |
| Secondary text | `#374151` | Supporting content and labels. |
| Border | `#E5E7EB` | Quiet boundaries and dividers. |
| Selection | `#E8F3F3` | Selected or subtle active surfaces. |
| Success text | `#14532D` | Success status text. |
| Success surface | `#E7F6EC` | Success status background. |
| Error text | `#991B1B` | Error text. |
| Error surface | `#FEF2F2` | Error background. |
| Warning text | `#713F12` | Warning text. |
| Warning surface | `#FEF9E7` | Warning background. |

Use semantic token names in code, not repeated raw hexadecimal values. Do not communicate status through colour alone.

### Typography

Use the system font stack for the MVP. A hosted custom font is not required.

- Headings establish hierarchy through size and weight.
- Labels and supporting text remain legible; do not reduce essential content to decorative microcopy.
- Use consistent weights and line heights rather than introducing one-off typography styles.

### Spacing

Use this fixed spacing scale:

```text
4, 8, 12, 16, 24, 32, 48 px
```

### Borders and shadows

- Use 8 px or 12 px radii.
- Use the border token for quiet visual separation.
- Use subtle shadows only for raised surfaces when a border alone is insufficient.
- Avoid heavy shadows, excessive rounding, gradients, and decorative visual noise.

### Focus and interaction

- Interactive elements must have a visible focus state.
- Preserve native keyboard behaviour and use native elements where possible.
- Buttons and controls need clear default, hover, focus, and disabled states.
- Loading, error, success, and unavailable states must include understandable text.

## Components

### Base components

The shared base set is:

- `Button`: primary, secondary, and disabled variants; loading when applicable.
- `Input`: text-entry control with standard focus and error support.
- `Select`: native or accessible select control using the shared styling.
- `Badge`: concise non-interactive status or category label.
- `Card`: surface for bounded related content.
- `Alert`: success, error, and warning messages.

### Composite components

The shared composite set is:

- `Header`: page-level heading and related action area.
- `FormField`: visible label, optional help text, required state, and linked error message.
- `Table`: semantic headers, rows, and explicit empty state.
- `CapacityIndicator`: total, occupied, and available places expressed in text as well as visually.

Use [Lucide](https://lucide.dev/) through the approved `lucide-react` package as the project's single icon library. Icons are decorative only when adjacent text already conveys the same meaning; otherwise they require an accessible name.

## Responsive behaviour

- Start with a usable desktop dashboard and ensure it reflows for tablet and mobile widths.
- Keep controls, labels, values, and actions usable without horizontal clipping.
- Tables may adapt to a list or horizontally scroll only when essential columns cannot be presented another way.
- Do not hide essential status or capacity information solely because the viewport is narrow.

## Accessibility baseline

The MVP targets WCAG 2.1 AA proportionately. Every user-facing change must check:

- Sufficient text/background contrast.
- Visible labels associated with form controls.
- Visible focus for interactive controls.
- Basic keyboard navigation.
- Clear error text associated with the relevant field.
- Native semantic HTML when it provides a simple suitable solution.

This is not a complete accessibility audit. Do not add complex ARIA or new libraries without a demonstrated need, but basic defects that prevent normal use must be corrected.

## Implementation rules

- Reuse approved tokens and components before creating a new style or component.
- Do not duplicate component styles within feature folders.
- Add a generic component only when an identified product requirement needs it.
- Keep this document aligned with implemented component variants and usage rules.
- Component demonstrations and all examples must use fictional data only.

## Review checklist

For every frontend pull request, check that:

- [ ] Existing tokens and components are reused where appropriate.
- [ ] New styles are not arbitrary duplicates of existing patterns.
- [ ] Relevant loading, empty, error, disabled, or success states are understandable.
- [ ] Basic responsive and keyboard behaviour has been verified.
- [ ] No real personal data, secrets, or credentials are present.
