# Spec: [Short feature title]

> Issue: #[number] | Branch: `[type]/[issue-number]-short-description`
> Status: `draft` / `ready` / `in-progress` / `done`
> Last updated: YYYY-MM-DD

---

## Problem

<!-- What user or project need does this work address? Link to the relevant business rule when applicable. -->

## Proposed solution

<!-- Describe the intended outcome at a high level. Do not invent implementation details that have not been approved. -->

## Scope

### In scope

- [ ] [Included outcome]

### Out of scope

- [ ] [Explicitly excluded outcome]

## Acceptance criteria

<!-- Use observable, testable statements. Prefer: Given [context], when [action], then [expected result]. -->

- [ ] Given ..., when ..., then ...
- [ ] Given ..., when ..., then ...

## Business rules and decisions

<!-- Link to the applicable RN-01 to RN-07 rule, ADR, or verified integration decision. -->

- Business rules: [link or not applicable]
- Related ADRs: [link or not applicable]
- Open questions or assumptions: [list or none]

## Technical design

### Backend

<!-- Spring Boot components, API contracts, validation, error handling, or background/webhook behaviour. -->

| Method | Route or event | Description |
| --- | --- | --- |
| [GET/POST/etc.] | [/path or webhook] | [Description] |

### Frontend

<!-- Next.js route, components, loading/error/empty states, and user interactions. Omit if not applicable. -->

### Data and migrations

<!-- PostgreSQL changes, constraints, indexes, and required Flyway migration. Omit if not applicable. -->

### Vonage integration

<!-- State the relevant API, verified official documentation, webhook behaviour, and failure handling. Never invent payloads or endpoints. Omit if not applicable. -->

## Security and data handling

<!-- Identify secrets, environment variables, webhook validation, and personal-data considerations. Test numbers require consent and must never be committed. -->

## Accessibility and UX

<!-- For UI work: labels, keyboard interaction, focus handling, dynamic-state announcements, loading, error, and empty states. Omit if not applicable. -->

## Testing

### Automated

<!-- List required unit and integration tests. Use Testcontainers for PostgreSQL-backed integration scenarios when applicable. -->

- [ ] [Test scenario]

### Manual verification

- [ ] [Scenario to verify]

## Dependencies and environment

<!-- Related issues, prerequisites, migrations, Docker configuration, or environment-variable names. Do not include values or secrets. -->

- [ ] [Dependency or none]

## Notes

<!-- Decisions, trade-offs, follow-up work, or links that reviewers need. -->
