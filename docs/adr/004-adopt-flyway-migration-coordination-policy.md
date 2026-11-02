# ADR-004: Adopt the Flyway migration coordination policy

## Status

Accepted on 2026-11-02.

## Context

The project will use PostgreSQL and Flyway for versioned schema changes. Several contributors may work on backend features during a short delivery window. A clear policy is needed before the first domain migration so that migrations remain ordered, reviewable, reproducible, and safe to apply in local and integration-test databases.

## Decision

Store versioned SQL migrations in the backend's standard Flyway migration location. Name each migration using this convention:

```text
V<version>__<lowercase_underscore_description>.sql
```

Examples:

```text
V1__create_reservations.sql
V2__add_notification_status.sql
```

Each schema change requires a new migration in the same pull request as the code that depends on it. A migration that has been merged must not be edited, renamed, deleted, or reordered; a later migration must correct or extend it instead.

The project will coordinate migration versions manually while the team is small:

1. An issue that changes the schema receives the `database` label.
2. Before creating a migration, its author updates `develop` and checks the migrations already integrated there.
3. The author records the proposed migration filename in the issue and pull request.
4. Before requesting review, the author updates the branch with the latest `develop` and checks again for a version collision.
5. The project mentor coordinates the merge order of pull requests containing migrations.
6. If two unmerged branches use the same version, the branch merged later renames its unmerged migration after updating from `develop`.

Pull requests containing migrations must state the migration file name and include PostgreSQL-backed validation.

Repeatable migrations and seed data are not part of the initial policy. They require a separate decision if needed.

## Alternatives considered

- Allow schema changes through Hibernate auto-DDL. This would make schema history and review less explicit and does not provide reproducible migration history.
- Allow changes to previously merged migrations. This would make already-applied environments diverge and break Flyway checks.
- Use timestamp-only names without coordination. This reduces simple numeric collisions but makes review and ordering less clear for this small team.
- Introduce an automated version-allocation script immediately. The project has three contributors, a six-week delivery window, and no evidence yet that manual coordination is insufficient. Such a script would add tooling, network, and cross-platform maintenance work before it has shown a need.

## Consequences

- The first approved domain-data spec will introduce the first migration; this initial setup does not create a speculative domain schema.
- Backend contributors must account for migrations in estimates, implementation, tests, and pull-request descriptions.
- `CONTRIBUTING.md` and `docs/setup.md` must reflect this policy before the first migration is merged.
- A migration conflict discovered before merge must be resolved by creating or renumbering an unmerged migration, never by changing a migration already applied through an integrated branch.
- The mentor will revisit automation if migration collisions or coordination overhead become a real recurring problem.

## Related material

- Issue [#1](https://github.com/femCodersClub-vonage-program/restaurant-receptionist/issues/1)
- [Initial technical setup spec](../../specs/devops-01-initial-technical-setup.spec.md)
- [ADR-001: Initial technology stack](001-initial-technology-stack.md)
- [Contribution guide](../../CONTRIBUTING.md)
