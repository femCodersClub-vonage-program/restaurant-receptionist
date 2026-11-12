# ADR-007: Serialize reservation creation with PostgreSQL advisory locks

## Status

Proposed

## Date

2026-11-12

## Context

The create-confirmed-reservation service must revalidate capacity and persist a reservation atomically (RN-01, RN-06). Under PostgreSQL's default `READ COMMITTED` isolation, two concurrent transactions can both read the same occupied total, both conclude the remaining places fit, and both insert, overselling the service.

Row-level locking (`SELECT ... FOR UPDATE`) cannot prevent this on its own: the first reservation for a date and service has no existing row to lock, and the `reservations` table has no per-service row that represents capacity.

## Decision

Inside the write transaction that creates a reservation, acquire a transaction-scoped PostgreSQL advisory lock for the requested date and service before reading the occupied total:

1. `pg_advisory_xact_lock(key1, key2)`, where `key1` is the reservation date as an epoch day and `key2` is an explicit, stable code for the dining session (lunch or dinner).
2. Sum confirmed party sizes for that date and service.
3. Insert the reservation only if the party fits.

PostgreSQL releases transaction-level advisory locks automatically at the end of the transaction. Under `READ COMMITTED`, each statement sees data committed before it starts, so a transaction that obtains the lock after a competing commit reads the updated total.

Lock keys are computed in application code rather than with an internal hash function, so distinct date and service pairs never share a key.

## Alternatives considered

- `SERIALIZABLE` isolation with retries. This needs no schema change, but it requires retry logic for serialization failures and makes outcomes harder to test deterministically.
- A capacity table with one row per date and service locked with `SELECT ... FOR UPDATE`. This uses conventional row locks but requires a new Flyway migration, row creation on first use, and keeping the table consistent with reservations.
- An application-level lock (for example, a Java `synchronized` block). It does not protect against multiple backend instances and was rejected.

## Consequences

- No schema change or migration is required.
- Creation is serialized only for the same date and service; other services and dates proceed in parallel.
- The approach is PostgreSQL-specific, which is consistent with the approved stack. Integration tests must run against PostgreSQL (Testcontainers), not an in-memory database.
- The lock is held until the transaction ends, so the creation transaction must stay short and must not call external services such as Vonage.
- Every code path that creates confirmed reservations must use this service; inserting reservations elsewhere bypasses the protection.
- A concurrency integration test must demonstrate that competing attempts for the last places never exceed service capacity.

## Related material

- [Create confirmed reservation service spec](../../specs/reservations-01-create-confirmed-reservation-service.spec.md)
- [Business rules RN-01 and RN-06](../business-rules.md)
- [ADR-003: Separate local database and integration-test environments](003-separate-local-database-and-integration-test-environments.md)
- [PostgreSQL 17: Advisory locks](https://www.postgresql.org/docs/17/explicit-locking.html#ADVISORY-LOCKS)
- [PostgreSQL 17: Advisory lock functions](https://www.postgresql.org/docs/17/functions-admin.html#FUNCTIONS-ADVISORY-LOCKS)
- [PostgreSQL 17: Read Committed isolation level](https://www.postgresql.org/docs/17/transaction-iso.html#XACT-READ-COMMITTED)
