# Spec: Configure Vonage MCP and CLI developer tooling

> Issue: pending creation | Branch: pending issue assignment
> Status: draft
> Last updated: 2026-11-14

---

## Problem

The team needs a repeatable, safe way to consult Vonage documentation and inspect approved test-account configuration during integration research. The proposed MCP and CLI tooling has not been evaluated for this project's development workflow.

## Proposed solution

Evaluate the official Vonage documentation MCP, the applicable API tooling MCP, and Vonage CLI for the limited research and test setup tasks. Document verified installation, configuration, permissions, useful read-only checks, and safe removal. Keep developer tooling outside the application's runtime and repository dependencies.

## Scope

### In scope

- [ ] Confirm the current official MCP offerings and CLI package, their requirements, supported clients, and relevant capabilities.
- [ ] Select which MCP service or services serve documentation lookup and approved test-account inspection; explain why each is useful for this MVP.
- [ ] Document local setup using placeholders, secure environment configuration, and the minimum permissions needed.
- [ ] Verify a documentation lookup and, if mentor-approved access exists, a non-sending account or application inspection using the selected tooling.
- [ ] Document the CLI commands actually tested, expected output with identifiers redacted, troubleshooting, and safe removal or credential rotation guidance.
- [ ] State which operations can spend credits or change account configuration and require explicit mentor approval before use.

### Out of scope

- [ ] Adding MCP or CLI packages to the backend or frontend dependencies, CI, or runtime architecture.
- [ ] Committing local MCP client files, credentials, private keys, actual account identifiers, or real numbers.
- [ ] Creating or changing a Vonage application, linking numbers, sending SMS, or placing calls as part of a default validation step.
- [ ] Treating MCP or CLI output as a substitute for official API contracts or implementing Voice/Messages integration.

## Acceptance criteria

- [ ] The guide identifies the official tools selected, current install source, prerequisites, and their narrow purpose for this project.
- [ ] A contributor can follow a placeholder-only local configuration procedure without putting credentials in the repository or browser bundle.
- [ ] At least one documentation lookup is verified; account-dependent checks are either verified safely or recorded as not run and blocked by missing access.
- [ ] Commands and results are labelled tested or untested, and operations that cost credits or mutate Vonage state are clearly identified.
- [ ] No project dependency or application architecture is changed.

## Business rules and decisions

- Business rules: not applicable to developer tooling. The resulting research supports [RN-06](../docs/business-rules.md#rn-06-confirmation-messages) and the Voice journey.
- Related ADRs: none expected; any runtime or security architecture change requires separate mentor approval.
- Open questions: which MCP client the team uses, who holds approved test-account access, and whether local account inspection is authorized.

## Technical design

### Backend

No backend change.

### Frontend

No frontend change.

### Data and migrations

No data or migration change.

### Vonage integration

Start with Vonage's official [MCP introduction](https://developer.vonage.com/en/mcp-server/conceptual-guides/introduction-to-vonage-mcp) and [CLI guide](https://developer.vonage.com/en/vonage-cli/getting-started). Verify current installation instructions when this work begins. MCP and CLI are local development aids; the later Spring Boot integration uses independently verified Voice and Messages contracts.

## Security and data handling

Use placeholders in published instructions. Never paste actual keys into an issue, PR, screenshot, command transcript, or tracked file. Inspect the chosen client's storage and permission model before entering test credentials. Do not use a production account for validation.

## Accessibility and UX

Not applicable to the restaurant UI.

## Testing

### Automated

- [ ] No application tests are required because this task changes no application code.

### Manual verification

- [ ] Verify official install source and tool version at execution time.
- [ ] Run a documentation query and record the result category without sensitive details.
- [ ] Run only mentor-approved read-only account checks, or document why they could not be run.
- [ ] Check the resulting diff for secrets and machine-specific configuration.

## Dependencies and environment

- [ ] Documentation lookup can proceed without test-account credentials.
- [ ] Account-bound inspection is blocked until the mentor provides approved access and the team chooses an MCP client.
- [ ] Coordinate with [Vonage contract research](vonage-01-contract-verification.spec.md) to avoid duplicate research notes.

## Notes

The deliverable is a concise developer guide or verified setup section, linked from this spec. No live API operation is implied by approval of this spec.
