# Contributing to Restaurant Receptionist

Thank you for contributing to Restaurant Receptionist, a FemCoders Club pilot project developed in collaboration with Vonage.

## Before you start

- Read the [README](README.md), [MVP specification](docs/mvp-specification.md), [business rules](docs/business-rules.md), and [AI agent guidance](AGENTS.md).
- Work only on an issue created and approved by the project mentor. The mentor creates and prioritizes all issues.
- If you identify a bug, question, or improvement, report it in the team's Slack channel. Do not open a new issue yourself.
- Raise blockers as soon as they appear. Technical decisions that affect the project must be recorded in the repository documentation or the relevant GitHub issue.

## From problem to code

We use Spec-Driven Development (SDD): a feature does not start with code alone.

1. The project mentor identifies and creates an issue for an approved need.
2. A spec in `specs/` defines the problem, scope, acceptance criteria, relevant business rules, technical considerations, and validation.
   Once GitHub assigns the issue number, update the spec header with the issue link and confirm the issue links back to the spec. Do not mark this preparation complete while either link is missing.
3. The mentor and team review the spec before implementation starts. Open questions must be resolved or explicitly recorded.
4. The work is implemented from a branch created from `develop` and remains within the approved issue and spec scope.
5. The pull request links the issue and the spec, explains validation, and records any follow-up work.

Use [the spec template](specs/_template.spec.md) for a new spec. Significant technical decisions are recorded as ADRs in [`docs/adr/`](docs/adr/).

## GitHub Project and issues

The GitHub Project tracks approved work. Every implementation issue should have a clear owner, scope, acceptance criteria, and expected validation.

- Only the project mentor creates and prioritizes issues.
- Link an implementation issue to its spec when a spec is required.
- Raise blockers promptly and explain the reason in the issue or its linked project item.
- Do not expand an issue's scope silently. Create or propose a follow-up issue instead.

## Language

Use English for all repository-facing content: source code, comments, documentation, issues, pull requests, and commit messages. Team conversations take place in Spanish in Slack.

## Branching workflow

- `main` is the stable branch and is managed by the project mentor.
- `develop` is the integration branch.
- Start every work branch from the latest `develop` branch.
- Open pull requests against `develop`.
- Contributors must not push directly to `main` or `develop`. The project mentor may make direct updates to either branch for small documentation or repository maintenance changes. Code changes follow the branch and pull request workflow.
- Only the mentor coordinates merges into `main`.

Use descriptive branch names linked to the assigned issue:

```text
feature/<issue-number>-short-description
fix/<issue-number>-short-description
docs/<issue-number>-short-description
test/<issue-number>-short-description
```

## Commits

