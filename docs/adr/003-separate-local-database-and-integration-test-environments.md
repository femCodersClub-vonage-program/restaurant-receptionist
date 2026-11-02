# ADR-003: Separate local database and integration-test environments

## Status

Accepted on 2026-11-02.

## Context

The MVP requires PostgreSQL for development and reliable PostgreSQL-backed integration tests. Contributors use different operating systems and must not need a manually installed database. At the same time, tests must be isolated, reproducible, and must not alter a contributor's local database.

## Decision

Use Docker Compose to provide a local PostgreSQL service for development. The initial `compose.yml` will run the database only; backend and frontend applications run directly through their documented development commands.

Use Testcontainers with PostgreSQL for backend integration tests. Each test run receives an isolated database container and must not rely on a locally running Docker Compose database.

Database settings are supplied through local environment variables. Committed environment examples contain variable names and safe placeholders only.

## Alternatives considered

- Require each contributor to install and configure PostgreSQL directly. This creates operating-system differences and onboarding work.
- Use an in-memory database for all backend tests. It would not validate PostgreSQL behaviour or Flyway migrations sufficiently for persistence-critical rules.
- Run all development commands inside Docker from the first iteration. This would add container-build and hot-reload complexity before it has demonstrated value for the six-week MVP.

## Consequences

- Docker is a prerequisite for local database use and PostgreSQL-backed integration tests.
- `docs/setup.md` must distinguish local-development database commands from automated test commands.
- CI must provide Docker-compatible execution for the Testcontainers integration tests.
- No real data, credentials, or personal phone numbers may be loaded into either environment.

## Related material

- Issue [#1](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/1)
- [Initial technical setup spec](../../specs/devops-01-initial-technical-setup.spec.md)
- [ADR-001: Initial technology stack](001-initial-technology-stack.md)
- [Security Policy](../../SECURITY.md)
