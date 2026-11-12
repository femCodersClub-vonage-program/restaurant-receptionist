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

| Token purpose | Token | Value | Use |
| --- | --- | --- | --- |
| Primary | `primary` | `#075B60` | Primary actions, active controls, and key interactive emphasis. |
| Background | `background` | `#F7F8FB` | Application background. |
| Surface | `surface` | `#FFFFFF` | Cards, panels, menus, and form surfaces. |
| Primary text | `foreground` | `#111827` | Headings and essential content. |
| Secondary text | `foreground-muted` | `#374151` | Supporting content and labels. |
| Border | `border` | `#E5E7EB` | Quiet boundaries and dividers. |
| Control border | `control-border` | `#6B7280` | Boundary of interactive controls (inputs, selects). Meets WCAG 1.4.11 3:1 non-text contrast against `surface`. |
| Selection | `selection` | `#E8F3F3` | Selected or subtle active surfaces. |
| Success text | `success` | `#14532D` | Success status text. |
| Success surface | `success-surface` | `#E7F6EC` | Success status background. |
| Error text | `error` | `#991B1B` | Error text. |
| Error surface | `error-surface` | `#FEF2F2` | Error background. |
| Warning text | `warning` | `#713F12` | Warning text. |
| Warning surface | `warning-surface` | `#FEF9E7` | Warning background. |

Tokens are defined once in `frontend/src/app/globals.css` and exposed as Tailwind CSS utilities, for example `bg-primary`, `text-foreground-muted`, and `border-border`. Always pair a state text token with its own surface, for example `text-error bg-error-surface`. Use semantic token names in code, not repeated raw hexadecimal values. Do not communicate status through colour alone.

### Typography

Use the system font stack for the MVP. A hosted custom font is not required.

- Headings establish hierarchy through size and weight.
- Labels and supporting text remain legible; do not reduce essential content to decorative microcopy.
- Use consistent weights and line heights rather than introducing one-off typography styles.

| Token | Size | Line height | Weight | Use |
| --- | --- | --- | --- | --- |
| `label` | 14 px | 20 px | 500 | Form labels, badges, and table headers. |
| `body` | 16 px | 24 px | 400 | Default body text. |
| `heading-sm` | 18 px | 24 px | 600 | Card and small section titles. |
| `heading-md` | 24 px | 32 px | 600 | Dashboard section titles. |
| `heading-lg` | 30 px | 36 px | 700 | Page title. |

Each utility, for example `text-heading-md`, applies size, line height, and weight together; do not add separate weight or line-height utilities. Values are defined in `rem` so text scales with the user's browser font size.

### Spacing

Use this fixed spacing scale:

```text
4, 8, 12, 16, 24, 32, 48 px
```

| Utility key | Value |
| --- | --- |
| `1` | 4 px |
| `2` | 8 px |
| `3` | 12 px |
| `4` | 16 px |
| `6` | 24 px |
| `8` | 32 px |
| `12` | 48 px |

The scale is closed: only these keys, plus `0`, exist for spacing and sizing utilities such as `p-4`, `gap-6`, or `h-12`. Other numeric keys, for example `p-5`, generate no CSS. Do not bypass the scale with arbitrary values such as `p-[20px]`.

### Borders and shadows

- Use `rounded-control` (8 px) for controls such as buttons, inputs, selects, and badges, and `rounded-surface` (12 px) for bounded surfaces such as cards, panels, and alerts.
- Use the `border` token for quiet visual separation; it is the default border colour, so a plain `border` utility applies it.
- Use the `control-border` token for the default boundary of interactive controls such as `Input` and `Select`, so their edge is perceivable against `surface` at the required 3:1 non-text contrast.
- Use `shadow-raised`, the only shadow token, for raised surfaces when a border alone is insufficient.
- Avoid heavy shadows, excessive rounding, gradients, and decorative visual noise.

### Focus and interaction

- Interactive elements must have a visible focus state.
- Preserve native keyboard behaviour and use native elements where possible.
- Buttons and controls need clear default, hover, focus, and disabled states.
- Loading, error, success, and unavailable states must include understandable text.

## Components

### Base components

The shared base set is implemented in `frontend/src/components/ui/` and exported from a single barrel file. Import them with the `@/components/ui` alias.

- `Button`: primary and secondary variants, plus hover, focus, disabled, and loading states.
- `Input`: native text-entry control with default, disabled, and error states.
- `Select`: native select control with default, disabled, and error states and a shared chevron.
- `Badge`: concise non-interactive status or category label with neutral, success, warning, and error variants.
- `Card`: bounded surface for related content.
- `Alert`: success, warning, and error messages.

Use [Lucide](https://lucide.dev/) through the approved `lucide-react` package as the project's single icon library. Icons are decorative only when adjacent text already conveys the same meaning; otherwise they require an accessible name.

#### Button

| Variant | Use |
| --- | --- |
| `primary` | The single prominent action in a context. |
| `secondary` | Secondary or low-emphasis actions. |

States: default, hover, visible focus, disabled, and loading. A loading button is disabled, exposes `aria-busy`, and shows a spinning Lucide icon next to its label.

Colour: the primary variant uses `bg-primary` with a white label (`text-white`) to maintain contrast on the dark primary surface. The system has no dedicated on-primary token, so white is the documented label colour for the primary action. On hover the primary background is shown at 90% opacity (`hover:bg-primary/90`) and the secondary background becomes `bg-selection`. Add a token before introducing any new button colour.

```tsx
<Button>Save</Button>
<Button variant="secondary">Cancel</Button>
<Button disabled>Unavailable</Button>
<Button loading>Saving...</Button>
```

#### Input

Native `input` styled with the shared control tokens. Set `error` to apply the error border and `aria-invalid`. Provide a visible label and, on error, an associated error message from the surrounding form.

```tsx
<label htmlFor="name">Name</label>
<Input id="name" error aria-describedby="name-error" />
<p id="name-error">Enter a valid name.</p>
```

#### Select

Native `select` with a decorative chevron. It preserves native keyboard and option behaviour.

```tsx
<label htmlFor="service">Service</label>
<Select id="service">
  <option value="lunch">Lunch</option>
  <option value="dinner">Dinner</option>
</Select>
```

#### Badge

| Variant | Use |
| --- | --- |
| `neutral` | Default, unclassified status. |
| `success` | Confirmed or completed status. |
| `warning` | Attention or pending status. |
| `error` | Failed or blocked status. |

```tsx
<Badge>Pending</Badge>
<Badge variant="success">Confirmed</Badge>
```

#### Card

A `rounded-surface` surface with the shared border and background. It does not impose padding; compose spacing inside the card.

```tsx
<Card className="p-6">...</Card>
```

#### Alert

| Variant | Role |
| --- | --- |
| `success` | `status` |
| `warning` | `status` |
| `error` | `alert` |

Each alert pairs its state text token with its own surface and includes a decorative Lucide icon. The title and body text carry the meaning; colour is not the only indicator.

```tsx
<Alert variant="error" title="Could not save">
  The requested party exceeds capacity.
</Alert>
```

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
