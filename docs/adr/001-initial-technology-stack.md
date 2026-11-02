# ADR-001: Adopt the initial technology stack and development tooling

## Status

Accepted on 2026-10-30.

## Context

The MVP needs a backend for reservation rules and Vonage integrations, a staff dashboard, a relational database, a reproducible local environment, and integration tests for critical persistence behaviour.

## Decision

The project will use:

- Java 21 and Spring Boot for the backend.
- Maven for backend builds and dependency management.
- PostgreSQL for persistence.
- Spring Data JPA / Hibernate for data access.
- Flyway for versioned database migrations.
- Next.js and Tailwind CSS for the staff dashboard.
- Docker and Docker Compose for the local environment.
- Testcontainers for PostgreSQL-backed integration tests.
- Vonage Voice API and Vonage Messages API for the agreed communication flows.

## Alternatives considered

- Other Java build tools and migration tools were considered before the team selected Maven and Flyway.
- Exact library versions, deployment tooling, CI, and the detailed repository structure are not decided by this ADR.

## Consequences

- Backend code, tests, documentation, and future CI will use Maven consistently.
- Database changes will be versioned through Flyway migrations.
- Critical integration tests can exercise PostgreSQL in containers rather than depend only on in-memory substitutes.
- Application scaffolding and dependencies still require explicit approval and an approved issue before they are introduced.
