# ADR-002: Establish the initial repository layout

## Status

Accepted on 2026-11-02.

## Context

The project needs independently runnable backend and frontend applications, shared documentation, and local-development configuration. Contributors need a predictable layout before feature work starts, but the MVP does not need a workspace manager, shared application package, or a complex multi-module build.

## Decision

Use one Git repository with the following high-level layout:

```text
backend/     Spring Boot application and Maven build
frontend/    Next.js application and frontend tooling
docs/        Product, architecture, setup, and ADR documentation
specs/       Approved implementation specifications
.github/     Issue templates, pull-request template, and CI workflows
compose.yml  Shared local PostgreSQL service configuration
```

Backend and frontend remain independently buildable and testable. Shared documentation and local-development configuration remain at the repository root. The initial setup will not introduce a Java multi-module build, JavaScript workspace tooling, a shared code package, or cross-application imports.

## Alternatives considered

- Keep all application files at the repository root. This would make ownership, commands, and configuration harder to distinguish as the project grows.
- Use separate repositories for frontend and backend. This would add coordination overhead for a small, time-boxed pilot with a shared release flow.
- Introduce a monorepo workspace manager or a shared package immediately. The project has no current shared-code requirement that justifies this additional tooling.

## Consequences

- Contributors can identify the application they are changing and run its commands independently.
- Root-level Docker Compose configuration can support both applications without making either application depend on a containerised runtime.
- Setup documentation and CI must state commands with their relevant working directory.
- If future requirements justify a shared package or workspace tooling, a new ADR must describe that change rather than introducing it silently.

## Related material

- Issue [#1](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/1)
- [Initial technical setup spec](../../specs/devops-01-initial-technical-setup.spec.md)
- [ADR-001: Initial technology stack](001-initial-technology-stack.md)
