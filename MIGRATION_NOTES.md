# Migration Notes

This upgrade supports both a fresh database and an existing database created by the earlier Hibernate `ddl-auto=update` version.

- Flyway is configured with `baseline-on-migrate=true` and baseline version `0` so an existing non-empty schema can adopt migrations safely.
- V1 installs pgvector and creates the RAG knowledge table.
- V2 seeds starter runbooks.
- V3 upgrades an existing `event_ai_analysis` table by making result fields nullable during `PENDING/PROCESSING`, adding lifecycle/metrics/idempotency columns, and creating the unique idempotency constraint.
- On a fresh database, Hibernate creates the application entities after Flyway runs; V3 intentionally checks whether `event_ai_analysis` already exists.

Before applying to a real shared database, take a backup and review the migrations according to your normal change-management process.
