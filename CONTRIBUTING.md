# Contributing to Restaurant Receptionist

Thank you for contributing to Restaurant Receptionist, a FemCoders Club pilot project developed in collaboration with Vonage.

## Before you start

- Read the [README](README.md), [MVP specification](docs/mvp-specification.md), and [AI agent guidance](AGENTS.md).
- Work only on an issue created and approved by the project mentor. The mentor creates and prioritizes all issues.
- If you identify a bug, question, or improvement, report it in the team's Slack channel. Do not open a new issue yourself.
- Raise blockers as soon as they appear. Technical decisions that affect the project must be recorded in the repository documentation or the relevant GitHub issue.

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
- Complete the pull request template in English.
- Explain the change, the relevant tests, and any decision the reviewer should know.
- Add or update tests when the change requires them.
- Update affected documentation.
- Address review feedback before the pull request is merged.
- Discuss significant dependency, architecture, security, or technology changes before implementing them.

The mentor reviews contributors' pull requests and coordinates their merge. The mentor may merge her own pull requests after a documented self-review when the repository rules permit it; GitHub does not count an author's own approval as an independent approving review.

## Security and data

- Never commit API keys, passwords, tokens, credentials, or real `.env` files.
- Use environment variables and commit `.env.example` files only with placeholder values.
- Do not use real personal data in demos, fixtures, issues, logs, screenshots, or documentation.
- If a secret is exposed, tell the mentor immediately so it can be revoked. Removing it from the latest commit is not sufficient.

## Team conduct

Follow the project's [Code of Conduct](CODE_OF_CONDUCT.md). Keep collaboration and code reviews respectful, constructive, and inclusive.
