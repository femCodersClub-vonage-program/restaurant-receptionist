# AI Agent Instructions

These instructions apply to coding assistants working in this repository.

## Project context

Restaurant Receptionist is an educational FemCoders Club pilot project developed in collaboration with Vonage. It demonstrates a reservation journey for a fictional restaurant: an incoming call, guided data capture, capacity validation, confirmed persistence, a notification request, and a basic staff dashboard.

Read [README.md](README.md), [CONTRIBUTING.md](CONTRIBUTING.md), and [docs/mvp-specification.md](docs/mvp-specification.md) before proposing implementation changes.

## Language and communication

- Write repository content in English, including source code, comments, documentation, issues, pull requests, and commit messages.
- Communicate with the project mentor in Spanish unless she asks otherwise.
- Distinguish confirmed decisions from recommendations, assumptions, and open questions.

## Scope and decisions

- Do not assume that the technology stack is final.
- Do not scaffold an application, install dependencies, or introduce an architectural decision without explicit approval.
- Work only within the approved issue scope. Do not add unrelated features or change unrelated files.
- Propose small, understandable changes and explain relevant trade-offs.
- Treat the MVP specification as a product baseline, not as confirmation of third-party API contracts.

## Collaboration workflow

- Follow the `main` and `develop` branching workflow in [CONTRIBUTING.md](CONTRIBUTING.md).
- Only the project mentor creates GitHub issues.
- Do not create branches, commits, pull requests, merges, releases, or repository configuration changes without explicit authorization from the mentor.
- Before significant changes, explain what will change and why.

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