Use [Conventional Commits](https://www.conventionalcommits.org/) in English:

```text
type(optional-scope): short imperative description
```

Common types include `feat`, `fix`, `docs`, `test`, `refactor`, and `chore`.

Examples:

```text
feat(reservations): validate service capacity
docs: explain the branching workflow
```

## Pull requests

- Keep each pull request focused on one approved issue.
- Update your branch with the latest `develop` changes before requesting review.
- Complete the pull request template in English.
- Link the related issue and spec.
- Explain the change, the relevant tests, and any decision the reviewer should know.
- Add or update tests when the change requires them.
- Update affected documentation.
- Address review feedback before the pull request is merged.
- Discuss significant dependency, architecture, security, or technology changes before implementing them.

The mentor reviews contributors' pull requests. After approval and completion of the required checks, the pull request author merges it into `develop`. Only the mentor coordinates merges from `develop` into `main`.

The mentor may merge her own pull requests after a documented self-review when the repository rules permit it; GitHub does not count an author's own approval as an independent approving review.

## Review profiles

The read-only profiles in [`.agents/`](.agents/README.md) make reviews consistent without replacing human judgement, required CI checks, or branch protection.

- Apply [`docs-reviewer`](.agents/docs-reviewer.md) to every pull request.
- Apply [`ui-reviewer`](.agents/ui-reviewer.md), [`accessibility-reviewer`](.agents/accessibility-reviewer.md), and [`design-system-reviewer`](.agents/design-system-reviewer.md) when a pull request changes a user-facing frontend flow, component, interaction, or visual style.
- Before opening a pull request for review, or marking a draft pull request ready for review, the author runs every applicable profile on their branch. The author resolves blocking findings first and reruns the relevant profile after the correction.
- The pull request author identifies the applicable profiles in the pull request template and records the final result in the pull request conversation. A result of `Ready for approval` may proceed to mentor review; `Requires changes` must be resolved first. When a profile reports `Not verifiable`, state what could not be checked and why.
- Resolve blocking findings before merge. Resolve important findings before merge unless the mentor records a reasoned exception. Suggestions do not block a pull request.
- A profile can report that a check was not verifiable. Do not claim a successful runtime result without evidence.

## Testing and validation

Acceptance criteria in the spec are the starting point for validation.

- Add or update unit tests for business rules and validation when applicable.
- Use PostgreSQL-backed integration tests with Testcontainers for critical persistence, migration, capacity, concurrency, and idempotency behaviour.
- Use test doubles or verified local test mechanisms for Vonage integrations; automated tests must not consume real credits by default.
- Document manual validation for user-facing flows, including expected error and empty states.
- Report checks that ran and checks that could not run. Do not claim a result without verification.

## Accessibility and UX

The project targets WCAG 2.1 AA for user-facing work.

- Interfaces must be usable with a keyboard.
- Form controls need clear labels and understandable error messages.
- Loading, empty, and error states must be explicit.
- Do not communicate information through colour alone.
- Record accessibility requirements and manual checks in the spec for interface changes.

## AI-assisted work

AI can support research, planning, implementation, and review, but it does not replace responsibility for the work.

- The spec comes first: do not ask an assistant to invent product or technical scope.
- The pull request author must understand and be able to explain every submitted change, including AI-generated content.
- Verify Vonage API claims against current official documentation before implementing them.
- Never paste credentials, private keys, tokens, or real personal data into an AI tool.
- Treat AI review findings as input for human judgement, not as an automatic approval or rejection.

## Database migrations

The project uses Flyway for versioned database migrations. Once the backend structure is initialized, all schema changes must be introduced through approved Flyway migrations.

- Name versioned SQL migrations as `V<version>__<lowercase_underscore_description>.sql`.
- Create a new migration in the same pull request as the code that requires its schema change.
- Never edit, rename, delete, or reorder a migration after it has been merged. Create a later migration instead.
- Add the `database` label to the related issue and state the proposed migration filename in both the issue and pull request.
- Before creating a migration and again before requesting review, update from `develop` and check for version collisions.
- If two unmerged branches use the same version, the branch merged later must rename its migration after updating from `develop`.
- The mentor coordinates the merge order of pull requests that contain migrations.
- Validate migrations against PostgreSQL, including through Testcontainers where applicable.

This manual coordination policy is intentionally proportionate to the current team size and six-week MVP. It will be revisited if recurring collisions show that automation is needed. See [ADR-004](docs/adr/004-adopt-flyway-migration-coordination-policy.md).

## Definition of Done

An issue is done when all of the following are true:

- [ ] Its acceptance criteria are met.
- [ ] The implementation remains within the approved issue and spec scope.
- [ ] Relevant automated and manual validation has been completed, or omitted checks are explained.
- [ ] Required tests have been added or updated.
- [ ] Affected documentation is up to date.
- [ ] No secrets or real personal data are included.
- [ ] The pull request has been reviewed and merged according to this guide.
- [ ] An ADR has been added or updated when a significant technical decision was made.

## Security and data

- Never commit API keys, passwords, tokens, credentials, or real `.env` files.
- Use environment variables and commit `.env.example` files only with placeholder values.
- Do not commit, publish, or include real personal data in fixtures, issues, logs, screenshots, or documentation.
- Controlled integration or demo testing may use a consenting team member's phone number or a Vonage test number through local configuration only. Never add those numbers to repository files, seed data, or public screenshots.
- If a secret is exposed, tell the mentor immediately so it can be revoked. Removing it from the latest commit is not sufficient.

For vulnerability reporting, follow the project's [Security Policy](SECURITY.md).

## Team conduct

Follow the project's [Code of Conduct](CODE_OF_CONDUCT.md). Keep collaboration and code reviews respectful, constructive, and inclusive.
