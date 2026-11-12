# AI Agent Instructions

These instructions apply to coding assistants working in this repository.

## Project context

Restaurant Receptionist is a FemCoders Club pilot project developed in collaboration with Vonage. It demonstrates a reservation journey for a fictional restaurant: an incoming call, guided data capture, capacity validation, confirmed persistence, a notification request, and a basic staff dashboard.

Read [README.md](README.md), [CONTRIBUTING.md](CONTRIBUTING.md), [docs/mvp-specification.md](docs/mvp-specification.md), and [docs/business-rules.md](docs/business-rules.md) before proposing implementation changes.

## Language and communication

- Write repository content in English, including source code, comments, documentation, issues, pull requests, and commit messages.
- Communicate with the project mentor in Spanish unless she asks otherwise.
- Distinguish confirmed decisions from recommendations, assumptions, and open questions.

## Scope and decisions

- Use the approved stack: Java 21, Spring Boot, Maven, PostgreSQL, Spring Data JPA / Hibernate, Flyway, Next.js, Tailwind CSS, Lucide React, Docker, Docker Compose, and Testcontainers for integration tests.
- Do not scaffold an application, install dependencies, change approved tooling, or introduce an architectural decision without explicit approval.
- Work only within the approved issue scope. Do not add unrelated features or change unrelated files.
- Propose small, understandable changes and explain relevant trade-offs.
- Treat the MVP specification as a product baseline, not as confirmation of third-party API contracts.

## Collaboration workflow

- Follow the `main` and `develop` branching workflow in [CONTRIBUTING.md](CONTRIBUTING.md).
- Create or assign GitHub issues only when authorized by the project mentor and within approved scope. Escalate new product or technical decisions to the mentor.
- Do not create branches, commits, pull requests, merges, releases, or repository configuration changes without explicit authorization from the mentor.
- Before significant changes, explain what will change and why.
- When asked to review a pull request, read and follow the applicable read-only profile in [`.agents/`](.agents/README.md). These profiles guide evidence-based review; they do not replace human approval or required CI checks.

## Vonage integration

- Verify Vonage API capabilities, authentication, webhooks, payloads, error behavior, and delivery states using current official documentation.
- Never invent API endpoints, payloads, or platform behavior.
- Clearly separate documented behavior, tested behavior, and unverified hypotheses.

## Quality and security

- Prioritize tests for capacity calculation, concurrent reservations, idempotency, validation, and notification failures.
- Report tests that ran and tests that did not run. Do not claim success without verification.
- Never expose secrets, credentials, tokens, real personal data, or real `.env` files.
- Do not change CI, core dependencies, security controls, or architecture without mentor approval.

Approved project decisions take precedence over AI-generated suggestions.
