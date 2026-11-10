# Spec: Add OpenAPI documentation foundation

> Issue: [#18](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/18) | Branch: `feature/18-openapi-documentation`
> Status: ready
> Last updated: 2026-11-10

---

## Problem

Backend API consumers need an inspectable contract as product endpoints are introduced. The initial technical setup has no OpenAPI dependency or product endpoints.

## Proposed solution

Add compatible generated OpenAPI documentation and Swagger UI to the Spring Web MVC backend under the accepted ADR-006, with clear local access instructions and a way to verify future operations.

## Scope

### In scope

- [ ] Add the approved, pinned springdoc Web MVC UI dependency.
- [ ] Configure basic API title, version, and description for this fictional demo.
- [ ] Document local access to the generated JSON and Swagger UI.
- [ ] Add a focused startup or HTTP test for the generated document.

### Out of scope

- [ ] Create product endpoints or invent request and response contracts.
- [ ] Document Vonage's external APIs or publish production documentation.
- [ ] Add authentication, deployment configuration, or unrelated dependencies.

## Acceptance criteria

- [ ] Given the local backend is running, the generated OpenAPI document and Swagger UI can be opened at their documented paths.
- [ ] The generated document has project metadata and accurately reflects the operations that exist at implementation time.
- [ ] Future endpoint contributors have an explicit rule to document requests, responses, validation, and error outcomes with the endpoint implementation.
- [ ] Backend tests pass with the added dependency and a focused documentation smoke test passes.

## Business rules and decisions

- Business rules: Not applicable; this is API documentation infrastructure.
- Related ADRs: [ADR-006](../docs/adr/006-add-generated-openapi-documentation.md), accepted.
- Open questions or assumptions: Deployment exposure is a separate decision.

## Technical design

### Backend

Use the Spring MVC starter documented in ADR-006. Keep generated documentation aligned with implemented controllers. Do not add a placeholder product controller simply to populate Swagger UI.

### Frontend

Not applicable.

### Data and migrations

None.

### Vonage integration

Not applicable.

## Security and data handling

Use fictional examples only. No credentials, real contacts, or backend secrets in examples or API metadata.

## Accessibility and UX

Not applicable to the restaurant dashboard.

## Testing

### Automated

- [ ] Run the backend test suite and verify the generated document endpoint responds successfully.

### Manual verification

- [ ] Open JSON and Swagger UI locally and check documented paths.

## Dependencies and environment

- [ ] ADR-006 is accepted. No issue dependency on #6 or #7.

## Notes

Later endpoint issues own the documentation of their own contracts.
