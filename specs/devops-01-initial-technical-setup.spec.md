# Spec: Initial technical setup

> Issue: [#1](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/1) (`devops-01`) | Branch: `chore/devops-01-initial-technical-setup`
> Status: `in-progress`
> Last updated: 2026-11-02

---

## Problem

The project has approved its product baseline and technology stack, but it does not yet contain runnable backend or frontend applications, a local PostgreSQL environment, or verified setup instructions. Contributors need a reproducible, secure starting point before they can implement reservation features.

## Proposed solution

Create a minimal, runnable technical foundation in a single repository. It will provide independently runnable Spring Boot and Next.js applications, a local PostgreSQL service through Docker Compose, safe environment-variable examples, and verified developer commands. It will deliberately contain no reservation business functionality, Vonage integration, real data, or invented API contracts.

## Scope

### In scope

- [ ] Establish the repository structure with `backend/`, `frontend/`, and shared local-development configuration at the repository root.
- [ ] Scaffold a Maven and Java 21 Spring Boot backend with only the agreed foundational dependencies.
- [ ] Configure the backend to obtain its PostgreSQL connection settings from environment variables.
- [ ] Add Flyway configuration and document the migration naming and coordination policy before any domain migration is introduced.
- [ ] Add Testcontainers support and a minimal PostgreSQL-backed integration test that proves the backend test setup works.
- [ ] Scaffold a TypeScript Next.js dashboard with Tailwind CSS, ESLint, App Router, a `src/` directory, and the `@/*` import alias.
- [ ] Add a root Docker Compose configuration that starts a local PostgreSQL service without credentials committed to the repository.
- [ ] Add `.env.example` files containing variable names and safe placeholder values only.
- [ ] Add or update `.gitignore` files so local environment files, build outputs, editor files, and secrets are not tracked.
- [ ] Create `docs/setup.md` with prerequisites and commands that have been executed successfully.
- [ ] Add a minimal CI workflow that builds and tests the backend and checks the frontend's linting and build commands, using the real project commands.
- [ ] Record the approved repository layout and initial setup decisions in ADRs.

### Out of scope

- [ ] Reservation entities, business rules, REST endpoints, dashboard screens, seed data, or production-like fixtures.
- [ ] Vonage SDKs, credentials, webhooks, voice flows, SMS sending, CORS policy, or a frontend API client.
- [ ] A domain Flyway migration before the data model has an approved spec and ADR.
- [ ] Authentication, authorisation, production deployment, Railway configuration, observability, or monitoring.
- [ ] OpenAPI/Swagger tooling until the team has confirmed that addition to the stack.
- [ ] A component library, global design system, or visual implementation beyond the default generated frontend page.

## Acceptance criteria

- [ ] Given a contributor follows `docs/setup.md` with the stated prerequisites, when they start the local database and backend, then the backend starts successfully with PostgreSQL settings supplied outside version control.
- [ ] Given a contributor follows `docs/setup.md`, when they start the frontend, then the Next.js application is available locally and its production build succeeds.
- [ ] Given the backend test command runs, when its PostgreSQL-backed integration test executes, then it uses Testcontainers rather than a locally installed database.
- [ ] Given Flyway is configured, when the backend starts against an empty local database, then the migration mechanism completes successfully and its policy is documented before a domain migration exists.
- [ ] Given repository files are inspected, when environment examples and ignore rules are reviewed, then no secret, real phone number, real personal data, or real environment file is present.
- [ ] Given CI runs on the pull request, when the configured commands execute, then backend build/tests and frontend lint/build complete successfully.
- [ ] Given a new contributor reads the setup guide and ADRs, when they need to understand the initial foundation, then they can identify the repository layout, local services, commands, and deliberately deferred decisions.

## Business rules and decisions

- Business rules: Not applicable. This technical setup must not implement or reinterpret RN-01 to RN-07.
- Related ADRs: [ADR-001: Initial technology stack](../docs/adr/001-initial-technology-stack.md), [ADR-002: Initial repository layout](../docs/adr/002-establish-initial-repository-layout.md), [ADR-003: Local database and integration-test environments](../docs/adr/003-separate-local-database-and-integration-test-environments.md), [ADR-004: Flyway migration coordination policy](../docs/adr/004-adopt-flyway-migration-coordination-policy.md), and [ADR-005: Initial runtime and tooling versions](../docs/adr/005-pin-initial-runtime-and-tooling-versions.md).
- Open questions or assumptions:
  - The team has not yet approved OpenAPI/Swagger tooling; it is excluded from this issue.
  - Deployment and Railway remain undecided and require a separate ADR if adopted.

## Technical design

### Backend

- Create a Maven and Java 21 Spring Boot application in `backend/`.
- Add only the foundational capabilities needed for the approved stack: web, validation, Spring Data JPA / Hibernate, PostgreSQL driver, Flyway, test support, and Testcontainers modules required for PostgreSQL-backed tests.
- Keep the package structure minimal and ready for feature-based organisation; do not create speculative reservation, notification, Vonage, or controller packages.
- Provide a non-business health or application-context verification only if it is necessary to prove the setup. It must not be presented as a product endpoint.

| Method | Route or event | Description |
| --- | --- | --- |
| Not applicable | Not applicable | No product API contract is created in this setup. |

### Frontend

- Create a TypeScript Next.js application in `frontend/` using App Router, Tailwind CSS, ESLint, a `src/` directory, and the `@/*` import alias.
- Retain only the minimal generated application needed to demonstrate that the app runs and builds.
- Do not add dashboard routes, API calls, a component library, or project-specific visual design.

### Data and migrations

- Use Docker Compose at the repository root to supply a local PostgreSQL service.
- Read database host, port, name, username, and password from local environment configuration; commit only placeholder examples.
- Configure Flyway and establish the naming/immutability/coordination policy in `CONTRIBUTING.md` before the first domain migration.
- Do not create a domain schema or seed data in this issue because the data model remains undecided.

### Vonage integration

Not applicable. The first Vonage-related dependency or configuration needs its own approved spec after official API contracts have been verified.

## Security and data handling

- Do not commit `.env` files, credentials, tokens, private keys, database passwords used outside local placeholders, phone numbers, or real personal data.
- `.env.example` files state variable names and non-sensitive example values only.
- The frontend must not receive backend, database, or future Vonage secrets.
- Use the project's [Security Policy](../SECURITY.md) if an accidental secret exposure is discovered.

## Accessibility and UX

- The initial frontend must preserve the semantic, keyboard-usable defaults generated by Next.js.
- No product interface is in scope. Feature specs must define the accessibility requirements for their user-facing work.

## Testing

### Automated

- [ ] Backend Maven build and unit-test command complete successfully.
- [ ] A PostgreSQL-backed backend integration test runs through Testcontainers.
- [ ] Frontend lint command completes successfully.
- [ ] Frontend production build completes successfully.
- [ ] CI executes the same commands used in the setup guide.

### Manual verification

- [ ] Start PostgreSQL with Docker Compose from a clean clone using only documented environment configuration.
- [ ] Start the backend and confirm it connects to the local PostgreSQL service.
- [ ] Start the frontend and load it in a browser.
- [ ] Follow `docs/setup.md` from a clean local clone and correct any command that is not reproducible.

## Dependencies and environment

- [ ] Docker Desktop or a compatible Docker Compose environment.
- [ ] Java 21 and Maven-compatible development environment.
- [ ] Node.js 22 LTS and npm 10.
- [ ] GitHub Actions availability for the repository's initial CI workflow.
- [ ] ADRs for repository layout and any confirmed local-development decisions.

## Notes

- This is mentor-owned initial setup work. Participants begin feature work only after the project can be cloned and run with the documented commands.
- The implementation issue is [#1](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/1); it must be added to the GitHub Project.
- The pull request must report the exact versions selected, commands run, CI result, and any setup not verified on Windows, macOS, or Linux.
