# Spec: Configure Vonage MCP and CLI developer tooling

> Issue: [#27](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/27) | Branch: `docs/27-vonage-tooling-setup`
> Status: ready
> Last updated: 2026-11-23

---

## Problem

The team needs a repeatable, safe way to use Vonage developer tooling during integration research and testing. The Tooling MCP server and CLI have not yet been installed and verified for this project's development workflow.

## Proposed solution

Install and configure the Vonage Server API Bindings (Tooling) MCP server and Vonage CLI in the assignee's local development environment using her own Vonage developer account. Verify both with safe read-only operations, then document the actual steps so other participants can reproduce the setup with their own accounts. The Documentation MCP server may be added if useful for the contract-research issue.

## Scope

### In scope

- [x] Verify current official installation instructions and prerequisites for the Tooling MCP server and CLI, then install both locally.
- [x] Configure both tools with the assignee's own Vonage developer account and verify an authenticated, read-only operation with each tool.
- [x] Document the MCP client setup and CLI configuration, including how local credentials are supplied, where each tool stores configuration, safe removal, account-specific setup, and troubleshooting in the [Vonage developer tooling guide](../docs/guides/vonage-developer-tooling.md).
- [x] Provide reproducible, placeholder-only setup steps for other participants using their own Vonage accounts; mark commands as tested or untested in the [Vonage developer tooling guide](../docs/guides/vonage-developer-tooling.md).
- [ ] Optionally install the Documentation MCP server and verify a documentation lookup if it helps the separate contract research.
- [x] State which operations can spend credits or change account configuration and confirm they were not used for this issue in the [Vonage developer tooling guide](../docs/guides/vonage-developer-tooling.md).

### Out of scope

- Adding MCP or CLI packages to the backend or frontend dependencies, CI, or runtime architecture.
- Committing local MCP client files, credentials, private keys, actual account identifiers, or real numbers.
- Creating or changing a Vonage application, linking numbers, sending SMS, or placing calls.
- Treating MCP or CLI output as a substitute for official API contracts or implementing Voice/Messages integration.

## Acceptance criteria

- [x] The Tooling MCP server and CLI are installed locally, their versions or package sources are recorded, and an authenticated read-only operation succeeds with each.
- [x] A contributor can follow the tested, placeholder-only setup for both tools with her own account without putting credentials in the repository or browser bundle.
- [x] The guide states how credentials reach each process and where configuration is stored; it does not assume either tool automatically reads the project's `.env` file.
- [x] Commands and results are labelled tested or untested, and operations that cost credits or mutate Vonage state are clearly identified.
- [x] No project dependency or application architecture is changed.

## Business rules and decisions

- Business rules: not applicable to developer tooling. The resulting research supports [RN-06](../docs/business-rules.md#rn-06-confirmation-messages) and the Voice journey.
- Related ADRs: none expected; any runtime or security architecture change requires separate mentor approval.
- The guide documents the GitHub Copilot CLI MCP configuration format. Participants using VS Code or another MCP client must follow that client's supported configuration format. Each participant uses her own Vonage developer account; no shared project account is required for this issue.

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

- [x] No application tests are required because this task changes no application code.

### Manual verification

- [x] Verify both installations and run one authenticated read-only operation with each using the assignee's account.
- [x] Follow the written steps from a separate local setup or ask another participant to verify them with her own account. The reviewer verified the CLI and the Codex Desktop MCP path, and a third participant verified Claude Code on Windows with her own account; VS Code, Codex CLI, and macOS remain unverified.
- [x] Check the resulting diff for secrets and machine-specific configuration.

## Dependencies and environment

- [x] The assignee needs her own Vonage developer account and a compatible MCP client. The mentor has approved read-only setup verification using participants' own accounts.
- [x] Creating or modifying applications, changing or linking numbers, sending messages, and placing calls are outside the scope of this issue.
- [x] Coordinate with [Vonage contract research](vonage-01-contract-verification.spec.md) to avoid duplicate research notes.

## Notes

The deliverable is a working local installation of both tools plus a concise guide linked from this spec. No account mutation or credit-consuming operation is implied by approval of this spec.

Second-person verification (2026-11-22): the reviewer ran the CLI (`vonage auth set`, `vonage balance`) and the Codex desktop MCP `balance` call on Windows with her own account, so the manual verification above counts as done for that path. A third participant verified Claude Code (`2.1.330`, native Windows, Node 22.23.5) with the pinned packages, `cmd /c`, and the deny rules. The single-package MCP command was verified in Copilot CLI and OpenCode on Windows; the pinned two-package form is now the documented default. The contributor criterion stays open, and VS Code, Codex CLI, and macOS remain unverified.
