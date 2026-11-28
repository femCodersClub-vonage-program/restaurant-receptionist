# Team guides

This directory contains short operational guides for recurring team tasks. Guides explain **how** to follow an approved process; architecture decisions and their rationale belong in [ADRs](../adr/README.md).

All repository content, including these guides, is written in English. Team conversation and mentoring take place in Spanish in Slack.

## Available guides

| Guide | Purpose |
| --- | --- |
| [Demo runbook](demo-runbook.md) | Run the controlled local MVP demo, reset it, and record what was verified. |
| [Flyway migrations](flyway-migrations.md) | Create, validate, coordinate, and recover versioned database migrations safely. |
| [Vonage developer tooling](vonage-developer-tooling.md) | Set up the Vonage MCP server and CLI locally without sharing credentials. |

## When to add a guide

Add or propose a guide when:

- A contributor solves a non-obvious problem that other contributors will probably encounter.
- The same question recurs in Slack or mentoring.
- An approved workflow needs practical steps beyond the rules in `CONTRIBUTING.md`.

Keep one topic per file, use an English kebab-case filename, and do not include credentials, secrets, real phone numbers, or personal data. Guides are reviewed and merged through pull requests like other repository content.
