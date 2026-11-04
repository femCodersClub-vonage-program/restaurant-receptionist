# Flyway migrations

Use Flyway migrations for every PostgreSQL schema change. A migration is a versioned SQL file that makes database history reviewable and reproducible. Do not create, alter, or remove tables manually in a local database.

This guide explains the operational steps. The authoritative policy is [ADR-004](../adr/004-adopt-flyway-migration-coordination-policy.md) and the [database-migration rules in CONTRIBUTING.md](../../CONTRIBUTING.md#database-migrations).

## Before creating a migration

1. Confirm that the schema change is in the approved issue and spec. If it is not, ask the project mentor before writing SQL.
2. Confirm that the issue has the `database` label.
3. Update your branch with the latest `develop` branch.
4. Inspect `backend/src/main/resources/db/migration/` to identify the latest migration already integrated into `develop`.
5. Choose the next available sequential version and record the proposed filename in the issue before creating the file.

The initial persistence issue uses `V1__create_reservations.sql`. Later migrations use the next sequential version, for example `V2__add_notification_status.sql`.

## Create the file

Create versioned SQL migrations in:

```text
backend/src/main/resources/db/migration/
```

Use this naming convention:

```text
V<version>__<lowercase_underscore_description>.sql
```

Examples:

```text
V1__create_reservations.sql
V2__add_notification_status.sql
```

The version and description must describe one deliberate schema change. Keep the SQL focused on the approved issue. Do not add speculative tables, seed data, customer accounts, or unrelated changes.

## Validate before requesting review

1. Run the backend integration tests using the verified command in [the local setup guide](../setup.md#run-backend-tests). Testcontainers starts a separate PostgreSQL instance and Flyway applies migrations to it.
2. Start the backend against the Docker Compose PostgreSQL instance using [the local setup guide](../setup.md#run-the-backend-locally). Confirm that Flyway applies the migration successfully.
3. Update your branch from the latest `develop` again.
4. Inspect the migration directory again for a version collision.
5. State the migration filename and the PostgreSQL-backed validation performed in the pull request.

## Working in parallel

Two unmerged branches can accidentally choose the same next version. Manual coordination is intentional for this small, time-boxed MVP.

- Record the proposed filename in the issue and pull request.
- Tell the mentor about migrations early so merge order can be coordinated.
- If another migration with the same version reaches `develop` first, update your branch and rename **your unmerged migration** to the next available version.
- If two issues need the same table or one table depends on another, do not implement both independently. The mentor should sequence the issues or create a shared persistence task.

## Rules after merge

Never edit, rename, delete, reorder, or reuse a migration that has been merged into `develop`. A later migration must correct or extend the schema instead.

Do not use Hibernate auto-DDL as a substitute for a Flyway migration.

## Recover a local development database

If an unmerged migration was renamed after you already applied it locally, your Docker Compose database can no longer match the files on your branch. For this fictional-data MVP, you can deliberately recreate the local PostgreSQL volume:

```powershell
docker compose down -v
docker compose up -d
docker compose ps
```

`docker compose down -v` permanently deletes the local Docker volume and all of its local database data. Stop the backend first, use it only when deletion is intended, and never treat it as a fix for a migration already merged into `develop`.

After the database is healthy, start the backend again so Flyway applies the migrations from the beginning. If the error involves a migration already merged into `develop`, do not reset or edit it blindly: raise the problem with the mentor.

## Common mistakes

| Situation | Correct action |
| --- | --- |
| A schema change is needed but the issue has no `database` label. | Ask the mentor to update the issue before implementation. |
| Two branches use the same unmerged version. | The branch merged later updates from `develop` and renames its unmerged file to the next available version. |
| A merged migration needs a correction. | Create a later migration; do not alter the merged file. |
| A local database no longer matches renamed, unmerged migrations. | Deliberately recreate the local Docker volume only if its data can be discarded. |
| A migration fails on Testcontainers or Docker Compose. | Do not bypass Flyway. Read the error, keep the failure in the pull request context, and ask for help if the approved schema is unclear. |
