# Spec: Configure Vonage MCP and CLI developer tooling

> Issue: [#27](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/27) | Branch: pending assignment
> Status: ready
> Last updated: 2026-11-15

---

## Problem

The team needs a repeatable, safe way to use Vonage developer tooling during integration research and testing. The Tooling MCP server and CLI have not yet been installed and verified for this project's development workflow.

## Proposed solution

Install and configure the Vonage Server API Bindings (Tooling) MCP server and Vonage CLI in the assignee's local development environment using her own Vonage developer account. Verify both with safe read-only operations, then document the actual steps so other participants can reproduce the setup with their own accounts. The Documentation MCP server may be added if useful for the contract-research issue.

## Scope

### In scope

- [ ] Verify current official installation instructions and prerequisites for the Tooling MCP server and CLI, then install both locally.
- [ ] Configure both tools with the assignee's own Vonage developer account and verify an authenticated, read-only operation with each tool.
- [ ] Document the actual MCP client setup and CLI configuration, including how local credentials are supplied, where each tool stores configuration, safe removal, and troubleshooting.
- [ ] Provide reproducible, placeholder-only setup steps for other participants using their own Vonage accounts; mark commands as tested or untested.
- [ ] Optionally install the Documentation MCP server and verify a documentation lookup if it helps the separate contract research.
- [ ] State which operations can spend credits or change account configuration and require explicit mentor approval before use.

### Out of scope

- [ ] Adding MCP or CLI packages to the backend or frontend dependencies, CI, or runtime architecture.
- [ ] Committing local MCP client files, credentials, private keys, actual account identifiers, or real numbers.
- [ ] Creating or changing a Vonage application, linking numbers, sending SMS, or placing calls.
- [ ] Treating MCP or CLI output as a substitute for official API contracts or implementing Voice/Messages integration.

## Acceptance criteria

- [ ] The Tooling MCP server and CLI are installed locally, their versions or package sources are recorded, and an authenticated read-only operation succeeds with each.
- [ ] A contributor can follow the tested, placeholder-only setup for both tools with her own account without putting credentials in the repository or browser bundle.
- [ ] The guide states how credentials reach each process and where configuration is stored; it does not assume either tool automatically reads the project's `.env` file.
- [ ] Commands and results are labelled tested or untested, and operations that cost credits or mutate Vonage state are clearly identified.
- [ ] No project dependency or application architecture is changed.

## Business rules and decisions

- Business rules: not applicable to developer tooling. The resulting research supports [RN-06](../docs/business-rules.md#rn-06-confirmation-messages) and the Voice journey.
- Related ADRs: none expected; any runtime or security architecture change requires separate mentor approval.
- Open questions: the assignee selects and records a compatible local MCP client. Each participant uses her own Vonage developer account; no shared project account is required for this issue.

## Technical design

### Backend

No backend change.

### Frontend

No frontend change.

### Data and migrations

No data or migration change.

### Vonage integration

Start with Vonage's official [Tooling MCP installation guide](https://developer.vonage.com/en/mcp-server/how-to-guides/install-tooling-mcp-server) and [CLI guide](https://developer.vonage.com/en/vonage-cli/getting-started). Verify their current instructions when work begins. Both tools are local development aids; later Spring Boot integration uses independently verified Voice and Messages contracts.

## Security and data handling

Use each participant's own Vonage developer account and placeholders in published instructions. A local ignored `.env` may hold values if the documented setup explicitly loads them, but neither tool is assumed to read it automatically. The CLI may store credentials in its own local or global configuration; verify the chosen location and avoid creating a credential-bearing file in the repository. Never paste actual keys into an issue, PR, screenshot, command transcript, or tracked file.

## Accessibility and UX

Not applicable to the restaurant UI.

## Testing

### Automated

- [ ] No application tests are required because this task changes no application code.

### Manual verification

- [ ] Verify both installations and run one authenticated read-only operation with each using the assignee's account.
- [ ] Follow the written steps from a separate local setup or ask another participant to verify them with her own account, recording anything not checked.
- [ ] Check the resulting diff for secrets and machine-specific configuration.

## Dependencies and environment

- [ ] The assignee needs her own Vonage developer account and a compatible MCP client. The mentor has approved read-only setup verification using participants' own accounts.
- [ ] Creation or modification of applications, number linking, calls, and messages require separate approved work.
- [ ] Coordinate with [Vonage contract research](vonage-01-contract-verification.spec.md) to avoid duplicate research notes.

## Notes

The deliverable is a working local installation of both tools plus a concise guide linked from this spec. No account mutation or credit-consuming operation is implied by approval of this spec.
