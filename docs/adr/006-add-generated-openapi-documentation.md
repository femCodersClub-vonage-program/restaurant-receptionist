# ADR-006: Add generated OpenAPI documentation

## Status

Accepted on 2026-11-10 by the project mentor.

## Date

2026-11-10

## Context

The backend has Spring Web MVC but no published description of its own HTTP contracts. Upcoming dashboard and manual-reservation work needs a reliable way to inspect request and response shapes. The initial setup deliberately excluded OpenAPI tooling pending a separate decision.

## Decision

Propose `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0` for generated OpenAPI 3 documentation and Swagger UI. The project's Spring Boot version is 4.1.2. The springdoc compatibility matrix lists Spring Boot 4.1.x with springdoc 3.1.x–3.2.x, and its 3.2.0 release targets Spring Boot 4.1.2. Keep the documentation generated from approved controllers and request/response types; document errors and examples as endpoints are implemented. Provide local access instructions and verify the generated document against actual contracts.

This decision approves no product endpoint or external Vonage contract. The documentation interface is intended for the controlled demo and local development; exposure in any later deployment needs a separate security decision.

## Alternatives considered

- Maintain a handwritten OpenAPI document. It avoids a runtime dependency but can drift from controller code and requires manual synchronization.
- Use only the OpenAPI JSON starter without Swagger UI. This provides machine-readable output but less convenient inspection for participants building the frontend.
- Defer documentation until all endpoints exist. That leaves the first API consumers without a shared contract.

## Consequences

- The backend gains a third-party runtime dependency and generated documentation routes. The dependency version must be reviewed and pinned in Maven.
- springdoc is distributed under Apache-2.0 and maintained in its public repository; review its release and security status before adding or upgrading it.
- Each later endpoint issue must keep its documented responses and validation errors aligned with implemented behaviour.
- Tests should verify the documentation endpoint starts and includes implemented product operations; an empty API description must not be presented as a finished product contract.
- The approved OpenAPI foundation issue owns the dependency addition and validation.

## Related material

- [Initial technology stack](001-initial-technology-stack.md)
- [Initial setup spec](../../specs/devops-01-initial-technical-setup.spec.md)
- [OpenAPI foundation spec](../../specs/backend-02-openapi-documentation.spec.md)
- [springdoc compatibility guide](https://springdoc.org/)
- [springdoc 3.2.0 release](https://github.com/springdoc/springdoc-openapi/releases/tag/v3.2.0)
